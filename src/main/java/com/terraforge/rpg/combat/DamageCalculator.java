package com.terraforge.rpg.combat;

import com.terraforge.rpg.entity.mob.ITerrariaMob;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.stats.calculation.AttributeCalculator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Server-authoritative unified damage calculator executing the Terraria 1.4.5.8 combat pipeline.
 */
public final class DamageCalculator {

    private static final Random RANDOM = new Random();

    private DamageCalculator() {}

    /**
     * Executes the full damage pipeline on the given CombatContext.
     * @return the final mitigated damage
     */
    public static double calculateDamage(CombatContext context) {
        // Step 1: Base Damage
        double damage = context.getBaseDamage();

        // Determine damage class and category if attacker is present
        LivingEntity attacker = context.getAttacker();
        PlayerRPGData attackerData = null;
        if (attacker instanceof Player player) {
            attackerData = player.getData(ModAttachments.PLAYER_RPG_DATA);
            resolveAttackerClassification(context, player);
            applyEmblemBonuses(context, player);
        }

        // Step 2 & 3: Stat Multipliers
        if (attackerData != null) {
            applyAttackerRpgMultipliers(context, attackerData);
        }
        damage *= context.getRpgStatMultiplier();
        damage *= context.getEquipmentMultiplier();
        damage *= context.getBuffMultiplier();
        damage *= context.getRacialMultiplier();
        damage *= context.getSpecialAccessoryMultiplier();

        // Step 8 & 9: Critical Hit Calculation
        if (attacker != null) {
            applyCriticalCalculation(context, attackerData);
        }
        if (context.isCritical()) {
            damage *= context.getCritMultiplier();
        }

        context.setRawDamage(damage);

        // Step 10 & 11: Target Defenses and Mitigation
        LivingEntity target = context.getTarget();
        PlayerRPGData targetData = null;
        if (target instanceof Player targetPlayer) {
            targetData = targetPlayer.getData(ModAttachments.PLAYER_RPG_DATA);
            applyTargetMitigation(context, targetData);
        } else if (target != null) {
            // Non-player entity armor mitigation fallback
            applyMobMitigation(context, target);
        }

        // Apply defense reduction if applicable
        if (context.getCategory() == DamageTypeCategory.PHYSICAL || context.getCategory() == DamageTypeCategory.MAGICAL) {
            damage *= (1.0 - context.getTargetDefenseReduction());
        }

        // Check target special accessory (e.g. Guardian Seal)
        if (targetData != null && "guardian_seal".equalsIgnoreCase(targetData.getEquippedSpecialAccessory())) {
            damage *= 0.90; // 10% flat damage reduction
        }

        // Step 12: Final Clamping (Terraria rule: minimum 1.0 damage)
        damage = Math.max(1.0, damage);
        context.setFinalDamage(damage);

        // Step 13: On-Hit Effects (Lifesteal)
        if (attackerData != null) {
            applyOnHitEffects(context, attackerData);
        }

        return damage;
    }

    private static void resolveAttackerClassification(CombatContext context, Player player) {
        ItemStack mainStack = player.getMainHandItem();

        if (mainStack.getItem() instanceof ITerrariaWeapon weapon) {
            context.setDamageClass(weapon.getDamageClass());
            context.setCategory(weapon.getDamageClass().isPhysicalDefault() ? DamageTypeCategory.PHYSICAL : DamageTypeCategory.MAGICAL);
            context.setBaseDamage(weapon.getTerrariaBaseDamage());
            context.setEquipmentMultiplier(1.0 + weapon.getPrefix(mainStack).damageModifier());
            return;
        }

        String mainItem = mainStack.getItem().toString();

        if (mainItem.contains("bow") || mainItem.contains("crossbow") || mainItem.contains("gun")) {
            context.setDamageClass(DamageClass.RANGED);
            context.setCategory(DamageTypeCategory.PHYSICAL);
        } else if (mainItem.contains("staff") || mainItem.contains("wand") || mainItem.contains("tome")) {
            context.setDamageClass(DamageClass.MAGIC);
            context.setCategory(DamageTypeCategory.MAGICAL);
        } else {
            context.setDamageClass(DamageClass.MELEE);
            context.setCategory(DamageTypeCategory.PHYSICAL);
        }
    }

