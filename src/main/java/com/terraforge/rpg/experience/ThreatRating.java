package com.terraforge.rpg.experience;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Threat classifications for entities that determine XP yield and fractional Status Point gains.
 */
public enum ThreatRating {
    WEAK_MOB(0.10, 5.0),
    COMMON(0.25, 15.0),
    STRONG(0.50, 40.0),
    ELITE(1.00, 100.0),
    RARE_MOB(5.00, 350.0),
    MINIBOSS(10.00, 800.0),
    BOSS(20.00, 2500.0),
    EVENT_BOSS(50.00, 6000.0);

    private final double defaultPointYield;
    private final double defaultXpYield;

    ThreatRating(double defaultPointYield, double defaultXpYield) {
        this.defaultPointYield = defaultPointYield;
        this.defaultXpYield = defaultXpYield;
    }

    public double getDefaultPointYield() {
        return defaultPointYield;
    }

    public double getDefaultXpYield() {
        return defaultXpYield;
    }

    /**
     * Resolves the threat rating of a given living entity.
     */
    public static ThreatRating resolve(LivingEntity entity) {
        if (entity == null) {
            return COMMON;
        }

        if (entity instanceof com.terraforge.rpg.entity.mob.ITerrariaMob terrariaMob) {
            return terrariaMob.getThreatRating();
        }

        // Vanilla bosses & custom boss indicators
        if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
            return BOSS;
        }

        float maxHp = entity.getMaxHealth();

        // Non-hostile or minimal HP
        if (!(entity instanceof Enemy)) {
            if (maxHp <= 10.0f) {
                return WEAK_MOB;
            }
            return COMMON;
        }

        // Hostile entities by health tier
        if (maxHp <= 12.0f) {
            return WEAK_MOB;
        } else if (maxHp <= 30.0f) {
            return COMMON;
        } else if (maxHp <= 80.0f) {
            return STRONG;
        } else if (maxHp <= 200.0f) {
            return ELITE;
        } else if (maxHp <= 500.0f) {
            return MINIBOSS;
        } else {
            return BOSS;
        }
    }
}
