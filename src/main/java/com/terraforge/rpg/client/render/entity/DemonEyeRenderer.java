package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.client.model.DemonEyeModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.entity.mob.DemonEyeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Demon Eye flying monsters.
 */
public class DemonEyeRenderer extends MobRenderer<DemonEyeEntity, DemonEyeModel<DemonEyeEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/eye/demon_eye.png");

    public DemonEyeRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonEyeModel<>(context.bakeLayer(ModModelLayers.DEMON_EYE)), 0.4F);
    }

    @Override
    protected void scale(DemonEyeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    public ResourceLocation getTextureLocation(DemonEyeEntity entity) {
        return TEXTURE;
    }
}
