package com.terraforge.rpg.network.payload;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-Server request to open the character menu (Key K).
 */
public record OpenCharacterMenuRequest() implements CustomPacketPayload {
    public static final Type<OpenCharacterMenuRequest> TYPE = new Type<>(TerraForgeRPG.id("open_character_menu"));

    public static final StreamCodec<FriendlyByteBuf, OpenCharacterMenuRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {},
            buffer -> new OpenCharacterMenuRequest()
    );

    @Override
    public Type<OpenCharacterMenuRequest> type() {
        return TYPE;
    }
}
