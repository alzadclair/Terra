package com.terraforge.rpg.client.render.entity;

import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.entity.mob.TerraZombieEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Terraria Zombies.
 */
public class TerraZombieRenderer extends HumanoidMobRenderer<TerraZombieEntity, HumanoidModel<TerraZombieEntity>> {
    private static final ResourceLocation ZOMBIE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/zombie/terra_zombie.png");

    public TerraZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TerraZombieEntity entity) {
        return ZOMBIE_TEXTURE;
    }
}
