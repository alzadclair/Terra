package com.terraforge.rpg.client.render.entity;

import com.terraforge.rpg.client.model.DemonEyeModel;
import com.terraforge.rpg.client.model.DukeFishronModel;
import com.terraforge.rpg.client.model.EyeOfCthulhuModel;
import com.terraforge.rpg.client.model.KingSlimeModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.client.model.MoonLordModel;
import com.terraforge.rpg.client.model.PlanteraModel;
import com.terraforge.rpg.client.model.SkeletronPrimeModel;
import com.terraforge.rpg.client.model.TheDestroyerModel;
import com.terraforge.rpg.client.model.TitanBossModel;
import com.terraforge.rpg.client.model.WallOfFleshModel;
import com.terraforge.rpg.registry.ModEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client registration for all TerraForge RPG entity renderers and model layer definitions.
 * Features dedicated Blockbench boss models for King Slime, Eye of Cthulhu, Skeletron Prime,
 * Plantera, Duke Fishron, Golem, and Moon Lord.
 */
public final class ModEntityRenderers {

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.DEMON_EYE, DemonEyeModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.EYE_OF_CTHULHU, EyeOfCthulhuModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.KING_SLIME, KingSlimeModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SKELETRON_PRIME, SkeletronPrimeModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.PLANTERA, PlanteraModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.DUKE_FISHRON, DukeFishronModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TITAN_BOSS, TitanBossModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.WALL_OF_FLESH, WallOfFleshModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.THE_DESTROYER, TheDestroyerModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MOON_LORD, MoonLordModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.FACE_MONSTER, com.terraforge.rpg.client.model.FaceMonsterModel::createBodyLayer);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Projectiles & Tethered Entities
        event.registerEntityRenderer(ModEntities.TERRA_PROJECTILE.get(), TerraProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.YOYO.get(), YoyoRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAPPLING_HOOK.get(), GrapplingHookRenderer::new);

        // Pre-Hardmode Mobs
        event.registerEntityRenderer(ModEntities.GREEN_SLIME.get(), TerraSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.BLUE_SLIME.get(), TerraSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.DEMON_EYE.get(), DemonEyeRenderer::new);
        event.registerEntityRenderer(ModEntities.TERRA_ZOMBIE.get(), TerraZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.FACE_MONSTER.get(), FaceMonsterRenderer::new);
        event.registerEntityRenderer(ModEntities.SERVANT_OF_CTHULHU.get(), ServantOfCthulhuRenderer::new);

        // Pre-Hardmode Bosses
        event.registerEntityRenderer(ModEntities.KING_SLIME.get(), KingSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.EYE_OF_CTHULHU.get(), EyeOfCthulhuRenderer::new);
        event.registerEntityRenderer(ModEntities.WALL_OF_FLESH.get(),
                ctx -> new BossGenericRenderer<>(ctx, new WallOfFleshModel(ctx.bakeLayer(ModModelLayers.WALL_OF_FLESH)), 3.0F, "textures/entity/boss/wall_of_flesh.png", 3.5F, 6.0F, 3.5F));
        event.registerEntityRenderer(ModEntities.THE_HUNGRY.get(),
                ctx -> new BossGenericRenderer<>(ctx, new DemonEyeModel<>(ctx.bakeLayer(ModModelLayers.DEMON_EYE)), 0.5F, "textures/entity/boss/the_hungry.png", 0.9F));

        // Goblin Army
        event.registerEntityRenderer(ModEntities.GOBLIN_PEON.get(), ctx -> new GoblinRenderer<>(ctx, "goblin_peon"));
        event.registerEntityRenderer(ModEntities.GOBLIN_THIEF.get(), ctx -> new GoblinRenderer<>(ctx, "goblin_thief"));
        event.registerEntityRenderer(ModEntities.GOBLIN_WARRIOR.get(), ctx -> new GoblinRenderer<>(ctx, "goblin_warrior"));
        event.registerEntityRenderer(ModEntities.GOBLIN_SORCERER.get(), ctx -> new GoblinRenderer<>(ctx, "goblin_sorcerer"));

        // Town NPCs
        event.registerEntityRenderer(ModEntities.GUIDE.get(), ctx -> new TownNpcRenderer<>(ctx, "guide"));
        event.registerEntityRenderer(ModEntities.MERCHANT.get(), ctx -> new TownNpcRenderer<>(ctx, "merchant"));
        event.registerEntityRenderer(ModEntities.NURSE.get(), ctx -> new TownNpcRenderer<>(ctx, "nurse"));
        event.registerEntityRenderer(ModEntities.GOBLIN_TINKERER.get(), ctx -> new TownNpcRenderer<>(ctx, "goblin_tinkerer"));

        // Mounts
        event.registerEntityRenderer(ModEntities.SLIME_MOUNT.get(), SlimeMountRenderer::new);

        // Hardmode Mechanical Bosses
        event.registerEntityRenderer(ModEntities.RETINAZER.get(), TwinBossRenderer::retinazer);
        event.registerEntityRenderer(ModEntities.SPAZMATISM.get(), TwinBossRenderer::spazmatism);
        event.registerEntityRenderer(ModEntities.DESTROYER_PROBE.get(),
                ctx -> new BossGenericRenderer<>(ctx, new DemonEyeModel<>(ctx.bakeLayer(ModModelLayers.DEMON_EYE)), 0.5F, "textures/entity/boss/destroyer_probe.png", 0.8F));
        event.registerEntityRenderer(ModEntities.THE_DESTROYER.get(),
                ctx -> new BossGenericRenderer<>(ctx, new TheDestroyerModel(ctx.bakeLayer(ModModelLayers.THE_DESTROYER)), 3.0F, "textures/entity/boss/the_destroyer.png", 3.0F));
        event.registerEntityRenderer(ModEntities.SKELETRON_PRIME.get(),
                ctx -> new BossGenericRenderer<>(ctx, new SkeletronPrimeModel<>(ctx.bakeLayer(ModModelLayers.SKELETRON_PRIME)), 2.5F, "textures/entity/boss/skeletron_prime.png", 2.2F));

        // Hardmode Endgame Bosses
        event.registerEntityRenderer(ModEntities.PLANTERA.get(),
                ctx -> new BossGenericRenderer<>(ctx, new PlanteraModel<>(ctx.bakeLayer(ModModelLayers.PLANTERA)), 2.5F, "textures/entity/boss/plantera.png", 2.5F));
        event.registerEntityRenderer(ModEntities.GOLEM.get(), BossGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.DUKE_FISHRON.get(),
                ctx -> new BossGenericRenderer<>(ctx, new DukeFishronModel<>(ctx.bakeLayer(ModModelLayers.DUKE_FISHRON)), 2.5F, "textures/entity/boss/duke_fishron.png", 2.2F));
        event.registerEntityRenderer(ModEntities.MOON_LORD.get(),
                ctx -> new BossGenericRenderer<>(ctx, new MoonLordModel(ctx.bakeLayer(ModModelLayers.MOON_LORD)), 4.0F, "textures/entity/boss/moon_lord.png", 4.0F));
    }

    private ModEntityRenderers() {}
}
