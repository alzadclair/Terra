package com.terraforge.rpg.event.invasion;

import com.terraforge.rpg.entity.mob.goblin.GoblinPeonEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinSorcererEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinThiefEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinWarriorEntity;
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
 * Goblin Army invasion event.
 * Canonical Terraria 1.4.5.8: requires 80 + 40 * Players kills.
 * Unlocks the Goblin Tinkerer NPC upon successful defense.
 */
public class GoblinArmyEvent implements ITerrariaEvent {

    private static final Random RANDOM = new Random();

    private boolean active = false;
    private int goblinsKilled = 0;
    private int requiredKills = 120;
    private int spawnTimer = 0;

    private ServerBossEvent invasionBar;

    @Override
    public String getEventId() {
        return "goblin_army";
    }

    @Override
    public String getDisplayName() {
        return "Goblin Army";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void start(ServerLevel level) {
        this.active = true;
        this.goblinsKilled = 0;
        this.spawnTimer = 0;

        int playerCount = Math.max(1, level.players().size());
        this.requiredKills = 80 + (40 * playerCount);

        this.invasionBar = new ServerBossEvent(
                Component.literal("Goblin Army (0%)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS
        );

        for (ServerPlayer player : level.players()) {
            invasionBar.addPlayer(player);
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("The goblin army has arrived!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                false
        );

        level.playSound(null, level.players().isEmpty() ? 0 : level.players().get(0).getX(), 100, 0,
                SoundEvents.RAID_HORN.value(), SoundSource.WEATHER, 3.0f, 0.9f);
    }

    @Override
    public void tick(ServerLevel level) {
        if (!active) return;

        float progress = Math.min(1.0f, (float) goblinsKilled / (float) requiredKills);
        invasionBar.setProgress(progress);
        invasionBar.setName(Component.literal("Goblin Army (" + (int) (progress * 100) + "%)")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        for (ServerPlayer player : level.players()) {
            if (!invasionBar.getPlayers().contains(player)) {
                invasionBar.addPlayer(player);
            }
        }

        spawnTimer++;
        if (spawnTimer >= 35) { // Spawn wave every 35 ticks (~1.75s)
            spawnTimer = 0;
            spawnGoblinWave(level);
        }
    }

    private void spawnGoblinWave(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (TerrariaLayers.getLayer((int) player.getY()) == TerrariaLayers.SURFACE) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 35.0;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 35.0;

                int roll = RANDOM.nextInt(100);
                if (roll < 60) {
                    GoblinPeonEntity peon = new GoblinPeonEntity(ModEntities.GOBLIN_PEON.get(), level);
                    peon.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
                    peon.setTarget(player);
                    level.addFreshEntity(peon);
                } else if (roll < 80) {
                    GoblinThiefEntity thief = new GoblinThiefEntity(ModEntities.GOBLIN_THIEF.get(), level);
                    thief.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
                    thief.setTarget(player);
                    level.addFreshEntity(thief);
                } else if (roll < 95) {
                    GoblinWarriorEntity warrior = new GoblinWarriorEntity(ModEntities.GOBLIN_WARRIOR.get(), level);
                    warrior.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
                    warrior.setTarget(player);
                    level.addFreshEntity(warrior);
                } else {
                    GoblinSorcererEntity sorcerer = new GoblinSorcererEntity(ModEntities.GOBLIN_SORCERER.get(), level);
                    sorcerer.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
                    sorcerer.setTarget(player);
                    level.addFreshEntity(sorcerer);
                }
            }
        }
    }

    @Override
    public void stop(ServerLevel level) {
        this.active = false;
        if (invasionBar != null) {
            invasionBar.removeAllPlayers();
        }

        if (level != null && level.getServer() != null) {
            WorldProgressionData worldData = WorldProgressionData.get(level.getServer());
            worldData.markEventCompleted("goblin_army");

            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The goblin army has been defeated!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    false
            );

            level.playSound(null, level.players().isEmpty() ? 0 : level.players().get(0).getX(), 100, 0,
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 2.0f, 1.0f);
        }
    }

    @Override
    public float getProgress() {
        return Math.min(1.0f, (float) goblinsKilled / (float) requiredKills);
    }

    @Override
    public void onEntityKilled(LivingEntity entity, Player killer) {
        if (!active) return;

        if (entity instanceof GoblinPeonEntity || entity instanceof GoblinThiefEntity ||
                entity instanceof GoblinWarriorEntity || entity instanceof GoblinSorcererEntity) {
            goblinsKilled++;
            if (goblinsKilled >= requiredKills) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    stop(serverLevel);
                }
            }
        }
    }

    public int getGoblinsKilled() {
        return goblinsKilled;
    }

    public int getRequiredKills() {
        return requiredKills;
    }
}
