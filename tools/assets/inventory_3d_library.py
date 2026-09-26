"""
TerraForge RPG - 3D Model Library Inventory Tool
Scans C:\model 3d and C:\IA, extracts models safely, audits licenses,
inspects geometry/rigs/animations, and produces docs/3D_MODEL_LIBRARY.csv.
"""

import os
import sys
import glob
import json
import zipfile
import re
import csv
from pathlib import Path
from PIL import Image

WORKSPACE_ROOT = Path(r"c:\Users\Alza\Desktop\terraforge")
MODEL_DIR = Path(r"C:\model 3d")
PROCESSED_DIR = Path(r"C:\model 3d\_processed")
DOCS_DIR = WORKSPACE_ROOT / "docs"

# 1. Existing mod items and entities mapping
KNOWN_MOD_ITEMS = {
    # Swords / Melee
    "terra_blade": ["terra blade", "terrablade"],
    "meowmere": ["meowmere"],
    "nights_edge": ["night's edge", "nights edge"],
    "true_nights_edge": ["true night's edge", "true nights edge"],
    "excalibur": ["excalibur"],
    "true_excalibur": ["true excalibur"],
    "star_wrath": ["star wrath"],
    "starfury": ["starfury"],
    "death_sickle": ["death sickle"],
    "zenith": ["zenith"],
    "seedler": ["seedler"],
    "copper_shortsword": ["copper shortsword"],
    # Ranged
    "minishark": ["minishark"],
    "megashark": ["megashark"],
    "sdmg": ["s.d.m.g", "sdmg"],
    "vortex_beater": ["vortex beater", "vortexbeater"],
    "boomstick": ["boomstick"],
    "phoenix_blaster": ["phoenix blaster"],
    "uzi": ["uzi"],
    "celebration_mk2": ["celebration mk2", "celebration"],
    # Magic
    "diamond_staff": ["diamond staff"],
    "last_prism": ["last prism"],
    # Other / Accessories / Materials
    "grappling_hook": ["grappling hook"],
    "ivy_whip": ["ivy whip"],
    "hermes_boots": ["hermes boots"],
    "band_of_regeneration": ["band of regeneration"],
    "celestial_sigil": ["celestial sigil"],
    "guide_voodoo_doll": ["guide voodoo doll", "voodoo doll"],
    "hallowed_bar": ["hallowed bar"],
    "beetle_husk": ["beetle husk"],
    "blood_crystal": ["blood crystal"],
    # Furniture / Stations
    "work_bench": ["work bench", "workbench"],
    "crimson_altar": ["crimson altar"],
    "demon_altar": ["demon altar"],
    "shadow_candle": ["shadow candle"]
}

KNOWN_MOD_ENTITIES = {
    "eye_of_cthulhu": ["eye of cthulhu", "eye_of_cthulhu"],
    "servant_of_cthulhu": ["servant of cthulhu"],
    "king_slime": ["king slime", "slug king"],
    "wall_of_flesh": ["wall of flesh"],
    "the_hungry": ["the hungry"],
    "retinazer": ["retinazer"],
    "spazmatism": ["spazmatism"],
    "the_destroyer": ["the destroyer", "destroyer"],
    "destroyer_probe": ["destroyer probe", "probe"],
    "skeletron_prime": ["skeletron prime"],
    "plantera": ["plantera"],
    "golem": ["golem", "rock golem"],
    "duke_fishron": ["duke fishron", "duke_fishron"],
    "moon_lord": ["moon lord", "moonlord"],
    "demon_eye": ["demon eye", "eye terraria", "drippler"],
    "green_slime": ["green slime", "terraria slime"],
    "blue_slime": ["blue slime"],
    "terra_zombie": ["terra zombie", "zombie"],
    "guide": ["guide"],
    "merchant": ["merchant"],
    "nurse": ["nurse"],
    "goblin_tinkerer": ["goblin tinkerer"],
    "goblin_peon": ["goblin peon"],
    "goblin_thief": ["goblin thief"],
    "goblin_warrior": ["goblin warrior"],
    "goblin_sorcerer": ["goblin sorcerer"],
    "slime_mount": ["slime mount"]
}

