package com.terraforge.rpg.client;

import com.terraforge.rpg.client.hud.TerraHudOverlay;
import com.terraforge.rpg.client.key.ModKeyMappings;
import com.terraforge.rpg.client.render.entity.ModEntityRenderers;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client entry point and initialization for TerraForge RPG.
 * Guarded against loading in dedicated server environments.
 */
public final class TerraForgeClient {

    public static void init(IEventBus modBus) {
        modBus.addListener(TerraForgeClient::onClientSetup);
        modBus.addListener(ModKeyMappings::onRegisterKeyMappings);
        modBus.addListener(TerraHudOverlay::register);
        modBus.addListener(ModEntityRenderers::registerLayerDefinitions);
        modBus.addListener(ModEntityRenderers::registerEntityRenderers);
        modBus.addListener(ModEntityRenderers::addLayers);
        modBus.addListener(TerraForgeClient::onRegisterReloadListeners);

        // Register client game bus listeners
        NeoForge.EVENT_BUS.addListener(ClientInputHandler::onClientTick);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TerraLogger.info("CLIENT", "TerraForge RPG client initialized successfully.");
        });
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) (ResourceManager resourceManager) -> {
            TerraMeshLoader.clearCache();
            com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader.preload(resourceManager);
            TerraLogger.info("CLIENT", "Reloaded and preloaded 3D skinned mesh caches on resource reload (F3+T).");
        });
    }

    private TerraForgeClient() {}
}
