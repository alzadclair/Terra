package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.accessory.special.SpecialAccessoryItem;
import com.terraforge.rpg.item.LifeCrystalItem;
import com.terraforge.rpg.item.LifeFruitItem;
import com.terraforge.rpg.item.ManaCrystalItem;
import com.terraforge.rpg.item.ManaPotionItem;
import com.terraforge.rpg.item.ammo.AmmoType;
import com.terraforge.rpg.item.ammo.TerrariaAmmoItem;
import com.terraforge.rpg.item.boss.SuspiciousLookingEyeItem;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.weapon.TerrariaBowItem;
import com.terraforge.rpg.item.weapon.TerrariaMagicStaffItem;
import com.terraforge.rpg.item.weapon.TerrariaSummonStaffItem;
import com.terraforge.rpg.item.weapon.TerrariaSwordItem;
import com.terraforge.rpg.item.weapon.whip.TerrariaWhipItem;
import com.terraforge.rpg.item.weapon.yoyo.TerrariaYoyoItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry for TerraForge RPG.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TerraForgeRPG.MOD_ID);

    // Terraria Coin Currency
    public static final DeferredItem<Item> COPPER_COIN = ITEMS.registerSimpleItem("copper_coin",
            new Item.Properties().stacksTo(99));

    public static final DeferredItem<Item> SILVER_COIN = ITEMS.registerSimpleItem("silver_coin",
            new Item.Properties().stacksTo(99));

    public static final DeferredItem<Item> GOLD_COIN = ITEMS.registerSimpleItem("gold_coin",
            new Item.Properties().stacksTo(99));

    public static final DeferredItem<Item> PLATINUM_COIN = ITEMS.registerSimpleItem("platinum_coin",
            new Item.Properties().stacksTo(99));

    // Starter Weapons (Terraria 1.4.5.8 Canonical Stats)
    public static final DeferredItem<TerrariaSwordItem> COPPER_SHORTSWORD = ITEMS.register("copper_shortsword",
            () -> new TerrariaSwordItem(new Item.Properties().stacksTo(1),
                    TerrariaRarity.WHITE, 70L, 5.0, 4.0, 4.0, 13));

    public static final DeferredItem<TerrariaBowItem> WOODEN_BOW = ITEMS.register("wooden_bow",
            () -> new TerrariaBowItem(new Item.Properties().stacksTo(1),
                    TerrariaRarity.WHITE, 20L, 4.0, 4.0, 0.0, 30));

    public static final DeferredItem<TerrariaMagicStaffItem> WAND_OF_SPARKING = ITEMS.register("wand_of_sparking",
            () -> new TerrariaMagicStaffItem(new Item.Properties().stacksTo(1),
                    TerrariaRarity.WHITE, 10_000L, 14.0, 14.0, 1.0, 26, 2.0));

    public static final DeferredItem<TerrariaSummonStaffItem> SLIME_STAFF = ITEMS.register("slime_staff",
            () -> new TerrariaSummonStaffItem(new Item.Properties().stacksTo(1),
                    TerrariaRarity.WHITE, 20_000L, 8.0, 4.0, 2.0, 28, 10.0));

    public static final DeferredItem<TerrariaYoyoItem> WOODEN_YOYO = ITEMS.register("wooden_yoyo",
            () -> new TerrariaYoyoItem(new Item.Properties().stacksTo(1),
                    TerrariaRarity.WHITE, 100L, 9.0, 4.0, 3.0, 25, 9.0, 80, 0x8B5A2B));

    public static final DeferredItem<TerrariaWhipItem> LEATHER_WHIP = ITEMS.register("leather_whip",
            () -> new TerrariaWhipItem(new Item.Properties().stacksTo(1),
                    TerrariaRarity.WHITE, 10_000L, 14.0, 4.0, 1.0, 30, 4.5, 4));

    // Ammunition (Arrows and Bullets) - capped at 99 to fit Minecraft 1.21.1 network codec [1; 99]
    public static final DeferredItem<TerrariaAmmoItem> WOODEN_ARROW = ITEMS.register("wooden_arrow",
            () -> new TerrariaAmmoItem(new Item.Properties().stacksTo(99),
                    AmmoType.ARROW, TerrariaRarity.WHITE, 1L, 4.0, 1.0, 2.0, 0, 0));

    public static final DeferredItem<TerrariaAmmoItem> FLAMING_ARROW = ITEMS.register("flaming_arrow",
            () -> new TerrariaAmmoItem(new Item.Properties().stacksTo(99),
                    AmmoType.ARROW, TerrariaRarity.WHITE, 2L, 7.0, 1.0, 2.0, 0, 0));

    public static final DeferredItem<TerrariaAmmoItem> JESTER_ARROW = ITEMS.register("jester_arrow",
            () -> new TerrariaAmmoItem(new Item.Properties().stacksTo(99),
                    AmmoType.ARROW, TerrariaRarity.WHITE, 10L, 9.0, 1.1, 4.0, 999, 0));

    public static final DeferredItem<TerrariaAmmoItem> UNHOLY_ARROW = ITEMS.register("unholy_arrow",
            () -> new TerrariaAmmoItem(new Item.Properties().stacksTo(99),
                    AmmoType.ARROW, TerrariaRarity.BLUE, 8L, 12.0, 1.1, 3.0, 5, 0));

    public static final DeferredItem<TerrariaAmmoItem> MUSKET_BALL = ITEMS.register("musket_ball",
            () -> new TerrariaAmmoItem(new Item.Properties().stacksTo(99),
                    AmmoType.BULLET, TerrariaRarity.WHITE, 7L, 7.0, 1.5, 2.0, 0, 0));

    public static final DeferredItem<TerrariaAmmoItem> METEOR_SHOT = ITEMS.register("meteor_shot",
            () -> new TerrariaAmmoItem(new Item.Properties().stacksTo(99),
                    AmmoType.BULLET, TerrariaRarity.GREEN, 16L, 9.0, 1.5, 2.0, 1, 1));

    // Boss Summoning Items
    public static final DeferredItem<SuspiciousLookingEyeItem> SUSPICIOUS_LOOKING_EYE = ITEMS.register("suspicious_looking_eye",
            () -> new SuspiciousLookingEyeItem(new Item.Properties().stacksTo(20)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.SlimeCrownItem> SLIME_CROWN = ITEMS.register("slime_crown",
            () -> new com.terraforge.rpg.item.boss.SlimeCrownItem(new Item.Properties().stacksTo(20)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.GuideVoodooDollItem> GUIDE_VOODOO_DOLL = ITEMS.register("guide_voodoo_doll",
            () -> new com.terraforge.rpg.item.boss.GuideVoodooDollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.MechanicalEyeItem> MECHANICAL_EYE = ITEMS.register("mechanical_eye",
            () -> new com.terraforge.rpg.item.boss.MechanicalEyeItem(new Item.Properties().stacksTo(20)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.MechanicalWormItem> MECHANICAL_WORM = ITEMS.register("mechanical_worm",
            () -> new com.terraforge.rpg.item.boss.MechanicalWormItem(new Item.Properties().stacksTo(20)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.MechanicalSkullItem> MECHANICAL_SKULL = ITEMS.register("mechanical_skull",
            () -> new com.terraforge.rpg.item.boss.MechanicalSkullItem(new Item.Properties().stacksTo(20)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.CelestialSigilItem> CELESTIAL_SIGIL = ITEMS.register("celestial_sigil",
            () -> new com.terraforge.rpg.item.boss.CelestialSigilItem(new Item.Properties().stacksTo(20)));

    public static final DeferredItem<com.terraforge.rpg.item.boss.LihzahrdPowerCellItem> LIHZAHRD_POWER_CELL = ITEMS.register("lihzahrd_power_cell",
            () -> new com.terraforge.rpg.item.boss.LihzahrdPowerCellItem(new Item.Properties().stacksTo(20)));

    // Hardmode Crafting Materials & Souls
    public static final DeferredItem<com.terraforge.rpg.item.material.TerrariaMaterialItem> HALLOWED_BAR = ITEMS.register("hallowed_bar",
            () -> new com.terraforge.rpg.item.material.TerrariaMaterialItem(new Item.Properties().stacksTo(99), com.terraforge.rpg.item.rarity.TerrariaRarity.PINK, 20_000L, "Forged from souls and sacred metals"));

    public static final DeferredItem<com.terraforge.rpg.item.material.TerrariaMaterialItem> SOUL_OF_SIGHT = ITEMS.register("soul_of_sight",
            () -> new com.terraforge.rpg.item.material.TerrariaMaterialItem(new Item.Properties().stacksTo(99), com.terraforge.rpg.item.rarity.TerrariaRarity.PINK, 10_000L, "The essence of omniscient vision"));

    public static final DeferredItem<com.terraforge.rpg.item.material.TerrariaMaterialItem> SOUL_OF_MIGHT = ITEMS.register("soul_of_might",
            () -> new com.terraforge.rpg.item.material.TerrariaMaterialItem(new Item.Properties().stacksTo(99), com.terraforge.rpg.item.rarity.TerrariaRarity.PINK, 10_000L, "The essence of pure destruction"));

    public static final DeferredItem<com.terraforge.rpg.item.material.TerrariaMaterialItem> SOUL_OF_FRIGHT = ITEMS.register("soul_of_fright",
            () -> new com.terraforge.rpg.item.material.TerrariaMaterialItem(new Item.Properties().stacksTo(99), com.terraforge.rpg.item.rarity.TerrariaRarity.PINK, 10_000L, "The essence of pure terror"));

    public static final DeferredItem<com.terraforge.rpg.item.material.TerrariaMaterialItem> BEETLE_HUSK = ITEMS.register("beetle_husk",
            () -> new com.terraforge.rpg.item.material.TerrariaMaterialItem(new Item.Properties().stacksTo(99), com.terraforge.rpg.item.rarity.TerrariaRarity.YELLOW, 50_000L, "Hardened chitin shed by the ancient Golem"));

    public static final DeferredItem<com.terraforge.rpg.item.material.TerrariaMaterialItem> LUMINITE_BAR = ITEMS.register("luminite_bar",
            () -> new com.terraforge.rpg.item.material.TerrariaMaterialItem(new Item.Properties().stacksTo(99), com.terraforge.rpg.item.rarity.TerrariaRarity.CYAN, 120_000L, "Imbued with the ultimate cosmic energy of the Moon Lord"));

    // Hardmode Weapons & Tools
    public static final DeferredItem<com.terraforge.rpg.item.weapon.PwnhammerItem> PWNHAMMER = ITEMS.register("pwnhammer",
            () -> new com.terraforge.rpg.item.weapon.PwnhammerItem(new Item.Properties().stacksTo(1)));

    // Wall of Flesh Class Emblems
    public static final DeferredItem<com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem> WARRIOR_EMBLEM = ITEMS.register("warrior_emblem",
            () -> new com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem(new Item.Properties().stacksTo(1), com.terraforge.rpg.combat.DamageClass.MELEE));

    public static final DeferredItem<com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem> RANGER_EMBLEM = ITEMS.register("ranger_emblem",
            () -> new com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem(new Item.Properties().stacksTo(1), com.terraforge.rpg.combat.DamageClass.RANGED));

    public static final DeferredItem<com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem> SORCERER_EMBLEM = ITEMS.register("sorcerer_emblem",
            () -> new com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem(new Item.Properties().stacksTo(1), com.terraforge.rpg.combat.DamageClass.MAGIC));

    public static final DeferredItem<com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem> SUMMONER_EMBLEM = ITEMS.register("summoner_emblem",
            () -> new com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem(new Item.Properties().stacksTo(1), com.terraforge.rpg.combat.DamageClass.SUMMON));

    // Consumable Health and Mana Progression (Terraria Items)
    public static final DeferredItem<LifeCrystalItem> LIFE_CRYSTAL = ITEMS.register("life_crystal",
            LifeCrystalItem::new);

    public static final DeferredItem<LifeFruitItem> LIFE_FRUIT = ITEMS.register("life_fruit",
            LifeFruitItem::new);

    public static final DeferredItem<ManaCrystalItem> MANA_CRYSTAL = ITEMS.register("mana_crystal",
            ManaCrystalItem::new);

    public static final DeferredItem<ManaPotionItem> LESSER_MANA_POTION = ITEMS.register("lesser_mana_potion",
            () -> new ManaPotionItem(50.0));

    public static final DeferredItem<ManaPotionItem> MANA_POTION = ITEMS.register("mana_potion",
            () -> new ManaPotionItem(100.0));

    // The 10 RPG Special Accessories
    public static final DeferredItem<SpecialAccessoryItem> PHOENIX_WINGS = ITEMS.register("phoenix_wings",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    Component.literal("Concede voo criativo completo.")));

    public static final DeferredItem<SpecialAccessoryItem> THUNDER_FRAGMENT = ITEMS.register("thunder_fragment",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    Component.literal("+40% de velocidade de movimento.")));

    public static final DeferredItem<SpecialAccessoryItem> VOID_HEART = ITEMS.register("void_heart",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    Component.literal("Teleporte curto controlado com tempo de recarga.")));

    public static final DeferredItem<SpecialAccessoryItem> TITAN_CORE = ITEMS.register("titan_core",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    Component.literal("Bônus massivo de dano físico e imunidade a repulsão.")));

    public static final DeferredItem<SpecialAccessoryItem> ARCANE_PRISM = ITEMS.register("arcane_prism",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    Component.literal("Aumento de dano mágico e regeneração acelerada de mana.")));

    public static final DeferredItem<SpecialAccessoryItem> GUARDIAN_SEAL = ITEMS.register("guardian_seal",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    Component.literal("Aumento simultâneo de Defesa Física e Defesa Mágica.")));

    public static final DeferredItem<SpecialAccessoryItem> BLOOD_CRYSTAL = ITEMS.register("blood_crystal",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    Component.literal("Roubo de vida controlado contra criaturas válidas.")));

    public static final DeferredItem<SpecialAccessoryItem> TIME_GEAR = ITEMS.register("time_gear",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    Component.literal("Aceleração na velocidade de ataque e redução de recargas.")));

    public static final DeferredItem<SpecialAccessoryItem> PREDATOR_EYE = ITEMS.register("predator_eye",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    Component.literal("Aumento expressivo na Chance de Crítico e no Dano Crítico.")));

    public static final DeferredItem<SpecialAccessoryItem> GRAVITY_SIGIL = ITEMS.register("gravity_sigil",
            () -> new SpecialAccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    Component.literal("Salto amplificado, controle aéreo e imunidade a dano de queda.")));

    // Utility Items
    public static final DeferredItem<com.terraforge.rpg.item.utility.MagicMirrorItem> MAGIC_MIRROR = ITEMS.register("magic_mirror",
            () -> new com.terraforge.rpg.item.utility.MagicMirrorItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<com.terraforge.rpg.item.utility.TempleKeyItem> TEMPLE_KEY = ITEMS.register("temple_key",
            () -> new com.terraforge.rpg.item.utility.TempleKeyItem(new Item.Properties().stacksTo(99)));

    public static final DeferredItem<com.terraforge.rpg.item.utility.PortalGunItem> PORTAL_GUN = ITEMS.register("portal_gun",
            () -> new com.terraforge.rpg.item.utility.PortalGunItem(new Item.Properties().stacksTo(1)));

    // Fishing Poles
    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem> WOOD_FISHING_POLE = ITEMS.register("wood_fishing_pole",
            () -> new com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem(new Item.Properties().stacksTo(1),
                    5, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 100L));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem> REINFORCED_FISHING_POLE = ITEMS.register("reinforced_fishing_pole",
            () -> new com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem(new Item.Properties().stacksTo(1),
                    15, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 2_000L));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem> GOLDEN_FISHING_ROD = ITEMS.register("golden_fishing_rod",
            () -> new com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem(new Item.Properties().stacksTo(1),
                    50, com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 50_000L));

    // Fishing Baits
    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaBaitItem> MONARCH_BUTTERFLY = ITEMS.register("monarch_butterfly",
            () -> new com.terraforge.rpg.item.fishing.TerrariaBaitItem(new Item.Properties().stacksTo(99),
                    5, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 50L));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaBaitItem> WORM = ITEMS.register("worm",
            () -> new com.terraforge.rpg.item.fishing.TerrariaBaitItem(new Item.Properties().stacksTo(99),
                    25, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 100L));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaBaitItem> ENCHANTED_NIGHTCRAWLER = ITEMS.register("enchanted_nightcrawler",
            () -> new com.terraforge.rpg.item.fishing.TerrariaBaitItem(new Item.Properties().stacksTo(99),
                    35, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 500L));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaBaitItem> MASTER_BAIT = ITEMS.register("master_bait",
            () -> new com.terraforge.rpg.item.fishing.TerrariaBaitItem(new Item.Properties().stacksTo(99),
                    50, com.terraforge.rpg.item.rarity.TerrariaRarity.GREEN, 1_000L));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TruffleWormItem> TRUFFLE_WORM = ITEMS.register("truffle_worm",
            () -> new com.terraforge.rpg.item.fishing.TruffleWormItem(new Item.Properties().stacksTo(99)));

    // Fishing Crates
    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaCrateItem> WOODEN_CRATE = ITEMS.register("wooden_crate",
            () -> new com.terraforge.rpg.item.fishing.TerrariaCrateItem(new Item.Properties().stacksTo(99),
                    com.terraforge.rpg.item.fishing.TerrariaCrateItem.CrateTier.WOODEN));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaCrateItem> IRON_CRATE = ITEMS.register("iron_crate",
            () -> new com.terraforge.rpg.item.fishing.TerrariaCrateItem(new Item.Properties().stacksTo(99),
                    com.terraforge.rpg.item.fishing.TerrariaCrateItem.CrateTier.IRON));

    public static final DeferredItem<com.terraforge.rpg.item.fishing.TerrariaCrateItem> GOLDEN_CRATE = ITEMS.register("golden_crate",
            () -> new com.terraforge.rpg.item.fishing.TerrariaCrateItem(new Item.Properties().stacksTo(99),
                    com.terraforge.rpg.item.fishing.TerrariaCrateItem.CrateTier.GOLDEN));

    // Grappling Hooks
    public static final DeferredItem<com.terraforge.rpg.item.hook.GrapplingHookItem> GRAPPLING_HOOK = ITEMS.register("grappling_hook",
            () -> new com.terraforge.rpg.item.hook.GrapplingHookItem(new Item.Properties().stacksTo(1),
                    18.0, 0.8, 1, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 2_000L));

    public static final DeferredItem<com.terraforge.rpg.item.hook.GrapplingHookItem> IVY_WHIP = ITEMS.register("ivy_whip",
            () -> new com.terraforge.rpg.item.hook.GrapplingHookItem(new Item.Properties().stacksTo(1),
                    25.0, 1.1, 3, com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 20_000L));

    // Mounts
    public static final DeferredItem<com.terraforge.rpg.item.mount.SlimySaddleItem> SLIMY_SADDLE = ITEMS.register("slimy_saddle",
            () -> new com.terraforge.rpg.item.mount.SlimySaddleItem(new Item.Properties().stacksTo(1)));

    // Terraria Armor Sets: Copper Armor
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> COPPER_HELMET = ITEMS.register("copper_helmet",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.IRON, net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1), "copper", 1, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 150L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> COPPER_CHESTPLATE = ITEMS.register("copper_chestplate",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.IRON, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1), "copper", 2, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 250L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> COPPER_LEGGINGS = ITEMS.register("copper_leggings",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.IRON, net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1), "copper", 1, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 200L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> COPPER_BOOTS = ITEMS.register("copper_boots",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.IRON, net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1), "copper", 0, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.WHITE, 150L));

    // Terraria Armor Sets: Shadow Armor (Corruption)
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> SHADOW_HELMET = ITEMS.register("shadow_helmet",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1), "shadow", 5, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 7_500L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> SHADOW_SCALEMAIL = ITEMS.register("shadow_scalemail",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1), "shadow", 7, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 10_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> SHADOW_GREAVES = ITEMS.register("shadow_greaves",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1), "shadow", 6, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 9_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> SHADOW_BOOTS = ITEMS.register("shadow_boots",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1), "shadow", 1, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 5_000L));

    // Terraria Armor Sets: Crimson Armor (Crimson)
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> CRIMSON_HELMET = ITEMS.register("crimson_helmet",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1), "crimson", 5, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 7_500L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> CRIMSON_SCALEMAIL = ITEMS.register("crimson_scalemail",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1), "crimson", 7, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 10_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> CRIMSON_GREAVES = ITEMS.register("crimson_greaves",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1), "crimson", 6, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 9_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> CRIMSON_BOOTS = ITEMS.register("crimson_boots",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1), "crimson", 1, com.terraforge.rpg.combat.DamageClass.GENERIC, com.terraforge.rpg.item.rarity.TerrariaRarity.BLUE, 5_000L));

    // Terraria Armor Sets: Molten Armor (Underworld Hellstone)
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> MOLTEN_HELMET = ITEMS.register("molten_helmet",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1), "molten", 8, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 15_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> MOLTEN_BREASTPLATE = ITEMS.register("molten_breastplate",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1), "molten", 9, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 20_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> MOLTEN_GREAVES = ITEMS.register("molten_greaves",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1), "molten", 7, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 18_000L));
    public static final DeferredItem<com.terraforge.rpg.armor.TerrariaArmorItem> MOLTEN_BOOTS = ITEMS.register("molten_boots",
            () -> new com.terraforge.rpg.armor.TerrariaArmorItem(net.minecraft.world.item.ArmorMaterials.NETHERITE, net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1), "molten", 1, com.terraforge.rpg.combat.DamageClass.MELEE, com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 12_000L));

    // Expanded Arsenal: Beam Swords
    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerraBeamSwordItem> NIGHTS_EDGE = ITEMS.register("nights_edge",
            () -> new com.terraforge.rpg.item.weapon.TerraBeamSwordItem(
                    new Item.Properties().stacksTo(1), com.terraforge.rpg.item.rarity.TerrariaRarity.ORANGE, 54_000L, 40.0, 0.04, 4.5, 21, 40.0, 2, 1.5f));

    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerraBeamSwordItem> EXCALIBUR = ITEMS.register("excalibur",
            () -> new com.terraforge.rpg.item.weapon.TerraBeamSwordItem(
                    new Item.Properties().stacksTo(1), com.terraforge.rpg.item.rarity.TerrariaRarity.PINK, 230_000L, 72.0, 0.04, 4.5, 18, 72.0, 1, 2.0f));

    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerraBeamSwordItem> TERRA_BLADE = ITEMS.register("terra_blade",
            () -> new com.terraforge.rpg.item.weapon.TerraBeamSwordItem(
                    new Item.Properties().stacksTo(1), com.terraforge.rpg.item.rarity.TerrariaRarity.YELLOW, 1_000_000L, 115.0, 0.04, 6.5, 14, 115.0, 3, 2.5f));

    // Expanded Arsenal: Firearms
    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerrariaGunItem> MINISHARK = ITEMS.register("minishark",
            () -> new com.terraforge.rpg.item.weapon.TerrariaGunItem(
                    new Item.Properties().stacksTo(1), com.terraforge.rpg.item.rarity.TerrariaRarity.GREEN, 350_000L, 6.0, 0.04, 0.0, 8, 0.33));

    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerrariaGunItem> MEGASHARK = ITEMS.register("megashark",
            () -> new com.terraforge.rpg.item.weapon.TerrariaGunItem(
                    new Item.Properties().stacksTo(1), com.terraforge.rpg.item.rarity.TerrariaRarity.PINK, 600_000L, 25.0, 0.04, 1.0, 6, 0.50));

    // Expanded Arsenal: Magic Spells
    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerrariaMagicSpellItem> WATER_BOLT = ITEMS.register("water_bolt",
            () -> new com.terraforge.rpg.item.weapon.TerrariaMagicSpellItem(
                    new Item.Properties().stacksTo(1), "water_bolt", com.terraforge.rpg.item.rarity.TerrariaRarity.GREEN, 50_000L, 19.0, 0.04, 5.0, 17, 10.0, 5, 5, 1.2f));

    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerrariaMagicSpellItem> SPACE_GUN = ITEMS.register("space_gun",
            () -> new com.terraforge.rpg.item.weapon.TerrariaMagicSpellItem(
                    new Item.Properties().stacksTo(1), "space_gun", com.terraforge.rpg.item.rarity.TerrariaRarity.GREEN, 40_000L, 17.0, 0.04, 0.75, 17, 6.0, 2, 0, 2.0f));

    // Expanded Arsenal: Canonical Accessories
    public static final DeferredItem<com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.HermesBootsItem> HERMES_BOOTS = ITEMS.register("hermes_boots",
            () -> new com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.HermesBootsItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.BandOfRegenerationItem> BAND_OF_REGENERATION = ITEMS.register("band_of_regeneration",
            () -> new com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.BandOfRegenerationItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.TerrasparkBootsItem> TERRASPARK_BOOTS = ITEMS.register("terraspark_boots",
            () -> new com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.TerrasparkBootsItem(new Item.Properties().stacksTo(1)));

    // Endgame Weapons
    public static final DeferredItem<com.terraforge.rpg.item.weapon.TerraBeamSwordItem> MEOWMERE = ITEMS.register("meowmere",
            () -> new com.terraforge.rpg.item.weapon.TerraBeamSwordItem(
                    new Item.Properties().stacksTo(1), com.terraforge.rpg.item.rarity.TerrariaRarity.RED, 1_000_000L, 200.0, 0.04, 4.0, 14, 200.0, 4, 2.5f));

    public static final DeferredItem<com.terraforge.rpg.item.weapon.TsunamiBowItem> TSUNAMI = ITEMS.register("tsunami",
            () -> new com.terraforge.rpg.item.weapon.TsunamiBowItem(new Item.Properties().stacksTo(1)));

    // Block Items
    public static final DeferredItem<BlockItem> WORK_BENCH_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.WORK_BENCH);
    public static final DeferredItem<BlockItem> COPPER_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.COPPER_ORE);
    public static final DeferredItem<BlockItem> TIN_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.TIN_ORE);
    public static final DeferredItem<BlockItem> ASH_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.ASH_BLOCK);
    public static final DeferredItem<BlockItem> HELLSTONE_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.HELLSTONE_ORE);
    public static final DeferredItem<BlockItem> EBONSTONE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.EBONSTONE_BLOCK);
    public static final DeferredItem<BlockItem> CRIMSTONE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.CRIMSTONE_BLOCK);
    public static final DeferredItem<BlockItem> DEMON_ALTAR_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.DEMON_ALTAR);
    public static final DeferredItem<BlockItem> CRIMSON_ALTAR_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.CRIMSON_ALTAR);
    public static final DeferredItem<BlockItem> COBALT_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.COBALT_ORE);
    public static final DeferredItem<BlockItem> PALLADIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.PALLADIUM_ORE);
    public static final DeferredItem<BlockItem> MYTHRIL_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MYTHRIL_ORE);
    public static final DeferredItem<BlockItem> ORICHALCUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.ORICHALCUM_ORE);
    public static final DeferredItem<BlockItem> ADAMANTITE_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.ADAMANTITE_ORE);
    public static final DeferredItem<BlockItem> TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.TITANIUM_ORE);
    public static final DeferredItem<BlockItem> LUMINITE_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.LUMINITE_ORE);

    private ModItems() {}
}
