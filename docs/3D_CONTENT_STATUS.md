# Status de Conteúdo 3D — TerraForge RPG

**Data:** 2026-09-26  
**Fase:** PASS 4 & PASS 5 (Mapeamento de Conteúdo e Deprecação de Placeholders)

---

## 1. Visão Geral da Migração

Conforme as diretrizes da reestruturação completa para 3D:
- **Itens com modelo 3D aprovado:** Mantêm ID canônico no registro, preservam mecânicas e recebem assets 3D otimizados.
- **Itens sem modelo 3D aprovado:** São marcados como `@Deprecated(forRemoval = true) // DEPRECATED_INTERNAL`, removidos da Creative Tab, desvinculados de receitas e drops, prevenindo placeholders 2D visíveis em jogo.
- **Entidades e Bosses com modelo 3D aprovado:** Migrados de renderers genéricos/placeholders (GhastModel, TitanBossModel, caixas simples) para malhas e rigs 3D reais.

---

## 2. Itens Migrados para 3D (12 itens)

| ID do Item | Campo Java | Modelo 3D Aprovado | Autor | Triângulos | Licença |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `terraforge_rpg:suspicious_looking_eye` | `SUSPICIOUS_LOOKING_EYE` | Eye | shovelsquid | 1272 | CC Attribution |
| `terraforge_rpg:mechanical_eye` | `MECHANICAL_EYE` | Eye | shovelsquid | 1272 | CC Attribution |
| `terraforge_rpg:lesser_mana_potion` | `LESSER_MANA_POTION` | Potion | toha3673 | 512 | CC Attribution |
| `terraforge_rpg:mana_potion` | `MANA_POTION` | Potion | toha3673 | 512 | CC Attribution |
| `terraforge_rpg:predator_eye` | `PREDATOR_EYE` | Eye | shovelsquid | 1272 | CC Attribution |
| `terraforge_rpg:worm` | `WORM` | Eater Of Worlds terraria + worm food | DAR 88 | 12984 | CC Attribution |
| `terraforge_rpg:nights_edge` | `NIGHTS_EDGE` | Night's Edge from Terraria video game | Artieee | 7950 | CC Attribution |
| `terraforge_rpg:excalibur` | `EXCALIBUR` | True Excalibur | StlMaster | 115804 | CC Attribution |
| `terraforge_rpg:terra_blade` | `TERRA_BLADE` | Terra Blade | alvarotakano | 2078 | CC Attribution |
| `terraforge_rpg:minishark` | `MINISHARK` | Terraria Minishark | SomeGuyUsingBlender | 21629 | CC Attribution |
| `terraforge_rpg:megashark` | `MEGASHARK` | Megashark minecraft model | Reza_artz | 156 | CC Attribution |
| `terraforge_rpg:meowmere` | `MEOWMERE` | Meowmere (Terraria) | Kuruzeus | 1176 | CC Attribution |

---

## 3. Itens Desabilitados / Ocultos (83 itens sem modelo 3D)

Estes itens não possuem modelos 3D comercialmente aprovados no catálogo local. Eles foram removidos das abas do modo Criativo, do sistema de receitas e tabelas de loot para garantir experiência 100% 3D sem placeholders.

