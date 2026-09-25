package com.terraforge.rpg.item;

import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.registry.ModSoundEvents;
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
 * Consumable Life Crystal permanently increasing maximum health up to 400 HP (15 crystals).
 */
public final class LifeCrystalItem extends Item {

    public LifeCrystalItem() {
        super(new Item.Properties().stacksTo(99).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerRPGData data = serverPlayer.getData(ModAttachments.PLAYER_RPG_DATA);

            if (data.getLifeCrystalsUsed() >= 15) {
                serverPlayer.sendSystemMessage(Component.literal("§cVocê já atingiu o limite de Cristais de Vida (15/15)!"));
                return InteractionResultHolder.fail(stack);
            }

            data.setLifeCrystalsUsed(data.getLifeCrystalsUsed() + 1);
            ManaService.updateMaxHealth(serverPlayer, data);
            serverPlayer.heal(2.0f);

            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            serverPlayer.sendSystemMessage(Component.literal("§a+20 de Vida Máxima permanente! (" + data.getLifeCrystalsUsed() + "/15)"));

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§eConsumível"));
        tooltipComponents.add(Component.literal("§7Aumenta permanentemente a vida máxima em +20 HP (1 Coração)."));
        tooltipComponents.add(Component.literal("§8Máximo de 15 cristais por jogador."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
