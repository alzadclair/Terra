package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.armor.ArmorSetBonus;
import com.terraforge.rpg.armor.ArmorSetService;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Base class for magic spellbooks and ray weapons (e.g. Water Bolt, Space Gun).
 */
public class TerrariaMagicSpellItem extends Item implements ITerrariaWeapon {

    private final String spellId;
    private final TerrariaRarity baseRarity;
    private final long baseValue;
    private final double baseDamage;
    private final double baseCritChance;
    private final double knockback;
    private final int useTime;
    private final double manaCost;
    private final int pierces;
    private final int bounces;
    private final float projectileSpeed;

    public TerrariaMagicSpellItem(
            Properties properties,
            String spellId,
            TerrariaRarity baseRarity,
            long baseValue,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime,
            double manaCost,
            int pierces,
            int bounces,
            float projectileSpeed
    ) {
        super(properties);
        this.spellId = spellId;
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.knockback = knockback;
        this.useTime = useTime;
        this.manaCost = manaCost;
        this.pierces = pierces;
        this.bounces = bounces;
        this.projectileSpeed = projectileSpeed;
    }

    public String getSpellId() { return spellId; }
    @Override public DamageClass getDamageClass() { return DamageClass.MAGIC; }
    @Override public double getTerrariaBaseDamage() { return baseDamage; }
    @Override public double getBaseCritChance() { return baseCritChance; }
    @Override public double getKnockback() { return knockback; }
    @Override public int getUseTime() { return useTime; }
    @Override public double getManaCost() { return manaCost; }
    @Override public TerrariaRarity getBaseRarity() { return baseRarity; }
    @Override public long getBaseValue() { return baseValue; }
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.MAGIC; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Calculate actual mana cost considering armor set bonuses
        double effectiveMana = manaCost;
        ArmorSetBonus setBonus = ArmorSetService.getActiveSetBonus(player);

        if (setBonus == ArmorSetBonus.METEOR && "space_gun".equalsIgnoreCase(spellId)) {
            effectiveMana = 0.0; // Space Gun is free with Meteor Armor!
        } else if (setBonus == ArmorSetBonus.JUNGLE) {
            effectiveMana *= 0.84; // -16% mana usage
        }

        if (effectiveMana > 0.0) {
            com.terraforge.rpg.player.data.PlayerRPGData data = player.getData(com.terraforge.rpg.registry.ModAttachments.PLAYER_RPG_DATA);
            if (!ManaService.consumeMana(data, effectiveMana)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            TerraProjectileEntity spell = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            spell.setPos(eyePos.x, eyePos.y, eyePos.z);
            spell.setOwner(player);
            spell.setDamageClass(DamageClass.MAGIC);
            spell.setDamage(baseDamage);
            spell.setMaxPierces(pierces);
            spell.setMaxBounces(bounces);
            spell.setProjectileGravity(0.0);
            spell.shoot(look.x, look.y, look.z, projectileSpeed, 0.2f);

            serverLevel.addFreshEntity(spell);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, eyePos.x, eyePos.y, eyePos.z,
                    10, look.x * 0.3, look.y * 0.3, look.z * 0.3, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.2f, 1.5f);

            player.getCooldowns().addCooldown(this, useTime);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        TerrariaPrefix prefix = getPrefix(stack);
        MutableComponent name;
        if (!prefix.isNone()) {
            name = Component.literal(prefix.displayName() + " ").append(super.getName(stack));
        } else {
            name = super.getName(stack).copy();
        }
        return name.withStyle(style -> style.withColor(getEffectiveRarity(stack).getTextColor()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        TerrariaTooltipHelper.buildTooltip(stack, this, tooltipComponents);
    }
}
