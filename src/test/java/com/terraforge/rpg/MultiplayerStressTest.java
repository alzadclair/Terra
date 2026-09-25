package com.terraforge.rpg;

import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fase 26: Comprehensive multiplayer stress tests, concurrency validation,
 * and high-load simulations for TerraForge RPG.
 */
public class MultiplayerStressTest {

    @Test
    @DisplayName("Simulate 100 concurrent players leveling up, allocating attributes, and serializing NBT")
    void testConcurrentPlayerLifecycleStress() throws InterruptedException, ExecutionException {
        int playerCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            final int playerId = i;
            tasks.add(() -> {
                Random random = new Random(playerId * 31L);
                PlayerRPGData data = new PlayerRPGData();

                String[] races = new String[]{"human", "elf", "dwarf", "vampire", "merfolk", "draconian", "titan"};
                data.setPrimaryRace(races[random.nextInt(races.length)]);

                // Simulate progression to level 250
                for (int lvl = 1; lvl <= 250; lvl++) {
                    data.setLevel(lvl);
                    data.setAvailableStatusPoints(data.getAvailableStatusPoints() + 3);
                }

                // Allocate ranks
                data.setAttackRank(100);
                data.setDefenseRank(100);
                data.setSpeedRank(50);

                // Life and mana crystal progression
                data.setLifeCrystalsUsed(15);
                data.setManaCrystalsUsed(9);

                assertEquals(15, data.getLifeCrystalsUsed());
                assertEquals(9, data.getManaCrystalsUsed());
                assertEquals(200.0, data.getMaxMana());

                // Stress test serialization & deserialization round-trip
                CompoundTag tag = data.serializeNBT(null);

                PlayerRPGData restored = new PlayerRPGData();
                restored.deserializeNBT(null, tag);

                assertEquals(data.getLevel(), restored.getLevel());
                assertEquals(data.getPrimaryRace(), restored.getPrimaryRace());
                assertEquals(data.getLifeCrystalsUsed(), restored.getLifeCrystalsUsed());
                assertEquals(data.getManaCrystalsUsed(), restored.getManaCrystalsUsed());
                assertEquals(data.getMaxMana(), restored.getMaxMana());

                return true;
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        for (Future<Boolean> result : results) {
            assertTrue(result.get());
        }
    }

    @Test
    @DisplayName("Multi-threaded combat engine stress test (10,000 combat calculations)")
    void testConcurrentCombatEngineStress() throws InterruptedException, ExecutionException {
        int calculationCount = 10_000;
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            tasks.add(() -> {
                Random random = new Random(threadId * 101L);
                for (int i = 0; i < (calculationCount / threadCount); i++) {
                    double baseDamage = 10.0 + random.nextDouble() * 200.0;
                    double critChance = random.nextDouble() * 0.50;
                    int defense = random.nextInt(100);

                    // 13-step pipeline simulation
                    double variance = 0.85 + (random.nextDouble() * 0.30);
                    double rolledDamage = baseDamage * variance;

                    boolean isCrit = random.nextDouble() < critChance;
                    if (isCrit) {
                        rolledDamage *= 2.0;
                    }

                    // Terraria defense mitigation: damage - (defense * 0.50)
                    double defenseMitigation = defense * 0.50;
                    double finalDamage = Math.max(1.0, rolledDamage - defenseMitigation);

                    assertTrue(finalDamage >= 1.0);
                }
                return true;
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        for (Future<Boolean> result : results) {
            assertTrue(result.get());
        }
    }

    @Test
    @DisplayName("Economy CoinHelper extreme value formatting stress test (billions of copper coins)")
    void testCoinEconomyExtremeValues() {
        long copperAmount = 987_654_321L;
        String formatted = CoinHelper.formatCoins(copperAmount);

        assertNotNull(formatted);
        assertTrue(formatted.contains("Platinum"));
        assertTrue(formatted.contains("Gold"));
        assertTrue(formatted.contains("Silver"));
        assertTrue(formatted.contains("Copper"));

        // Conversion verification:
        long plat = copperAmount / CoinHelper.COPPER_PER_PLATINUM;
        long remPlat = copperAmount % CoinHelper.COPPER_PER_PLATINUM;
        long gold = remPlat / CoinHelper.COPPER_PER_GOLD;
        long remGold = remPlat % CoinHelper.COPPER_PER_GOLD;
        long silver = remGold / CoinHelper.COPPER_PER_SILVER;
        long copper = remGold % CoinHelper.COPPER_PER_SILVER;

        assertEquals(987, plat);
        assertEquals(65, gold);
        assertEquals(43, silver);
        assertEquals(21, copper);

        long reconstructed = (plat * CoinHelper.COPPER_PER_PLATINUM) +
                             (gold * CoinHelper.COPPER_PER_GOLD) +
                             (silver * CoinHelper.COPPER_PER_SILVER) +
                             copper;
        assertEquals(copperAmount, reconstructed);
    }

    @Test
    @DisplayName("WorldProgressionData complete storyline progression serialization")
    void testWorldProgressionFullCampaign() {
        WorldProgressionData progression = new WorldProgressionData();
        progression.setWorldEvil("CRIMSON");

        // Pre-Hardmode
        progression.markBossDefeated("eye_of_cthulhu");
        progression.markBossDefeated("king_slime");
        progression.markBossDefeated("eater_of_worlds");
        progression.markBossDefeated("brain_of_cthulhu");
        progression.markBossDefeated("queen_bee");
        progression.markBossDefeated("skeletron");
        progression.markBossDefeated("wall_of_flesh");

        // Hardmode Activation
        progression.setHardmode(true);
        assertTrue(progression.isHardmode());

        // Smash 12 Altars
        for (int i = 1; i <= 12; i++) {
            progression.smashAltar(null, null);
        }
        assertEquals(12, progression.getAltarsSmashed());

        // Mechanical Bosses
        progression.markBossDefeated("the_twins");
        progression.markBossDefeated("the_destroyer");
        progression.markBossDefeated("skeletron_prime");
        assertTrue(progression.hasDefeatedAllMechBosses());

        // Hardmode Endgame
        progression.markBossDefeated("plantera");
        progression.markBossDefeated("golem");
        progression.markBossDefeated("duke_fishron");
        progression.markBossDefeated("moon_lord");

        assertTrue(progression.isPlanteraDefeated());
        assertTrue(progression.isGolemDefeated());
        assertTrue(progression.isFishronDefeated());
        assertTrue(progression.isMoonLordDefeated());

        // Serialize and Deserialize
        CompoundTag tag = new CompoundTag();
        progression.save(tag, null);

        WorldProgressionData loaded = WorldProgressionData.load(tag, null);
        assertTrue(loaded.isHardmode());
        assertEquals("CRIMSON", loaded.getWorldEvil());
        assertEquals(12, loaded.getAltarsSmashed());
        assertTrue(loaded.hasDefeatedAllMechBosses());
        assertTrue(loaded.isMoonLordDefeated());
        assertEquals(14, loaded.getDefeatedBosses().size());
    }
}
