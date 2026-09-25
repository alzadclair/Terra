package com.terraforge.rpg.network.payload;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-Server request to equip a special accessory by ID.
 */
public record EquipSpecialAccessoryRequest(String accessoryId) implements CustomPacketPayload {
    public static final Type<EquipSpecialAccessoryRequest> TYPE = new Type<>(TerraForgeRPG.id("equip_special_accessory"));

    public static final StreamCodec<FriendlyByteBuf, EquipSpecialAccessoryRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(value.accessoryId),
            buffer -> new EquipSpecialAccessoryRequest(buffer.readUtf(64))
    );

    @Override
    public Type<EquipSpecialAccessoryRequest> type() {
        return TYPE;
    }
}
