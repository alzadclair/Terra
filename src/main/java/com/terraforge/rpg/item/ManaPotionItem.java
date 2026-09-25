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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Consumable Mana Potion restoring a fixed amount of mana.
 */
public final class ManaPotionItem extends Item {
    private final double restoreAmount;

    public ManaPotionItem(double restoreAmount) {
        super(new Item.Properties().stacksTo(30).rarity(Rarity.COMMON));
        this.restoreAmount = restoreAmount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerRPGData data = serverPlayer.getData(ModAttachments.PLAYER_RPG_DATA);

            if (data.getCurrentMana() >= data.getMaxMana()) {
                serverPlayer.sendSystemMessage(Component.literal("§bSua Mana já está cheia!"));
                return InteractionResultHolder.fail(stack);
            }

            data.setCurrentMana(data.getCurrentMana() + restoreAmount);
            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
                serverPlayer.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§bPoção"));
        tooltipComponents.add(Component.literal(String.format("§7Restaura %.0f de mana instantaneamente.", restoreAmount)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
