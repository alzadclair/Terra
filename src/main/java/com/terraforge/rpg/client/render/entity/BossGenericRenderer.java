package com.terraforge.rpg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Robust scalable renderer for large bosses and specialized entities.
 */
public class BossGenericRenderer<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {
    private final ResourceLocation texture;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;

    public BossGenericRenderer(EntityRendererProvider.Context context, M model, float shadowRadius, String texturePath, float scaleX, float scaleY, float scaleZ) {
        super(context, model, shadowRadius);
        this.texture = ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, texturePath);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public BossGenericRenderer(EntityRendererProvider.Context context, M model, float shadowRadius, String texturePath, float uniformScale) {
        this(context, model, shadowRadius, texturePath, uniformScale, uniformScale, uniformScale);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }
}
