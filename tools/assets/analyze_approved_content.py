"""
Analyze all approved 3D models in docs/3D_MODEL_LIBRARY.csv
"""

import csv
from pathlib import Path

LIB_CSV = Path(r"c:\Users\Alza\Desktop\terraforge\docs\3D_MODEL_LIBRARY.csv")

def main():
    with open(LIB_CSV, "r", encoding="utf-8") as f:
        models = list(csv.DictReader(f))

    primary = [m for m in models if m["candidate_quality"] == "PRIMARY"]
    print(f"Total PRIMARY models: {len(primary)}")

    by_type = {}
    for m in primary:
        t = m["content_type"]
        by_type.setdefault(t, []).append(m)

    for t, m_list in sorted(by_type.items()):
        print(f"\n=== {t} ({len(m_list)}) ===")
        for m in sorted(m_list, key=lambda x: x["name"])[:15]:
            print(f"  {m['model_id'][:8]} | {m['name']} | Tris: {m['triangle_count']} | Anim: {m['animation_count']} | Fam: {m['content_family']}")

if __name__ == "__main__":
    main()
