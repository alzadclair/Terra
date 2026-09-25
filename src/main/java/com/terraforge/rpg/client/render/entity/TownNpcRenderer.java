package com.terraforge.rpg.client.render.entity;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Renderer for Terraria Town NPCs (Guide, Merchant, Nurse, Goblin Tinkerer).
 */
public class TownNpcRenderer<T extends Mob> extends MobRenderer<T, VillagerModel<T>> {
    private final ResourceLocation texture;

    public TownNpcRenderer(EntityRendererProvider.Context context, String npcName) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.texture = ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "textures/entity/npc/" + npcName + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }
}
