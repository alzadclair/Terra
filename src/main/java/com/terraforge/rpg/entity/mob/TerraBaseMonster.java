package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Base monster class for Terraria 1.4.5.8 enemies in TerraForge RPG.
 * Integrates ThreatRating, Terraria Defense, and canonical coin drops.
 */
public abstract class TerraBaseMonster extends Monster implements ITerrariaMob {

    protected final Random mobRandom = new Random();

    private final ThreatRating threatRating;
    private final long minCoins;
    private final long maxCoins;
    private final int terrariaDefense;
    private final DamageClass attackDamageClass;

    protected TerraBaseMonster(
            EntityType<? extends Monster> entityType,
            Level level,
            ThreatRating threatRating,
            long minCoins,
            long maxCoins,
            int terrariaDefense,
            DamageClass attackDamageClass
    ) {
        super(entityType, level);
        this.threatRating = threatRating;
        this.minCoins = minCoins;
        this.maxCoins = maxCoins;
        this.terrariaDefense = terrariaDefense;
        this.attackDamageClass = attackDamageClass;
    }

    @Override
    public ThreatRating getThreatRating() {
        return threatRating;
    }

    @Override
    public long getMinCoinDrop() {
        return minCoins;
    }

    @Override
    public long getMaxCoinDrop() {
        return maxCoins;
    }

    @Override
    public int getTerrariaDefense() {
        return terrariaDefense;
    }

    @Override
    public DamageClass getAttackDamageClass() {
        return attackDamageClass;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide()) {
            dropTerrariaCoins();
        }
    }

    /**
     * Drops coins based on the mob's min and max copper values.
     */
    protected void dropTerrariaCoins() {
        if (maxCoins <= 0) return;

        long dropAmount;
        if (maxCoins > minCoins) {
            dropAmount = minCoins + (Math.abs(mobRandom.nextLong()) % (maxCoins - minCoins + 1));
        } else {
            dropAmount = minCoins;
        }

        if (dropAmount <= 0) return;

        long platinum = dropAmount / CoinHelper.COPPER_PER_PLATINUM;
        long remPlat = dropAmount % CoinHelper.COPPER_PER_PLATINUM;

        long gold = remPlat / CoinHelper.COPPER_PER_GOLD;
        long remGold = remPlat % CoinHelper.COPPER_PER_GOLD;

        long silver = remGold / CoinHelper.COPPER_PER_SILVER;
        long copper = remGold % CoinHelper.COPPER_PER_SILVER;

        spawnCoinEntity(ModItems.PLATINUM_COIN.get(), platinum);
        spawnCoinEntity(ModItems.GOLD_COIN.get(), gold);
        spawnCoinEntity(ModItems.SILVER_COIN.get(), silver);
        spawnCoinEntity(ModItems.COPPER_COIN.get(), copper);
    }

    private void spawnCoinEntity(net.minecraft.world.item.Item coinItem, long count) {
        while (count > 0) {
            int stackCount = (int) Math.min(count, 99);
            ItemStack stack = new ItemStack(coinItem, stackCount);
            ItemEntity itemEntity = new ItemEntity(
                    this.level(),
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    stack
            );
            itemEntity.setDefaultPickUpDelay();
            itemEntity.setDeltaMovement(
                    (mobRandom.nextDouble() - 0.5) * 0.2,
                    0.25 + mobRandom.nextDouble() * 0.15,
                    (mobRandom.nextDouble() - 0.5) * 0.2
            );
            this.level().addFreshEntity(itemEntity);
            count -= stackCount;
        }
    }
}
