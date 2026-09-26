package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.CombatContext;
import com.terraforge.rpg.combat.DamageCalculator;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.combat.DamageTypeCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Zenith — The Pinnacle of Terraria Melee Combat (Terraria 1.4.5.8 PC).
 * Projects an ethereal swirling vortex of constituent swords toward the cursor,
 * cutting through terrain, striking multiple enemies in line-of-sight and dealing colossal damage.
 */
public class ZenithItem extends TerrariaSwordItem {

    public ZenithItem(Properties properties) {
        super(properties, TerrariaRarity.RED, 2_000_000L, 190.0, 0.14, 6.5, 12);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 targetCenter = eyePos.add(look.scale(16.0));

            // Sound: sonic astral sweep
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 1.4f);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.4f, 2.0f);

            // Spawn celestial vortex particle trail
            for (double step = 1.0; step <= 24.0; step += 1.5) {
                Vec3 p = eyePos.add(look.scale(step));
                double offsetRadius = Math.sin(step * 0.8) * 1.2;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        p.x + (Math.random() - 0.5) * offsetRadius,
                        p.y + (Math.random() - 0.5) * offsetRadius,
                        p.z + (Math.random() - 0.5) * offsetRadius,
                        2, 0.05, 0.05, 0.05, 0.02);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }

            // AOE target sweep along ray and at destination
            AABB targetBox = new AABB(eyePos, targetCenter).inflate(4.0);
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, targetBox,
                    e -> e != player && e.isAlive() && !player.isAlliedTo(e));

            for (LivingEntity target : targets) {
                DamageSource ds = player.damageSources().playerAttack(player);
                CombatContext ctx = new CombatContext(player, target, ds, getTerrariaBaseDamage());
                ctx.setDamageClass(DamageClass.MELEE);
                ctx.setCategory(DamageTypeCategory.PHYSICAL);

                double damage = DamageCalculator.calculateDamage(ctx);
                target.hurt(ds, (float) damage);

                // Knockback
                Vec3 push = target.position().subtract(player.position()).normalize().scale(getKnockback() * 0.15);
                target.push(push.x, 0.2, push.z);

                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 0.8, target.getZ(), 8, 0.2, 0.3, 0.2, 0.1);
            }

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
