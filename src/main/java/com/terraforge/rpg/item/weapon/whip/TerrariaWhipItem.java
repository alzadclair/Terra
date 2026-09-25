package com.terraforge.rpg.item.weapon.whip;

import com.terraforge.rpg.combat.CombatContext;
import com.terraforge.rpg.combat.DamageCalculator;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.combat.DamageTypeCategory;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Base class for Terraria summoner whips.
 * Implements linear reach sweep attack and applies summon tag damage.
 */
public class TerrariaWhipItem extends Item implements ITerrariaWhip {

    private final TerrariaRarity baseRarity;
    private final long baseValue;
    private final double baseDamage;
    private final double baseCritChance;
    private final double knockback;
    private final int useTime;
    private final double reach;
    private final int summonTagDamage;

    public TerrariaWhipItem(
            Properties properties,
            TerrariaRarity baseRarity,
            long baseValue,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime,
            double reach,
            int summonTagDamage
    ) {
        super(properties);
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.knockback = knockback;
        this.useTime = useTime;
        this.reach = reach;
        this.summonTagDamage = summonTagDamage;
    }

    @Override public DamageClass getDamageClass() { return DamageClass.SUMMON; }
    @Override public double getTerrariaBaseDamage() { return baseDamage; }
    @Override public double getBaseCritChance() { return baseCritChance; }
    @Override public double getKnockback() { return knockback; }
    @Override public int getUseTime() { return useTime; }
    @Override public double getManaCost() { return 0.0; }
    @Override public TerrariaRarity getBaseRarity() { return baseRarity; }
    @Override public long getBaseValue() { return baseValue; }
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.MELEE; } // Whips use melee modifiers

    public int getSummonTagDamage() { return summonTagDamage; }
    public double getReach() { return reach; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 tip = eye.add(look.scale(this.reach));

            AABB sweepBox = new AABB(eye, tip).inflate(1.2);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, sweepBox, e -> !e.is(player) && e.isAlive());

            DamageSource damageSource = level.damageSources().playerAttack(player);
            double effectiveDamage = getEffectiveDamage(stack);

            for (LivingEntity target : targets) {
                CombatContext context = new CombatContext(player, target, damageSource, effectiveDamage);
                context.setDamageClass(DamageClass.SUMMON);
                context.setCategory(DamageTypeCategory.PHYSICAL);

                double finalDmg = DamageCalculator.calculateDamage(context);
                target.hurt(damageSource, (float) finalDmg);

                // Knockback
                target.push(look.x * knockback * 0.1, 0.1, look.z * knockback * 0.1);

                // Visual whip crack particles
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.2, 0.2, 0.2, 0.1);
                }
            }

            // Whip crack sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.4f);
            player.getCooldowns().addCooldown(this, getEffectiveUseTime(stack));
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

        if (summonTagDamage > 0) {
            tooltipComponents.add(Component.literal("+" + summonTagDamage + " summon tag damage").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.literal("Your summons will focus struck enemies").withStyle(ChatFormatting.GRAY));
        }
    }
}
