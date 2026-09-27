package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.client.model.EyeOfCthulhuModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.client.render.entity.state.EyeRenderState;
import com.terraforge.rpg.client.render.entity.state.EyeRenderStateManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Eye of Cthulhu boss using authentic 3D skeletal mesh with dynamic teeth jaws and optic tendrils.
 * Obtains per-entity EyeRenderState from EyeRenderStateManager and passes it to the stateless model.
 */
public class EyeOfCthulhuRenderer extends MobRenderer<EyeOfCthulhuEntity, EyeOfCthulhuModel> {
    private static final ResourceLocation TEXTURE_P1 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/boss/eye_of_cthulhu_p1.png");
    private static final ResourceLocation TEXTURE_P2 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/boss/eye_of_cthulhu_p2.png");

    public EyeOfCthulhuRenderer(EntityRendererProvider.Context context) {
        super(context, new EyeOfCthulhuModel(context.bakeLayer(ModModelLayers.EYE_OF_CTHULHU)), 1.5F);
    }

    @Override
    public void render(EyeOfCthulhuEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        EyeRenderState state = EyeRenderStateManager.getOrCreate(entity);
        this.model.setRenderState(state);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(EyeOfCthulhuEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(2.8F, 2.8F, 2.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(EyeOfCthulhuEntity entity) {
        return entity.isRenderPhase2() ? TEXTURE_P2 : TEXTURE_P1;
    }
}