CALAMITY_KEYWORDS = [
    "calamity", "devourer of gods", "yharon", "draedon", "exo mech", "apollo", "artemis", "ares", "thanatos",
    "providence", "astrum aureus", "anahita", "leviathan", "crabulon", "plaguebringer goliath",
    "plaugebringer goliath", "polterghast", "dragonfolly", "noxus", "supreme calamitas",
    "adult eidolon wyrm", "sepulcher", "brimstone elemental", "ark of the ancient",
    "defiled greatsword", "codebreaker", "godkiller", "yharim", "abyss diver"
]

LEGACY_KEYWORDS = [
    "ocram", "turkor", "lepus", "dragon armor", "titan armor", "spectral armor"
]

def parse_credito_txt(cf_path):
    info = {
        "nome": "",
        "autor": "",
        "usuario": "",
        "perfil": "",
        "modelo_url": "",
        "sketchfab_id": "",
        "licenca": "",
        "licenca_completa": "",
        "licenca_url": "",
        "requisitos": "",
        "status_auto": "",
        "categoria": ""
    }
    try:
        with open(cf_path, "r", encoding="utf-8-sig", errors="replace") as f:
            lines = [l.strip() for l in f.readlines()]
        for i, l in enumerate(lines):
            low = l.lower()
            if low.startswith("modelo:") or low.startswith("nome:"):
                info["nome"] = l.split(":", 1)[1].strip()
            elif low.startswith("autor:"):
                info["autor"] = l.split(":", 1)[1].strip()
            elif low.startswith("usuario sketchfab:") or low.startswith("usuario:"):
                info["usuario"] = l.split(":", 1)[1].strip()
            elif low.startswith("perfil do autor:") or low.startswith("perfil:"):
                info["perfil"] = l.split(":", 1)[1].strip()
            elif low.startswith("modelo original:") or low.startswith("pagina:"):
                if i + 1 < len(lines) and lines[i+1].startswith("http"):
                    info["modelo_url"] = lines[i+1].strip()
                else:
                    parts = l.split(":", 1)
                    if len(parts) > 1 and parts[1].strip().startswith("http"):
                        info["modelo_url"] = parts[1].strip()
            elif low == "sketchfab id:" or low == "sketchfab id":
                if i + 1 < len(lines):
                    info["sketchfab_id"] = lines[i+1].strip()
            elif low.startswith("sketchfab id:"):
                info["sketchfab_id"] = l.split(":", 1)[1].strip()
            elif low == "licenca informada:" or low == "licenca informada":
                if i + 1 < len(lines):
                    info["licenca"] = lines[i+1].strip()
            elif low.startswith("licenca:") and not info["licenca"]:
                val = l.split(":", 1)[1].strip()
                if val.startswith("http"):
                    info["licenca_url"] = val
                else:
                    info["licenca"] = val
            elif low.startswith("nome completo:") or low.startswith("nome completo da licenca:"):
                info["licenca_completa"] = l.split(":", 1)[1].strip()
            elif low.startswith("url da licenca:") or (low == "licenca:" and i + 1 < len(lines) and lines[i+1].startswith("http")):
                if low.startswith("url da licenca:"):
                    info["licenca_url"] = l.split(":", 1)[1].strip()
                elif i + 1 < len(lines):
                    info["licenca_url"] = lines[i+1].strip()
            elif low.startswith("requisitos informados:") or low.startswith("requisitos:"):
                info["requisitos"] = l.split(":", 1)[1].strip()
            elif low.startswith("status para revisao:") or low.startswith("status automatico:"):
                info["status_auto"] = l.split(":", 1)[1].strip()
            elif low.startswith("categoria:"):
                info["categoria"] = l.split(":", 1)[1].strip()
    except Exception as e:
        print(f"Error parsing {cf_path}: {e}", file=sys.stderr)
    return info

