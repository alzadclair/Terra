package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.endgame.GolemEntity;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.client.model.TitanBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Golem boss (ancient temple guardian).
 */
public class BossGolemRenderer extends MobRenderer<GolemEntity, TitanBossModel<GolemEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/boss/golem.png");

    public BossGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new TitanBossModel<>(context.bakeLayer(ModModelLayers.TITAN_BOSS)), 1.4F);
    }

    @Override
    protected void scale(GolemEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(GolemEntity entity) {
        return TEXTURE;
    }
}
