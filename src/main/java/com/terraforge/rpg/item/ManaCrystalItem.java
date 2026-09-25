package com.terraforge.rpg.item;

import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
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
 * Consumable Mana Crystal permanently increasing maximum Mana by +20 up to 200 (9 crystals).
 */
public final class ManaCrystalItem extends Item {

    public ManaCrystalItem() {
        super(new Item.Properties().stacksTo(99).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerRPGData data = serverPlayer.getData(ModAttachments.PLAYER_RPG_DATA);

            if (data.getManaCrystalsUsed() >= 9) {
                serverPlayer.sendSystemMessage(Component.literal("§cVocê já atingiu o limite de Cristais de Mana (9/9)!"));
                return InteractionResultHolder.fail(stack);
            }

            data.setManaCrystalsUsed(data.getManaCrystalsUsed() + 1);
            data.setCurrentMana(data.getMaxMana());

            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f);
            serverPlayer.sendSystemMessage(Component.literal("§b+20 de Mana Máxima permanente! (" + (int) data.getMaxMana() + " Mana Total)"));

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§bConsumível"));
        tooltipComponents.add(Component.literal("§7Aumenta permanentemente a mana máxima em +20."));
        tooltipComponents.add(Component.literal("§8Máximo de 9 cristais por jogador (200 Mana)."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