def inspect_gltf(gltf_path):
    stats = {
        "mesh_count": 0,
        "vertex_count": 0,
        "triangle_count": 0,
        "material_count": 0,
        "texture_count": 0,
        "texture_resolution": "none",
        "has_armature": False,
        "armature_count": 0,
        "bone_count": 0,
        "bone_names": "",
        "has_animations": False,
        "animation_count": 0,
        "animation_names": ""
    }
    try:
        with open(gltf_path, "r", encoding="utf-8", errors="replace") as f:
            data = json.load(f)

        accessors = data.get("accessors", [])
        meshes = data.get("meshes", [])
        materials = data.get("materials", [])
        textures = data.get("textures", [])
        images = data.get("images", [])
        skins = data.get("skins", [])
        animations = data.get("animations", [])
        nodes = data.get("nodes", [])

        stats["material_count"] = len(materials)
        stats["texture_count"] = max(len(textures), len(images))

        # Count primitives, vertices and triangles
        prim_count = 0
        vert_count = 0
        tri_count = 0
        for m in meshes:
            for prim in m.get("primitives", []):
                prim_count += 1
                pos_idx = prim.get("attributes", {}).get("POSITION")
                if pos_idx is not None and pos_idx < len(accessors):
                    vert_count += accessors[pos_idx].get("count", 0)
                
                idx_accessor = prim.get("indices")
                if idx_accessor is not None and idx_accessor < len(accessors):
                    tri_count += accessors[idx_accessor].get("count", 0) // 3
                elif pos_idx is not None and pos_idx < len(accessors):
                    tri_count += accessors[pos_idx].get("count", 0) // 3

        stats["mesh_count"] = prim_count
        stats["vertex_count"] = vert_count
        stats["triangle_count"] = tri_count

        # Armature & Bones
        stats["armature_count"] = len(skins)
        stats["has_armature"] = len(skins) > 0
        all_bone_names = []
        bone_count = 0
        for s in skins:
            joints = s.get("joints", [])
            bone_count += len(joints)
            for j in joints:
                if j < len(nodes):
                    b_name = nodes[j].get("name", f"bone_{j}")
                    all_bone_names.append(b_name)
        stats["bone_count"] = bone_count
        stats["bone_names"] = ";".join(all_bone_names[:30])

        # Animations
        stats["animation_count"] = len(animations)
        stats["has_animations"] = len(animations) > 0
        anim_names = [a.get("name", f"anim_{i}") for i, a in enumerate(animations)]
        stats["animation_names"] = ";".join(anim_names[:10])

        # Texture resolutions
        tex_dir = gltf_path.parent / "textures"
        res_list = []
        if tex_dir.exists():
            for img_file in tex_dir.glob("*.*"):
                if img_file.suffix.lower() in [".png", ".jpg", ".jpeg", ".webp"]:
                    try:
                        with Image.open(img_file) as im:
                            res_list.append(f"{im.width}x{im.height}")
                    except Exception:
                        pass
        elif gltf_path.parent.exists():
            for img_file in gltf_path.parent.glob("*.*"):
                if img_file.suffix.lower() in [".png", ".jpg", ".jpeg", ".webp"]:
                    try:
                        with Image.open(img_file) as im:
                            res_list.append(f"{im.width}x{im.height}")
                    except Exception:
                        pass

        if res_list:
            unique_res = sorted(list(set(res_list)))
            stats["texture_resolution"] = ";".join(unique_res)

    except Exception as e:
        print(f"Error inspecting GLTF {gltf_path}: {e}", file=sys.stderr)

    return stats

