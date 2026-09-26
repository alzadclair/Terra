# Mapeamento Canônico de Receitas 3D — TerraForge RPG

**Referência Terraria:** PC 1.4.5.8  
**Regra 3D:** Todo resultado e todo ingrediente custom deve ser um asset 3D aprovado. Nenhum placeholder 2D é permitido.

---

| Item Final | Nome Canônico | Receita Original Terraria 1.4.5.8 | Receita Adaptada Minecraft 3D | Estação | Modificações | Motivo Técnico |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `terraforge_rpg:work_bench` | **Work Bench** | 10 Any Wood | 4 Any Wood Planks (2x2) | `By Hand / Vanilla Crafting` | Adapted to standard 2x2 Minecraft inventory crafting grid | Allows immediate early-game progression identical to Terraria and Minecraft starter flow |
| `terraforge_rpg:copper_shortsword` | **Copper Shortsword** | 7 Copper Bars at Work Bench | 2 Copper Ingots + 1 Stick | `terraforge_rpg:work_bench` | Uses vanilla copper ingots and sticks | Canonical Terraria starter weapon adapted to Minecraft 3D crafting ergonomics |
| `terraforge_rpg:nights_edge` | **Night's Edge** | Light's Bane/Blood Butcherer + Muramasa + Blade of Grass + Fiery Greatsword at Demon/Crimson Altar | 4 Corrupted/Crimson materials + 1 Diamond Sword at Altar | `terraforge_rpg:demon_altar / crimson_altar` | Requires smashing pre-hardmode evils or crafting at Altar | Preserves canonical pre-hardmode climax sword progression |
| `terraforge_rpg:true_nights_edge` | **True Night's Edge** | Night's Edge + 20 Soul of Fright + 20 Soul of Might + 20 Soul of Sight at Mythril/Orichalcum Anvil | Night's Edge + Mechanical Boss Souls | `terraforge_rpg:work_bench` | Direct canonical Terraria 1.4.5.8 recipe adapted with 3D models | Accurate 1.4.5.8 recipe requirement matching the 3 mechanical bosses |
| `terraforge_rpg:terra_blade` | **Terra Blade** | True Night's Edge + True Excalibur at Mythril/Orichalcum Anvil | True Night's Edge + Excalibur + Hallowed Core | `terraforge_rpg:work_bench` | Direct canonical fusion of the light and dark swords | Iconic Terraria post-Plantera weapon synthesis |
| `terraforge_rpg:zenith` | **Zenith** | Terra Blade + Meowmere + Star Wrath + Influx Waver + The Horseman's Blade + Seedler + Starfury + Bee Keeper + Enchanted Sword + Copper Shortsword at Mythril Anvil | Terra Blade + Meowmere + Seedler + Starfury + Copper Shortsword | `terraforge_rpg:work_bench` | Synthesizes the key 3D weapons in the journey from starter to endgame | 100% 3D ingredient compliance without non-existent placeholder weapons |

---

## 2. Diretrizes de Ingredientes e Estações 3D

1. **Work Bench:** Primeiro bloco e estação 3D do mod, craftado a partir de 4 tábuas de madeira na grade 2x2. Permite acesso ao menu de crafting de receitas Terraria.
2. **Ingredientes 3D Mandatórios:** Nenhuma receita pode utilizar itens desabilitados ou placeholders marcados como `DEPRECATED_INTERNAL`. Se uma receita exige um ingrediente que ainda não possui modelo 3D aprovado no catálogo, a receita permanece bloqueada até que o asset seja otimizado.
3. **Vanilla Integrado:** Itens vanilla do Minecraft (como `minecraft:copper_ingot`, `minecraft:stick`, `minecraft:diamond_sword`, `minecraft:nether_star`) são utilizados como pontes canônicas onde o equivalente Minecraft faz sentido físico e conceitual.
