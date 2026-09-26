package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative mode tabs for TerraForge RPG.
 */
public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TerraForgeRPG.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_MODE_TABS.register("terraforge_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.terraforge_rpg"))
                    .icon(() -> new ItemStack(ModItems.PHOENIX_WINGS.get()))
                    .displayItems((parameters, output) -> {
                        // Currency
                        output.accept(ModItems.COPPER_COIN.get());
                        output.accept(ModItems.SILVER_COIN.get());
                        output.accept(ModItems.GOLD_COIN.get());
                        output.accept(ModItems.PLATINUM_COIN.get());

                        // Starter Weapons
                        output.accept(ModItems.COPPER_SHORTSWORD.get());
                        output.accept(ModItems.WOODEN_BOW.get());
                        output.accept(ModItems.WAND_OF_SPARKING.get());
                        output.accept(ModItems.SLIME_STAFF.get());
                        output.accept(ModItems.WOODEN_YOYO.get());
                        output.accept(ModItems.LEATHER_WHIP.get());

                        // Ammunition
                        output.accept(ModItems.WOODEN_ARROW.get());
                        output.accept(ModItems.FLAMING_ARROW.get());
                        output.accept(ModItems.JESTER_ARROW.get());
                        output.accept(ModItems.UNHOLY_ARROW.get());
                        output.accept(ModItems.MUSKET_BALL.get());
                        output.accept(ModItems.METEOR_SHOT.get());

                        // Boss Summoning Items
                        output.accept(ModItems.SUSPICIOUS_LOOKING_EYE.get());
                        output.accept(ModItems.SLIME_CROWN.get());
                        output.accept(ModItems.GUIDE_VOODOO_DOLL.get());
                        output.accept(ModItems.MECHANICAL_EYE.get());
                        output.accept(ModItems.MECHANICAL_WORM.get());
                        output.accept(ModItems.MECHANICAL_SKULL.get());
                        output.accept(ModItems.CELESTIAL_SIGIL.get());
                        output.accept(ModItems.LIHZAHRD_POWER_CELL.get());

                        // Hardmode Materials & Souls
                        output.accept(ModItems.HALLOWED_BAR.get());
                        output.accept(ModItems.SOUL_OF_SIGHT.get());
                        output.accept(ModItems.SOUL_OF_MIGHT.get());
                        output.accept(ModItems.SOUL_OF_FRIGHT.get());
                        output.accept(ModItems.BEETLE_HUSK.get());
                        output.accept(ModItems.LUMINITE_BAR.get());

                        // Hardmode Weapons & Tools
                        output.accept(ModItems.PWNHAMMER.get());

                        // Wall of Flesh Class Emblems
                        output.accept(ModItems.WARRIOR_EMBLEM.get());
                        output.accept(ModItems.RANGER_EMBLEM.get());
                        output.accept(ModItems.SORCERER_EMBLEM.get());
                        output.accept(ModItems.SUMMONER_EMBLEM.get());

                        // Health and Mana Progression
                        output.accept(ModItems.LIFE_CRYSTAL.get());
                        output.accept(ModItems.LIFE_FRUIT.get());
                        output.accept(ModItems.MANA_CRYSTAL.get());
                        output.accept(ModItems.LESSER_MANA_POTION.get());
                        output.accept(ModItems.MANA_POTION.get());

                        // Special Accessories
                        output.accept(ModItems.PHOENIX_WINGS.get());
                        output.accept(ModItems.THUNDER_FRAGMENT.get());
                        output.accept(ModItems.VOID_HEART.get());
                        output.accept(ModItems.TITAN_CORE.get());
                        output.accept(ModItems.ARCANE_PRISM.get());
                        output.accept(ModItems.GUARDIAN_SEAL.get());
                        output.accept(ModItems.BLOOD_CRYSTAL.get());
                        output.accept(ModItems.TIME_GEAR.get());
                        output.accept(ModItems.PREDATOR_EYE.get());
                        output.accept(ModItems.GRAVITY_SIGIL.get());

                        // Utility Items
                        output.accept(ModItems.MAGIC_MIRROR.get());
                        output.accept(ModItems.TEMPLE_KEY.get());
                        output.accept(ModItems.PORTAL_GUN.get());

                        // Fishing Gear & Crates
                        output.accept(ModItems.WOOD_FISHING_POLE.get());
                        output.accept(ModItems.REINFORCED_FISHING_POLE.get());
                        output.accept(ModItems.GOLDEN_FISHING_ROD.get());
                        output.accept(ModItems.MONARCH_BUTTERFLY.get());
                        output.accept(ModItems.WORM.get());
                        output.accept(ModItems.ENCHANTED_NIGHTCRAWLER.get());
                        output.accept(ModItems.MASTER_BAIT.get());
                        output.accept(ModItems.TRUFFLE_WORM.get());
                        output.accept(ModItems.WOODEN_CRATE.get());
                        output.accept(ModItems.IRON_CRATE.get());
                        output.accept(ModItems.GOLDEN_CRATE.get());

                        // Grappling Hooks & Mounts
                        output.accept(ModItems.GRAPPLING_HOOK.get());
                        output.accept(ModItems.IVY_WHIP.get());
                        output.accept(ModItems.SLIMY_SADDLE.get());

                        // Terraria Armor Sets
                        output.accept(ModItems.COPPER_HELMET.get());
                        output.accept(ModItems.COPPER_CHESTPLATE.get());
                        output.accept(ModItems.COPPER_LEGGINGS.get());
                        output.accept(ModItems.COPPER_BOOTS.get());

                        output.accept(ModItems.SHADOW_HELMET.get());
                        output.accept(ModItems.SHADOW_SCALEMAIL.get());
                        output.accept(ModItems.SHADOW_GREAVES.get());
                        output.accept(ModItems.SHADOW_BOOTS.get());

                        output.accept(ModItems.CRIMSON_HELMET.get());
                        output.accept(ModItems.CRIMSON_SCALEMAIL.get());
                        output.accept(ModItems.CRIMSON_GREAVES.get());
                        output.accept(ModItems.CRIMSON_BOOTS.get());

                        output.accept(ModItems.MOLTEN_HELMET.get());
                        output.accept(ModItems.MOLTEN_BREASTPLATE.get());
                        output.accept(ModItems.MOLTEN_GREAVES.get());
                        output.accept(ModItems.MOLTEN_BOOTS.get());

                        // Expanded Arsenal: Weapons & Accessories
                        output.accept(ModItems.NIGHTS_EDGE.get());
                        output.accept(ModItems.TRUE_NIGHTS_EDGE.get());
                        output.accept(ModItems.EXCALIBUR.get());
                        output.accept(ModItems.TERRA_BLADE.get());
                        output.accept(ModItems.STARFURY.get());
                        output.accept(ModItems.SEEDLER.get());
                        output.accept(ModItems.MEOWMERE.get());
                        output.accept(ModItems.ZENITH.get());
                        output.accept(ModItems.BOOMSTICK.get());
                        output.accept(ModItems.PHOENIX_BLASTER.get());
                        output.accept(ModItems.MINISHARK.get());
                        output.accept(ModItems.MEGASHARK.get());
                        output.accept(ModItems.UZI.get());
                        output.accept(ModItems.VORTEX_BEATER.get());
                        output.accept(ModItems.CELEBRATION_MK2.get());
                        output.accept(ModItems.WATER_BOLT.get());
                        output.accept(ModItems.SPACE_GUN.get());
                        output.accept(ModItems.DIAMOND_STAFF.get());
                        output.accept(ModItems.HERMES_BOOTS.get());
                        output.accept(ModItems.BAND_OF_REGENERATION.get());
                        output.accept(ModItems.TERRASPARK_BOOTS.get());
                        output.accept(ModItems.TSUNAMI.get());

                        // Crafting Stations & Blocks
                        output.accept(ModItems.WORK_BENCH_ITEM.get());
                        output.accept(ModItems.COPPER_ORE_ITEM.get());
                        output.accept(ModItems.TIN_ORE_ITEM.get());
                        output.accept(ModItems.ASH_BLOCK_ITEM.get());
                        output.accept(ModItems.HELLSTONE_ORE_ITEM.get());
                        output.accept(ModItems.EBONSTONE_BLOCK_ITEM.get());
                        output.accept(ModItems.CRIMSTONE_BLOCK_ITEM.get());
                        output.accept(ModItems.DEMON_ALTAR_ITEM.get());
                        output.accept(ModItems.CRIMSON_ALTAR_ITEM.get());
                        output.accept(ModItems.COBALT_ORE_ITEM.get());
                        output.accept(ModItems.PALLADIUM_ORE_ITEM.get());
                        output.accept(ModItems.MYTHRIL_ORE_ITEM.get());
                        output.accept(ModItems.ORICHALCUM_ORE_ITEM.get());
                        output.accept(ModItems.ADAMANTITE_ORE_ITEM.get());
                        output.accept(ModItems.TITANIUM_ORE_ITEM.get());
                        output.accept(ModItems.LUMINITE_ORE_ITEM.get());
                    })
                    .build());

    private ModCreativeModeTabs() {}
}
