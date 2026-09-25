package com.terraforge.rpg.player.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.UUID;

/**
 * Server-authoritative persistent RPG data attachment for players.
 */
public final class PlayerRPGData implements INBTSerializable<CompoundTag> {
    public static final int CURRENT_SAVE_VERSION = 1;

    private UUID playerUuid;
    private int saveVersion = CURRENT_SAVE_VERSION;
    private boolean raceAssigned = false;

    // Level & Experience
    private int level = 1;
    private double currentXp = 0.0;
    private int availableStatusPoints = 5;
    private int totalStatusPointsAcquired = 5;
    private int statusPointsSpent = 0;
    private double killPointProgress = 0.0;

    // Race & Hybrid
    private String primaryRace = "human";
    private String secondaryRace = "";
    private boolean hybrid = false;
    private String primaryAbility = "evolution";
    private String secondaryAbility = "";

    // The 7 RPG Stat Invested Ranks
    private int defenseRank = 0;
    private int magicDefenseRank = 0;
    private int attackRank = 0;
    private int magicAttackRank = 0;
    private int criticalRank = 0;
    private int criticalChanceRank = 0;
    private int speedRank = 0;

    // Health & Mana Progression (Terraria Items)
    private int lifeCrystalsUsed = 0; // Max 15 -> +15 * 20 HP = +300 HP (Terraria standard)
    private int lifeFruitsUsed = 0;   // Max 20 -> +20 * 5 HP = +100 HP (Hardmode standard)
    private int manaCrystalsUsed = 0; // Max 9 -> +9 * 20 Mana = +180 Mana (Total 200 standard)

    // Mana & Utility
    private double currentMana = 20.0;
    private double maxMana = 20.0;
    private String equippedSpecialAccessory = "";
    private long primaryAbilityCooldownUntil = 0L;
    private long secondaryAbilityCooldownUntil = 0L;
    private boolean flightAuthorized = false;

    public PlayerRPGData() {}

    // Getters and Setters
    public int getSaveVersion() { return saveVersion; }
    public boolean isRaceAssigned() { return raceAssigned; }
    public void setRaceAssigned(boolean raceAssigned) { this.raceAssigned = raceAssigned; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }

    public double getCurrentXp() { return currentXp; }
    public void setCurrentXp(double currentXp) { this.currentXp = Math.max(0.0, currentXp); }

    public int getAvailableStatusPoints() { return availableStatusPoints; }
    public void setAvailableStatusPoints(int points) { this.availableStatusPoints = Math.max(0, points); }

    public int getTotalStatusPointsAcquired() { return totalStatusPointsAcquired; }
    public void setTotalStatusPointsAcquired(int points) { this.totalStatusPointsAcquired = Math.max(0, points); }

    public int getStatusPointsSpent() { return statusPointsSpent; }
    public void setStatusPointsSpent(int points) { this.statusPointsSpent = Math.max(0, points); }

    public double getKillPointProgress() { return killPointProgress; }
    public void setKillPointProgress(double progress) { this.killPointProgress = Math.max(0.0, progress); }

    public String getPrimaryRace() { return primaryRace; }
    public void setPrimaryRace(String primaryRace) { this.primaryRace = primaryRace == null ? "human" : primaryRace; }

    public String getSecondaryRace() { return secondaryRace; }
    public void setSecondaryRace(String secondaryRace) { this.secondaryRace = secondaryRace == null ? "" : secondaryRace; }

    public boolean isHybrid() { return hybrid; }
    public void setHybrid(boolean hybrid) { this.hybrid = hybrid; }

    public String getPrimaryAbility() { return primaryAbility; }
    public void setPrimaryAbility(String ability) { this.primaryAbility = ability == null ? "" : ability; }

    public String getSecondaryAbility() { return secondaryAbility; }
    public void setSecondaryAbility(String ability) { this.secondaryAbility = ability == null ? "" : ability; }

    public int getDefenseRank() { return defenseRank; }
    public void setDefenseRank(int defenseRank) { this.defenseRank = Math.max(0, defenseRank); }

    public int getMagicDefenseRank() { return magicDefenseRank; }
    public void setMagicDefenseRank(int magicDefenseRank) { this.magicDefenseRank = Math.max(0, magicDefenseRank); }

    public int getAttackRank() { return attackRank; }
    public void setAttackRank(int attackRank) { this.attackRank = Math.max(0, attackRank); }

    public int getMagicAttackRank() { return magicAttackRank; }
    public void setMagicAttackRank(int magicAttackRank) { this.magicAttackRank = Math.max(0, magicAttackRank); }

    public int getCriticalRank() { return criticalRank; }
    public void setCriticalRank(int criticalRank) { this.criticalRank = Math.max(0, criticalRank); }

    public int getCriticalChanceRank() { return criticalChanceRank; }
    public void setCriticalChanceRank(int criticalChanceRank) { this.criticalChanceRank = Math.max(0, criticalChanceRank); }

    public int getSpeedRank() { return speedRank; }
    public void setSpeedRank(int speedRank) { this.speedRank = Math.max(0, speedRank); }

    public int getLifeCrystalsUsed() { return lifeCrystalsUsed; }
    public void setLifeCrystalsUsed(int count) { this.lifeCrystalsUsed = Math.clamp(count, 0, 15); }

    public int getLifeFruitsUsed() { return lifeFruitsUsed; }
    public void setLifeFruitsUsed(int count) { this.lifeFruitsUsed = Math.clamp(count, 0, 20); }

    public int getManaCrystalsUsed() { return manaCrystalsUsed; }
    public void setManaCrystalsUsed(int count) {
        this.manaCrystalsUsed = Math.clamp(count, 0, 9);
        this.maxMana = 20.0 + (manaCrystalsUsed * 20.0);
    }

