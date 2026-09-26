"""
Update all 19 MTL files in TerraForge RPG so that map_Kd points directly to their
valid on-disk texture relative to the MTL directory.
"""
import os
import re

MTL_UPDATES = {
    # Items (models/item -> textures/item)
    "src/main/resources/assets/terraforge_rpg/models/item/boomstick.mtl": "../../textures/item/boomstick.png",
    "src/main/resources/assets/terraforge_rpg/models/item/megashark.mtl": "../../textures/item/megashark.png",
    "src/main/resources/assets/terraforge_rpg/models/item/meowmere.mtl": "../../textures/item/meowmere.png",
    "src/main/resources/assets/terraforge_rpg/models/item/minishark.mtl": "../../textures/item/minishark.png",
    "src/main/resources/assets/terraforge_rpg/models/item/nights_edge.mtl": "../../textures/item/nights_edge.png",
    "src/main/resources/assets/terraforge_rpg/models/item/phoenix_blaster.mtl": "../../textures/item/phoenix_blaster.png",
    "src/main/resources/assets/terraforge_rpg/models/item/seedler.mtl": "../../textures/item/seedler.png",
    "src/main/resources/assets/terraforge_rpg/models/item/terra_blade.mtl": "../../textures/item/terra_blade.png",
    "src/main/resources/assets/terraforge_rpg/models/item/true_nights_edge.mtl": "../../textures/item/true_nights_edge.png",
    "src/main/resources/assets/terraforge_rpg/models/item/uzi.mtl": "../../textures/item/uzi.png",
    "src/main/resources/assets/terraforge_rpg/models/item/vortex_beater.mtl": "../../textures/item/vortex_beater.png",
    "src/main/resources/assets/terraforge_rpg/models/item/zenith.mtl": "../../textures/item/zenith.png",
    
    # Bosses (models/entity/boss -> textures/entity/boss)
    "src/main/resources/assets/terraforge_rpg/models/entity/boss/eye_of_cthulhu_p1.mtl": "../../../textures/entity/boss/eye_of_cthulhu_p1.png",
    "src/main/resources/assets/terraforge_rpg/models/entity/boss/eye_of_cthulhu_p2.mtl": "../../../textures/entity/boss/eye_of_cthulhu_p2.png",
    "src/main/resources/assets/terraforge_rpg/models/entity/boss/moon_lord.mtl": "../../../textures/entity/boss/moon_lord.png",
    "src/main/resources/assets/terraforge_rpg/models/entity/boss/the_destroyer.mtl": "../../../textures/entity/boss/the_destroyer.png",
    "src/main/resources/assets/terraforge_rpg/models/entity/boss/wall_of_flesh.mtl": "../../../textures/entity/boss/wall_of_flesh.png",
    
    # Mobs (models/entity/mob -> textures/entity/mob)
    "src/main/resources/assets/terraforge_rpg/models/entity/mob/face_monster.mtl": "../../../textures/entity/mob/face_monster.png",
    
    # Blocks (models/block -> textures/block)
    "src/main/resources/assets/terraforge_rpg/models/block/work_bench.mtl": "../../textures/block/work_bench.png",
}

def update_mtls():
    for mtl_rel, tex_rel in MTL_UPDATES.items():
        mtl_path = os.path.normpath(mtl_rel)
        if not os.path.exists(mtl_path):
            print(f"Warning: MTL not found: {mtl_path}")
            continue
        
        # Verify target texture exists
        dir_path = os.path.dirname(mtl_path)
        resolved_tex = os.path.normpath(os.path.join(dir_path, tex_rel))
        if not os.path.isfile(resolved_tex):
            raise FileNotFoundError(f"Texture does not exist: {resolved_tex} (referenced by {mtl_path})")
        
        with open(mtl_path, "r", encoding="utf-8") as f:
            lines = f.readlines()
        
        new_lines = []
        replaced_count = 0
        for line in lines:
            if line.strip().startswith("map_Kd"):
                new_lines.append(f"map_Kd {tex_rel}\n")
                replaced_count += 1
            else:
                new_lines.append(line)
        
        with open(mtl_path, "w", encoding="utf-8", newline="\n") as f:
            f.writelines(new_lines)
        
        print(f"Updated {os.path.basename(mtl_path)} ({replaced_count} map_Kd lines replaced -> {tex_rel})")

if __name__ == "__main__":
    update_mtls()
