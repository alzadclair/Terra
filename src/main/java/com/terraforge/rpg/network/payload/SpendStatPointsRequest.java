package com.terraforge.rpg.network.payload;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-Server request to allocate status points into a specific attribute.
 * Server validates amounts, ownership, and caps before applying.
 */
public record SpendStatPointsRequest(String attributeKey, int amount) implements CustomPacketPayload {
    public static final Type<SpendStatPointsRequest> TYPE = new Type<>(TerraForgeRPG.id("spend_stat_points"));

    public static final StreamCodec<FriendlyByteBuf, SpendStatPointsRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeUtf(value.attributeKey);
                buffer.writeVarInt(value.amount);
            },
            buffer -> new SpendStatPointsRequest(buffer.readUtf(64), buffer.readVarInt())
    );

    @Override
    public Type<SpendStatPointsRequest> type() {
        return TYPE;
    }
}
