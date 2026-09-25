package com.terraforge.rpg.event.invasion;

import com.terraforge.rpg.entity.mob.DemonEyeEntity;
import com.terraforge.rpg.entity.mob.TerraZombieEntity;
import com.terraforge.rpg.event.ITerrariaEvent;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.world.layer.TerrariaLayers;
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
 * Canonical Terraria Blood Moon event.
 * Occurs during night (13000 to 23500 ticks), tripling monster spawns and infusing creatures with blood fury.
 */
public class BloodMoonEvent implements ITerrariaEvent {

    private static final Random RANDOM = new Random();

    private boolean active = false;
    private int spawnTimer = 0;
    private ServerBossEvent bloodMoonBar;

    @Override
    public String getEventId() {
        return "blood_moon";
    }

    @Override
    public String getDisplayName() {
        return "Blood Moon";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void start(ServerLevel level) {
        this.active = true;
        this.spawnTimer = 0;

        this.bloodMoonBar = new ServerBossEvent(
                Component.literal("The Blood Moon is rising...").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );

        for (ServerPlayer player : level.players()) {
            bloodMoonBar.addPlayer(player);
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("The Blood Moon is rising...").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                false
        );

        level.playSound(null, level.players().isEmpty() ? 0 : level.players().get(0).getX(), 100, 0,
                SoundEvents.WITHER_AMBIENT, SoundSource.WEATHER, 1.8f, 0.6f);
    }

    @Override
    public void tick(ServerLevel level) {
        if (!active) return;

        long dayTime = level.getDayTime() % 24000L;

        // Morning end check (23500 or daytime < 12500)
        if (dayTime > 23500L || dayTime < 12500L) {
            stop(level);
            return;
        }

        // Night progress calculation (13000 to 23000 ticks)
        float progress = Math.clamp((float) (dayTime - 13000L) / 10000.0f, 0.0f, 1.0f);
        bloodMoonBar.setProgress(progress);
        bloodMoonBar.setName(Component.literal("Blood Moon (" + (int) (progress * 100) + "%)").withStyle(ChatFormatting.DARK_RED));

        for (ServerPlayer player : level.players()) {
            if (!bloodMoonBar.getPlayers().contains(player)) {
                bloodMoonBar.addPlayer(player);
            }
        }

        // Hyper-spawn monsters near surface players every 30 ticks
        spawnTimer++;
        if (spawnTimer >= 30) {
            spawnTimer = 0;
            spawnBloodMoonMobs(level);
        }
    }

    private void spawnBloodMoonMobs(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (TerrariaLayers.getLayer((int) player.getY()) == TerrariaLayers.SURFACE) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 40.0;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 40.0;

                if (RANDOM.nextBoolean()) {
                    TerraZombieEntity zombie = new TerraZombieEntity(ModEntities.TERRA_ZOMBIE.get(), level);
                    zombie.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
                    zombie.setTarget(player);
                    level.addFreshEntity(zombie);
                } else {
                    DemonEyeEntity eye = new DemonEyeEntity(ModEntities.DEMON_EYE.get(), level);
                    eye.setPos(player.getX() + offsetX, player.getY() + 10.0, player.getZ() + offsetZ);
                    eye.setTarget(player);
                    level.addFreshEntity(eye);
                }
            }
        }
    }

    @Override
    public void stop(ServerLevel level) {
        this.active = false;
        if (bloodMoonBar != null) {
            bloodMoonBar.removeAllPlayers();
        }

        if (level != null && level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The Blood Moon has fallen.").withStyle(ChatFormatting.DARK_RED),
                    false
            );
        }
    }

    @Override
    public float getProgress() {
        return bloodMoonBar != null ? bloodMoonBar.getProgress() : 0.0f;
    }

    @Override
    public void onEntityKilled(LivingEntity entity, Player killer) {
        // Blood Moon kills drop extra coins/loot handled by standard mob drops
    }
}
