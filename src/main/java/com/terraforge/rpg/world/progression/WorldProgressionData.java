package com.terraforge.rpg.world.progression;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Server-authoritative persistent world progression data for Terraria milestones.
 */
public final class WorldProgressionData extends SavedData {
    public static final int CURRENT_VERSION = 1;
    private static final String DATA_NAME = TerraForgeRPG.MOD_ID + "_world_progression";

    private boolean hardmode = false;
    private String worldEvil = "CORRUPTION";
    private int altarsSmashed = 0;
    private final Set<String> defeatedBosses = new HashSet<>();
    private final Set<String> completedEvents = new HashSet<>();

    public WorldProgressionData() {}

    public static WorldProgressionData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldProgressionData::new, WorldProgressionData::load, null),
                DATA_NAME
        );
    }

    public boolean isHardmode() { return hardmode; }
    public void setHardmode(boolean enabled) {
        if (this.hardmode != enabled) {
            this.hardmode = enabled;
            setDirty();
        }
    }

    public boolean activateHardmode(net.minecraft.server.level.ServerLevel level) {
        if (this.hardmode) return false;
        this.hardmode = true;
        setDirty();

        if (level != null && level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("The ancient spirits of light and dark have been released.")
                            .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.BOLD),
                    false
            );

            for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                player.playNotifySound(net.minecraft.sounds.SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.MASTER, 1.0f, 0.8f);
            }
        }
        return true;
    }

    public int getAltarsSmashed() { return altarsSmashed; }

    public String smashAltar(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos) {
        if (!this.hardmode) return null;

        this.altarsSmashed++;
        setDirty();

        // Tier 1: Cobalt/Palladium, Tier 2: Mythril/Orichalcum, Tier 3: Adamantite/Titanium
        int cycle = ((altarsSmashed - 1) % 3) + 1;
        String oreName;
        net.minecraft.ChatFormatting color;

        if (cycle == 1) {
            oreName = "Cobalt";
            color = net.minecraft.ChatFormatting.BLUE;
        } else if (cycle == 2) {
            oreName = "Mythril";
            color = net.minecraft.ChatFormatting.DARK_GREEN;
        } else {
            oreName = "Titanium";
            color = net.minecraft.ChatFormatting.GRAY;
        }

        if (level != null && level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Your world has been blessed with " + oreName + "!")
                            .withStyle(color, net.minecraft.ChatFormatting.BOLD),
                    false
            );

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.BLOCKS, 2.0f, 0.8f);
        }

        return oreName;
    }

    public String getWorldEvil() { return worldEvil; }
    public void setWorldEvil(String evil) {
        this.worldEvil = evil == null ? "CORRUPTION" : evil;
        setDirty();
    }

    public boolean isBossDefeated(String bossId) {
        return defeatedBosses.contains(bossId);
    }

    public boolean markBossDefeated(String bossId) {
        if (defeatedBosses.add(bossId)) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean isTwinsDefeated() { return isBossDefeated("the_twins"); }
    public boolean isDestroyerDefeated() { return isBossDefeated("the_destroyer"); }
    public boolean isSkeletronPrimeDefeated() { return isBossDefeated("skeletron_prime"); }
    public boolean hasDefeatedAllMechBosses() {
        return isTwinsDefeated() && isDestroyerDefeated() && isSkeletronPrimeDefeated();
    }
    public boolean isPlanteraDefeated() { return isBossDefeated("plantera"); }
    public boolean isGolemDefeated() { return isBossDefeated("golem"); }
    public boolean isFishronDefeated() { return isBossDefeated("duke_fishron"); }
    public boolean isMoonLordDefeated() { return isBossDefeated("moon_lord"); }

    public Set<String> getDefeatedBosses() {
        return Collections.unmodifiableSet(defeatedBosses);
    }

    public boolean isEventCompleted(String eventId) {
        return completedEvents.contains(eventId);
    }

    public boolean markEventCompleted(String eventId) {
        if (completedEvents.add(eventId)) {
            setDirty();
            return true;
        }
        return false;
    }

    public Set<String> getCompletedEvents() {
        return Collections.unmodifiableSet(completedEvents);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("version", CURRENT_VERSION);
        tag.putBoolean("hardmode", hardmode);
        tag.putString("world_evil", worldEvil);
        tag.putInt("altars_smashed", altarsSmashed);

        ListTag bossesTag = new ListTag();
        for (String boss : defeatedBosses) {
            bossesTag.add(StringTag.valueOf(boss));
        }
        tag.put("defeated_bosses", bossesTag);

        ListTag eventsTag = new ListTag();
        for (String ev : completedEvents) {
            eventsTag.add(StringTag.valueOf(ev));
        }
        tag.put("completed_events", eventsTag);

        return tag;
    }

    public static WorldProgressionData load(CompoundTag tag, HolderLookup.Provider provider) {
        WorldProgressionData data = new WorldProgressionData();
        data.hardmode = tag.getBoolean("hardmode");
        data.worldEvil = tag.getString("world_evil");
        if (data.worldEvil.isEmpty()) data.worldEvil = "CORRUPTION";
        data.altarsSmashed = tag.getInt("altars_smashed");

        ListTag bosses = tag.getList("defeated_bosses", Tag.TAG_STRING);
        for (int i = 0; i < bosses.size(); i++) {
            data.defeatedBosses.add(bosses.getString(i));
        }

        ListTag events = tag.getList("completed_events", Tag.TAG_STRING);
        for (int i = 0; i < events.size(); i++) {
            data.completedEvents.add(events.getString(i));
        }
        return data;
    }
}
