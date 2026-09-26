"""
TerraForge RPG - Comprehensive 3D & Registry Validation Suite
Audits:
- Dynamic registry extraction (ModItems, ModBlocks, ModEntities)
- Recipe results & ingredients against registries
- Runtime item & block models (.json, .obj, .mtl, .png)
- Orphan model detection
- Absolute path prohibition in runtime assets
- Entity model implementation checks (blacklisting CubeListBuilder/GhastModel for INTEGRATED_3D bosses)
- Translation keys in en_us.json and pt_br.json
- Fully relative paths (reproducible across any machine/clone location)
"""

import os
import sys
import json
import re
from pathlib import Path

# Dynamically locate project root relative to this script
PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
SRC_JAVA = PROJECT_ROOT / "src/main/java/com/terraforge/rpg"
RESOURCES = PROJECT_ROOT / "src/main/resources"
ASSETS_DIR = RESOURCES / "assets/terraforge_rpg"
DATA_DIR = RESOURCES / "data/terraforge_rpg"

MODELS_ITEM = ASSETS_DIR / "models/item"
MODELS_BLOCK = ASSETS_DIR / "models/block"
TEX_ITEM = ASSETS_DIR / "textures/item"
TEX_BLOCK = ASSETS_DIR / "textures/block"
TEX_ENTITY = ASSETS_DIR / "textures/entity"
LANG_DIR = ASSETS_DIR / "lang"
RECIPES_DIR = DATA_DIR / "recipe"

# Known vanilla items permitted in recipes
VANILLA_ITEMS = {
    "stick", "iron_ingot", "gold_ingot", "diamond", "netherite_ingot", "copper_ingot",
    "string", "feather", "arrow", "bow", "glass", "obsidian", "ender_pearl",
    "blaze_rod", "emerald", "redstone", "lapis_lazuli", "amethyst_shard",
    "crafting_table", "furnace", "anvil", "oak_planks", "spruce_planks", "birch_planks",
    "jungle_planks", "acacia_planks", "dark_oak_planks", "mangrove_planks", "cherry_planks",
    "bamboo_planks", "crimson_planks", "warped_planks"
}

def extract_registries():
    items = set()
    blocks = set()
    entities = set()

    for root, dirs, files in os.walk(SRC_JAVA):
        for f in files:
            if f.endswith(".java"):
                p = Path(root) / f
                with open(p, "r", encoding="utf-8", errors="ignore") as jf:
                    code = jf.read()
                # ITEMS.register("...", ...), ITEMS.registerSimpleItem("...", ...), ITEMS.registerItem("...", ...)
                for m in re.finditer(r'ITEMS\.(?:register|registerSimpleItem|registerItem)\s*\(\s*["\']([^"\']+)["\']', code):
                    items.add(m.group(1))
                # BLOCKS.register("...", ...), BLOCKS.registerSimpleBlock("...", ...)
                for m in re.finditer(r'BLOCKS\.(?:register|registerSimpleBlock)\s*\(\s*["\']([^"\']+)["\']', code):
                    blocks.add(m.group(1))
                # ENTITIES.register("...", ...)
                for m in re.finditer(r'ENTITIES\.register\s*\(\s*["\']([^"\']+)["\']', code):
                    entities.add(m.group(1))

    return items, blocks, entities

