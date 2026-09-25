package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.hardmode.RetinazerEntity;
import com.terraforge.rpg.boss.hardmode.SpazmatismEntity;
import com.terraforge.rpg.client.model.DemonEyeModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;

/**
 * Renderer for The Twins: Retinazer (red optic laser) and Spazmatism (green cursed flame).
 */
public class TwinBossRenderer<T extends Monster> extends MobRenderer<T, DemonEyeModel<T>> {
    private static final ResourceLocation RETINAZER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/boss/retinazer.png");
    private static final ResourceLocation SPAZMATISM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/boss/spazmatism.png");

    private final boolean isRetinazer;

    public TwinBossRenderer(EntityRendererProvider.Context context, boolean isRetinazer) {
        super(context, new DemonEyeModel<>(context.bakeLayer(ModModelLayers.DEMON_EYE)), 1.2F);
        this.isRetinazer = isRetinazer;
    }

    public static TwinBossRenderer<RetinazerEntity> retinazer(EntityRendererProvider.Context context) {
        return new TwinBossRenderer<>(context, true);
    }

    public static TwinBossRenderer<SpazmatismEntity> spazmatism(EntityRendererProvider.Context context) {
        return new TwinBossRenderer<>(context, false);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(2.8F, 2.8F, 2.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return isRetinazer ? RETINAZER_TEXTURE : SPAZMATISM_TEXTURE;
    }
}
