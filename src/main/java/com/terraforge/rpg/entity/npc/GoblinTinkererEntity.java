package com.terraforge.rpg.entity.npc;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.reforge.ReforgeService;
import com.terraforge.rpg.registry.ModEntities;
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
 * The Goblin Tinkerer - master tinkerer and reforger.
 * Discovered bound in underground caves after defeating the Goblin Army.
 * Reforges player equipment with prefixes via ReforgeService.
 */
public class GoblinTinkererEntity extends TerraBaseTownNPC {

    private boolean bound = false;

    public GoblinTinkererEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level, "goblin_tinkerer");
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    @Override
    protected int getAttackInterval() {
        return 22; // Throws spiky balls every 1.1s
    }

    @Override
    protected void performDefenseAttack(LivingEntity target, ServerLevel level) {
        if (bound) return; // Cannot attack while bound

        Vec3 eyePos = this.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity spikyBall = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), level);
        spikyBall.setPos(eyePos.x, eyePos.y, eyePos.z);
        spikyBall.setOwner(this);
        spikyBall.setDamageClass(DamageClass.MELEE);
        spikyBall.setDamage(15.0);
        spikyBall.setProjectileGravity(0.04);
        spikyBall.setMaxBounces(3);
        spikyBall.shoot(dir.x, dir.y + 0.15, dir.z, 0.8f, 2.0f);

        level.addFreshEntity(spikyBall);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.METAL_HIT, SoundSource.NEUTRAL, 0.8f, 1.3f);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            if (bound) {
                bound = false;
                player.sendSystemMessage(
                        Component.literal("[Goblin Tinkerer] Thanks for untying me! Those other goblins are completely uncivilized. I'll head to your town!")
                                .withStyle(ChatFormatting.BLUE)
                );
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.LEASH_KNOT_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }

            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ITerrariaItem)) {
                player.sendSystemMessage(
                        Component.literal("[Goblin Tinkerer] Hold a Terraria weapon or accessory in your hand to reforge it!")
                                .withStyle(ChatFormatting.BLUE)
                );
                return InteractionResult.SUCCESS;
            }

            // Perform item reforge
            ReforgeService.ReforgeResult result = ReforgeService.reforge(player, heldItem);
            if (result.success()) {
                player.sendSystemMessage(
                        Component.literal("[Goblin Tinkerer] Done! Reforged with ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(result.newPrefix().displayName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                                .append(Component.literal(" for " + CoinHelper.formatCoins(result.costPaid()) + "!").withStyle(ChatFormatting.GREEN))
                );
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(
                        Component.literal("[Goblin Tinkerer] " + result.failureReason() + " (Reforge cost: " + CoinHelper.formatCoins(result.costPaid()) + ")")
                                .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.CONSUME;
    }
}
