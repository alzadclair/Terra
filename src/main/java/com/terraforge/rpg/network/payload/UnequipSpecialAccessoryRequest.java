package com.terraforge.rpg.network.payload;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-Server request to unequip the active special accessory.
 */
public record UnequipSpecialAccessoryRequest() implements CustomPacketPayload {
    public static final Type<UnequipSpecialAccessoryRequest> TYPE = new Type<>(TerraForgeRPG.id("unequip_special_accessory"));

    public static final StreamCodec<FriendlyByteBuf, UnequipSpecialAccessoryRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {},
            buffer -> new UnequipSpecialAccessoryRequest()
    );

    @Override
    public Type<UnequipSpecialAccessoryRequest> type() {
        return TYPE;
    }
}
