package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.KingSlimeEntity;
import com.terraforge.rpg.client.model.KingSlimeModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for King Slime boss using Blockbench model with ninja core and golden jewel crown.
 */
public class KingSlimeRenderer extends MobRenderer<KingSlimeEntity, KingSlimeModel<KingSlimeEntity>> {
    private static final ResourceLocation KING_SLIME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/slime/king_slime.png");

    public KingSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new KingSlimeModel<>(context.bakeLayer(ModModelLayers.KING_SLIME)), 1.5F);
    }

    @Override
    protected void scale(KingSlimeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(3.2F, 3.2F, 3.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(KingSlimeEntity entity) {
        return KING_SLIME_TEXTURE;
    }
}
