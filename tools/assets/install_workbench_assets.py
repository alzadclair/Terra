"""
Install 3D Workbench assets, models, blockstates, textures, recipes, and loot tables.
"""

import os
import json
import shutil
from PIL import Image
from pathlib import Path

WORKSPACE = Path(r"c:\Users\Alza\Desktop\terraforge")
RES = WORKSPACE / "src/main/resources"
SOURCE_TEX = Path(r"C:\model 3d\_processed\9c938a10fee54b6d9d26c8e4bc5fa9aa\source\textures\m_12_baseColor.png")
NORMALIZED_OBJ = WORKSPACE / "art_work/workbench_normalized.obj"

def main():
    # 1. Texture: Optimize 4096 to 512x512
    tex_dir = RES / "assets/terraforge_rpg/textures/block"
    tex_dir.mkdir(parents=True, exist_ok=True)
    target_tex = tex_dir / "work_bench.png"
    with Image.open(SOURCE_TEX) as img:
        img_512 = img.resize((512, 512), Image.Resampling.LANCZOS)
        img_512.save(target_tex, "PNG", optimize=True)
    print(f"Installed optimized texture at {target_tex} (512x512)")

    # 2. Block OBJ Model
    model_block_dir = RES / "assets/terraforge_rpg/models/block"
    model_block_dir.mkdir(parents=True, exist_ok=True)
    target_obj = model_block_dir / "work_bench.obj"
    shutil.copyfile(NORMALIZED_OBJ, target_obj)
    print(f"Installed 3D block OBJ model at {target_obj}")

    # 3. Blockstate JSON
    blockstate_dir = RES / "assets/terraforge_rpg/blockstates"
    blockstate_dir.mkdir(parents=True, exist_ok=True)
    blockstate_json = blockstate_dir / "work_bench.json"
    blockstate_data = {
        "variants": {
            "": { "model": "terraforge_rpg:block/work_bench" }
        }
    }
    with open(blockstate_json, "w", encoding="utf-8") as f:
        json.dump(blockstate_data, f, indent=2)
    print(f"Created blockstate at {blockstate_json}")

    # 4. Block Model JSON (NeoForge OBJ Loader)
    block_model_json = model_block_dir / "work_bench.json"
    block_model_data = {
        "loader": "neoforge:obj",
        "model": "terraforge_rpg:models/block/work_bench.obj",
        "textures": {
            "m_12": "terraforge_rpg:block/work_bench"
        },
        "ambientocclusion": True
    }
    with open(block_model_json, "w", encoding="utf-8") as f:
        json.dump(block_model_data, f, indent=2)
    print(f"Created block model JSON at {block_model_json}")

    # 5. Item Model JSON
    model_item_dir = RES / "assets/terraforge_rpg/models/item"
    model_item_dir.mkdir(parents=True, exist_ok=True)
    item_model_json = model_item_dir / "work_bench.json"
    item_model_data = {
        "parent": "terraforge_rpg:block/work_bench",
        "display": {
            "gui": {
                "rotation": [ 30, 225, 0 ],
                "translation": [ 0, 0, 0 ],
                "scale": [ 0.625, 0.625, 0.625 ]
            },
            "ground": {
                "rotation": [ 0, 0, 0 ],
                "translation": [ 0, 3, 0 ],
                "scale": [ 0.25, 0.25, 0.25 ]
            },
            "fixed": {
                "rotation": [ 0, 0, 0 ],
                "translation": [ 0, 0, 0 ],
                "scale": [ 0.5, 0.5, 0.5 ]
            },
            "thirdperson_righthand": {
                "rotation": [ 75, 45, 0 ],
                "translation": [ 0, 2.5, 0 ],
                "scale": [ 0.375, 0.375, 0.375 ]
            },
            "firstperson_righthand": {
                "rotation": [ 0, 45, 0 ],
                "translation": [ 0, 0, 0 ],
                "scale": [ 0.4, 0.4, 0.4 ]
            }
        }
    }
    with open(item_model_json, "w", encoding="utf-8") as f:
        json.dump(item_model_data, f, indent=2)
    print(f"Created item model JSON at {item_model_json}")

    # 6. Recipe JSON (Crafted from 10 wood planks in vanilla crafting grid)
    recipe_dir = RES / "data/terraforge_rpg/recipe"
    recipe_dir.mkdir(parents=True, exist_ok=True)
    recipe_json = recipe_dir / "work_bench.json"
    recipe_data = {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "key": {
            "#": {
                "tag": "minecraft:planks"
            }
        },
        "pattern": [
            "##",
            "##"
        ],
        "result": {
            "count": 1,
            "id": "terraforge_rpg:work_bench"
        }
    }
    with open(recipe_json, "w", encoding="utf-8") as f:
        json.dump(recipe_data, f, indent=2)
    print(f"Created recipe at {recipe_json}")

    # 7. Loot Table JSON
    loot_dir = RES / "data/terraforge_rpg/loot_table/blocks"
    loot_dir.mkdir(parents=True, exist_ok=True)
    loot_json = loot_dir / "work_bench.json"
    loot_data = {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ],
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "terraforge_rpg:work_bench"
                    }
                ],
                "rolls": 1.0
            }
        ]
    }
    with open(loot_json, "w", encoding="utf-8") as f:
        json.dump(loot_data, f, indent=2)
    print(f"Created loot table at {loot_json}")

    # 8. Translations
    lang_dir = RES / "assets/terraforge_rpg/lang"
    en_json = lang_dir / "en_us.json"
    pt_json = lang_dir / "pt_br.json"
    if en_json.exists():
        with open(en_json, "r", encoding="utf-8") as f:
            en_data = json.load(f)
        en_data["block.terraforge_rpg.work_bench"] = "Work Bench"
        with open(en_json, "w", encoding="utf-8") as f:
            json.dump(en_data, f, indent=2, ensure_ascii=False)
    if pt_json.exists():
        with open(pt_json, "r", encoding="utf-8") as f:
            pt_data = json.load(f)
        pt_data["block.terraforge_rpg.work_bench"] = "Bancada de Trabalho"
        with open(pt_json, "w", encoding="utf-8") as f:
            json.dump(pt_data, f, indent=2, ensure_ascii=False)
    print("Updated language files with Work Bench translations.")

if __name__ == "__main__":
    main()