def validate_recipes(registered_items, registered_blocks):
    errors = []
    warnings = []

    if not RECIPES_DIR.exists():
        return errors, warnings

    for f in RECIPES_DIR.glob("*.json"):
        try:
            with open(f, "r", encoding="utf-8") as rf:
                recipe = json.load(rf)
        except Exception as e:
            errors.append(f"Invalid JSON in recipe {f.name}: {e}")
            continue

        # Check result
        result = recipe.get("result", {})
        res_id = result.get("id", "")
        if res_id:
            if ":" in res_id:
                ns, name = res_id.split(":", 1)
            else:
                ns, name = "terraforge_rpg", res_id

            if ns == "terraforge_rpg" and name not in registered_items and name not in registered_blocks:
                errors.append(f"Recipe {f.name} outputs unregistered item: {res_id}")
            elif ns == "minecraft" and name not in VANILLA_ITEMS:
                warnings.append(f"Recipe {f.name} outputs uncommon vanilla item: {res_id}")

        # Check ingredients (key map or ingredients array)
        ingredients = []
        if "key" in recipe:
            for k, ing in recipe["key"].items():
                if isinstance(ing, dict):
                    ingredients.append(ing)
                elif isinstance(ing, list):
                    ingredients.extend(ing)
        if "ingredients" in recipe:
            for ing in recipe["ingredients"]:
                if isinstance(ing, dict):
                    ingredients.append(ing)
                elif isinstance(ing, list):
                    ingredients.extend(ing)

        for ing in ingredients:
            item_ref = ing.get("item", "")
            tag_ref = ing.get("tag", "")
            if item_ref:
                if ":" in item_ref:
                    ins, iname = item_ref.split(":", 1)
                else:
                    ins, iname = "terraforge_rpg", item_ref

                if ins == "terraforge_rpg" and iname not in registered_items and iname not in registered_blocks:
                    errors.append(f"Recipe {f.name} requires unregistered ingredient: {item_ref}")
            # Tags are acceptable if formatted correctly
            if tag_ref and not (tag_ref.startswith("c:") or tag_ref.startswith("minecraft:") or tag_ref.startswith("terraforge_rpg:")):
                warnings.append(f"Recipe {f.name} uses unusual tag: {tag_ref}")

    return errors, warnings

def validate_models_and_textures(registered_items, registered_blocks):
    errors = []
    warnings = []

    # Check for absolute path leaks in all json files in assets
    for json_file in ASSETS_DIR.rglob("*.json"):
        with open(json_file, "r", encoding="utf-8", errors="ignore") as jf:
            content = jf.read()
            if "C:\\" in content or "c:/" in content or "C:/" in content:
                errors.append(f"Absolute path found in runtime asset: {json_file.relative_to(PROJECT_ROOT)}")

    # Check models in models/item/
    if MODELS_ITEM.exists():
        for m_file in MODELS_ITEM.glob("*.json"):
            item_name = m_file.stem
            try:
                with open(m_file, "r", encoding="utf-8") as jf:
                    data = json.load(jf)
            except Exception as e:
                errors.append(f"Malformed JSON in item model {m_file.name}: {e}")
                continue

            # Check if this item is registered
            if item_name not in registered_items and item_name not in registered_blocks:
                errors.append(f"Orphan model JSON without registered item in ModItems: {m_file.name}")

            # Check NeoForge OBJ references
            if data.get("loader") == "neoforge:obj":
                model_ref = data.get("model", "")
                if model_ref.startswith("terraforge_rpg:models/item/"):
                    obj_filename = model_ref.split("/")[-1]
                    obj_path = MODELS_ITEM / obj_filename
                    if not obj_path.exists():
                        errors.append(f"Referenced OBJ model does not exist: {obj_filename} in {m_file.name}")
                    else:
                        # Check OBJ content for absolute paths
                        with open(obj_path, "r", encoding="utf-8", errors="ignore") as of:
                            for line in of:
                                if line.startswith("mtllib ") and ("C:\\" in line or "C:/" in line):
                                    errors.append(f"Absolute mtllib path in OBJ: {obj_path.name}")

            # Check texture references
            textures = data.get("textures", {})
            for tex_key, tex_val in textures.items():
                if tex_val.startswith("terraforge_rpg:item/"):
                    tex_file = TEX_ITEM / f"{tex_val.split('/')[-1]}.png"
                    if not tex_file.exists():
                        errors.append(f"Missing texture referenced by {m_file.name}: {tex_val}")
                elif tex_val.startswith("terraforge_rpg:block/"):
                    tex_file = TEX_BLOCK / f"{tex_val.split('/')[-1]}.png"
                    if not tex_file.exists():
                        errors.append(f"Missing block texture referenced by {m_file.name}: {tex_val}")

    return errors, warnings

