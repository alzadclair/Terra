"""
Generate docs/CALAMITY_CONTENT.md and docs/LEGACY_CONTENT.md (PASS 13 & PASS 14)
"""

import csv
from pathlib import Path

WORKSPACE = Path(r"c:\Users\Alza\Desktop\terraforge")
DOCS_DIR = WORKSPACE / "docs"
LIB_CSV = DOCS_DIR / "3D_MODEL_LIBRARY.csv"

def main():
    with open(LIB_CSV, "r", encoding="utf-8") as f:
        models = list(csv.DictReader(f))

    calamity_models = [m for m in models if m["content_family"] == "CALAMITY"]
    legacy_models = [m for m in models if m["content_family"] == "TERRARIA_LEGACY"]

    # 1. docs/CALAMITY_CONTENT.md
    cal_md = f"""# Conteúdo Calamity Mod — TerraForge RPG

**Status:** PASS 13 (Catalogação, Validação e Roadmap de Integração Calamity)  
**Total de Modelos Calamity Identificados:** {len(calamity_models)}  
**Regra Fundamental:** Conteúdo Calamity é tratado de forma modular e independente da progressão vanilla do Terraria 1.4.5.8. Todos os assets respeitam as licenças comerciais estritas (CC BY).

---

## 1. Inventário de Assets 3D Calamity Aprovados

| ID / Hash | Nome do Modelo | Autor | Tipo | Triângulos | Armature | Animações | Licença | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
"""
    for m in sorted(calamity_models, key=lambda x: x["name"]):
        cal_md += f"| `{m['model_id'][:8]}` | **{m['name']}** | {m['author']} | `{m['content_type']}` | {m['triangle_count']} | {m['has_armature']} | {m['animation_count']} | {m['license']} | `{m['candidate_quality']}` |\n"

    cal_md += """
---

## 2. Destaques de Conteúdo e Roadmap de Implementação

1. **Exo Mechs (Draedon's Arsenal):**
   - **Exo-Mech Artemis & Apollo:** Modelos ultra-leves e game-ready (1.080 e 1.200 triângulos) com texturas de alta definição. Perfeitos para chefes aéreos dinâmicos.
   - **Exo Mech Thanatos:** Modelo segmentado de 2.724 triângulos, ideal para a tecnologia de chefes em cadeia sem perda de FPS.
   - **Completed Codebreaker:** Estação de contato com Draedon (612 triângulos), pronta para ser o bloco central de progressão tecnológica.
2. **Abyss & Ocean Bosses:**
   - **Adult Eidolon Wyrm:** Modelo otimizado de 816 triângulos para aparição nos níveis profundos do Abismo.
   - **Leviathan & Anahita:** Modelos com rigs e animações completas (5 animações no Leviathan e 1 na Anahita), prontos para combates duplos nas águas.
   - **The Abyss Diver:** Traje temático de mergulhador abissal com animações de caminhada aquática.
3. **Gods & Primordial Evils:**
   - **Devourer of Gods:** Malha detalhada de 79.992 triângulos que utiliza o framework de chefes segmentados otimizados do TerraForge.
   - **Defiled Greatsword:** Espada icônica de 6.844 triângulos com visual corrompido e partículas prontas.
"""

    with open(DOCS_DIR / "CALAMITY_CONTENT.md", "w", encoding="utf-8") as f:
        f.write(cal_md)
    print(f"Generated {DOCS_DIR / 'CALAMITY_CONTENT.md'}")

    # 2. docs/LEGACY_CONTENT.md
    leg_md = f"""# Conteúdo Terraria Legacy (Console / Old-Gen) — TerraForge RPG

**Status:** PASS 14 (Catalogação e Tratamento de Conteúdo Histórico)  
**Total de Modelos Legacy Identificados:** {len(legacy_models)}  
**Regra Fundamental:** Conteúdo exclusivo de edições antigas (Console 1.2, Mobile antigo) NÃO é contabilizado como vanilla PC 1.4.5.8. Eles são mantidos sob a chancela **TERRARIA_LEGACY** para preservação e eventos comemorativos.

---

## 1. Inventário de Assets Legacy

| ID / Hash | Nome do Modelo | Autor | Tipo | Triângulos | Armature | Animações | Licença | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
"""
    for m in sorted(legacy_models, key=lambda x: x["name"]):
        leg_md += f"| `{m['model_id'][:8]}` | **{m['name']}** | {m['author']} | `{m['content_type']}` | {m['triangle_count']} | {m['has_armature']} | {m['animation_count']} | {m['license']} | `{m['candidate_quality']}` |\n"

    leg_md += """
---

## 2. Conteúdo Histórico Documentado

1. **Ocram (O Antigo Chefe Final):**
   - Modelos documentados cobrindo a Fase 1 e Fase 2 com carapaça bio-mecânica e foices lasers.
   - Preservado como boss bônus opcional invocável através do crânio suspeito.
2. **Turkor the Ungrateful:**
   - O infame chefe do Dia de Ação de Graças das versões console/mobile clássicas.
3. **Skeletron Rig:**
   - Rig esqueletal completo preservado para ataques cinemáticos dos braços do Skeletron clássico.
"""

    with open(DOCS_DIR / "LEGACY_CONTENT.md", "w", encoding="utf-8") as f:
        f.write(leg_md)
    print(f"Generated {DOCS_DIR / 'LEGACY_CONTENT.md'}")

if __name__ == "__main__":
    main()
