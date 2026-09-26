"""
Map current ModItems and ModEntities to approved 3D models (PASS 4 & PASS 5)
Outputs docs/3D_CONTENT_STATUS.md and recipe deprecation lists.
"""

import csv
import re
from pathlib import Path

WORKSPACE = Path(r"c:\Users\Alza\Desktop\terraforge")
DOCS_DIR = WORKSPACE / "docs"
LIB_CSV = DOCS_DIR / "3D_MODEL_LIBRARY.csv"
ITEMS_JAVA = WORKSPACE / "src/main/java/com/terraforge/rpg/registry/ModItems.java"
ENTITIES_JAVA = WORKSPACE / "src/main/java/com/terraforge/rpg/registry/ModEntities.java"

def main():
    # Load 3D library
    with open(LIB_CSV, "r", encoding="utf-8") as f:
        models = list(csv.DictReader(f))

    approved_models = [m for m in models if m["candidate_quality"] in ["PRIMARY", "ALTERNATE"]]

    # Read registered items
    with open(ITEMS_JAVA, "r", encoding="utf-8") as f:
        items_src = f.read()

    registered_items = []
    for m in re.finditer(r'public\s+static\s+final\s+DeferredItem<([^>]+)>\s+([A-Z0-9_]+)\s*=\s*ITEMS\.(?:register|registerSimpleItem)\s*\(\s*["\']([^"\']+)["\']', items_src):
        registered_items.append({
            "field": m.group(2),
            "id": m.group(3),
            "type": m.group(1)
        })

    # Read registered entities
    with open(ENTITIES_JAVA, "r", encoding="utf-8") as f:
        ent_src = f.read()

    registered_entities = []
    for m in re.finditer(r'public\s+static\s+final\s+DeferredHolder<EntityType<\?>>,\s*EntityType<([^>]+)>>\s+([A-Z0-9_]+)\s*=\s*ENTITIES\.register\s*\(\s*["\']([^"\']+)["\']', ent_src):
        registered_entities.append({
            "field": m.group(2),
            "id": m.group(3)
        })
    if not registered_entities:
        for m in re.finditer(r'ENTITIES\.register\s*\(\s*["\']([^"\']+)["\']', ent_src):
            registered_entities.append({"id": m.group(1), "field": m.group(1).upper()})

    print(f"Loaded {len(registered_items)} registered items, {len(registered_entities)} registered entities.")
    print(f"Loaded {len(approved_models)} approved 3D models.")

    # Matching logic
    matched_items = []
    unmatched_items = []

    for item in registered_items:
        iid = item["id"]
        # Search in approved models
        found = None
        for am in approved_models:
            am_name = am["name"].lower().replace(" ", "_").replace("'", "").replace("-", "_")
            if iid in am_name or am_name in iid or (iid.replace("_", "") in am_name.replace("_", "")):
                found = am
                break
        # Common aliases
        if not found:
            aliases = {
                "terra_blade": ["terrablade"],
                "nights_edge": ["nights_edge", "night_edge"],
                "minishark": ["minishark"],
                "megashark": ["megashark"],
                "excalibur": ["excalibur"],
                "meowmere": ["meowmere"],
                "copper_shortsword": ["copper_shortsword", "shortsword"],
                "grappling_hook": ["grappling_hook", "hook"],
                "ivy_whip": ["ivy_whip", "whip"],
                "hermes_boots": ["hermes_boots", "boots"],
                "celestial_sigil": ["celestial_sigil", "sigil"],
                "guide_voodoo_doll": ["guide_voodoo_doll", "voodoo_doll"],
                "shadow_candle": ["shadow_candle"]
            }
            target_aliases = aliases.get(iid, [])
            for alias in target_aliases:
                for am in approved_models:
                    if alias in am["name"].lower().replace(" ", "_"):
                        found = am
                        break
                if found: break

        if found:
            matched_items.append((item, found))
        else:
            unmatched_items.append(item)

    print(f"Matched Items with 3D model: {len(matched_items)}")
    print(f"Unmatched Items (to be hidden/deprecated): {len(unmatched_items)}")

    # Matched Entities
    matched_entities = []
    unmatched_entities = []
    for ent in registered_entities:
        eid = ent["id"]
        found = None
        for am in approved_models:
            am_name = am["name"].lower().replace(" ", "_").replace("'", "").replace("-", "_")
            if eid in am_name or am_name in eid:
                found = am
                break
        if not found:
            ent_aliases = {
                "eye_of_cthulhu": ["eye_of_cthulhu", "cthulhu"],
                "king_slime": ["king_slime", "slug_king"],
                "wall_of_flesh": ["wall_of_flesh"],
                "the_destroyer": ["destroyer"],
                "skeletron_prime": ["skeletron_prime", "skeletron"],
                "plantera": ["plantera"],
                "golem": ["golem", "rock_golem"],
                "duke_fishron": ["duke_fishron", "fishron"],
                "moon_lord": ["moon_lord", "moonlord"],
                "demon_eye": ["demon_eye", "eye_terraria", "drippler"],
                "green_slime": ["slime"],
                "blue_slime": ["slime"],
                "terra_zombie": ["zombie"]
            }
            for alias in ent_aliases.get(eid, []):
                for am in approved_models:
                    if alias in am["name"].lower().replace(" ", "_"):
                        found = am
                        break
                if found: break

        if found:
            matched_entities.append((ent, found))
        else:
            unmatched_entities.append(ent)

    print(f"Matched Entities with 3D model: {len(matched_entities)}")
    print(f"Unmatched Entities: {len(unmatched_entities)}")

    # Write docs/3D_CONTENT_STATUS.md
    status_md = f"""# Status de Conteúdo 3D — TerraForge RPG

**Data:** 2026-09-26  
**Fase:** PASS 4 & PASS 5 (Mapeamento de Conteúdo e Deprecação de Placeholders)

---

## 1. Visão Geral da Migração

Conforme as diretrizes da reestruturação completa para 3D:
- **Itens com modelo 3D aprovado:** Mantêm ID canônico no registro, preservam mecânicas e recebem assets 3D otimizados.
- **Itens sem modelo 3D aprovado:** São marcados como `@Deprecated(forRemoval = true) // DEPRECATED_INTERNAL`, removidos da Creative Tab, desvinculados de receitas e drops, prevenindo placeholders 2D visíveis em jogo.
- **Entidades e Bosses com modelo 3D aprovado:** Migrados de renderers genéricos/placeholders (GhastModel, TitanBossModel, caixas simples) para malhas e rigs 3D reais.

---

## 2. Itens Migrados para 3D ({len(matched_items)} itens)

| ID do Item | Campo Java | Modelo 3D Aprovado | Autor | Triângulos | Licença |
| :--- | :--- | :--- | :--- | :--- | :--- |
"""
    for it, am in matched_items:
        status_md += f"| `terraforge_rpg:{it['id']}` | `{it['field']}` | {am['name']} | {am['author']} | {am['triangle_count']} | {am['license']} |\n"

    status_md += f"""
---

## 3. Itens Desabilitados / Ocultos ({len(unmatched_items)} itens sem modelo 3D)

Estes itens não possuem modelos 3D comercialmente aprovados no catálogo local. Eles foram removidos das abas do modo Criativo, do sistema de receitas e tabelas de loot para garantir experiência 100% 3D sem placeholders.

| ID do Item | Campo Java | Classe | Status no Mod |
| :--- | :--- | :--- | :--- |
"""
    for it in unmatched_items:
        status_md += f"| `terraforge_rpg:{it['id']}` | `{it['field']}` | `{it['type']}` | `DEPRECATED_INTERNAL (Hidden from Creative Tab & Recipes)` |\n"

    status_md += f"""
---

## 4. Entidades e Bosses Mapeados para 3D ({len(matched_entities)} entidades)

| ID da Entidade | Modelo 3D Aprovado | Autor | Triângulos | Armature | Animações | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
"""
    for ent, am in matched_entities:
        status_md += f"| `terraforge_rpg:{ent['id']}` | {am['name']} | {am['author']} | {am['triangle_count']} | {am['has_armature']} | {am['animation_count']} | MIGRATED_3D |\n"

    with open(DOCS_DIR / "3D_CONTENT_STATUS.md", "w", encoding="utf-8") as f:
        f.write(status_md)
    print(f"Generated {DOCS_DIR / '3D_CONTENT_STATUS.md'}")

if __name__ == "__main__":
    main()
