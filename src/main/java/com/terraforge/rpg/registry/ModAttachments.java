package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.player.data.PlayerRPGData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data Attachment registry for TerraForge RPG.
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TerraForgeRPG.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerRPGData>> PLAYER_RPG_DATA =
            ATTACHMENT_TYPES.register("player_rpg_data", () -> AttachmentType.serializable(PlayerRPGData::new)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {}
}
