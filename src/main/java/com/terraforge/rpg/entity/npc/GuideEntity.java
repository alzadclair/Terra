package com.terraforge.rpg.entity.npc;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * The Guide - first Town NPC to assist the player with world survival tips and lore.
 * Defends himself using a wooden bow and wooden arrows.
 */
public class GuideEntity extends TerraBaseTownNPC {

    private static final Random RANDOM = new Random();

    private static final List<String> TIPS = List.of(
            "If you want to survive the night, build a shelter with a door, table, chair, and light source.",
            "Deep underground, you can find glowing Life Crystals. Smash them with a pickaxe to increase your health!",
            "You should craft an Anvil using iron or lead ingots to forge weapons, tools, and armor.",
            "Defeating bosses across the land will attract new citizens to settle in your town.",
            "They say deep beneath the cavern layer lies the Underworld, where molten rock burns forever."
    );

    public GuideEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level, "guide");
    }

    @Override
    protected int getAttackInterval() {
        return 25; // Shoots bow every 1.25s
    }

    @Override
    protected void performDefenseAttack(LivingEntity target, ServerLevel level) {
        Vec3 eyePos = this.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity arrow = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), level);
        arrow.setPos(eyePos.x, eyePos.y, eyePos.z);
        arrow.setOwner(this);
        arrow.setDamageClass(DamageClass.RANGED);
        arrow.setDamage(12.0);
        arrow.setProjectileGravity(0.03);
        arrow.shoot(dir.x, dir.y + 0.1, dir.z, 1.2f, 1.0f);

        level.addFreshEntity(arrow);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0f, 1.2f);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            String tip = TIPS.get(RANDOM.nextInt(TIPS.size()));
            player.sendSystemMessage(
                    Component.literal("[Guide] ").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(tip).withStyle(ChatFormatting.WHITE))
            );
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
