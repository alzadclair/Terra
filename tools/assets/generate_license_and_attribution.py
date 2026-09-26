"""
Generate ASSET_LICENSE_MATRIX.md and ASSET_ATTRIBUTION.md
and perform Subject Deduplication & Quality Selection (PASS 2 & PASS 3)
"""

import csv
from pathlib import Path
from collections import defaultdict

DOCS_DIR = Path(r"c:\Users\Alza\Desktop\terraforge\docs")
LIB_CSV = DOCS_DIR / "3D_MODEL_LIBRARY.csv"

def main():
    with open(LIB_CSV, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        rows = list(reader)

    print(f"Loaded {len(rows)} models from {LIB_CSV}")

    # Group by subject / content type for deduplication (PASS 3)
    subjects = defaultdict(list)
    for r in rows:
        # Normalize subject name
        name_clean = r["name"].lower()
        for prefix in ["terraria", "calamity mod", "calamity", "sketchup", "fanart", "3d model", "lowpoly", "voxel", "from"]:
            name_clean = name_clean.replace(prefix, "")
        name_clean = name_clean.split("(")[0].split("[")[0].strip(" -_")
        subjects[(name_clean, r["content_type"])].append(r)

    # PASS 3: Deduplication and Quality Selection
    for (subj, c_type), items in subjects.items():
        if len(items) == 1:
            item = items[0]
            if item["commercial_use"] in ["COMMERCIAL_OK", "COMMERCIAL_OK_SHAREALIKE"]:
                item["candidate_quality"] = "PRIMARY"
                item["runtime_status"] = "COMMERCIAL_OK"
            else:
                item["candidate_quality"] = "BLOCKED_LICENSE"
                item["runtime_status"] = "LICENSE_BLOCKED"
        else:
            # Multiple candidates: sort by:
            # 1. License: COMMERCIAL_OK > COMMERCIAL_OK_SHAREALIKE > BLOCKED
            # 2. Has animation / armature
            # 3. Reasonable polycount (not 1M+ tris, ideally between 500 and 40,000 tris)
            def sort_key(it):
                lic_score = 3 if it["commercial_use"] == "COMMERCIAL_OK" else (2 if it["commercial_use"] == "COMMERCIAL_OK_SHAREALIKE" else 0)
                anim_score = 2 if it["has_animations"] == "True" else (1 if it["has_armature"] == "True" else 0)
                tris = int(it["triangle_count"])
                # Budget penalty if excessively high (> 100k) or 0
                poly_score = 0
                if 200 <= tris <= 50000:
                    poly_score = 3
                elif 50000 < tris <= 100000:
                    poly_score = 2
                elif tris > 100000:
                    poly_score = 1
                return (lic_score, anim_score, poly_score, -tris)

            sorted_items = sorted(items, key=sort_key, reverse=True)
            primary_elected = False
            for idx, it in enumerate(sorted_items):
                if it["commercial_use"] in ["COMMERCIAL_OK", "COMMERCIAL_OK_SHAREALIKE"]:
                    if not primary_elected:
                        it["candidate_quality"] = "PRIMARY"
                        it["runtime_status"] = "COMMERCIAL_OK"
                        primary_elected = True
                    else:
                        it["candidate_quality"] = "ALTERNATE"
                        it["runtime_status"] = "ANALYZED"
                else:
                    it["candidate_quality"] = "BLOCKED_LICENSE"
                    it["runtime_status"] = "LICENSE_BLOCKED"

    # Save updated CSV
    with open(LIB_CSV, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys())
        writer.writeheader()
        writer.writerows(rows)
    print(f"Updated {LIB_CSV} with deduplication candidate qualities.")

    # PASS 2: Generate ASSET_LICENSE_MATRIX.md
    comm_ok_rows = [r for r in rows if r["commercial_use"] == "COMMERCIAL_OK"]
    comm_sa_rows = [r for r in rows if r["commercial_use"] == "COMMERCIAL_OK_SHAREALIKE"]
    blocked_nc_rows = [r for r in rows if r["commercial_use"] == "BLOCKED_NONCOMMERCIAL"]
    blocked_plat_rows = [r for r in rows if r["commercial_use"] == "BLOCKED_PLATFORM_LICENSE"]
    blocked_other_rows = [r for r in rows if r["commercial_use"] not in ["COMMERCIAL_OK", "COMMERCIAL_OK_SHAREALIKE", "BLOCKED_NONCOMMERCIAL", "BLOCKED_PLATFORM_LICENSE"]]

    matrix_md = f"""# Matriz de Licenciamento de Assets 3D — TerraForge RPG

**Versão da Matriz:** 1.0.0 (MIGRAÇÃO TOTAL PARA CONTEÚDO 3D)  
**Total de Modelos Auditados:** {len(rows)}  
**Data da Auditoria:** 2026-09-26  

---

## 1. Princípios e Regras de Licenciamento Comercial

O TerraForge RPG adota uma política rigorosa de propriedade intelectual e conformidade de licenciamento para garantir que o mod seja comercialmente seguro e compatível com futuras distribuições monetizadas:

1. **Aprovados Automaticamente:**
   - `CC0 / Public Domain`: Livre para qualquer uso sem requisitos de atribuição.
   - `CC BY (2.0 / 3.0 / 4.0)`: Uso comercial permitido, modificações permitidas, atribuição obrigatória.
2. **Aprovados sob Condições Específicas:**
   - `CC BY-SA (ShareAlike)`: Uso comercial e modificações permitidos. Qualquer asset derivado e otimizado deve manter a mesma licença ShareAlike e ser devidamente documentado.
3. **Estritamente Bloqueados do Runtime:**
   - `CC BY-NC / CC BY-NC-SA / CC BY-NC-ND`: Proibido uso comercial. Totalmente excluídos do pacote final.
   - `CC BY-ND / NoDerivatives`: Não permite criação de obras derivadas (incompatível com decimação, retopologia, rigs e conversão de formato).
   - `Free Standard / Standard / Editorial`: Licenças de plataforma restritivas ou não comerciais. Bloqueados até revisão jurídica explícita.

### Distinção Crítica entre Licença de Modelo e Propriedade Intelectual (IP)
> [!IMPORTANT]
> Uma licença Creative Commons (CC BY) concedida pelo autor do modelo 3D fan-made cobre **exclusivamente os direitos autorais sobre a malha poligonal, rig e texturas criadas pelo artista**. Ela **não** transfere direitos de marca registrada ou propriedade intelectual sobre os personagens, conceitos e designs originais de **Terraria** (de propriedade da Re-Logic) ou do **Calamity Mod** (de propriedade da equipe Calamity).
> O mod TerraForge RPG opera sob as diretrizes de conteúdo para fãs e mods de videogame, mantendo conformidade dual: respeito à propriedade intelectual original e respeito rigoroso às licenças dos criadores dos modelos poligonais.

---

## 2. Resumo Quantitativo da Auditoria

| Status de Licenciamento | Quantidade | Percentual | Ação no Projeto |
| :--- | :--- | :--- | :--- |
| **COMMERCIAL_OK (CC BY / CC0)** | **{len(comm_ok_rows)}** | **{len(comm_ok_rows)/len(rows)*100:.1f}%** | **Aprovado para Otimização e Runtime** |
| **COMMERCIAL_OK_SHAREALIKE (CC BY-SA)** | **{len(comm_sa_rows)}** | **{len(comm_sa_rows)/len(rows)*100:.1f}%** | **Aprovado com Atribuição ShareAlike** |
| **BLOCKED_PLATFORM_LICENSE (Free Standard)** | **{len(blocked_plat_rows)}** | **{len(blocked_plat_rows)/len(rows)*100:.1f}%** | **Bloqueado do Runtime (Revisão)** |
| **BLOCKED_NONCOMMERCIAL (CC BY-NC)** | **{len(blocked_nc_rows)}** | **{len(blocked_nc_rows)/len(rows)*100:.1f}%** | **Bloqueado do Runtime (Incompatível)** |
| **BLOCKED_OTHER (NoDerivs / Desconhecido)** | **{len(blocked_other_rows)}** | **{len(blocked_other_rows)/len(rows)*100:.1f}%** | **Bloqueado do Runtime** |
| **TOTAL AUDITADO** | **{len(rows)}** | **100.0%** | |

---

## 3. Tabela Detalhada de Modelos Aprovados (`COMMERCIAL_OK` / `COMMERCIAL_OK_SHAREALIKE`)

| ID / Hash | Nome do Modelo | Autor | Licença | Família | Tipo | Vértices | Triângulos | Armature | Animações | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
"""
    for r in comm_ok_rows + comm_sa_rows:
        matrix_md += f"| `{r['model_id'][:8]}` | {r['name']} | {r['author']} | {r['license']} | {r['content_family']} | {r['content_type']} | {r['vertex_count']} | {r['triangle_count']} | {r['has_armature']} | {r['animation_count']} | {r['candidate_quality']} |\n"

    matrix_md += f"""
---

## 4. Tabela de Modelos Bloqueados (`BLOCKED_*`)

| ID / Hash | Nome do Modelo | Autor | Licença Declarada | Motivo do Bloqueio |
| :--- | :--- | :--- | :--- | :--- |
"""
    for r in blocked_plat_rows + blocked_nc_rows + blocked_other_rows:
        matrix_md += f"| `{r['model_id'][:8]}` | {r['name']} | {r['author']} | {r['license']} | {r['commercial_use']} |\n"

    with open(DOCS_DIR / "ASSET_LICENSE_MATRIX.md", "w", encoding="utf-8") as f:
        f.write(matrix_md)
    print(f"Generated {DOCS_DIR / 'ASSET_LICENSE_MATRIX.md'}")

    # Generate ASSET_ATTRIBUTION.md
    attr_md = f"""# Manifesto de Atribuição de Assets 3D — TerraForge RPG

Este documento registra os créditos completos de todos os modelos 3D utilizados no mod **TerraForge RPG**, em estrita conformidade com os termos das licenças Creative Commons Attribution (CC BY 4.0 / 3.0 / 2.0) e Creative Commons Attribution-ShareAlike (CC BY-SA 4.0).

Cada asset listado abaixo é atribuído ao seu autor original, com links diretos para a fonte original e identificação das adaptações técnicas realizadas para o runtime do Minecraft NeoForge 1.21.1.

---

## Assets Aprovados e Atribuições Oficiais

"""
    approved = [r for r in rows if r["candidate_quality"] == "PRIMARY"]
    for idx, a in enumerate(approved, 1):
        attr_md += f"""### {idx}. {a['name']}
- **Autor:** {a['author']}
- **Model ID (Sketchfab):** `{a['model_id']}`
- **URL Original:** [{a['source_url']}]({a['source_url']})
- **Licença:** {a['license']} ([Termos Oficiais](https://creativecommons.org/licenses/by/4.0/))
- **Família de Conteúdo:** {a['content_family']}
- **Tipo de Conteúdo:** {a['content_type']}
- **Polígonos (Original):** {a['triangle_count']} triângulos, {a['vertex_count']} vértices
- **Rig / Animações:** {a['armature_count']} armatures ({a['bone_count']} bones), {a['animation_count']} animações ({a['animation_names'] or 'N/A'})
- **Modificações Realizadas:**
  - Extração do arquivo source GLTF 2.0
  - Normalização de escala para unidade do Minecraft (1 bloco = 1 metro)
  - Otimização de UVs e materiais para NeoForge Render Pipeline
  - Decimação / retopologia proporcional quando exigido pelo budget
- **Identificador no Mod:** `{a['current_mod_id'] or 'A ser atribuído na migração'}`

---
"""

    with open(DOCS_DIR / "ASSET_ATTRIBUTION.md", "w", encoding="utf-8") as f:
        f.write(attr_md)
    print(f"Generated {DOCS_DIR / 'ASSET_ATTRIBUTION.md'}")

if __name__ == "__main__":
    main()
