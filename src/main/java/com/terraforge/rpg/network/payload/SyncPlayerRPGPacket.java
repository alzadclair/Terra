package com.terraforge.rpg.network.payload;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Objects;

/**
 * Server-to-Client packet syncing the authoritative PlayerRPGData to the client.
 */
public record SyncPlayerRPGPacket(CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncPlayerRPGPacket> TYPE = new Type<>(TerraForgeRPG.id("sync_player_rpg"));

    public static final StreamCodec<FriendlyByteBuf, SyncPlayerRPGPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeNbt(value.data),
            buffer -> new SyncPlayerRPGPacket(Objects.requireNonNullElseGet(buffer.readNbt(), CompoundTag::new))
    );

    public SyncPlayerRPGPacket {
        data = data.copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }

    @Override
    public Type<SyncPlayerRPGPacket> type() {
        return TYPE;
    }
}