| ID do Item | Campo Java | Classe | Status no Mod |
| :--- | :--- | :--- | :--- |
| `terraforge_rpg:copper_coin` | `COPPER_COIN` | `Item` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:silver_coin` | `SILVER_COIN` | `Item` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:gold_coin` | `GOLD_COIN` | `Item` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:platinum_coin` | `PLATINUM_COIN` | `Item` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:copper_shortsword` | `COPPER_SHORTSWORD` | `TerrariaSwordItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:wooden_bow` | `WOODEN_BOW` | `TerrariaBowItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:wand_of_sparking` | `WAND_OF_SPARKING` | `TerrariaMagicStaffItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:slime_staff` | `SLIME_STAFF` | `TerrariaSummonStaffItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:wooden_yoyo` | `WOODEN_YOYO` | `TerrariaYoyoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:leather_whip` | `LEATHER_WHIP` | `TerrariaWhipItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:wooden_arrow` | `WOODEN_ARROW` | `TerrariaAmmoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:flaming_arrow` | `FLAMING_ARROW` | `TerrariaAmmoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:jester_arrow` | `JESTER_ARROW` | `TerrariaAmmoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:unholy_arrow` | `UNHOLY_ARROW` | `TerrariaAmmoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:musket_ball` | `MUSKET_BALL` | `TerrariaAmmoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:meteor_shot` | `METEOR_SHOT` | `TerrariaAmmoItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:slime_crown` | `SLIME_CROWN` | `com.terraforge.rpg.item.boss.SlimeCrownItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:guide_voodoo_doll` | `GUIDE_VOODOO_DOLL` | `com.terraforge.rpg.item.boss.GuideVoodooDollItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:mechanical_worm` | `MECHANICAL_WORM` | `com.terraforge.rpg.item.boss.MechanicalWormItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:mechanical_skull` | `MECHANICAL_SKULL` | `com.terraforge.rpg.item.boss.MechanicalSkullItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:celestial_sigil` | `CELESTIAL_SIGIL` | `com.terraforge.rpg.item.boss.CelestialSigilItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:lihzahrd_power_cell` | `LIHZAHRD_POWER_CELL` | `com.terraforge.rpg.item.boss.LihzahrdPowerCellItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:hallowed_bar` | `HALLOWED_BAR` | `com.terraforge.rpg.item.material.TerrariaMaterialItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:soul_of_sight` | `SOUL_OF_SIGHT` | `com.terraforge.rpg.item.material.TerrariaMaterialItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:soul_of_might` | `SOUL_OF_MIGHT` | `com.terraforge.rpg.item.material.TerrariaMaterialItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:soul_of_fright` | `SOUL_OF_FRIGHT` | `com.terraforge.rpg.item.material.TerrariaMaterialItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:beetle_husk` | `BEETLE_HUSK` | `com.terraforge.rpg.item.material.TerrariaMaterialItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:luminite_bar` | `LUMINITE_BAR` | `com.terraforge.rpg.item.material.TerrariaMaterialItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:pwnhammer` | `PWNHAMMER` | `com.terraforge.rpg.item.weapon.PwnhammerItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:warrior_emblem` | `WARRIOR_EMBLEM` | `com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:ranger_emblem` | `RANGER_EMBLEM` | `com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:sorcerer_emblem` | `SORCERER_EMBLEM` | `com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:summoner_emblem` | `SUMMONER_EMBLEM` | `com.terraforge.rpg.item.accessory.emblem.ClassEmblemItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:life_crystal` | `LIFE_CRYSTAL` | `LifeCrystalItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:life_fruit` | `LIFE_FRUIT` | `LifeFruitItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:mana_crystal` | `MANA_CRYSTAL` | `ManaCrystalItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:phoenix_wings` | `PHOENIX_WINGS` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:thunder_fragment` | `THUNDER_FRAGMENT` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:void_heart` | `VOID_HEART` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:titan_core` | `TITAN_CORE` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:arcane_prism` | `ARCANE_PRISM` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:guardian_seal` | `GUARDIAN_SEAL` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:blood_crystal` | `BLOOD_CRYSTAL` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:time_gear` | `TIME_GEAR` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:gravity_sigil` | `GRAVITY_SIGIL` | `SpecialAccessoryItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:magic_mirror` | `MAGIC_MIRROR` | `com.terraforge.rpg.item.utility.MagicMirrorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:temple_key` | `TEMPLE_KEY` | `com.terraforge.rpg.item.utility.TempleKeyItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:portal_gun` | `PORTAL_GUN` | `com.terraforge.rpg.item.utility.PortalGunItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:wood_fishing_pole` | `WOOD_FISHING_POLE` | `com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:reinforced_fishing_pole` | `REINFORCED_FISHING_POLE` | `com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:golden_fishing_rod` | `GOLDEN_FISHING_ROD` | `com.terraforge.rpg.item.fishing.TerrariaFishingPoleItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:monarch_butterfly` | `MONARCH_BUTTERFLY` | `com.terraforge.rpg.item.fishing.TerrariaBaitItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:enchanted_nightcrawler` | `ENCHANTED_NIGHTCRAWLER` | `com.terraforge.rpg.item.fishing.TerrariaBaitItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:master_bait` | `MASTER_BAIT` | `com.terraforge.rpg.item.fishing.TerrariaBaitItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:truffle_worm` | `TRUFFLE_WORM` | `com.terraforge.rpg.item.fishing.TruffleWormItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:wooden_crate` | `WOODEN_CRATE` | `com.terraforge.rpg.item.fishing.TerrariaCrateItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:iron_crate` | `IRON_CRATE` | `com.terraforge.rpg.item.fishing.TerrariaCrateItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:golden_crate` | `GOLDEN_CRATE` | `com.terraforge.rpg.item.fishing.TerrariaCrateItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:grappling_hook` | `GRAPPLING_HOOK` | `com.terraforge.rpg.item.hook.GrapplingHookItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:ivy_whip` | `IVY_WHIP` | `com.terraforge.rpg.item.hook.GrapplingHookItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:slimy_saddle` | `SLIMY_SADDLE` | `com.terraforge.rpg.item.mount.SlimySaddleItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:copper_helmet` | `COPPER_HELMET` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:copper_chestplate` | `COPPER_CHESTPLATE` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:copper_leggings` | `COPPER_LEGGINGS` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:copper_boots` | `COPPER_BOOTS` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:shadow_helmet` | `SHADOW_HELMET` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:shadow_scalemail` | `SHADOW_SCALEMAIL` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:shadow_greaves` | `SHADOW_GREAVES` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:shadow_boots` | `SHADOW_BOOTS` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:crimson_helmet` | `CRIMSON_HELMET` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:crimson_scalemail` | `CRIMSON_SCALEMAIL` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:crimson_greaves` | `CRIMSON_GREAVES` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:crimson_boots` | `CRIMSON_BOOTS` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:molten_helmet` | `MOLTEN_HELMET` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:molten_breastplate` | `MOLTEN_BREASTPLATE` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:molten_greaves` | `MOLTEN_GREAVES` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:molten_boots` | `MOLTEN_BOOTS` | `com.terraforge.rpg.armor.TerrariaArmorItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:water_bolt` | `WATER_BOLT` | `com.terraforge.rpg.item.weapon.TerrariaMagicSpellItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:space_gun` | `SPACE_GUN` | `com.terraforge.rpg.item.weapon.TerrariaMagicSpellItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:hermes_boots` | `HERMES_BOOTS` | `com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.HermesBootsItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:band_of_regeneration` | `BAND_OF_REGENERATION` | `com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.BandOfRegenerationItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:terraspark_boots` | `TERRASPARK_BOOTS` | `com.terraforge.rpg.item.accessory.standard.StandardTerrariaAccessories.TerrasparkBootsItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |
| `terraforge_rpg:tsunami` | `TSUNAMI` | `com.terraforge.rpg.item.weapon.TsunamiBowItem` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |

