package com.terraforge.rpg.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Intercepts damage events and applies the centralized TerraForge RPG combat calculation pipeline.
 */
public final class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target == null || target.level().isClientSide()) return;

        DamageSource source = event.getSource();
        Entity directEntity = source.getEntity();
        LivingEntity attacker = directEntity instanceof LivingEntity living ? living : null;

        // Only calculate RPG damage if either attacker or target is a Player or custom RPG entity
        if (!(attacker instanceof Player) && !(target instanceof Player)) {
            return;
        }

        CombatContext context = new CombatContext(attacker, target, source, event.getOriginalDamage());
        double calculatedDamage = DamageCalculator.calculateDamage(context);

        // Apply new damage
        event.setNewDamage((float) calculatedDamage);

        // Handle Critical effects
        if (context.isCritical() && target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX(),
                    target.getY() + (target.getBbHeight() * 0.6),
                    target.getZ(),
                    15,
                    0.3,
                    0.3,
                    0.3,
                    0.15
            );
        }

        // Handle Lifesteal
        if (context.getLifestealAmount() > 0.0 && attacker != null && attacker.isAlive()) {
            attacker.heal((float) context.getLifestealAmount());
        }
    }

    private CombatEventHandler() {}
}
