package com.terraforge.rpg.network.payload;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-Server request to trigger a racial ability.
 * Slot 0 = Primary ability (Key R), Slot 1 = Secondary hybrid ability (Key V).
 */
public record ActivateRaceAbilityRequest(int slot) implements CustomPacketPayload {
    public static final Type<ActivateRaceAbilityRequest> TYPE = new Type<>(TerraForgeRPG.id("activate_race_ability"));

    public static final StreamCodec<FriendlyByteBuf, ActivateRaceAbilityRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeVarInt(value.slot),
            buffer -> new ActivateRaceAbilityRequest(buffer.readVarInt())
    );

    @Override
    public Type<ActivateRaceAbilityRequest> type() {
        return TYPE;
    }
}