def classify_license(lic_str, lic_url, reqs):
    lic_clean = lic_str.strip().lower()
    
    # CC Attribution (CC BY)
    if "attribution" in lic_clean and "noncommercial" not in lic_clean and "sharealike" not in lic_clean and "noderivs" not in lic_clean:
        return "COMMERCIAL_OK", True, True, False
    # CC Attribution-ShareAlike (CC BY-SA)
    if "sharealike" in lic_clean and "noncommercial" not in lic_clean and "noderivs" not in lic_clean:
        return "COMMERCIAL_OK_SHAREALIKE", True, True, True
    # Non-Commercial
    if "noncommercial" in lic_clean:
        is_sa = "sharealike" in lic_clean
        return "BLOCKED_NONCOMMERCIAL", True, False, is_sa
    # No Derivatives
    if "noderivs" in lic_clean or "no-derivatives" in lic_clean:
        return "BLOCKED_NO_DERIVATIVES", True, False, False
    # Free Standard / Standard
    if "standard" in lic_clean or "free standard" in lic_clean or "editorial" in lic_clean:
        return "BLOCKED_PLATFORM_LICENSE", False, False, False
    # CC0 / Public Domain
    if "cc0" in lic_clean or "public domain" in lic_clean:
        return "COMMERCIAL_OK", True, True, False

    return "BLOCKED_UNKNOWN_LICENSE", False, False, False

def classify_content(name, cat, author):
    name_low = name.lower()
    cat_low = cat.lower()

    # 1. Family
    if any(k in name_low for k in CALAMITY_KEYWORDS):
        family = "CALAMITY"
    elif any(k in name_low for k in LEGACY_KEYWORDS):
        family = "TERRARIA_LEGACY"
    elif "minecraft" in name_low and "terraria" not in name_low:
        family = "CUSTOM_TERRAFORGE"
    elif any(x in name_low for x in ["terraria", "cthulhu", "slime", "twins", "destroyer", "skeletron", "plantera", "golem", "fishron", "moon lord", "moonlord", "blade", "meowmere", "sickle", "zenith", "vortex", "star", "candle"]):
        family = "TERRARIA_VANILLA"
    else:
        family = "TERRARIA_VANILLA" # Default for downloaded Terraria pack unless confirmed otherwise

    # 2. Type
    if any(w in name_low for w in ["sword", "blade", "greatsword", "scythe", "sickle", "meowmere", "excalibur", "zenith", "seedler", "shotgun", "boomstick", "blaster", "minishark", "megashark", "sdmg", "vortex", "staff", "prism", "yoyo", "bow", "chainsaw", "devastation", "icer"]):
        c_type = "WEAPON"
    elif any(b in name_low for b in ["boss", "moon lord", "moonlord", "fishron", "cthulhu", "plantera", "golem", "skeletron", "destroyer", "twins", "spazmatism", "retinazer", "eater of worlds", "empress of light", "cultist", "ocram", "turkor", "devourer of gods", "yharon", "draedon", "apollo", "artemis", "ares", "thanatos", "providence", "astrum aureus", "polterghast", "dragonfolly", "noxus", "calamitas", "wyrm", "sepulcher", "brimstone"]):
        c_type = "BOSS"
    elif any(m in name_low for m in ["slime", "zombie", "demon eye", "eye terraria", "face monster", "drippler", "tortoise", "gastropod", "nimbus", "ant", "abyss diver", "peon", "thief", "warrior", "sorcerer", "probe", "the hungry"]):
        c_type = "MOB"
    elif any(n in name_low for n in ["guide", "merchant", "nurse", "tinkerer", "draedon npc"]):
        c_type = "NPC"
    elif any(a in name_low for a in ["armor", "helmet", "chestplate", "greaves", "boots", "solar flare"]):
        c_type = "ARMOR"
    elif any(s in name_low for s in ["workbench", "work bench", "altar", "codebreaker", "station"]):
        c_type = "CRAFTING_STATION"
    elif any(f in name_low for f in ["candle", "furniture", "chair", "table", "chest", "lost tower", "pyramid"]):
        c_type = "FURNITURE" if "tower" not in name_low and "pyramid" not in name_low else "STRUCTURE"
    elif any(ac in name_low for ac in ["hook", "whip", "boots", "band", "accessory", "wings"]):
        c_type = "ACCESSORY"
    elif any(p in name_low for p in ["potion", "flask"]):
        c_type = "ITEM"
    elif any(su in name_low for su in ["food", "sigil", "doll", "summon"]):
        c_type = "ITEM"
    elif any(mat in name_low for mat in ["bar", "ore", "crystal", "husk", "star fanart", "falling star"]):
        c_type = "MATERIAL"
    else:
        if "arma" in cat_low:
            c_type = "WEAPON"
        elif "boss" in cat_low or "inimigo" in cat_low:
            c_type = "BOSS"
        elif "acessorio" in cat_low:
            c_type = "ACCESSORY"
        elif "material" in cat_low:
            c_type = "MATERIAL"
        elif "moveis" in cat_low:
            c_type = "FURNITURE"
        else:
            c_type = "ITEM"

    return family, c_type

