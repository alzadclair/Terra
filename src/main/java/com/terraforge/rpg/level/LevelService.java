package com.terraforge.rpg.level;

import com.terraforge.rpg.config.TerraServerConfig;
import com.terraforge.rpg.experience.DefaultExperienceCurve;
import com.terraforge.rpg.experience.ExperienceCurve;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.RaceDefinition;
import com.terraforge.rpg.race.RaceRegistry;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.registry.ModSoundEvents;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

/**
 * Server-authoritative service controlling XP progression, level-ups, and kill rewards.
 */
public final class LevelService {
    private static final ExperienceCurve EXPERIENCE_CURVE = new DefaultExperienceCurve();

    private LevelService() {}

    /**
     * Calculates the experience required for a player to reach the next level.
     */
    public static double getRequiredXp(PlayerRPGData data) {
        int level = data.getLevel();
        double multiplier = getEffectiveXpMultiplier(data);
        return EXPERIENCE_CURVE.getRequiredXp(level, multiplier);
    }

    /**
     * Calculates the effective XP multiplier based on race and hybrid status.
     */
    public static double getEffectiveXpMultiplier(PlayerRPGData data) {
        double primaryMultiplier = RaceRegistry.get(data.getPrimaryRace())
                .map(RaceDefinition::xpMultiplier)
                .orElse(1.0);

        if (!data.isHybrid() || data.getSecondaryRace().isEmpty()) {
            return primaryMultiplier;
        }

        double secondaryMultiplier = RaceRegistry.get(data.getSecondaryRace())
                .map(RaceDefinition::xpMultiplier)
                .orElse(1.0);

        double penalty = 0.10;
        try {
            if (TerraServerConfig.HYBRID_PENALTY != null && TerraServerConfig.HYBRID_PENALTY.get() != null) {
                penalty = TerraServerConfig.HYBRID_PENALTY.get();
            }
        } catch (Exception ignored) {}

        return ((primaryMultiplier + secondaryMultiplier) / 2.0) + penalty;
    }

    /**
     * Awards experience to a player and handles sequential level-ups.
     */
    public static void awardXp(ServerPlayer player, double amount) {
        if (player == null || amount <= 0.0) {
            return;
        }

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        int maxLevel = 1000;
        int pointsPerLevel = 5;

        try {
            if (TerraServerConfig.MAX_LEVEL != null && TerraServerConfig.MAX_LEVEL.get() != null) {
                maxLevel = TerraServerConfig.MAX_LEVEL.get();
            }
            if (TerraServerConfig.POINTS_PER_LEVEL != null && TerraServerConfig.POINTS_PER_LEVEL.get() != null) {
                pointsPerLevel = TerraServerConfig.POINTS_PER_LEVEL.get();
            }
        } catch (Exception ignored) {}

        // Non-evolution races and even Evolution races cap visual level at 1000
        if (data.getLevel() >= maxLevel) {
            return;
        }

        double globalMultiplier = 1.0;
        try {
            if (TerraServerConfig.GLOBAL_XP_MULTIPLIER != null && TerraServerConfig.GLOBAL_XP_MULTIPLIER.get() != null) {
                globalMultiplier = TerraServerConfig.GLOBAL_XP_MULTIPLIER.get();
            }
        } catch (Exception ignored) {}

        double effectiveAmount = amount * globalMultiplier;
        data.setCurrentXp(data.getCurrentXp() + effectiveAmount);

        boolean leveledUp = false;
        double requiredXp = getRequiredXp(data);

        while (data.getCurrentXp() >= requiredXp && data.getLevel() < maxLevel) {
            data.setCurrentXp(data.getCurrentXp() - requiredXp);
            data.setLevel(data.getLevel() + 1);
            data.setAvailableStatusPoints(data.getAvailableStatusPoints() + pointsPerLevel);
            data.setTotalStatusPointsAcquired(data.getTotalStatusPointsAcquired() + pointsPerLevel);
            leveledUp = true;
            requiredXp = getRequiredXp(data);
        }

        if (leveledUp) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSoundEvents.LEVEL_UP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(Component.literal("§6§l[TerraForge] §aLevel Up! You reached Level " + data.getLevel() + "!"));
            TerraLogger.info("LEVEL", "Player {} leveled up to Level {}", player.getName().getString(), data.getLevel());
        }

        TerraNetwork.sync(player);
    }

    /**
     * Handles rewards granted upon killing an entity (KillPointProgress and Status Points).
     */
    public static void handleKillReward(ServerPlayer player, ThreatRating rating, boolean isFirstBossKill) {
        if (player == null || rating == null) {
            return;
        }

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        int maxLevel = 1000;
        try {
            if (TerraServerConfig.MAX_LEVEL != null && TerraServerConfig.MAX_LEVEL.get() != null) {
                maxLevel = TerraServerConfig.MAX_LEVEL.get();
            }
        } catch (Exception ignored) {}

        // Rule of Level 1000: Non-evolution races cease gaining points from kills
        if (data.getLevel() >= maxLevel && !data.hasEvolution()) {
            return;
        }

        // Calculate fractional point yield
        double pointYield = rating.getDefaultPointYield();
        double multiplier = 1.0;
        double weakPenalty = 0.10;
        double firstBossBonus = 50.0;
        double repeatBossMultiplier = 1.0;

        try {
            if (TerraServerConfig.KILL_POINT_MULTIPLIER != null && TerraServerConfig.KILL_POINT_MULTIPLIER.get() != null) {
                multiplier = TerraServerConfig.KILL_POINT_MULTIPLIER.get();
            }
            if (TerraServerConfig.WEAK_MOB_PENALTY != null && TerraServerConfig.WEAK_MOB_PENALTY.get() != null) {
                weakPenalty = TerraServerConfig.WEAK_MOB_PENALTY.get();
            }
            if (TerraServerConfig.FIRST_BOSS_KILL_BONUS != null && TerraServerConfig.FIRST_BOSS_KILL_BONUS.get() != null) {
                firstBossBonus = TerraServerConfig.FIRST_BOSS_KILL_BONUS.get();
            }
            if (TerraServerConfig.REPEAT_BOSS_KILL_MULTIPLIER != null && TerraServerConfig.REPEAT_BOSS_KILL_MULTIPLIER.get() != null) {
                repeatBossMultiplier = TerraServerConfig.REPEAT_BOSS_KILL_MULTIPLIER.get();
            }
        } catch (Exception ignored) {}

        pointYield *= multiplier;

        if (rating == ThreatRating.WEAK_MOB) {
            pointYield *= weakPenalty;
        } else if (rating == ThreatRating.BOSS || rating == ThreatRating.EVENT_BOSS) {
            if (isFirstBossKill) {
                pointYield += firstBossBonus;
            } else {
                pointYield *= repeatBossMultiplier;
            }
        }

        // Accumulate progress
        double currentProgress = data.getKillPointProgress() + pointYield;
        int wholePoints = (int) Math.floor(currentProgress);
        data.setKillPointProgress(currentProgress - wholePoints);

        if (wholePoints > 0) {
            data.setAvailableStatusPoints(data.getAvailableStatusPoints() + wholePoints);
            data.setTotalStatusPointsAcquired(data.getTotalStatusPointsAcquired() + wholePoints);
            player.sendSystemMessage(Component.literal("§6§l[TerraForge] §e+" + wholePoints + " Status Point(s) earned from kills!"));
        }

        // Also award XP if below level cap
        if (data.getLevel() < maxLevel) {
            awardXp(player, rating.getDefaultXpYield());
        } else {
            TerraNetwork.sync(player);
        }
    }
}
