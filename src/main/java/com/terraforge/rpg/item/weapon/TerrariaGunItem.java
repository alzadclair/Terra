package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.ammo.AmmoType;
import com.terraforge.rpg.item.ammo.ITerrariaAmmo;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.ChatFormatting;
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
import java.util.Random;

/**
 * Base class for all Terraria guns and firearms (e.g. Minishark, Megashark).
 */
public class TerrariaGunItem extends Item implements ITerrariaWeapon {

    private static final Random RANDOM = new Random();

    private final TerrariaRarity baseRarity;
    private final long baseValue;
    private final double baseDamage;
    private final double baseCritChance;
    private final double knockback;
    private final int useTime;
    private final double ammoConservationChance;

    public TerrariaGunItem(
            Properties properties,
            TerrariaRarity baseRarity,
            long baseValue,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime,
            double ammoConservationChance
    ) {
        super(properties);
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.knockback = knockback;
        this.useTime = useTime;
        this.ammoConservationChance = ammoConservationChance;
    }

    @Override public DamageClass getDamageClass() { return DamageClass.RANGED; }
    @Override public double getTerrariaBaseDamage() { return baseDamage; }
    @Override public double getBaseCritChance() { return baseCritChance; }
    @Override public double getKnockback() { return knockback; }
    @Override public int getUseTime() { return useTime; }
    @Override public double getManaCost() { return 0.0; }
    @Override public TerrariaRarity getBaseRarity() { return baseRarity; }
    @Override public long getBaseValue() { return baseValue; }
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.RANGED; }
    public double getAmmoConservationChance() { return ammoConservationChance; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Find bullet in inventory
        ItemStack bulletStack = ItemStack.EMPTY;
        ITerrariaAmmo bulletItem = null;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() instanceof ITerrariaAmmo ammo && ammo.getAmmoType() == AmmoType.BULLET) {
                bulletStack = invStack;
                bulletItem = ammo;
                break;
            }
        }

        if (bulletStack.isEmpty() && !player.isCreative()) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            double bulletBonusDamage = bulletItem != null ? bulletItem.getBonusDamage() : 7.0;
            double bulletSpeedMod = bulletItem != null ? (1.5 + bulletItem.getBonusVelocity()) : 1.5;
            int pierces = bulletItem != null ? bulletItem.getPiercingCount() : 0;
            int bounces = bulletItem != null ? bulletItem.getBounceCount() : 0;

            TerraProjectileEntity bullet = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            bullet.setPos(eyePos.x, eyePos.y, eyePos.z);
            bullet.setOwner(player);
            bullet.setDamageClass(DamageClass.RANGED);
            bullet.setDamage(baseDamage + bulletBonusDamage);
            bullet.setMaxPierces(pierces);
            bullet.setMaxBounces(bounces);
            bullet.setProjectileGravity(0.005);
            bullet.shoot(look.x, look.y, look.z, (float) (2.5 * bulletSpeedMod), 0.5f);

            serverLevel.addFreshEntity(bullet);
            serverLevel.sendParticles(ParticleTypes.SMOKE, eyePos.x, eyePos.y, eyePos.z,
                    5, look.x * 0.2, look.y * 0.2, look.z * 0.2, 0.05);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2f, 1.8f);

            // Consume ammo unless conserved
            if (!player.isCreative() && RANDOM.nextDouble() >= ammoConservationChance) {
                bulletStack.shrink(1);
            }

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
        if (ammoConservationChance > 0) {
            tooltipComponents.add(Component.literal((int) (ammoConservationChance * 100) + "% chance not to consume ammo")
                    .withStyle(ChatFormatting.BLUE));
        }
    }
}
