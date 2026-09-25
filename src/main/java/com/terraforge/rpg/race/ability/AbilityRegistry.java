package com.terraforge.rpg.race.ability;

import com.terraforge.rpg.player.data.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Registry containing all 30 canonical racial abilities with server-authoritative logic.
 */
public final class AbilityRegistry {
    private static final Map<String, RaceAbility> ABILITIES = new LinkedHashMap<>();

    static {
        // 1. Human - Evolution
        register(new BaseAbility("evolution", "human", AbilityType.PASSIVE, 0, 0,
                Component.literal("Potencial Infinito: ultrapassa os limites máximos de atributos após o nível 1000."),
                (player, data) -> true));

        // 2. Goblin - Goblin Craft
        register(new BaseAbility("goblin_craft", "goblin", AbilityType.ACTIVE, 600, 5,
                Component.literal("Engenho Goblin: concede pressa e sorte por 15 segundos."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 300, 1));
                    return true;
                }));

        // 3. Slime - Gelatinous Body
        register(new BaseAbility("gelatinous_body", "slime", AbilityType.ACTIVE, 400, 5,
                Component.literal("Corpo Gelatinoso: super salto e amortecimento elástico de impacto."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SLIME_JUMP, SoundSource.PLAYERS, 1.0f, 1.0f);
                    return true;
                }));

        // 4. Demon - Infernal Domain
        register(new BaseAbility("infernal_domain", "demon", AbilityType.ACTIVE, 800, 15,
                Component.literal("Domínio Infernal: força ígnea e queima criaturas ao redor."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
                    damageNearby(player, 6.0, 5.0f, true);
                    return true;
                }));

        // 5. Angel - Celestial Grace
        register(new BaseAbility("celestial_grace", "angel", AbilityType.ACTIVE, 900, 20,
                Component.literal("Graça Celestial: cura instantânea de 6 de vida e regeneração."),
                (player, data) -> {
                    player.heal(6.0f);
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
                    return true;
                }));

        // 6. Elf - Arcane Flow
        register(new BaseAbility("arcane_flow", "elf", AbilityType.ACTIVE, 600, 0,
                Component.literal("Fluxo Arcano: restaura 15 de mana e acelera velocidade mágica."),
                (player, data) -> {
                    data.setCurrentMana(data.getCurrentMana() + 15.0);
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                    return true;
                }));

        // 7. Dwarf - Mountain Heart
        register(new BaseAbility("mountain_heart", "dwarf", AbilityType.ACTIVE, 700, 10,
                Component.literal("Coração da Montanha: grande resistência e imunidade a repulsão no chão."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2));
                    return true;
                }));

        // 8. Orc - Blood Fury
        register(new BaseAbility("blood_fury", "orc", AbilityType.ACTIVE, 600, 10,
                Component.literal("Fúria de Sangue: bônus de força massivo que escala com a vida perdida."),
                (player, data) -> {
                    float missing = player.getMaxHealth() - player.getHealth();
                    int amp = missing > 10 ? 2 : 1;
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, amp));
                    return true;
                }));

        // 9. Vampire - Crimson Feast
        register(new BaseAbility("crimson_feast", "vampire", AbilityType.ACTIVE, 800, 15,
                Component.literal("Banquete Carmesim: drena 4 de vida de criaturas próximas e ganha velocidade."),
                (player, data) -> {
                    float drained = damageNearby(player, 5.0, 4.0f, false);
                    if (drained > 0) player.heal(Math.min(6.0f, drained));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                    return true;
                }));

        // 10. Werewolf - Lunar Frenzy
        register(new BaseAbility("lunar_frenzy", "werewolf", AbilityType.ACTIVE, 1000, 15,
                Component.literal("Frenesi Lunar: transforma temporariamente, aumentando velocidade e dano."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
                    return true;
                }));

        // 11. Draconic - Draconic Awakening
        register(new BaseAbility("draconic_awakening", "draconic", AbilityType.ACTIVE, 800, 20,
                Component.literal("Despertar Dracônico: sopra chamas dracônicas em cone frontal."),
                (player, data) -> {
                    damageNearby(player, 7.0, 7.0f, true);
                    return true;
                }));

        // 12. Fairy - Fae Leap
        register(new BaseAbility("fae_leap", "fairy", AbilityType.ACTIVE, 300, 10,
                Component.literal("Salto Feérico: teleporte curto instantâneo com névoa mágica."),
                (player, data) -> teleportForward(player, 8.0)));

        // 13. Harpy - Wind Sovereignty
        register(new BaseAbility("wind_sovereignty", "harpy", AbilityType.ACTIVE, 400, 10,
                Component.literal("Soberania dos Ventos: investida aérea e rajada de vento repulsora."),
                (player, data) -> {
                    Vec3 look = player.getLookAngle();
                    player.setDeltaMovement(look.x * 1.5, look.y * 1.2 + 0.3, look.z * 1.5);
                    player.hurtMarked = true;
                    return true;
                }));

        // 14. Triton - Tide Dominion
        register(new BaseAbility("tide_dominion", "triton", AbilityType.ACTIVE, 600, 10,
                Component.literal("Domínio das Marés: respiração aquática e impulso hidráulico."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 400, 0));
                    return true;
                }));

        // 15. Undead - Death Denial
        register(new BaseAbility("death_denial", "undead", AbilityType.CONDITIONAL, 6000, 0,
                Component.literal("Negação da Morte: impede dano fatal uma vez a cada longo intervalo."),
                (player, data) -> true));

        // 16. Golem - Living Fortress
        register(new BaseAbility("living_fortress", "golem", AbilityType.ACTIVE, 1200, 20,
                Component.literal("Fortaleza Viva: enorme resistência e absorção em troca de mobilidade."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 3));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                    return true;
                }));

        // 17. Shadowborn - Shadow Step
        register(new BaseAbility("shadow_step", "shadowborn", AbilityType.ACTIVE, 300, 10,
                Component.literal("Passo Sombrio: teleporte pelas sombras e invisibilidade temporária."),
                (player, data) -> {
                    teleportForward(player, 7.0);
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0));
                    return true;
                }));

        // 18. Stormborn - Overcharge
        register(new BaseAbility("overcharge", "stormborn", AbilityType.ACTIVE, 600, 15,
                Component.literal("Sobrecarga: libera descarga elétrica em cadeia atingindo inimigos próximos."),
                (player, data) -> {
                    damageNearby(player, 6.0, 6.0f, false);
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
                    return true;
                }));

        // 19. Phoenix - Rebirth
        register(new BaseAbility("rebirth", "phoenix", AbilityType.CONDITIONAL, 7200, 0,
                Component.literal("Renascimento: evita a morte com uma explosão purificadora de fogo."),
                (player, data) -> true));

        // 20. Frostborn - Absolute Zero
        register(new BaseAbility("absolute_zero", "frostborn", AbilityType.ACTIVE, 800, 15,
                Component.literal("Zero Absoluto: cria uma aura glacial que congela e lentifica inimigos."),
                (player, data) -> {
                    slowNearby(player, 7.0, 200, 2);
                    return true;
                }));

        // 21. Dryad - Nature Pact
        register(new BaseAbility("nature_pact", "dryad", AbilityType.ACTIVE, 700, 15,
                Component.literal("Pacto da Natureza: regeneração progressiva e vínculo com a flora."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                    slowNearby(player, 6.0, 140, 1);
                    return true;
                }));

        // 22. Beastman - Predator Instinct
        register(new BaseAbility("predator_instinct", "beastman", AbilityType.ACTIVE, 600, 10,
                Component.literal("Instinto Predador: mobilidade felina e precisão letal contra alvos feridos."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
                    return true;
                }));

        // 23. Enderian - Rift Walker
        register(new BaseAbility("rift_walker", "enderian", AbilityType.ACTIVE, 400, 12,
                Component.literal("Caminhante da Fenda: salto dimensional direto para onde o olhar aponta."),
                (player, data) -> teleportForward(player, 10.0)));

        // 24. Voidborn - Devour Void
        register(new BaseAbility("devour_void", "voidborn", AbilityType.ACTIVE, 800, 15,
                Component.literal("Devorar o Vazio: absorve energia circundante e ganha escudo sombrio."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2));
                    return true;
                }));

        // 25. Astral - Astral Surge
        register(new BaseAbility("astral_surge", "astral", AbilityType.ACTIVE, 900, 25,
                Component.literal("Surto Astral: conjura energia estelar atingindo a área ao redor."),
                (player, data) -> {
                    damageNearby(player, 8.0, 8.0f, false);
                    return true;
                }));

        // 26. Spectre - Ethereal Form
        register(new BaseAbility("ethereal_form", "spectre", AbilityType.ACTIVE, 800, 20,
                Component.literal("Forma Etérea: intangibilidade, invisibilidade e grande redução de dano."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
                    return true;
                }));

        // 27. Insectoid - Metamorphosis
        register(new BaseAbility("metamorphosis", "insectoid", AbilityType.ACTIVE, 1200, 20,
                Component.literal("Metamorfose: carapaça reforçada e voo temporário."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 2));
                    return true;
                }));

        // 28. Titan - Colossus
        register(new BaseAbility("colossus", "titan", AbilityType.ACTIVE, 1200, 20,
                Component.literal("Colosso: força titânica e resistência imensa a repulsão."),
                (player, data) -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                    return true;
                }));

        // 29. Aetherian - Mana Singularity
        register(new BaseAbility("mana_singularity", "aetherian", AbilityType.ACTIVE, 1200, 0,
                Component.literal("Singularidade de Mana: restaura toda a mana imediatamente."),
                (player, data) -> {
                    data.setCurrentMana(data.getMaxMana());
                    return true;
                }));

        // 30. Reaper - Soul Harvest
        register(new BaseAbility("soul_harvest", "reaper", AbilityType.ACTIVE, 700, 15,
                Component.literal("Colheita de Almas: golpe sombrio em área que consome almas dos mortos."),
                (player, data) -> {
                    damageNearby(player, 6.0, 7.5f, false);
                    return true;
                }));
    }

    private static void register(RaceAbility ability) {
        ABILITIES.put(ability.getId().toLowerCase(), ability);
    }

    public static Optional<RaceAbility> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(ABILITIES.get(id.toLowerCase()));
    }

    public static int getCount() {
        return ABILITIES.size();
    }

    // Helper Action Methods
    private static boolean teleportForward(ServerPlayer player, double distance) {
        Vec3 look = player.getLookAngle();
        Vec3 target = player.position().add(look.x * distance, look.y * distance, look.z * distance);
        player.teleportTo(target.x, target.y, target.z);
        player.level().playSound(null, target.x, target.y, target.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }

    private static float damageNearby(ServerPlayer player, double radius, float amount, boolean ignite) {
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), amount);
            if (ignite) target.igniteForSeconds(4);
        }
        return targets.size() * amount;
    }

    private static void slowNearby(ServerPlayer player, double radius, int durationTicks, int amplifier) {
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, amplifier));
        }
    }

    private record BaseAbility(
            String id,
            String ownerRace,
            AbilityType type,
            long cooldownTicks,
            double manaCost,
            Component description,
            AbilityAction action
    ) implements RaceAbility {
        @Override public String getId() { return id; }
        @Override public String getOwnerRace() { return ownerRace; }
        @Override public AbilityType getType() { return type; }
        @Override public long getCooldownTicks() { return cooldownTicks; }
        @Override public double getManaCost() { return manaCost; }
        @Override public Component getDescription() { return description; }
        @Override public boolean execute(ServerPlayer player, PlayerRPGData data) { return action.execute(player, data); }
    }

    @FunctionalInterface
    public interface AbilityAction {
        boolean execute(ServerPlayer player, PlayerRPGData data);
    }
}
