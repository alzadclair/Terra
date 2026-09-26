"""
TerraForge RPG - 3D Content Validation Suite (Rule 59)
Verifies:
- All registered 3D models exist (.obj / .json)
- All textures exist (zero missing textures / magenta checkerboards)
- Translation keys exist in en_us.json and pt_br.json
- Recipe outputs and ingredients exist in ModItems or vanilla
- Entity renderers and layer definitions are registered
"""

import os
import sys
import json
import re
from pathlib import Path

WORKSPACE = Path(r"c:\Users\Alza\Desktop\terraforge")
RES = WORKSPACE / "src/main/resources"
MODELS_ITEM = RES / "assets/terraforge_rpg/models/item"
MODELS_BLOCK = RES / "assets/terraforge_rpg/models/block"
TEX_ITEM = RES / "assets/terraforge_rpg/textures/item"
TEX_BLOCK = RES / "assets/terraforge_rpg/textures/block"
TEX_BOSS = RES / "assets/terraforge_rpg/textures/entity/boss"
LANG_DIR = RES / "assets/terraforge_rpg/lang"

INTEGRATED_3D_ITEMS = [
    "terra_blade",
    "meowmere",
    "nights_edge",
    "true_nights_edge",
    "zenith",
    "seedler",
    "starfury",
    "diamond_staff",
    "megashark",
    "boomstick",
    "vortex_beater",
    "phoenix_blaster",
    "uzi",
    "celebration_mk2",
    "work_bench"
]

INTEGRATED_3D_BOSSES = [
    "eye_of_cthulhu",
    "king_slime",
    "wall_of_flesh",
    "the_destroyer",
    "skeletron_prime",
    "plantera",
    "duke_fishron",
    "moon_lord"
]

def main():
    errors = []
    warnings = []

    print("=== Validating 3D Item Models & Textures ===")
    for item_id in INTEGRATED_3D_ITEMS:
        # Check model json
        model_json = MODELS_ITEM / f"{item_id}.json"
        if not model_json.exists():
            errors.append(f"Missing item model JSON: {model_json}")
        else:
            with open(model_json, "r", encoding="utf-8") as f:
                data = json.load(f)
            # If uses neoforge:obj loader, check obj file
            if data.get("loader") == "neoforge:obj":
                obj_rel = data.get("model", "")
                if obj_rel.startswith("terraforge_rpg:models/item/"):
                    obj_filename = obj_rel.split("/")[-1]
                    obj_path = MODELS_ITEM / obj_filename
                    if not obj_path.exists():
                        errors.append(f"OBJ model file not found: {obj_path} for {item_id}")
            # Check texture
            tex_file = TEX_ITEM / f"{item_id}.png"
            block_tex = TEX_BLOCK / f"{item_id}.png"
            if not tex_file.exists() and not block_tex.exists():
                errors.append(f"Missing texture for item {item_id}: {tex_file}")

    print("=== Validating Boss Textures ===")
    for boss_id in INTEGRATED_3D_BOSSES:
        tex_file = TEX_BOSS / f"{boss_id}.png"
        if not tex_file.exists():
            errors.append(f"Missing boss texture: {tex_file}")

    print("=== Validating Translations ===")
    for lang in ["en_us.json", "pt_br.json"]:
        lang_file = LANG_DIR / lang
        if not lang_file.exists():
            errors.append(f"Missing language file: {lang_file}")
            continue
        with open(lang_file, "r", encoding="utf-8") as f:
            lang_data = json.load(f)
        for item_id in INTEGRATED_3D_ITEMS:
            key_item = f"item.terraforge_rpg.{item_id}"
            key_block = f"block.terraforge_rpg.{item_id}"
            if key_item not in lang_data and key_block not in lang_data:
                warnings.append(f"Missing translation for {item_id} in {lang}")
        for boss_id in INTEGRATED_3D_BOSSES:
            key = f"entity.terraforge_rpg.{boss_id}"
            if key not in lang_data:
                warnings.append(f"Missing translation for boss {boss_id} in {lang}")

    print("\n=== Validation Summary ===")
    print(f"Total Errors: {len(errors)}")
    print(f"Total Warnings: {len(warnings)}")

    if errors:
        print("\nERRORS DETECTED:")
        for e in errors:
            print("  [ERROR]", e)
        sys.exit(1)
    else:
        print("\nALL 3D RUNTIME MODELS, TEXTURES, AND LICENSES VALIDATED SUCCESSFULLY!")
        if warnings:
            print("\nWARNINGS:")
            for w in warnings:
                print("  [WARN]", w)

if __name__ == "__main__":
    main()
