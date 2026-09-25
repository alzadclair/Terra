package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.entity.mount.SlimeMountEntity;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Slime Mount.
 */
public class SlimeMountRenderer extends MobRenderer<SlimeMountEntity, SlimeModel<SlimeMountEntity>> {
    private static final ResourceLocation SLIME_MOUNT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/slime/slime_mount.png");

    public SlimeMountRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.5F);
    }

    @Override
    protected void scale(SlimeMountEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeMountEntity entity) {
        return SLIME_MOUNT_TEXTURE;
    }
}
