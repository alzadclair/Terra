package com.terraforge.rpg.item;

import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Consumable Life Fruit permanently increasing maximum health up to 500 HP (20 fruits).
 * Requires Hardmode and maximum Life Crystals (15/15).
 */
public final class LifeFruitItem extends Item {

    public LifeFruitItem() {
        super(new Item.Properties().stacksTo(99).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerRPGData data = serverPlayer.getData(ModAttachments.PLAYER_RPG_DATA);
            WorldProgressionData worldData = WorldProgressionData.get(serverPlayer.server);

            if (!worldData.isHardmode()) {
                serverPlayer.sendSystemMessage(Component.literal("§cA Fruta da Vida só pode ser consumida no Hardmode!"));
                return InteractionResultHolder.fail(stack);
            }

            if (data.getLifeCrystalsUsed() < 15) {
                serverPlayer.sendSystemMessage(Component.literal("§cVocê precisa atingir o limite de Cristais de Vida (15/15) primeiro!"));
                return InteractionResultHolder.fail(stack);
            }

            if (data.getLifeFruitsUsed() >= 20) {
                serverPlayer.sendSystemMessage(Component.literal("§cVocê já atingiu o limite de Frutas da Vida (20/20)!"));
                return InteractionResultHolder.fail(stack);
            }

            data.setLifeFruitsUsed(data.getLifeFruitsUsed() + 1);
            ManaService.updateMaxHealth(serverPlayer, data);
            serverPlayer.heal(1.0f);

            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.4f);
            serverPlayer.sendSystemMessage(Component.literal("§6+5 de Vida Dourada permanente! (" + data.getLifeFruitsUsed() + "/20)"));

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6Consumível (Hardmode)"));
        tooltipComponents.add(Component.literal("§7Aumenta permanentemente a vida máxima em +5 HP dourados."));
        tooltipComponents.add(Component.literal("§8Requer 15 Cristais de Vida. Máximo de 20 frutas."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
