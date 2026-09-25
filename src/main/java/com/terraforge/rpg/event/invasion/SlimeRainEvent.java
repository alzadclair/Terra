package com.terraforge.rpg.event.invasion;

import com.terraforge.rpg.boss.prehardmode.KingSlimeEntity;
import com.terraforge.rpg.entity.mob.TerraSlimeEntity;
import com.terraforge.rpg.event.ITerrariaEvent;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.world.layer.TerrariaLayers;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

/**
 * Slime Rain event.
 * Canonical Terraria 1.4.5.8: requires 150 slimes killed (75 if King Slime defeated before).
 * Summons King Slime when goal is reached, concluding when King Slime is defeated.
 */
public class SlimeRainEvent implements ITerrariaEvent {

    private static final Random RANDOM = new Random();

    private boolean active = false;
    private boolean bossSpawned = false;
    private int slimesKilled = 0;
    private int targetKills = 150;
    private int spawnTimer = 0;

    private ServerBossEvent progressBossBar;

    @Override
    public String getEventId() {
        return "slime_rain";
    }

    @Override
    public String getDisplayName() {
        return "Slime Rain";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void start(ServerLevel level) {
        this.active = true;
        this.bossSpawned = false;
        this.slimesKilled = 0;
        this.spawnTimer = 0;

        // Terraria rule: 75 if King Slime already beaten, else 150
        WorldProgressionData worldData = WorldProgressionData.get(level.getServer());
        this.targetKills = worldData.isBossDefeated("king_slime") ? 75 : 150;

        this.progressBossBar = new ServerBossEvent(
                Component.literal("Slime Rain: 0 / " + targetKills).withStyle(ChatFormatting.AQUA),
                BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarOverlay.PROGRESS
        );

        for (ServerPlayer player : level.players()) {
            progressBossBar.addPlayer(player);
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("Slime is falling from the sky!").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                false
        );

        level.playSound(null, level.players().isEmpty() ? 0 : level.players().get(0).getX(), 100, 0,
                SoundEvents.SLIME_SQUISH, SoundSource.WEATHER, 2.0f, 0.8f);
    }

    @Override
    public void tick(ServerLevel level) {
        if (!active) return;

        // Keep boss bar synchronized
        for (ServerPlayer player : level.players()) {
            if (!progressBossBar.getPlayers().contains(player)) {
                progressBossBar.addPlayer(player);
            }
        }

        float progress = Math.min(1.0f, (float) slimesKilled / (float) targetKills);
        progressBossBar.setProgress(progress);
        progressBossBar.setName(Component.literal("Slime Rain: " + slimesKilled + " / " + targetKills).withStyle(ChatFormatting.AQUA));

        // Raining slimes from sky every 40 ticks (~2 seconds)
        spawnTimer++;
        if (spawnTimer >= 40 && !bossSpawned) {
            spawnTimer = 0;
            spawnRainSlimes(level);
        }
    }

    private void spawnRainSlimes(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (TerrariaLayers.getLayer((int) player.getY()) == TerrariaLayers.SURFACE) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 30.0;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 30.0;
                double spawnY = player.getY() + 20.0; // Fall from sky

                TerraSlimeEntity.SlimeVariant variant = RANDOM.nextBoolean() ?
                        TerraSlimeEntity.SlimeVariant.BLUE : TerraSlimeEntity.SlimeVariant.GREEN;

                TerraSlimeEntity slime = new TerraSlimeEntity(ModEntities.BLUE_SLIME.get(), level, variant);
                slime.setPos(player.getX() + offsetX, spawnY, player.getZ() + offsetZ);
                slime.setTarget(player);
                level.addFreshEntity(slime);
            }
        }
    }

    @Override
    public void stop(ServerLevel level) {
        this.active = false;
        if (progressBossBar != null) {
            progressBossBar.removeAllPlayers();
        }

        if (level != null && level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Slime Rain has stopped.").withStyle(ChatFormatting.AQUA),
                    false
            );
        }
    }

    @Override
    public float getProgress() {
        return Math.min(1.0f, (float) slimesKilled / (float) targetKills);
    }

    @Override
    public void onEntityKilled(LivingEntity entity, Player killer) {
        if (!active) return;

        if (entity instanceof TerraSlimeEntity) {
            slimesKilled++;
            if (slimesKilled >= targetKills && !bossSpawned) {
                bossSpawned = true;
                if (killer != null && killer.level() instanceof ServerLevel serverLevel) {
                    // Spawn King Slime
                    KingSlimeEntity king = new KingSlimeEntity(ModEntities.KING_SLIME.get(), serverLevel);
                    king.setPos(killer.getX() + 10.0, killer.getY() + 10.0, killer.getZ() + 10.0);
                    king.setTarget(killer);
                    serverLevel.addFreshEntity(king);

                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal("King Slime has awoken!").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD),
                            false
                    );
                }
            }
        } else if (entity instanceof KingSlimeEntity) {
            // King Slime defeated - complete the event
            if (entity.level() instanceof ServerLevel serverLevel) {
                stop(serverLevel);
            }
        }
    }

    public int getSlimesKilled() {
        return slimesKilled;
    }

    public int getTargetKills() {
        return targetKills;
    }
}
