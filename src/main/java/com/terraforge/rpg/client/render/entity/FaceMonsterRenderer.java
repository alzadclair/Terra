package com.terraforge.rpg.client.render.entity;

import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.client.model.FaceMonsterModel;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.entity.mob.FaceMonsterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the 3D Face Monster Crimson mob.
 */
public class FaceMonsterRenderer extends MobRenderer<FaceMonsterEntity, FaceMonsterModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TerraForgeRPG.MOD_ID, "textures/entity/mob/face_monster.png");

    public FaceMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new FaceMonsterModel(context.bakeLayer(ModModelLayers.FACE_MONSTER)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(FaceMonsterEntity entity) {
        return TEXTURE;
    }
}
