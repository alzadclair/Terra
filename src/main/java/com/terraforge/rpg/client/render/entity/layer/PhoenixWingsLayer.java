package com.terraforge.rpg.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.client.model.ModModelLayers;
import com.terraforge.rpg.client.model.PhoenixWingsModel;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 3D Wearable render layer displaying Phoenix Wings on the player's back
 * whenever equipped in the Special Accessory slot.
 * Uses dedicated PhoenixWingsModel with parented torso anchoring to preserve
 * anatomically correct positioning during standing, crouching, running, gliding, and swimming.
 */
public class PhoenixWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/phoenix_wings.png");

    private final PhoenixWingsModel wingsModel;

    public PhoenixWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
        super(parent);
        this.wingsModel = new PhoenixWingsModel(modelSet.bakeLayer(ModModelLayers.PHOENIX_WINGS));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        boolean hasWings = false;
        try {
            PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
            if (data != null && "phoenix_wings".equalsIgnoreCase(data.getEquippedSpecialAccessory())) {
                hasWings = true;
            }
        } catch (Exception ignored) {}

        // Fallback for creative mode inspection
        if (!hasWings) {
            hasWings = player.getMainHandItem().is(ModItems.PHOENIX_WINGS.get()) ||
                       player.getOffhandItem().is(ModItems.PHOENIX_WINGS.get());
        }

        if (!hasWings) {
            return;
        }

        poseStack.pushPose();

        // Anchor wings to the player model torso so they naturally track crouching, riding, swimming, and running
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0, 0.0, 0.05);

        this.wingsModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.wingsModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }
}
