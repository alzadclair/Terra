package com.terraforge.rpg.client.render.entity;

import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for TerraForge projectiles.
 */
public class TerraProjectileRenderer extends EntityRenderer<TerraProjectileEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/projectile/projectile.png");

    public TerraProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TerraProjectileEntity entity) {
        return TEXTURE;
    }
}
