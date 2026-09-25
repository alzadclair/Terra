package com.terraforge.rpg.item.weapon.yoyo;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.YoyoEntity;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Base class for all Terraria Yoyos.
 */
public class TerrariaYoyoItem extends Item implements ITerrariaYoyo {

    private final TerrariaRarity baseRarity;
    private final long baseValue;
    private final double baseDamage;
    private final double baseCritChance;
    private final double knockback;
    private final int useTime;
    private final double reach;
    private final int flightTime;
    private final int stringColor;

    public TerrariaYoyoItem(
            Properties properties,
            TerrariaRarity baseRarity,
            long baseValue,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime,
            double reach,
            int flightTime,
            int stringColor
    ) {
        super(properties);
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.knockback = knockback;
        this.useTime = useTime;
        this.reach = reach;
        this.flightTime = flightTime;
        this.stringColor = stringColor;
    }

    @Override
    public DamageClass getDamageClass() {
        return DamageClass.MELEE;
    }

    @Override
    public double getTerrariaBaseDamage() {
        return baseDamage;
    }

    @Override
    public double getBaseCritChance() {
        return baseCritChance;
    }

    @Override
    public double getKnockback() {
        return knockback;
    }

    @Override
    public int getUseTime() {
        return useTime;
    }

    @Override
    public double getManaCost() {
        return 0.0;
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return baseRarity;
    }

    @Override
    public long getBaseValue() {
        return baseValue;
    }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.MELEE;
    }

    @Override
    public double getReach() {
        return reach;
    }

    @Override
    public int getFlightTime() {
        return flightTime;
    }

    @Override
    public int getStringColor() {
        return stringColor;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            double effectiveDmg = getEffectiveDamage(stack);
            double effectiveCrit = getEffectiveCritChance(stack);
            double effectiveKb = getEffectiveKnockback(stack);

            YoyoEntity yoyo = new YoyoEntity(level, player, reach, flightTime, effectiveDmg, effectiveCrit, effectiveKb);
            level.addFreshEntity(yoyo);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        TerrariaPrefix prefix = getPrefix(stack);
        MutableComponent name;
        if (!prefix.isNone()) {
            name = Component.literal(prefix.displayName() + " ").append(super.getName(stack));
        } else {
            name = super.getName(stack).copy();
        }
        return name.withStyle(style -> style.withColor(getEffectiveRarity(stack).getTextColor()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        TerrariaTooltipHelper.buildTooltip(stack, this, tooltipComponents);

        tooltipComponents.add(Component.literal("Reach: " + (int) reach + " blocks").withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
        if (flightTime > 0) {
            tooltipComponents.add(Component.literal("Air time: " + String.format("%.1f", flightTime / 20.0) + "s").withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
        } else {
            tooltipComponents.add(Component.literal("Air time: Infinite").withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
        }
    }
}
