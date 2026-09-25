package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Base class for Terraria magic weapons that consume mana and cast spells.
 */
public class TerrariaMagicStaffItem extends Item implements ITerrariaWeapon {

    private final TerrariaRarity baseRarity;
    private final long baseValue;
    private final double baseDamage;
    private final double baseCritChance;
    private final double knockback;
    private final int useTime;
    private final double manaCost;

    public TerrariaMagicStaffItem(
            Properties properties,
            TerrariaRarity baseRarity,
            long baseValue,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime,
            double manaCost
    ) {
        super(properties);
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.knockback = knockback;
        this.useTime = useTime;
        this.manaCost = manaCost;
    }

    @Override
    public DamageClass getDamageClass() {
        return DamageClass.MAGIC;
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
        return manaCost;
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
        return TerrariaPrefixCategory.MAGIC;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerRPGData data = serverPlayer.getData(ModAttachments.PLAYER_RPG_DATA);
            double effectiveMana = getEffectiveManaCost(stack);

            if (!ManaService.consumeMana(data, effectiveMana)) {
                // Out of mana feedback
                serverPlayer.displayClientMessage(
                        Component.literal("Not enough mana!").withStyle(net.minecraft.ChatFormatting.AQUA),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Audio feedback
            level.playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                    1.0f, 1.2f
            );

            // Visual cast spark
            if (level instanceof ServerLevel serverLevel) {
                Vec3 look = player.getLookAngle();
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANTED_HIT,
                        player.getX() + look.x * 1.5,
                        player.getY() + 1.2 + look.y * 1.5,
                        player.getZ() + look.z * 1.5,
                        10,
                        0.2, 0.2, 0.2, 0.05
                );
            }

            player.getCooldowns().addCooldown(this, getEffectiveUseTime(stack));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
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
