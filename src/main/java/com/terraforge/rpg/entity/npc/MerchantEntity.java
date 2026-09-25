package com.terraforge.rpg.entity.npc;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Merchant - shopkeeper Town NPC.
 * Moves into valid housing when players accumulate at least 50 Silver coins.
 * Sells essential supplies like Wooden Arrows and Lesser Mana Potions.
 */
public class MerchantEntity extends TerraBaseTownNPC {

    public MerchantEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level, "merchant");
    }

    @Override
    protected int getAttackInterval() {
        return 20; // Throws knives every 1s
    }

    @Override
    protected void performDefenseAttack(LivingEntity target, ServerLevel level) {
        Vec3 eyePos = this.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity knife = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), level);
        knife.setPos(eyePos.x, eyePos.y, eyePos.z);
        knife.setOwner(this);
        knife.setDamageClass(DamageClass.RANGED);
        knife.setDamage(14.0);
        knife.setProjectileGravity(0.02);
        knife.shoot(dir.x, dir.y + 0.05, dir.z, 1.4f, 1.0f);

        level.addFreshEntity(knife);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.TRIDENT_THROW.value(), SoundSource.NEUTRAL, 0.8f, 1.5f);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            // Quick purchase: If player sneaks and clicks with 100 copper (1 Silver), buy 1 Lesser Mana Potion!
            if (player.isShiftKeyDown()) {
                if (CoinHelper.hasEnoughCoins(player, 100L)) {
                    CoinHelper.deductCoins(player, 100L);
                    player.getInventory().add(new ItemStack(ModItems.LESSER_MANA_POTION.get()));
                    player.sendSystemMessage(
                            Component.literal("[Merchant] Purchased 1x Lesser Mana Potion for 1 Silver!")
                                    .withStyle(ChatFormatting.GOLD)
                    );
                    this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(
                            Component.literal("[Merchant] You need at least 1 Silver Coin (100 Copper)!")
                                    .withStyle(ChatFormatting.RED)
                    );
                    return InteractionResult.CONSUME;
                }
            }

            player.sendSystemMessage(
                    Component.literal("[Merchant] Greetings! Sneak-click to purchase a Lesser Mana Potion (1 Silver). Check back often for more supplies!")
                            .withStyle(ChatFormatting.GOLD)
            );
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
