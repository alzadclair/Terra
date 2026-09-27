package com.terraforge.rpg;

import com.terraforge.rpg.client.TerraForgeClient;
import com.terraforge.rpg.combat.CombatEventHandler;
import com.terraforge.rpg.config.TerraClientConfig;
import com.terraforge.rpg.config.TerraServerConfig;
import com.terraforge.rpg.data.DataGenerators;
import com.terraforge.rpg.entity.EntityDeathHandler;
import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.PlayerLifecycleHandler;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.registry.ModBlocks;
import com.terraforge.rpg.registry.ModCreativeModeTabs;
import com.terraforge.rpg.registry.ModDataComponents;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import com.terraforge.rpg.registry.ModMenus;
import com.terraforge.rpg.registry.ModSoundEvents;
import com.terraforge.rpg.util.TerraLogger;
import com.terraforge.rpg.validation.ContentValidator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Main entry point for TerraForge RPG.
 */
@Mod(TerraForgeRPG.MOD_ID)
public final class TerraForgeRPG {
    public static final String MOD_ID = "terraforge_rpg";

    public TerraForgeRPG(IEventBus modBus, ModContainer container) {
        TerraLogger.info("CORE", "Initializing TerraForge RPG mod core...");

        // Register Registries
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modBus);
        ModSoundEvents.SOUND_EVENTS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);

        // Register Configuration Specs
        container.registerConfig(ModConfig.Type.SERVER, TerraServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, TerraClientConfig.SPEC);

        // Network, Attributes and Lifecycle Events
        modBus.addListener(TerraNetwork::register);
        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(com.terraforge.rpg.world.spawn.ModSpawnPlacements::register);
        modBus.addListener(this::commonSetup);
        modBus.addListener(DataGenerators::gatherData);

        // Register Game Event Listeners
        NeoForge.EVENT_BUS.register(PlayerLifecycleHandler.class);
        NeoForge.EVENT_BUS.register(EntityDeathHandler.class);
        NeoForge.EVENT_BUS.register(CombatEventHandler.class);
        NeoForge.EVENT_BUS.register(com.terraforge.rpg.world.WorldTickHandler.class);
        NeoForge.EVENT_BUS.register(com.terraforge.rpg.command.TerraForgeCommands.class);

        // Client-Specific Initialization
        if (FMLEnvironment.dist.isClient()) {
            TerraForgeClient.init(modBus);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TerraLogger.info("CORE", "Executing common setup for TerraForge RPG.");
            ContentValidator.runSanityCheck();
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
