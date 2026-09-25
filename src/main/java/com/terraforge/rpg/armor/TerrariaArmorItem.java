package com.terraforge.rpg.armor;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base item class for all Terraria armor pieces.
 */
public class TerrariaArmorItem extends ArmorItem implements ITerrariaArmor {

    private final String setId;
    private final int terrariaDefense;
    private final DamageClass armorClass;
    private final TerrariaRarity baseRarity;
    private final long baseValue;

    public TerrariaArmorItem(
            Holder<ArmorMaterial> material,
            Type type,
            Properties properties,
            String setId,
            int terrariaDefense,
            DamageClass armorClass,
            TerrariaRarity baseRarity,
            long baseValue
    ) {
        super(material, type, properties);
        this.setId = setId;
        this.terrariaDefense = terrariaDefense;
        this.armorClass = armorClass;
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
    }

    @Override
    public String getSetId() {
        return setId;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.getType().getSlot();
    }

    @Override
    public int getTerrariaDefense() {
        return terrariaDefense;
    }

    @Override
    public DamageClass getArmorClass() {
        return armorClass;
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
        return TerrariaPrefixCategory.ACCESSORY; // Defense/utility prefixes
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
        tooltipComponents.add(Component.literal(terrariaDefense + " defense").withStyle(ChatFormatting.BLUE));
        if (armorClass != DamageClass.GENERIC) {
            tooltipComponents.add(Component.literal(armorClass.getDisplayName() + " class piece").withStyle(ChatFormatting.GRAY));
        }
    }
}
