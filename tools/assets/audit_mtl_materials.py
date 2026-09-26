import glob
import os

def audit_mtls():
    mtl_files = glob.glob('src/main/resources/assets/terraforge_rpg/**/*.mtl', recursive=True)
    print(f"Total MTL files found: {len(mtl_files)}")
    
    issues = []
    checked = 0
    
    for mtl in sorted(mtl_files):
        dir_path = os.path.dirname(mtl)
        with open(mtl, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        has_map_kd = False
        for i, line in enumerate(lines, 1):
            line_s = line.strip()
            if line_s.startswith('map_Kd'):
                has_map_kd = True
                checked += 1
                parts = line_s.split(None, 1)
                tex = parts[1].strip() if len(parts) > 1 else ""
                
                # Check for absolute Windows paths (e.g., C:\...)
                if len(tex) > 1 and tex[1] == ':':
                    issues.append(f"ABSOLUTE PATH in {mtl}:{i} -> '{tex}'")
                    continue
                
                # Check resolution
                # Path could be relative to MTL dir, or relative to textures dir, or relative to assets root
                candidates = [
                    os.path.normpath(os.path.join(dir_path, tex)),
                    os.path.normpath(os.path.join('src/main/resources/assets/terraforge_rpg/textures', tex)),
                    os.path.normpath(os.path.join('src/main/resources/assets/terraforge_rpg', tex)),
                    os.path.normpath(os.path.join('src/main/resources/assets/terraforge_rpg/textures/item', tex)),
                    os.path.normpath(os.path.join('src/main/resources/assets/terraforge_rpg/textures/entity/boss', tex)),
                    os.path.normpath(os.path.join('src/main/resources/assets/terraforge_rpg/textures/entity/mob', tex)),
                    os.path.normpath(os.path.join('src/main/resources/assets/terraforge_rpg/textures/block', tex))
                ]
                
                found_path = None
                for c in candidates:
                    if os.path.isfile(c):
                        found_path = c
                        break
                
                if not found_path:
                    issues.append(f"MISSING TEXTURE in {mtl}:{i} -> '{tex}' (searched candidates: {candidates[:3]})")
                else:
                    print(f"OK: {os.path.basename(mtl)} -> '{tex}' (found at {found_path})")
    
    print(f"\nAudit complete: checked {checked} map_Kd directives across {len(mtl_files)} MTL files.")
    if issues:
        print(f"FAIL: Found {len(issues)} issues:")
        for issue in issues:
            print("  " + issue)
    else:
        print("SUCCESS: 100% of map_Kd directives resolve to existing textures on disk.")

if __name__ == "__main__":
    audit_mtls()