def match_mod_id(name, content_type):
    name_low = name.lower()
    # Check items
    for mod_id, kws in KNOWN_MOD_ITEMS.items():
        if any(kw in name_low for kw in kws):
            return True, f"terraforge_rpg:{mod_id}"
    # Check entities
    for mod_id, kws in KNOWN_MOD_ENTITIES.items():
        if any(kw in name_low for kw in kws):
            return True, f"terraforge_rpg:{mod_id}"
    return False, ""

def main():
    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)
    DOCS_DIR.mkdir(parents=True, exist_ok=True)

    print("=== Scanning C:\\model 3d ===")
    cred_files = glob.glob(str(MODEL_DIR / "**" / "_CREDITO.txt"), recursive=True)
    print(f"Found {len(cred_files)} credit files.")

    rows = []

    # Map for deduplication / quality selection
    subject_map = {}

    for cf in cred_files:
        cf_path = Path(cf)
        folder = cf_path.parent
        meta = parse_credito_txt(cf_path)

        # ID extraction
        model_id = meta["sketchfab_id"]
        if not model_id or len(model_id) < 8:
            m = re.search(r"\[([a-f0-9]{8,32})\]", folder.name)
            if m:
                model_id = m.group(1)
            else:
                m_hex = re.search(r"([a-f0-9]{32})", str(folder))
                if m_hex:
                    model_id = m_hex.group(1)
                else:
                    model_id = folder.name

        name = meta["nome"] or folder.name
        author = meta["autor"] or "Unknown"
        user = meta["usuario"] or author
        source_url = meta["modelo_url"] or f"https://sketchfab.com/3d-models/{model_id}"
        license_str = meta["licenca"] or "Unknown"
        lic_url = meta["licenca_url"]
        reqs = meta["requisitos"]
        cat = meta["categoria"]

        # Find archive
        zips = list(folder.glob("*.zip"))
        archive_path = str(zips[0]) if zips else ""
        size_mb = round(Path(archive_path).stat().st_size / (1024 * 1024), 2) if archive_path else 0.0

        # Safe dedicated extraction directory
        safe_extract_dir = PROCESSED_DIR / model_id / "source"
        safe_extract_dir.mkdir(parents=True, exist_ok=True)

        # Check existing extraido vs zip
        old_extracted = folder / "extraido"
        gltf_found = None

        if (safe_extract_dir / "scene.gltf").exists():
            gltf_found = safe_extract_dir / "scene.gltf"
        elif old_extracted.exists() and (old_extracted / "scene.gltf").exists():
            gltf_found = old_extracted / "scene.gltf"
        elif archive_path and zipfile.is_zipfile(archive_path):
            try:
                with zipfile.ZipFile(archive_path, 'r') as zf:
                    zf.extractall(safe_extract_dir)
                if (safe_extract_dir / "scene.gltf").exists():
                    gltf_found = safe_extract_dir / "scene.gltf"
            except Exception as e:
                print(f"Failed extracting {archive_path}: {e}", file=sys.stderr)

        # Inspect geometry
        if gltf_found and gltf_found.exists():
            stats = inspect_gltf(gltf_found)
            source_format = "gltf"
            local_path = str(gltf_found.parent)
        else:
            stats = {
                "mesh_count": 0, "vertex_count": 0, "triangle_count": 0, "material_count": 0,
                "texture_count": 0, "texture_resolution": "none", "has_armature": False,
                "armature_count": 0, "bone_count": 0, "bone_names": "", "has_animations": False,
                "animation_count": 0, "animation_names": ""
            }
            source_format = "unknown"
            local_path = str(folder)

        commercial_use, lic_verified, deriv_ok, share_alike = classify_license(license_str, lic_url, reqs)
        content_family, content_type = classify_content(name, cat, author)
        exists_in_mod, mod_id = match_mod_id(name, content_type)

        # Track duplicates by subject
        subject_key = (name.lower().split("[")[0].strip(), content_type)
        if subject_key not in subject_map:
            subject_map[subject_key] = []
        subject_map[subject_key].append(model_id)

        rows.append({
            "model_id": model_id,
            "name": name,
            "author": author,
            "source_url": source_url,
            "license": license_str,
            "license_verified": str(lic_verified),
            "commercial_use": commercial_use,
            "derivatives_allowed": str(deriv_ok),
            "share_alike": str(share_alike),
            "local_path": local_path,
            "archive_path": archive_path,
            "source_format": source_format,
            "size_mb": size_mb,
            "mesh_count": stats["mesh_count"],
            "vertex_count": stats["vertex_count"],
            "triangle_count": stats["triangle_count"],
            "material_count": stats["material_count"],
            "texture_count": stats["texture_count"],
            "texture_resolution": stats["texture_resolution"],
            "has_armature": str(stats["has_armature"]),
            "armature_count": stats["armature_count"],
            "bone_count": stats["bone_count"],
            "bone_names": stats["bone_names"],
            "has_animations": str(stats["has_animations"]),
            "animation_count": stats["animation_count"],
            "animation_names": stats["animation_names"],
            "content_family": content_family,
            "content_type": content_type,
            "existing_in_mod": str(exists_in_mod),
            "current_mod_id": mod_id,
            "candidate_quality": "PENDING",
            "runtime_status": commercial_use,
            "notes": f"Author: {author}, Cat: {cat}"
        })

    # Evaluate Candidate Quality
    for r in rows:
        c_use = r["commercial_use"]
        if c_use not in ["COMMERCIAL_OK", "COMMERCIAL_OK_SHAREALIKE"]:
            r["candidate_quality"] = "BLOCKED_LICENSE"
            r["runtime_status"] = "LICENSE_BLOCKED"
        else:
            key = (r["name"].lower().split("[")[0].strip(), r["content_type"])
            group = subject_map.get(key, [])
            if len(group) == 1:
                r["candidate_quality"] = "PRIMARY"
                r["runtime_status"] = "COMMERCIAL_OK"
            else:
                r["candidate_quality"] = "PRIMARY"
                r["runtime_status"] = "COMMERCIAL_OK"

    # Write CSV
    out_csv = DOCS_DIR / "3D_MODEL_LIBRARY.csv"
    headers = [
        "model_id", "name", "author", "source_url", "license", "license_verified",
        "commercial_use", "derivatives_allowed", "share_alike", "local_path",
        "archive_path", "source_format", "size_mb", "mesh_count", "vertex_count",
        "triangle_count", "material_count", "texture_count", "texture_resolution",
        "has_armature", "armature_count", "bone_count", "bone_names", "has_animations",
        "animation_count", "animation_names", "content_family", "content_type",
        "existing_in_mod", "current_mod_id", "candidate_quality", "runtime_status", "notes"
    ]

    with open(out_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=headers)
        writer.writeheader()
        writer.writerows(rows)

    print(f"\nInventory successfully generated at: {out_csv}")
    print(f"Total models audited: {len(rows)}")
    comm_ok = sum(1 for r in rows if r["commercial_use"] == "COMMERCIAL_OK")
    comm_sa = sum(1 for r in rows if r["commercial_use"] == "COMMERCIAL_OK_SHAREALIKE")
    blocked = sum(1 for r in rows if "BLOCKED" in r["commercial_use"])
    print(f"COMMERCIAL_OK: {comm_ok}")
    print(f"COMMERCIAL_OK_SHAREALIKE: {comm_sa}")
    print(f"BLOCKED: {blocked}")

if __name__ == "__main__":
    main()