---

## 4. Entidades e Bosses Mapeados para 3D (10 entidades)

| ID da Entidade | Modelo 3D Aprovado | Autor | Triângulos | Armature | Animações | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `terraforge_rpg:green_slime` | King Slime | SomeGuyUsingBlender | 107855 | False | 0 | MIGRATED_3D |
| `terraforge_rpg:blue_slime` | King Slime | SomeGuyUsingBlender | 107855 | False | 0 | MIGRATED_3D |
| `terraforge_rpg:demon_eye` | Eye | shovelsquid | 1272 | True | 1 | MIGRATED_3D |
| `terraforge_rpg:eye_of_cthulhu` | Eye of cthulhu terraria fanart | pedrohmm123 | 7106 | False | 0 | MIGRATED_3D |
| `terraforge_rpg:king_slime` | King Slime | SomeGuyUsingBlender | 107855 | False | 0 | MIGRATED_3D |
| `terraforge_rpg:the_destroyer` | The Destroyer | GGend | 8892 | False | 0 | MIGRATED_3D |
| `terraforge_rpg:skeletron_prime` | Skeletron Rig | NO DONT EAT ME CASEOH (Ferris wheel) | 19304 | True | 1 | MIGRATED_3D |
| `terraforge_rpg:golem` | Terraria Rock Golem | Bleinisin | 384 | False | 4 | MIGRATED_3D |
| `terraforge_rpg:duke_fishron` | Duke Fishron | Damazo26 | 6529 | True | 0 | MIGRATED_3D |
| `terraforge_rpg:moon_lord` | Moonlord - A realistic 3D Terraria model | StlMaster | 2785212 | False | 0 | MIGRATED_3D |
