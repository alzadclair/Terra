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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Starfury (Terraria 1.4.5.8 PC).
 * Summons a radiant celestial star falling from high above targeting the cursor position.
 */
public class StarfuryItem extends TerrariaSwordItem {

    public StarfuryItem(Properties properties) {
        super(properties, TerrariaRarity.GREEN, 50_000L, 25.0, 0.04, 5.0, 20);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 reach = eyePos.add(look.scale(32.0));

            BlockHitResult ray = level.clip(new ClipContext(eyePos, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 hitPos = ray.getType() != HitResult.Type.MISS ? ray.getLocation() : reach;

            // Spawn celestial star falling from Y + 20
            Vec3 starSpawn = hitPos.add(look.x * -4.0, 20.0, look.z * -4.0);

            // Audio: Amethyst chime & firework launch
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.8f);
            serverLevel.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.8f, 1.2f);

            // Star trajectory particles
            int steps = 16;
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                Vec3 p = starSpawn.lerp(hitPos, t);
                serverLevel.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.02);
                serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.01);
            }

            // Impact burst
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, hitPos.x, hitPos.y, hitPos.z, 1, 0, 0, 0, 0);

            // Damage entities in star impact radius (3.0 blocks)
            AABB impactBox = new AABB(hitPos.x - 3.0, hitPos.y - 1.5, hitPos.z - 3.0, hitPos.x + 3.0, hitPos.y + 2.5, hitPos.z + 3.0);
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, impactBox,
                    e -> e != player && e.isAlive() && !player.isAlliedTo(e));

            for (LivingEntity target : targets) {
                DamageSource ds = player.damageSources().playerAttack(player);
                CombatContext ctx = new CombatContext(player, target, ds, getTerrariaBaseDamage() * 1.5);
                ctx.setDamageClass(DamageClass.MELEE);
                ctx.setCategory(DamageTypeCategory.PHYSICAL);

                double damage = DamageCalculator.calculateDamage(ctx);
                target.hurt(ds, (float) damage);
                target.push(look.x * 0.4, 0.2, look.z * 0.4);
            }

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