    private static void applyAttackerRpgMultipliers(CombatContext context, PlayerRPGData data) {
        // RPG Stat multiplier
        if (context.getDamageClass() == DamageClass.MAGIC || context.getCategory() == DamageTypeCategory.MAGICAL) {
            context.setRpgStatMultiplier(AttributeCalculator.calculateMagicDamageMultiplier(data.getMagicAttackRank()));
        } else if (context.getDamageClass() == DamageClass.SUMMON) {
            int avgRank = (data.getAttackRank() + data.getMagicAttackRank()) / 2;
            context.setRpgStatMultiplier(1.0 + (avgRank * 0.0020));
        } else {
            context.setRpgStatMultiplier(AttributeCalculator.calculatePhysicalDamageMultiplier(data.getAttackRank()));
        }

        // Racial combat traits
        double racialBonus = 1.0;
        String race = data.getPrimaryRace().toLowerCase();
        if ("titan".equals(race)) racialBonus += 0.15;
        if ("elf".equals(race) && context.getDamageClass() == DamageClass.RANGED) racialBonus += 0.10;
        if ("fairy".equals(race) && context.getDamageClass() == DamageClass.MAGIC) racialBonus += 0.15;
        if ("demon".equals(race)) racialBonus += 0.10;
        if (data.isHybrid() && !data.getSecondaryRace().isEmpty()) {
            String sec = data.getSecondaryRace().toLowerCase();
            if ("titan".equals(sec)) racialBonus += 0.075;
            if ("elf".equals(sec) && context.getDamageClass() == DamageClass.RANGED) racialBonus += 0.05;
            if ("fairy".equals(sec) && context.getDamageClass() == DamageClass.MAGIC) racialBonus += 0.075;
            if ("demon".equals(sec)) racialBonus += 0.05;
        }
        context.setRacialMultiplier(racialBonus);

        // Special Accessory combat bonuses
        String accessory = data.getEquippedSpecialAccessory();
        double accBonus = 1.0;
        if ("titan_core".equalsIgnoreCase(accessory) && context.getCategory() == DamageTypeCategory.PHYSICAL) {
            accBonus += 0.20; // +20% physical damage
        } else if ("arcane_prism".equalsIgnoreCase(accessory) && context.getCategory() == DamageTypeCategory.MAGICAL) {
            accBonus += 0.25; // +25% magic damage
        }
        context.setSpecialAccessoryMultiplier(accBonus);

        // Armor Set Bonus damage bonus
        if (context.getAttacker() instanceof Player player) {
            com.terraforge.rpg.armor.ArmorSetBonus setBonus = com.terraforge.rpg.armor.ArmorSetService.getActiveSetBonus(player);
            if (setBonus.getDamageMultiplierBonus() > 0.0) {
                if (setBonus.getTargetClass() == DamageClass.GENERIC || setBonus.getTargetClass() == context.getDamageClass()) {
                    context.setEquipmentMultiplier(context.getEquipmentMultiplier() * (1.0 + setBonus.getDamageMultiplierBonus()));
                }
            }
        }
    }

    private static void applyCriticalCalculation(CombatContext context, PlayerRPGData data) {
        double critChance = 4.0; // Terraria base critical chance is 4%
        double critMultiplier = 1.5; // Base 1.5x

        if (context.getAttacker() instanceof Player player) {
            ItemStack mainStack = player.getMainHandItem();
            if (mainStack.getItem() instanceof ITerrariaWeapon weapon) {
                critChance = weapon.getBaseCritChance() + weapon.getPrefix(mainStack).critChanceBonus();
            }
        }

        if (data != null) {
            critChance += AttributeCalculator.calculateCriticalChanceBonus(data.getCriticalChanceRank());
            critMultiplier = AttributeCalculator.calculateCriticalMultiplier(data.getCriticalRank());

            if ("predator_eye".equalsIgnoreCase(data.getEquippedSpecialAccessory())) {
                critChance += 10.0;
                critMultiplier += 0.25;
            }
        }

        critChance = Math.clamp(critChance, 0.0, 100.0);
        context.setCritMultiplier(critMultiplier);

        boolean isCrit = RANDOM.nextDouble() * 100.0 < critChance;
        context.setCritical(isCrit);
    }

    private static void applyTargetMitigation(CombatContext context, PlayerRPGData targetData) {
        int armorDefense = 0;
        if (context.getTarget() instanceof Player player) {
            armorDefense = com.terraforge.rpg.armor.ArmorSetService.getTotalArmorDefense(player);
        }

        if (context.getCategory() == DamageTypeCategory.PHYSICAL) {
            double reduction = AttributeCalculator.calculatePhysicalDamageReduction(targetData.getDefenseRank());
            double armorMitigation = (armorDefense * 0.5) / Math.max(1.0, context.getBaseDamage());
            context.setTargetDefenseReduction(Math.min(0.85, reduction + armorMitigation));
        } else if (context.getCategory() == DamageTypeCategory.MAGICAL) {
            double reduction = AttributeCalculator.calculateMagicDamageReduction(targetData.getMagicDefenseRank());
            double armorMitigation = (armorDefense * 0.25) / Math.max(1.0, context.getBaseDamage());
            context.setTargetDefenseReduction(Math.min(0.85, reduction + armorMitigation));
        }
    }

    private static void applyMobMitigation(CombatContext context, LivingEntity target) {
        if (target instanceof ITerrariaMob terrariaMob) {
            double terrariaDef = terrariaMob.getTerrariaDefense();
            double flatMitigation = terrariaDef * 0.5;
            double currentBase = context.getBaseDamage();
            if (currentBase > 0) {
                double reduction = Math.min(0.85, flatMitigation / currentBase);
                context.setTargetDefenseReduction(reduction);
            }
            return;
        }

        // Fallback for vanilla mobs using armor value
        double armor = target.getArmorValue();
        if (armor > 0) {
            // Asymptotic conversion: armor / (armor + 50)
            double reduction = Math.min(0.80, armor / (armor + 50.0));
            context.setTargetDefenseReduction(reduction);
        }
    }

    private static void applyOnHitEffects(CombatContext context, PlayerRPGData data) {
        // Blood Crystal or Vampire lifesteal: 5% of final damage dealt, capped at 5.0 HP
        boolean hasBloodCrystal = "blood_crystal".equalsIgnoreCase(data.getEquippedSpecialAccessory());
        boolean isVampire = "vampire".equalsIgnoreCase(data.getPrimaryRace()) ||
                (data.isHybrid() && "vampire".equalsIgnoreCase(data.getSecondaryRace()));

        if (hasBloodCrystal || isVampire) {
            double lifesteal = Math.min(context.getFinalDamage() * 0.05, 5.0);
            context.setLifestealAmount(lifesteal);
        }
    }

    private static void applyEmblemBonuses(CombatContext context, Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem emblem) {
                if (emblem.getTargetDamageClass() == context.getDamageClass()) {
                    context.setEquipmentMultiplier(context.getEquipmentMultiplier() * (1.0 + emblem.getDamageBonus()));
                    break;
                }
            }
        }
    }
}