def validate_entity_models():
    """
    Checks entity models for blacklisted placeholder code when marked INTEGRATED_3D.
    """
    errors = []
    warnings = []

    # Map of entity -> expected non-CubeListBuilder status
    # If an entity is marked FINAL 3D, it must NOT use CubeListBuilder
    entity_model_dir = SRC_JAVA / "client/model"
    if not entity_model_dir.exists():
        return errors, warnings

    final_3d_models = {
        "EyeOfCthulhuModel.java": "models/entity/boss/eye_of_cthulhu_p1.obj",
        "MoonLordModel.java": "models/entity/boss/moon_lord.obj",
        "TheDestroyerModel.java": "models/entity/boss/the_destroyer.obj",
        "WallOfFleshModel.java": "models/entity/boss/wall_of_flesh.obj",
        "FaceMonsterModel.java": "models/entity/mob/face_monster.obj",
        "KingSlimeModel.java": "models/entity/boss/king_slime.obj",
        "DukeFishronModel.java": "models/entity/boss/duke_fishron.obj"
    }

    for model_filename, obj_rel_path in final_3d_models.items():
        model_file = entity_model_dir / model_filename
        if not model_file.exists():
            errors.append(f"Missing entity model class: {model_filename}")
            continue

        with open(model_file, "r", encoding="utf-8", errors="ignore") as f:
            code = f.read()

        # Disallow CubeListBuilder placeholder logic in final 3D models
        if "CubeListBuilder" in code and "net.minecraft.client.model.geom.builders.CubeListBuilder" not in code:
            pass # ignore comments
        lines = [line.strip() for line in code.splitlines() if not line.strip().startswith("*") and not line.strip().startswith("//")]
        active_code = "\n".join(lines)

        if "CubeListBuilder.create" in active_code or ".addBox(" in active_code:
            errors.append(f"Model {model_filename} is still using CubeListBuilder placeholder boxes!")

        if "GhastModel" in active_code:
            errors.append(f"Model {model_filename} is using GhastModel placeholder!")

        # Verify underlying 3D OBJ model asset exists
        obj_file = ASSETS_DIR / obj_rel_path
        if not obj_file.exists():
            errors.append(f"Entity model {model_filename} missing real 3D mesh asset: {obj_rel_path}")

    return errors, warnings

def validate_translations(registered_items, registered_blocks, registered_entities):
    errors = []
    warnings = []

    for lang in ["en_us.json", "pt_br.json"]:
        lang_file = LANG_DIR / lang
        if not lang_file.exists():
            errors.append(f"Missing translation file: {lang_file}")
            continue
        try:
            with open(lang_file, "r", encoding="utf-8") as f:
                lang_data = json.load(f)
        except Exception as e:
            errors.append(f"Malformed translation JSON in {lang}: {e}")
            continue

        for item in registered_items:
            key = f"item.terraforge_rpg.{item}"
            bkey = f"block.terraforge_rpg.{item}"
            if key not in lang_data and bkey not in lang_data:
                warnings.append(f"Missing translation for registered item {item} in {lang}")

        for entity in registered_entities:
            key = f"entity.terraforge_rpg.{entity}"
            if key not in lang_data:
                warnings.append(f"Missing translation for registered entity {entity} in {lang}")

    return errors, warnings

def main():
    print(f"=== TerraForge RPG Validator ===")
    print(f"Project Root: {PROJECT_ROOT}")

    registered_items, registered_blocks, registered_entities = extract_registries()
    print(f"Extracted Registries: {len(registered_items)} items, {len(registered_blocks)} blocks, {len(registered_entities)} entities")

    all_errors = []
    all_warnings = []

    # 1. Recipes
    r_err, r_warn = validate_recipes(registered_items, registered_blocks)
    all_errors.extend(r_err)
    all_warnings.extend(r_warn)

    # 2. Models & Textures
    m_err, m_warn = validate_models_and_textures(registered_items, registered_blocks)
    all_errors.extend(m_err)
    all_warnings.extend(m_warn)

    # 3. Entity Models
    e_err, e_warn = validate_entity_models()
    all_errors.extend(e_err)
    all_warnings.extend(e_warn)

    # 4. Translations
    t_err, t_warn = validate_translations(registered_items, registered_blocks, registered_entities)
    all_errors.extend(t_err)
    all_warnings.extend(t_warn)

    print("\n=== Validation Results ===")
    print(f"Total Errors: {len(all_errors)}")
    print(f"Total Warnings: {len(all_warnings)}")

    if all_errors:
        print("\nERRORS DETECTED:")
        for e in all_errors:
            print(f"  [ERROR] {e}")
        sys.exit(1)
    else:
        print("\nValidation PASSED without critical registry or asset errors.")
        if all_warnings:
            print("\nWARNINGS:")
            for w in all_warnings[:20]:
                print(f"  [WARN] {w}")
            if len(all_warnings) > 20:
                print(f"  ... and {len(all_warnings) - 20} more warnings.")

if __name__ == "__main__":
    main()
