package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base class for Terraria ranged bows.
 */
public class TerrariaBowItem extends BowItem implements ITerrariaWeapon {

    private final TerrariaRarity baseRarity;
    private final long baseValue;
    private final double baseDamage;
    private final double baseCritChance;
    private final double knockback;
    private final int useTime;

    public TerrariaBowItem(
            Properties properties,
            TerrariaRarity baseRarity,
            long baseValue,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime
    ) {
        super(properties);
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.knockback = knockback;
        this.useTime = useTime;
    }

    @Override
    public DamageClass getDamageClass() {
        return DamageClass.RANGED;
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
        return TerrariaPrefixCategory.RANGED;
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
    }
}