    public double getCurrentMana() { return currentMana; }
    public void setCurrentMana(double currentMana) { this.currentMana = Math.clamp(currentMana, 0.0, maxMana); }

    public double getMaxMana() { return maxMana; }
    public void setMaxMana(double maxMana) { this.maxMana = Math.max(0.0, maxMana); }

    public String getEquippedSpecialAccessory() { return equippedSpecialAccessory; }
    public void setEquippedSpecialAccessory(String id) { this.equippedSpecialAccessory = id == null ? "" : id; }

    public long getPrimaryAbilityCooldownUntil() { return primaryAbilityCooldownUntil; }
    public void setPrimaryAbilityCooldownUntil(long tick) { this.primaryAbilityCooldownUntil = Math.max(0L, tick); }

    public long getSecondaryAbilityCooldownUntil() { return secondaryAbilityCooldownUntil; }
    public void setSecondaryAbilityCooldownUntil(long tick) { this.secondaryAbilityCooldownUntil = Math.max(0L, tick); }

    public boolean isFlightAuthorized() { return flightAuthorized; }
    public void setFlightAuthorized(boolean flightAuthorized) { this.flightAuthorized = flightAuthorized; }

    public boolean hasEvolution() {
        return "human".equalsIgnoreCase(primaryRace) || (hybrid && "human".equalsIgnoreCase(secondaryRace));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("save_version", CURRENT_SAVE_VERSION);
        tag.putBoolean("race_assigned", raceAssigned);
        tag.putInt("level", level);
        tag.putDouble("current_xp", currentXp);
        tag.putInt("available_points", availableStatusPoints);
        tag.putInt("total_points_acquired", totalStatusPointsAcquired);
        tag.putInt("status_points_spent", statusPointsSpent);
        tag.putDouble("kill_point_progress", killPointProgress);

        tag.putString("primary_race", primaryRace);
        tag.putString("secondary_race", secondaryRace);
        tag.putBoolean("is_hybrid", hybrid);
        tag.putString("primary_ability", primaryAbility);
        tag.putString("secondary_ability", secondaryAbility);

        tag.putInt("rank_defense", defenseRank);
        tag.putInt("rank_magic_defense", magicDefenseRank);
        tag.putInt("rank_attack", attackRank);
        tag.putInt("rank_magic_attack", magicAttackRank);
        tag.putInt("rank_critical", criticalRank);
        tag.putInt("rank_critical_chance", criticalChanceRank);
        tag.putInt("rank_speed", speedRank);

        tag.putInt("life_crystals", lifeCrystalsUsed);
        tag.putInt("life_fruits", lifeFruitsUsed);
        tag.putInt("mana_crystals", manaCrystalsUsed);

        tag.putDouble("current_mana", currentMana);
        tag.putDouble("max_mana", maxMana);
        tag.putString("special_accessory", equippedSpecialAccessory);
        tag.putLong("primary_cooldown", primaryAbilityCooldownUntil);
        tag.putLong("secondary_cooldown", secondaryAbilityCooldownUntil);
        tag.putBoolean("flight_authorized", flightAuthorized);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.saveVersion = tag.getInt("save_version");
        this.raceAssigned = tag.getBoolean("race_assigned");
        this.level = Math.max(1, tag.getInt("level"));
        this.currentXp = Math.max(0.0, tag.getDouble("current_xp"));
        this.availableStatusPoints = Math.max(0, tag.getInt("available_points"));
        this.totalStatusPointsAcquired = Math.max(0, tag.getInt("total_points_acquired"));
        this.statusPointsSpent = Math.max(0, tag.getInt("status_points_spent"));
        this.killPointProgress = Math.max(0.0, tag.getDouble("kill_point_progress"));

        this.primaryRace = tag.getString("primary_race");
        if (primaryRace.isEmpty()) primaryRace = "human";
        this.secondaryRace = tag.getString("secondary_race");
        this.hybrid = tag.getBoolean("is_hybrid");
        this.primaryAbility = tag.getString("primary_ability");
        this.secondaryAbility = tag.getString("secondary_ability");

        this.defenseRank = Math.max(0, tag.getInt("rank_defense"));
        this.magicDefenseRank = Math.max(0, tag.getInt("rank_magic_defense"));
        this.attackRank = Math.max(0, tag.getInt("rank_attack"));
        this.magicAttackRank = Math.max(0, tag.getInt("rank_magic_attack"));
        this.criticalRank = Math.max(0, tag.getInt("rank_critical"));
        this.criticalChanceRank = Math.max(0, tag.getInt("rank_critical_chance"));
        this.speedRank = Math.max(0, tag.getInt("rank_speed"));

        this.lifeCrystalsUsed = Math.clamp(tag.getInt("life_crystals"), 0, 15);
        this.lifeFruitsUsed = Math.clamp(tag.getInt("life_fruits"), 0, 20);
        this.setManaCrystalsUsed(Math.clamp(tag.getInt("mana_crystals"), 0, 9));

        double savedMaxMana = tag.getDouble("max_mana");
        if (savedMaxMana > 0) this.maxMana = savedMaxMana;
        this.currentMana = Math.clamp(tag.getDouble("current_mana"), 0.0, maxMana);
        this.equippedSpecialAccessory = tag.getString("special_accessory");
        this.primaryAbilityCooldownUntil = Math.max(0L, tag.getLong("primary_cooldown"));
        this.secondaryAbilityCooldownUntil = Math.max(0L, tag.getLong("secondary_cooldown"));
        this.flightAuthorized = tag.getBoolean("flight_authorized");
    }
}
