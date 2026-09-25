package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.client.model.DemonEyeModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.entity.mob.ServantOfCthulhuEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Servant of Cthulhu (small eye minion).
 */
public class ServantOfCthulhuRenderer extends MobRenderer<ServantOfCthulhuEntity, DemonEyeModel<ServantOfCthulhuEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/eye/servant_of_cthulhu.png");

    public ServantOfCthulhuRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonEyeModel<>(context.bakeLayer(ModModelLayers.DEMON_EYE)), 0.25F);
    }

    @Override
    protected void scale(ServantOfCthulhuEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ServantOfCthulhuEntity entity) {
        return TEXTURE;
    }
}
