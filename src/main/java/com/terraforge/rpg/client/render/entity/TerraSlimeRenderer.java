package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.entity.mob.TerraSlimeEntity;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Green and Blue Terraria Slimes.
 */
public class TerraSlimeRenderer extends MobRenderer<TerraSlimeEntity, SlimeModel<TerraSlimeEntity>> {
    private static final ResourceLocation GREEN_SLIME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/slime/green_slime.png");
    private static final ResourceLocation BLUE_SLIME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/slime/blue_slime.png");

    public TerraSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.3F);
    }

    @Override
    protected void scale(TerraSlimeEntity slime, PoseStack poseStack, float partialTick) {
        float s = slime.getVariant() == TerraSlimeEntity.SlimeVariant.GREEN ? 0.85F : 1.1F;
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(TerraSlimeEntity entity) {
        return entity.getVariant() == TerraSlimeEntity.SlimeVariant.GREEN ? GREEN_SLIME_TEXTURE : BLUE_SLIME_TEXTURE;
    }
}
