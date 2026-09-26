"""
TerraForge RPG - 3D Item Model Generator & Auditor
Converts all remaining 2D items (minecraft:item/generated) into true 3D volumetric models
with custom elements, multi-box geometry, depth, bevels, and isometric GUI display transforms.
Generates docs/ITEM_3D_AUDIT.csv.
"""

import json
import os
import csv
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent.parent.parent
MODELS_DIR = ROOT_DIR / "src/main/resources/assets/terraforge_rpg/models/item"
AUDIT_CSV = ROOT_DIR / "docs/ITEM_3D_AUDIT.csv"

def get_display_transforms(category):
    if category in ("weapon_melee", "weapon_ranged", "tool"):
        return {
            "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
            "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2.0, 0], "scale": [0.5, 0.5, 0.5]},
            "gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1.0, 1.0, 1.0]},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1.0, 1.0, 1.0]}
        }
    elif category == "crate":
        return {
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3.0, 0], "scale": [0.25, 0.25, 0.25]},
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
            "head": {"rotation": [0, 0, 0], "translation": [0, 14.5, 0], "scale": [1.0, 1.0, 1.0]},
            "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]}
        }
    else:
        # Standard isometric 3D item transforms (depth & bevels visible in GUI)
        return {
            "thirdperson_righthand": {"rotation": [0, 90, -35], "translation": [0, 1.25, -3.5], "scale": [0.85, 0.85, 0.85]},
            "thirdperson_lefthand": {"rotation": [0, -90, 35], "translation": [0, 1.25, -3.5], "scale": [0.85, 0.85, 0.85]},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3.0, 0], "scale": [0.5, 0.5, 0.5]},
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
            "head": {"rotation": [0, 0, 0], "translation": [0, 13.0, 7.0], "scale": [0.8, 0.8, 0.8]},
            "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]}
        }

def make_coin_elements(tex):
    # Cylindrical beveled octagonal disc
    return [
        {
            "name": "coin_center",
            "from": [4.0, 4.0, 7.25],
            "to": [12.0, 12.0, 8.75],
            "faces": {
                "north": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
                "south": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
                "up": {"uv": [4, 7, 12, 9], "texture": "#layer0"},
                "down": {"uv": [4, 7, 12, 9], "texture": "#layer0"},
                "west": {"uv": [7, 4, 9, 12], "texture": "#layer0"},
                "east": {"uv": [7, 4, 9, 12], "texture": "#layer0"}
            }
        },
        {
            "name": "coin_top_bevel",
            "from": [5.0, 12.0, 7.25],
            "to": [11.0, 13.0, 8.75],
            "faces": {
                "north": {"uv": [5, 3, 11, 4], "texture": "#layer0"},
                "south": {"uv": [5, 3, 11, 4], "texture": "#layer0"},
                "up": {"uv": [5, 7, 11, 9], "texture": "#layer0"},
                "west": {"uv": [7, 3, 9, 4], "texture": "#layer0"},
                "east": {"uv": [7, 3, 9, 4], "texture": "#layer0"}
            }
        },
        {
            "name": "coin_bottom_bevel",
            "from": [5.0, 3.0, 7.25],
            "to": [11.0, 4.0, 8.75],
            "faces": {
                "north": {"uv": [5, 12, 11, 13], "texture": "#layer0"},
                "south": {"uv": [5, 12, 11, 13], "texture": "#layer0"},
                "down": {"uv": [5, 7, 11, 9], "texture": "#layer0"},
                "west": {"uv": [7, 12, 9, 13], "texture": "#layer0"},
                "east": {"uv": [7, 12, 9, 13], "texture": "#layer0"}
            }
        },
        {
            "name": "coin_left_bevel",
            "from": [3.0, 5.0, 7.25],
            "to": [4.0, 11.0, 8.75],
            "faces": {
                "north": {"uv": [3, 5, 4, 11], "texture": "#layer0"},
                "south": {"uv": [3, 5, 4, 11], "texture": "#layer0"},
                "west": {"uv": [7, 5, 9, 11], "texture": "#layer0"},
                "up": {"uv": [3, 7, 4, 9], "texture": "#layer0"},
                "down": {"uv": [3, 7, 4, 9], "texture": "#layer0"}
            }
        },
        {
            "name": "coin_right_bevel",
            "from": [12.0, 5.0, 7.25],
            "to": [13.0, 11.0, 8.75],
            "faces": {
                "north": {"uv": [12, 5, 13, 11], "texture": "#layer0"},
                "south": {"uv": [12, 5, 13, 11], "texture": "#layer0"},
                "east": {"uv": [7, 5, 9, 11], "texture": "#layer0"},
                "up": {"uv": [12, 7, 13, 9], "texture": "#layer0"},
                "down": {"uv": [12, 7, 13, 9], "texture": "#layer0"}
            }
        }
    ]

def make_crystal_heart_elements(tex):
    # Multi-faceted crystalline cluster
    return [
        {
            "name": "heart_core",
            "from": [5.0, 5.0, 6.0],
            "to": [11.0, 11.0, 10.0],
            "faces": {
                "north": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "south": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "up": {"uv": [5, 6, 11, 10], "texture": "#layer0"},
                "down": {"uv": [5, 6, 11, 10], "texture": "#layer0"},
                "west": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "east": {"uv": [6, 5, 10, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "heart_point",
            "from": [6.5, 2.0, 6.5],
            "to": [9.5, 5.0, 9.5],
            "faces": {
                "north": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "south": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "down": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "west": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "east": {"uv": [6, 11, 10, 14], "texture": "#layer0"}
            }
        },
        {
            "name": "left_lobe",
            "from": [3.5, 9.0, 6.5],
            "to": [8.0, 13.5, 9.5],
            "faces": {
                "north": {"uv": [3, 2, 8, 7], "texture": "#layer0"},
                "south": {"uv": [3, 2, 8, 7], "texture": "#layer0"},
                "up": {"uv": [3, 6, 8, 10], "texture": "#layer0"},
                "west": {"uv": [6, 2, 10, 7], "texture": "#layer0"}
            }
        },
        {
            "name": "right_lobe",
            "from": [8.0, 9.0, 6.5],
            "to": [12.5, 13.5, 9.5],
            "faces": {
                "north": {"uv": [8, 2, 13, 7], "texture": "#layer0"},
                "south": {"uv": [8, 2, 13, 7], "texture": "#layer0"},
                "up": {"uv": [8, 6, 13, 10], "texture": "#layer0"},
                "east": {"uv": [6, 2, 10, 7], "texture": "#layer0"}
            }
        },
        {
            "name": "crystal_gem_facet",
            "from": [6.0, 6.0, 5.0],
            "to": [10.0, 10.0, 11.0],
            "faces": {
                "north": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "south": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "up": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "down": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "west": {"uv": [5, 6, 11, 10], "texture": "#layer0"},
                "east": {"uv": [5, 6, 11, 10], "texture": "#layer0"}
            }
        }
    ]

def make_potion_elements(tex):
    # Volumetric potion bottle with neck, stopper, and liquid core
    return [
        {
            "name": "bottle_body",
            "from": [5.0, 2.0, 5.0],
            "to": [11.0, 9.0, 11.0],
            "faces": {
                "north": {"uv": [5, 7, 11, 14], "texture": "#layer0"},
                "south": {"uv": [5, 7, 11, 14], "texture": "#layer0"},
                "east": {"uv": [5, 7, 11, 14], "texture": "#layer0"},
                "west": {"uv": [5, 7, 11, 14], "texture": "#layer0"},
                "down": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "up": {"uv": [5, 5, 11, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "bottle_neck",
            "from": [6.5, 9.0, 6.5],
            "to": [9.5, 12.0, 9.5],
            "faces": {
                "north": {"uv": [6, 4, 10, 7], "texture": "#layer0"},
                "south": {"uv": [6, 4, 10, 7], "texture": "#layer0"},
                "east": {"uv": [6, 4, 10, 7], "texture": "#layer0"},
                "west": {"uv": [6, 4, 10, 7], "texture": "#layer0"}
            }
        },
        {
            "name": "bottle_lip",
            "from": [6.0, 12.0, 6.0],
            "to": [10.0, 13.0, 10.0],
            "faces": {
                "north": {"uv": [6, 3, 10, 4], "texture": "#layer0"},
                "south": {"uv": [6, 3, 10, 4], "texture": "#layer0"},
                "east": {"uv": [6, 3, 10, 4], "texture": "#layer0"},
                "west": {"uv": [6, 3, 10, 4], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "cork_stopper",
            "from": [6.5, 13.0, 6.5],
            "to": [9.5, 15.0, 9.5],
            "faces": {
                "north": {"uv": [6, 1, 10, 3], "texture": "#layer0"},
                "south": {"uv": [6, 1, 10, 3], "texture": "#layer0"},
                "east": {"uv": [6, 1, 10, 3], "texture": "#layer0"},
                "west": {"uv": [6, 1, 10, 3], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        }
    ]

def make_soul_elements(tex):
    # Floating celestial orb with orbiting essence shards
    return [
        {
            "name": "soul_core",
            "from": [5.5, 5.5, 5.5],
            "to": [10.5, 10.5, 10.5],
            "faces": {
                "north": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "south": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "east": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "west": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "up": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "down": {"uv": [5, 5, 11, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "orb_flare_top",
            "from": [6.5, 10.5, 6.5],
            "to": [9.5, 13.5, 9.5],
            "faces": {
                "north": {"uv": [6, 2, 10, 5], "texture": "#layer0"},
                "south": {"uv": [6, 2, 10, 5], "texture": "#layer0"},
                "east": {"uv": [6, 2, 10, 5], "texture": "#layer0"},
                "west": {"uv": [6, 2, 10, 5], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "orb_flare_bottom",
            "from": [6.5, 2.5, 6.5],
            "to": [9.5, 5.5, 9.5],
            "faces": {
                "north": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "south": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "east": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "west": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "down": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "orb_flare_left",
            "from": [2.5, 6.5, 6.5],
            "to": [5.5, 9.5, 9.5],
            "faces": {
                "north": {"uv": [2, 6, 5, 10], "texture": "#layer0"},
                "south": {"uv": [2, 6, 5, 10], "texture": "#layer0"},
                "west": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "up": {"uv": [2, 6, 5, 10], "texture": "#layer0"},
                "down": {"uv": [2, 6, 5, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "orb_flare_right",
            "from": [10.5, 6.5, 6.5],
            "to": [13.5, 9.5, 9.5],
            "faces": {
                "north": {"uv": [11, 6, 14, 10], "texture": "#layer0"},
                "south": {"uv": [11, 6, 14, 10], "texture": "#layer0"},
                "east": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "up": {"uv": [11, 6, 14, 10], "texture": "#layer0"},
                "down": {"uv": [11, 6, 14, 10], "texture": "#layer0"}
            }
        }
    ]

def make_ingot_elements(tex):
    # Trapezoidal beveled metallic ingot
    return [
        {
            "name": "ingot_base",
            "from": [3.0, 5.0, 5.0],
            "to": [13.0, 8.0, 11.0],
            "faces": {
                "north": {"uv": [3, 8, 13, 11], "texture": "#layer0"},
                "south": {"uv": [3, 8, 13, 11], "texture": "#layer0"},
                "east": {"uv": [5, 8, 11, 11], "texture": "#layer0"},
                "west": {"uv": [5, 8, 11, 11], "texture": "#layer0"},
                "down": {"uv": [3, 5, 13, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "ingot_top_bevel",
            "from": [4.0, 8.0, 6.0],
            "to": [12.0, 10.0, 10.0],
            "faces": {
                "north": {"uv": [4, 6, 12, 8], "texture": "#layer0"},
                "south": {"uv": [4, 6, 12, 8], "texture": "#layer0"},
                "east": {"uv": [6, 6, 10, 8], "texture": "#layer0"},
                "west": {"uv": [6, 6, 10, 8], "texture": "#layer0"},
                "up": {"uv": [4, 6, 12, 10], "texture": "#layer0"}
            }
        }
    ]

def make_boss_summon_elements(tex):
    # 3D relic artifact / eye summon
    return [
        {
            "name": "relic_core",
            "from": [5.0, 5.0, 5.0],
            "to": [11.0, 11.0, 11.0],
            "faces": {
                "north": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "south": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "east": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "west": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "up": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "down": {"uv": [5, 5, 11, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "optic_stalk",
            "from": [6.5, 6.5, 11.0],
            "to": [9.5, 9.5, 14.0],
            "faces": {
                "north": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "south": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "east": {"uv": [11, 6, 14, 10], "texture": "#layer0"},
                "west": {"uv": [11, 6, 14, 10], "texture": "#layer0"},
                "up": {"uv": [6, 11, 10, 14], "texture": "#layer0"},
                "down": {"uv": [6, 11, 10, 14], "texture": "#layer0"}
            }
        },
        {
            "name": "relic_gem_crown",
            "from": [6.0, 11.0, 6.0],
            "to": [10.0, 13.0, 10.0],
            "faces": {
                "north": {"uv": [6, 3, 10, 5], "texture": "#layer0"},
                "south": {"uv": [6, 3, 10, 5], "texture": "#layer0"},
                "east": {"uv": [6, 3, 10, 5], "texture": "#layer0"},
                "west": {"uv": [6, 3, 10, 5], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        }
    ]

def make_key_elements(tex):
    # 3D ornate temple key
    return [
        {
            "name": "key_handle_ring",
            "from": [5.5, 10.0, 7.0],
            "to": [10.5, 15.0, 9.0],
            "faces": {
                "north": {"uv": [5, 1, 11, 6], "texture": "#layer0"},
                "south": {"uv": [5, 1, 11, 6], "texture": "#layer0"},
                "east": {"uv": [7, 1, 9, 6], "texture": "#layer0"},
                "west": {"uv": [7, 1, 9, 6], "texture": "#layer0"},
                "up": {"uv": [5, 7, 11, 9], "texture": "#layer0"},
                "down": {"uv": [5, 7, 11, 9], "texture": "#layer0"}
            }
        },
        {
            "name": "key_stem",
            "from": [7.0, 3.0, 7.25],
            "to": [9.0, 10.0, 8.75],
            "faces": {
                "north": {"uv": [7, 6, 9, 13], "texture": "#layer0"},
                "south": {"uv": [7, 6, 9, 13], "texture": "#layer0"},
                "east": {"uv": [7, 6, 9, 13], "texture": "#layer0"},
                "west": {"uv": [7, 6, 9, 13], "texture": "#layer0"},
                "down": {"uv": [7, 7, 9, 9], "texture": "#layer0"}
            }
        },
        {
            "name": "key_bit_teeth",
            "from": [9.0, 3.0, 7.25],
            "to": [12.0, 6.0, 8.75],
            "faces": {
                "north": {"uv": [9, 10, 12, 13], "texture": "#layer0"},
                "south": {"uv": [9, 10, 12, 13], "texture": "#layer0"},
                "east": {"uv": [7, 10, 9, 13], "texture": "#layer0"},
                "up": {"uv": [9, 7, 12, 9], "texture": "#layer0"},
                "down": {"uv": [9, 7, 12, 9], "texture": "#layer0"}
            }
        }
    ]

def make_accessory_elements(tex):
    # 3D prismatic talisman / accessory artifact
    return [
        {
            "name": "talisman_frame",
            "from": [4.0, 4.0, 6.5],
            "to": [12.0, 12.0, 9.5],
            "faces": {
                "north": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
                "south": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
                "east": {"uv": [6, 4, 10, 12], "texture": "#layer0"},
                "west": {"uv": [6, 4, 10, 12], "texture": "#layer0"},
                "up": {"uv": [4, 6, 12, 10], "texture": "#layer0"},
                "down": {"uv": [4, 6, 12, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "gem_socket",
            "from": [5.5, 5.5, 5.75],
            "to": [10.5, 10.5, 10.25],
            "faces": {
                "north": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "south": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "east": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "west": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "up": {"uv": [5, 6, 11, 10], "texture": "#layer0"},
                "down": {"uv": [5, 6, 11, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "talisman_loop",
            "from": [6.5, 12.0, 7.25],
            "to": [9.5, 14.5, 8.75],
            "faces": {
                "north": {"uv": [6, 1, 10, 4], "texture": "#layer0"},
                "south": {"uv": [6, 1, 10, 4], "texture": "#layer0"},
                "east": {"uv": [7, 1, 9, 4], "texture": "#layer0"},
                "west": {"uv": [7, 1, 9, 4], "texture": "#layer0"},
                "up": {"uv": [6, 7, 10, 9], "texture": "#layer0"}
            }
        }
    ]

def make_wings_elements(tex):
    # 3D Phoenix Wings item artifact
    return [
        {
            "name": "wing_spine_center",
            "from": [7.0, 5.0, 7.0],
            "to": [9.0, 11.0, 9.0],
            "faces": {
                "north": {"uv": [7, 5, 9, 11], "texture": "#layer0"},
                "south": {"uv": [7, 5, 9, 11], "texture": "#layer0"},
                "up": {"uv": [7, 7, 9, 9], "texture": "#layer0"},
                "down": {"uv": [7, 7, 9, 9], "texture": "#layer0"}
            }
        },
        {
            "name": "left_wing_primary",
            "from": [1.0, 6.0, 6.5],
            "to": [7.0, 14.0, 8.5],
            "faces": {
                "north": {"uv": [1, 2, 7, 10], "texture": "#layer0"},
                "south": {"uv": [1, 2, 7, 10], "texture": "#layer0"},
                "west": {"uv": [6, 2, 8, 10], "texture": "#layer0"},
                "up": {"uv": [1, 6, 7, 8], "texture": "#layer0"}
            }
        },
        {
            "name": "right_wing_primary",
            "from": [9.0, 6.0, 6.5],
            "to": [15.0, 14.0, 8.5],
            "faces": {
                "north": {"uv": [9, 2, 15, 10], "texture": "#layer0"},
                "south": {"uv": [9, 2, 15, 10], "texture": "#layer0"},
                "east": {"uv": [6, 2, 8, 10], "texture": "#layer0"},
                "up": {"uv": [9, 6, 15, 8], "texture": "#layer0"}
            }
        },
        {
            "name": "left_wing_feathers",
            "from": [2.0, 3.0, 7.0],
            "to": [7.0, 6.0, 8.0],
            "faces": {
                "north": {"uv": [2, 10, 7, 13], "texture": "#layer0"},
                "south": {"uv": [2, 10, 7, 13], "texture": "#layer0"},
                "down": {"uv": [2, 7, 7, 8], "texture": "#layer0"},
                "west": {"uv": [7, 10, 8, 13], "texture": "#layer0"}
            }
        },
        {
            "name": "right_wing_feathers",
            "from": [9.0, 3.0, 7.0],
            "to": [14.0, 6.0, 8.0],
            "faces": {
                "north": {"uv": [9, 10, 14, 13], "texture": "#layer0"},
                "south": {"uv": [9, 10, 14, 13], "texture": "#layer0"},
                "down": {"uv": [9, 7, 14, 8], "texture": "#layer0"},
                "east": {"uv": [7, 10, 8, 13], "texture": "#layer0"}
            }
        }
    ]

def make_arrow_elements(tex):
    # 3D Arrow with shaft, arrowhead, and 4-way fletching
    return [
        {
            "name": "arrow_shaft",
            "from": [7.25, 2.0, 7.25],
            "to": [8.75, 13.0, 8.75],
            "faces": {
                "north": {"uv": [7, 3, 9, 14], "texture": "#layer0"},
                "south": {"uv": [7, 3, 9, 14], "texture": "#layer0"},
                "east": {"uv": [7, 3, 9, 14], "texture": "#layer0"},
                "west": {"uv": [7, 3, 9, 14], "texture": "#layer0"}
            }
        },
        {
            "name": "arrow_head",
            "from": [6.0, 13.0, 6.0],
            "to": [10.0, 16.0, 10.0],
            "faces": {
                "north": {"uv": [6, 0, 10, 3], "texture": "#layer0"},
                "south": {"uv": [6, 0, 10, 3], "texture": "#layer0"},
                "east": {"uv": [6, 0, 10, 3], "texture": "#layer0"},
                "west": {"uv": [6, 0, 10, 3], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "down": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "fletching_z",
            "from": [7.75, 0.5, 5.5],
            "to": [8.25, 3.5, 10.5],
            "faces": {
                "east": {"uv": [5, 12, 11, 15], "texture": "#layer0"},
                "west": {"uv": [5, 12, 11, 15], "texture": "#layer0"},
                "down": {"uv": [7, 5, 9, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "fletching_x",
            "from": [5.5, 0.5, 7.75],
            "to": [10.5, 3.5, 8.25],
            "faces": {
                "north": {"uv": [5, 12, 11, 15], "texture": "#layer0"},
                "south": {"uv": [5, 12, 11, 15], "texture": "#layer0"},
                "down": {"uv": [5, 7, 11, 9], "texture": "#layer0"}
            }
        }
    ]

def make_bullet_elements(tex):
    # 3D bullet cartridge with brass case and projectile slug
    return [
        {
            "name": "cartridge_case",
            "from": [6.0, 3.0, 6.0],
            "to": [10.0, 10.0, 10.0],
            "faces": {
                "north": {"uv": [6, 6, 10, 13], "texture": "#layer0"},
                "south": {"uv": [6, 6, 10, 13], "texture": "#layer0"},
                "east": {"uv": [6, 6, 10, 13], "texture": "#layer0"},
                "west": {"uv": [6, 6, 10, 13], "texture": "#layer0"},
                "down": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "bullet_slug",
            "from": [6.5, 10.0, 6.5],
            "to": [9.5, 13.5, 9.5],
            "faces": {
                "north": {"uv": [6, 2, 10, 6], "texture": "#layer0"},
                "south": {"uv": [6, 2, 10, 6], "texture": "#layer0"},
                "east": {"uv": [6, 2, 10, 6], "texture": "#layer0"},
                "west": {"uv": [6, 2, 10, 6], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "primer_rim",
            "from": [5.5, 2.0, 5.5],
            "to": [10.5, 3.0, 10.5],
            "faces": {
                "north": {"uv": [5, 13, 11, 14], "texture": "#layer0"},
                "south": {"uv": [5, 13, 11, 14], "texture": "#layer0"},
                "east": {"uv": [5, 13, 11, 14], "texture": "#layer0"},
                "west": {"uv": [5, 13, 11, 14], "texture": "#layer0"},
                "down": {"uv": [5, 5, 11, 11], "texture": "#layer0"}
            }
        }
    ]

def make_crate_elements(tex):
    # 3D Shipping Crate Box
    return [
        {
            "name": "crate_body",
            "from": [2.0, 2.0, 2.0],
            "to": [14.0, 14.0, 14.0],
            "faces": {
                "north": {"uv": [2, 2, 14, 14], "texture": "#layer0"},
                "south": {"uv": [2, 2, 14, 14], "texture": "#layer0"},
                "east": {"uv": [2, 2, 14, 14], "texture": "#layer0"},
                "west": {"uv": [2, 2, 14, 14], "texture": "#layer0"},
                "up": {"uv": [2, 2, 14, 14], "texture": "#layer0"},
                "down": {"uv": [2, 2, 14, 14], "texture": "#layer0"}
            }
        },
        {
            "name": "corner_trim_top",
            "from": [1.5, 13.5, 1.5],
            "to": [14.5, 14.5, 14.5],
            "faces": {
                "north": {"uv": [1, 1, 15, 2], "texture": "#layer0"},
                "south": {"uv": [1, 1, 15, 2], "texture": "#layer0"},
                "east": {"uv": [1, 1, 15, 2], "texture": "#layer0"},
                "west": {"uv": [1, 1, 15, 2], "texture": "#layer0"},
                "up": {"uv": [1, 1, 15, 15], "texture": "#layer0"}
            }
        },
        {
            "name": "corner_trim_bottom",
            "from": [1.5, 1.5, 1.5],
            "to": [14.5, 2.5, 14.5],
            "faces": {
                "north": {"uv": [1, 14, 15, 15], "texture": "#layer0"},
                "south": {"uv": [1, 14, 15, 15], "texture": "#layer0"},
                "east": {"uv": [1, 14, 15, 15], "texture": "#layer0"},
                "west": {"uv": [1, 14, 15, 15], "texture": "#layer0"},
                "down": {"uv": [1, 1, 15, 15], "texture": "#layer0"}
            }
        }
    ]

def make_hook_elements(tex):
    # 3D Grappling Hook mechanism
    return [
        {
            "name": "hook_chassis",
            "from": [6.0, 4.0, 6.0],
            "to": [10.0, 11.0, 10.0],
            "faces": {
                "north": {"uv": [6, 5, 10, 12], "texture": "#layer0"},
                "south": {"uv": [6, 5, 10, 12], "texture": "#layer0"},
                "east": {"uv": [6, 5, 10, 12], "texture": "#layer0"},
                "west": {"uv": [6, 5, 10, 12], "texture": "#layer0"},
                "up": {"uv": [6, 6, 10, 10], "texture": "#layer0"},
                "down": {"uv": [6, 6, 10, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "claw_left",
            "from": [3.0, 10.0, 7.0],
            "to": [6.0, 14.0, 9.0],
            "faces": {
                "north": {"uv": [3, 2, 6, 6], "texture": "#layer0"},
                "south": {"uv": [3, 2, 6, 6], "texture": "#layer0"},
                "west": {"uv": [7, 2, 9, 6], "texture": "#layer0"},
                "up": {"uv": [3, 7, 6, 9], "texture": "#layer0"}
            }
        },
        {
            "name": "claw_right",
            "from": [10.0, 10.0, 7.0],
            "to": [13.0, 14.0, 9.0],
            "faces": {
                "north": {"uv": [10, 2, 13, 6], "texture": "#layer0"},
                "south": {"uv": [10, 2, 13, 6], "texture": "#layer0"},
                "east": {"uv": [7, 2, 9, 6], "texture": "#layer0"},
                "up": {"uv": [10, 7, 13, 9], "texture": "#layer0"}
            }
        },
        {
            "name": "hook_ring",
            "from": [7.0, 1.5, 7.25],
            "to": [9.0, 4.0, 8.75],
            "faces": {
                "north": {"uv": [7, 12, 9, 15], "texture": "#layer0"},
                "south": {"uv": [7, 12, 9, 15], "texture": "#layer0"},
                "east": {"uv": [7, 12, 9, 15], "texture": "#layer0"},
                "west": {"uv": [7, 12, 9, 15], "texture": "#layer0"},
                "down": {"uv": [7, 7, 9, 9], "texture": "#layer0"}
            }
        }
    ]

def make_armor_helmet_elements(tex):
    return [
        {
            "name": "helmet_dome",
            "from": [3.5, 4.0, 3.5],
            "to": [12.5, 13.0, 12.5],
            "faces": {
                "north": {"uv": [3, 3, 13, 12], "texture": "#layer0"},
                "south": {"uv": [3, 3, 13, 12], "texture": "#layer0"},
                "east": {"uv": [3, 3, 13, 12], "texture": "#layer0"},
                "west": {"uv": [3, 3, 13, 12], "texture": "#layer0"},
                "up": {"uv": [3, 3, 13, 13], "texture": "#layer0"}
            }
        },
        {
            "name": "brow_visor",
            "from": [3.0, 6.0, 3.0],
            "to": [13.0, 9.0, 5.0],
            "faces": {
                "north": {"uv": [3, 7, 13, 10], "texture": "#layer0"},
                "east": {"uv": [3, 7, 5, 10], "texture": "#layer0"},
                "west": {"uv": [3, 7, 5, 10], "texture": "#layer0"},
                "up": {"uv": [3, 3, 13, 5], "texture": "#layer0"},
                "down": {"uv": [3, 3, 13, 5], "texture": "#layer0"}
            }
        }
    ]

def make_armor_chest_elements(tex):
    return [
        {
            "name": "cuirass_torso",
            "from": [4.0, 2.0, 5.0],
            "to": [12.0, 12.0, 11.0],
            "faces": {
                "north": {"uv": [4, 4, 12, 14], "texture": "#layer0"},
                "south": {"uv": [4, 4, 12, 14], "texture": "#layer0"},
                "east": {"uv": [5, 4, 11, 14], "texture": "#layer0"},
                "west": {"uv": [5, 4, 11, 14], "texture": "#layer0"},
                "up": {"uv": [4, 5, 12, 11], "texture": "#layer0"},
                "down": {"uv": [4, 5, 12, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "left_pauldron",
            "from": [1.5, 9.0, 4.5],
            "to": [4.5, 13.0, 11.5],
            "faces": {
                "north": {"uv": [1, 3, 4, 7], "texture": "#layer0"},
                "south": {"uv": [1, 3, 4, 7], "texture": "#layer0"},
                "west": {"uv": [4, 3, 11, 7], "texture": "#layer0"},
                "up": {"uv": [1, 4, 4, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "right_pauldron",
            "from": [11.5, 9.0, 4.5],
            "to": [14.5, 13.0, 11.5],
            "faces": {
                "north": {"uv": [12, 3, 15, 7], "texture": "#layer0"},
                "south": {"uv": [12, 3, 15, 7], "texture": "#layer0"},
                "east": {"uv": [4, 3, 11, 7], "texture": "#layer0"},
                "up": {"uv": [12, 4, 15, 11], "texture": "#layer0"}
            }
        }
    ]

def make_armor_legs_elements(tex):
    return [
        {
            "name": "greave_waist",
            "from": [4.5, 9.0, 5.5],
            "to": [11.5, 12.0, 10.5],
            "faces": {
                "north": {"uv": [4, 4, 12, 7], "texture": "#layer0"},
                "south": {"uv": [4, 4, 12, 7], "texture": "#layer0"},
                "east": {"uv": [5, 4, 11, 7], "texture": "#layer0"},
                "west": {"uv": [5, 4, 11, 7], "texture": "#layer0"},
                "up": {"uv": [4, 5, 12, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "left_thigh",
            "from": [4.5, 3.0, 6.0],
            "to": [7.5, 9.0, 10.0],
            "faces": {
                "north": {"uv": [4, 7, 7, 13], "texture": "#layer0"},
                "south": {"uv": [4, 7, 7, 13], "texture": "#layer0"},
                "west": {"uv": [6, 7, 10, 13], "texture": "#layer0"},
                "down": {"uv": [4, 6, 7, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "right_thigh",
            "from": [8.5, 3.0, 6.0],
            "to": [11.5, 9.0, 10.0],
            "faces": {
                "north": {"uv": [9, 7, 12, 13], "texture": "#layer0"},
                "south": {"uv": [9, 7, 12, 13], "texture": "#layer0"},
                "east": {"uv": [6, 7, 10, 13], "texture": "#layer0"},
                "down": {"uv": [9, 6, 12, 10], "texture": "#layer0"}
            }
        }
    ]

def make_armor_boots_elements(tex):
    return [
        {
            "name": "left_boot",
            "from": [4.0, 2.0, 4.5],
            "to": [7.5, 8.0, 11.5],
            "faces": {
                "north": {"uv": [4, 8, 7, 14], "texture": "#layer0"},
                "south": {"uv": [4, 8, 7, 14], "texture": "#layer0"},
                "west": {"uv": [4, 8, 11, 14], "texture": "#layer0"},
                "east": {"uv": [4, 8, 11, 14], "texture": "#layer0"},
                "up": {"uv": [4, 4, 7, 11], "texture": "#layer0"},
                "down": {"uv": [4, 4, 7, 11], "texture": "#layer0"}
            }
        },
        {
            "name": "right_boot",
            "from": [8.5, 2.0, 4.5],
            "to": [12.0, 8.0, 11.5],
            "faces": {
                "north": {"uv": [9, 8, 12, 14], "texture": "#layer0"},
                "south": {"uv": [9, 8, 12, 14], "texture": "#layer0"},
                "west": {"uv": [4, 8, 11, 14], "texture": "#layer0"},
                "east": {"uv": [4, 8, 11, 14], "texture": "#layer0"},
                "up": {"uv": [9, 4, 12, 11], "texture": "#layer0"},
                "down": {"uv": [9, 4, 12, 11], "texture": "#layer0"}
            }
        }
    ]

def make_generic_3d_artifact(tex):
    # High-depth relief artifact with beveled edges and central core
    return [
        {
            "name": "artifact_body",
            "from": [4.0, 4.0, 6.5],
            "to": [12.0, 12.0, 9.5],
            "faces": {
                "north": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
                "south": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
                "east": {"uv": [6, 4, 10, 12], "texture": "#layer0"},
                "west": {"uv": [6, 4, 10, 12], "texture": "#layer0"},
                "up": {"uv": [4, 6, 12, 10], "texture": "#layer0"},
                "down": {"uv": [4, 6, 12, 10], "texture": "#layer0"}
            }
        },
        {
            "name": "artifact_relief",
            "from": [5.5, 5.5, 5.75],
            "to": [10.5, 10.5, 10.25],
            "faces": {
                "north": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "south": {"uv": [5, 5, 11, 11], "texture": "#layer0"},
                "east": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "west": {"uv": [6, 5, 10, 11], "texture": "#layer0"},
                "up": {"uv": [5, 6, 11, 10], "texture": "#layer0"},
                "down": {"uv": [5, 6, 11, 10], "texture": "#layer0"}
            }
        }
    ]

def make_sword_elements(tex):
    return [
        {"name": "blade", "from": [7.0, 6.0, 7.5], "to": [9.0, 15.5, 8.5], "faces": {
            "north": {"uv": [7, 0, 9, 10], "texture": "#layer0"}, "south": {"uv": [7, 0, 9, 10], "texture": "#layer0"},
            "east": {"uv": [7, 0, 8, 10], "texture": "#layer0"}, "west": {"uv": [7, 0, 8, 10], "texture": "#layer0"},
            "up": {"uv": [7, 7, 9, 8], "texture": "#layer0"}}},
        {"name": "crossguard", "from": [4.0, 5.0, 7.0], "to": [12.0, 6.0, 9.0], "faces": {
            "north": {"uv": [4, 10, 12, 11], "texture": "#layer0"}, "south": {"uv": [4, 10, 12, 11], "texture": "#layer0"},
            "up": {"uv": [4, 7, 12, 9], "texture": "#layer0"}, "down": {"uv": [4, 7, 12, 9], "texture": "#layer0"},
            "west": {"uv": [7, 10, 9, 11], "texture": "#layer0"}, "east": {"uv": [7, 10, 9, 11], "texture": "#layer0"}}},
        {"name": "grip", "from": [7.25, 2.0, 7.25], "to": [8.75, 5.0, 8.75], "faces": {
            "north": {"uv": [7, 11, 9, 14], "texture": "#layer0"}, "south": {"uv": [7, 11, 9, 14], "texture": "#layer0"},
            "east": {"uv": [7, 11, 8, 14], "texture": "#layer0"}, "west": {"uv": [7, 11, 8, 14], "texture": "#layer0"}}},
        {"name": "pommel", "from": [6.5, 0.5, 6.5], "to": [9.5, 2.0, 9.5], "faces": {
            "north": {"uv": [6, 14, 10, 15], "texture": "#layer0"}, "south": {"uv": [6, 14, 10, 15], "texture": "#layer0"},
            "east": {"uv": [6, 14, 10, 15], "texture": "#layer0"}, "west": {"uv": [6, 14, 10, 15], "texture": "#layer0"},
            "down": {"uv": [6, 6, 10, 10], "texture": "#layer0"}}}
    ]

def make_bow_elements(tex):
    return [
        {"name": "bow_riser", "from": [7.0, 6.0, 7.0], "to": [9.0, 10.0, 9.0], "faces": {
            "north": {"uv": [7, 6, 9, 10], "texture": "#layer0"}, "south": {"uv": [7, 6, 9, 10], "texture": "#layer0"},
            "east": {"uv": [7, 6, 9, 10], "texture": "#layer0"}, "west": {"uv": [7, 6, 9, 10], "texture": "#layer0"}}},
        {"name": "upper_limb", "from": [6.5, 10.0, 7.0], "to": [8.5, 15.0, 8.5], "faces": {
            "north": {"uv": [6, 1, 9, 6], "texture": "#layer0"}, "south": {"uv": [6, 1, 9, 6], "texture": "#layer0"},
            "east": {"uv": [7, 1, 8, 6], "texture": "#layer0"}, "west": {"uv": [7, 1, 8, 6], "texture": "#layer0"},
            "up": {"uv": [6, 7, 9, 8], "texture": "#layer0"}}},
        {"name": "lower_limb", "from": [6.5, 1.0, 7.0], "to": [8.5, 6.0, 8.5], "faces": {
            "north": {"uv": [6, 10, 9, 15], "texture": "#layer0"}, "south": {"uv": [6, 10, 9, 15], "texture": "#layer0"},
            "east": {"uv": [7, 10, 8, 15], "texture": "#layer0"}, "west": {"uv": [7, 10, 8, 15], "texture": "#layer0"},
            "down": {"uv": [6, 7, 9, 8], "texture": "#layer0"}}},
        {"name": "bowstring", "from": [9.0, 1.5, 7.75], "to": [9.5, 14.5, 8.25], "faces": {
            "north": {"uv": [9, 1, 10, 15], "texture": "#layer0"}, "south": {"uv": [9, 1, 10, 15], "texture": "#layer0"},
            "east": {"uv": [7, 1, 8, 15], "texture": "#layer0"}}}
    ]

def make_staff_elements(tex):
    return [
        {"name": "staff_shaft", "from": [7.25, 0.5, 7.25], "to": [8.75, 12.0, 8.75], "faces": {
            "north": {"uv": [7, 4, 9, 15], "texture": "#layer0"}, "south": {"uv": [7, 4, 9, 15], "texture": "#layer0"},
            "east": {"uv": [7, 4, 9, 15], "texture": "#layer0"}, "west": {"uv": [7, 4, 9, 15], "texture": "#layer0"},
            "down": {"uv": [7, 7, 9, 9], "texture": "#layer0"}}},
        {"name": "gem_socket", "from": [6.0, 11.0, 6.0], "to": [10.0, 13.0, 10.0], "faces": {
            "north": {"uv": [6, 3, 10, 5], "texture": "#layer0"}, "south": {"uv": [6, 3, 10, 5], "texture": "#layer0"},
            "east": {"uv": [6, 3, 10, 5], "texture": "#layer0"}, "west": {"uv": [6, 3, 10, 5], "texture": "#layer0"}}},
        {"name": "crystal_focus", "from": [5.5, 13.0, 5.5], "to": [10.5, 16.0, 10.5], "faces": {
            "north": {"uv": [5, 0, 11, 3], "texture": "#layer0"}, "south": {"uv": [5, 0, 11, 3], "texture": "#layer0"},
            "east": {"uv": [5, 0, 11, 3], "texture": "#layer0"}, "west": {"uv": [5, 0, 11, 3], "texture": "#layer0"},
            "up": {"uv": [5, 5, 11, 11], "texture": "#layer0"}}}
    ]

def make_spellbook_elements(tex):
    return [
        {"name": "book_cover", "from": [3.0, 2.0, 5.0], "to": [13.0, 14.0, 11.0], "faces": {
            "north": {"uv": [3, 2, 13, 14], "texture": "#layer0"}, "south": {"uv": [3, 2, 13, 14], "texture": "#layer0"},
            "west": {"uv": [5, 2, 11, 14], "texture": "#layer0"}, "east": {"uv": [5, 2, 11, 14], "texture": "#layer0"},
            "up": {"uv": [3, 5, 13, 11], "texture": "#layer0"}, "down": {"uv": [3, 5, 13, 11], "texture": "#layer0"}}},
        {"name": "page_rims", "from": [3.5, 2.5, 5.5], "to": [12.5, 13.5, 10.5], "faces": {
            "east": {"uv": [6, 3, 10, 13], "texture": "#layer0"}, "up": {"uv": [4, 6, 12, 10], "texture": "#layer0"},
            "down": {"uv": [4, 6, 12, 10], "texture": "#layer0"}}}
    ]

def make_yoyo_elements(tex):
    return [
        {"name": "left_disc", "from": [4.0, 4.0, 5.0], "to": [12.0, 12.0, 7.0], "faces": {
            "north": {"uv": [4, 4, 12, 12], "texture": "#layer0"}, "south": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
            "west": {"uv": [5, 4, 7, 12], "texture": "#layer0"}, "up": {"uv": [4, 5, 12, 7], "texture": "#layer0"},
            "down": {"uv": [4, 5, 12, 7], "texture": "#layer0"}}},
        {"name": "axle", "from": [7.0, 7.0, 7.0], "to": [9.0, 9.0, 9.0], "faces": {
            "up": {"uv": [7, 7, 9, 9], "texture": "#layer0"}, "down": {"uv": [7, 7, 9, 9], "texture": "#layer0"}}},
        {"name": "right_disc", "from": [4.0, 4.0, 9.0], "to": [12.0, 12.0, 11.0], "faces": {
            "north": {"uv": [4, 4, 12, 12], "texture": "#layer0"}, "south": {"uv": [4, 4, 12, 12], "texture": "#layer0"},
            "east": {"uv": [9, 4, 11, 12], "texture": "#layer0"}, "up": {"uv": [4, 9, 12, 11], "texture": "#layer0"},
            "down": {"uv": [4, 9, 12, 11], "texture": "#layer0"}}}
    ]

def make_whip_elements(tex):
    return [
        {"name": "whip_handle", "from": [7.0, 1.0, 7.0], "to": [9.0, 6.0, 9.0], "faces": {
            "north": {"uv": [7, 10, 9, 15], "texture": "#layer0"}, "south": {"uv": [7, 10, 9, 15], "texture": "#layer0"},
            "east": {"uv": [7, 10, 9, 15], "texture": "#layer0"}, "west": {"uv": [7, 10, 9, 15], "texture": "#layer0"},
            "down": {"uv": [7, 7, 9, 9], "texture": "#layer0"}}},
        {"name": "whip_coil", "from": [4.0, 6.0, 6.0], "to": [12.0, 14.0, 10.0], "faces": {
            "north": {"uv": [4, 2, 12, 10], "texture": "#layer0"}, "south": {"uv": [4, 2, 12, 10], "texture": "#layer0"},
            "east": {"uv": [6, 2, 10, 10], "texture": "#layer0"}, "west": {"uv": [6, 2, 10, 10], "texture": "#layer0"},
            "up": {"uv": [4, 6, 12, 10], "texture": "#layer0"}}}
    ]

BATCH_MAPPING = {
    # 9 Weapon models
    "copper_shortsword": ("sword", "Armas 3D"),
    "wooden_bow": ("bow", "Armas 3D"),
    "wand_of_sparking": ("staff", "Armas 3D"),
    "slime_staff": ("staff", "Armas 3D"),
    "wooden_yoyo": ("yoyo", "Armas 3D"),
    "leather_whip": ("whip", "Armas 3D"),
    "space_gun": ("bullet", "Armas 3D"),
    "water_bolt": ("spellbook", "Armas 3D"),
    "tsunami": ("bow", "Armas 3D"),

    # Lote A: Core Progression
    "copper_coin": ("coin", "Lote A - Progressão Core"),
    "silver_coin": ("coin", "Lote A - Progressão Core"),
    "gold_coin": ("coin", "Lote A - Progressão Core"),
    "platinum_coin": ("coin", "Lote A - Progressão Core"),
    "life_crystal": ("crystal_heart", "Lote A - Progressão Core"),
    "life_fruit": ("crystal_heart", "Lote A - Progressão Core"),
    "mana_crystal": ("crystal_heart", "Lote A - Progressão Core"),
    "lesser_mana_potion": ("potion", "Lote A - Progressão Core"),
    "mana_potion": ("potion", "Lote A - Progressão Core"),
    "soul_of_sight": ("soul", "Lote A - Progressão Core"),
    "soul_of_might": ("soul", "Lote A - Progressão Core"),
    "soul_of_fright": ("soul", "Lote A - Progressão Core"),
    "hallowed_bar": ("ingot", "Lote A - Progressão Core"),
    "luminite_bar": ("ingot", "Lote A - Progressão Core"),
    "suspicious_looking_eye": ("boss_summon", "Lote A - Progressão Core"),
    "slime_crown": ("boss_summon", "Lote A - Progressão Core"),
    "mechanical_eye": ("boss_summon", "Lote A - Progressão Core"),
    "mechanical_worm": ("boss_summon", "Lote A - Progressão Core"),
    "mechanical_skull": ("boss_summon", "Lote A - Progressão Core"),
    "celestial_sigil": ("boss_summon", "Lote A - Progressão Core"),
    "lihzahrd_power_cell": ("boss_summon", "Lote A - Progressão Core"),
    "temple_key": ("key", "Lote A - Progressão Core"),
    "magic_mirror": ("artifact", "Lote A - Progressão Core"),
    "guide_voodoo_doll": ("artifact", "Lote A - Progressão Core"),

    # Lote B: Special Accessories
    "phoenix_wings": ("wings", "Lote B - Special Accessories"),
    "thunder_fragment": ("accessory", "Lote B - Special Accessories"),
    "void_heart": ("crystal_heart", "Lote B - Special Accessories"),
    "titan_core": ("artifact", "Lote B - Special Accessories"),
    "arcane_prism": ("crystal_heart", "Lote B - Special Accessories"),
    "guardian_seal": ("accessory", "Lote B - Special Accessories"),
    "blood_crystal": ("crystal_heart", "Lote B - Special Accessories"),
    "time_gear": ("coin", "Lote B - Special Accessories"),
    "predator_eye": ("boss_summon", "Lote B - Special Accessories"),
    "gravity_sigil": ("coin", "Lote B - Special Accessories"),
    "band_of_regeneration": ("coin", "Lote B - Special Accessories"),
    "hermes_boots": ("boots", "Lote B - Special Accessories"),
    "terraspark_boots": ("boots", "Lote B - Special Accessories"),
    "warrior_emblem": ("coin", "Lote B - Special Accessories"),
    "ranger_emblem": ("coin", "Lote B - Special Accessories"),
    "sorcerer_emblem": ("coin", "Lote B - Special Accessories"),
    "summoner_emblem": ("coin", "Lote B - Special Accessories"),

    # Lote C: Ammunition
    "wooden_arrow": ("arrow", "Lote C - Munições"),
    "flaming_arrow": ("arrow", "Lote C - Munições"),
    "jester_arrow": ("arrow", "Lote C - Munições"),
    "unholy_arrow": ("arrow", "Lote C - Munições"),
    "musket_ball": ("bullet", "Lote C - Munições"),
    "meteor_shot": ("bullet", "Lote C - Munições"),

    # Lote D: Utilitários & Crates
    "wooden_crate": ("crate", "Lote D - Utilitários"),
    "iron_crate": ("crate", "Lote D - Utilitários"),
    "golden_crate": ("crate", "Lote D - Utilitários"),
    "grappling_hook": ("hook", "Lote D - Utilitários"),
    "ivy_whip": ("hook", "Lote D - Utilitários"),
    "wood_fishing_pole": ("tool", "Lote D - Utilitários"),
    "reinforced_fishing_pole": ("tool", "Lote D - Utilitários"),
    "golden_fishing_rod": ("tool", "Lote D - Utilitários"),
    "worm": ("artifact", "Lote D - Utilitários"),
    "enchanted_nightcrawler": ("artifact", "Lote D - Utilitários"),
    "truffle_worm": ("artifact", "Lote D - Utilitários"),
    "monarch_butterfly": ("wings", "Lote D - Utilitários"),
    "master_bait": ("potion", "Lote D - Utilitários"),
    "slimy_saddle": ("artifact", "Lote D - Utilitários"),
    "portal_gun": ("tool", "Lote D - Utilitários"),
    "pwnhammer": ("tool", "Lote D - Utilitários"),
    "beetle_husk": ("artifact", "Lote D - Utilitários"),

    # Lote E: Armaduras
    "copper_helmet": ("helmet", "Lote E - Armaduras"),
    "copper_chestplate": ("chestplate", "Lote E - Armaduras"),
    "copper_leggings": ("leggings", "Lote E - Armaduras"),
    "copper_boots": ("boots", "Lote E - Armaduras"),
    "shadow_helmet": ("helmet", "Lote E - Armaduras"),
    "shadow_scalemail": ("chestplate", "Lote E - Armaduras"),
    "shadow_greaves": ("leggings", "Lote E - Armaduras"),
    "shadow_boots": ("boots", "Lote E - Armaduras"),
    "crimson_helmet": ("helmet", "Lote E - Armaduras"),
    "crimson_scalemail": ("chestplate", "Lote E - Armaduras"),
    "crimson_greaves": ("leggings", "Lote E - Armaduras"),
    "crimson_boots": ("boots", "Lote E - Armaduras"),
    "molten_helmet": ("helmet", "Lote E - Armaduras"),
    "molten_breastplate": ("chestplate", "Lote E - Armaduras"),
    "molten_greaves": ("leggings", "Lote E - Armaduras"),
    "molten_boots": ("boots", "Lote E - Armaduras")
}

def build_model_json(item_name, geom_type, batch):
    tex = f"terraforge_rpg:item/{item_name}"

    if geom_type == "coin":
        elements = make_coin_elements(tex)
    elif geom_type == "crystal_heart":
        elements = make_crystal_heart_elements(tex)
    elif geom_type == "potion":
        elements = make_potion_elements(tex)
    elif geom_type == "soul":
        elements = make_soul_elements(tex)
    elif geom_type == "ingot":
        elements = make_ingot_elements(tex)
    elif geom_type == "boss_summon":
        elements = make_boss_summon_elements(tex)
    elif geom_type == "key":
        elements = make_key_elements(tex)
    elif geom_type == "accessory":
        elements = make_accessory_elements(tex)
    elif geom_type == "wings":
        elements = make_wings_elements(tex)
    elif geom_type == "arrow":
        elements = make_arrow_elements(tex)
    elif geom_type == "bullet":
        elements = make_bullet_elements(tex)
    elif geom_type == "crate":
        elements = make_crate_elements(tex)
    elif geom_type == "hook":
        elements = make_hook_elements(tex)
    elif geom_type == "helmet":
        elements = make_armor_helmet_elements(tex)
    elif geom_type == "chestplate":
        elements = make_armor_chest_elements(tex)
    elif geom_type == "leggings":
        elements = make_armor_legs_elements(tex)
    elif geom_type == "boots":
        elements = make_armor_boots_elements(tex)
    elif geom_type == "sword":
        elements = make_sword_elements(tex)
    elif geom_type == "bow":
        elements = make_bow_elements(tex)
    elif geom_type == "staff":
        elements = make_staff_elements(tex)
    elif geom_type == "spellbook":
        elements = make_spellbook_elements(tex)
    elif geom_type == "yoyo":
        elements = make_yoyo_elements(tex)
    elif geom_type == "whip":
        elements = make_whip_elements(tex)
    else:
        elements = make_generic_3d_artifact(tex)

    category = "crate" if geom_type == "crate" else ("weapon_melee" if geom_type in ("sword", "whip") else ("weapon_ranged" if geom_type in ("bow", "bullet", "staff") else "item"))
    display = get_display_transforms(category)

    return {
        "credit": "TerraForge RPG 3D Volumetric Engine",
        "textures": {
            "layer0": tex,
            "particle": tex
        },
        "elements": elements,
        "display": display
    }

def main():
    print("=== TerraForge RPG 3D Item Migration & Audit ===")
    audit_rows = []

    # Get all items from models/item
    json_files = sorted(MODELS_DIR.glob("*.json"))
    converted_count = 0
    already_3d_count = 0

    for model_path in json_files:
        item_name = model_path.stem
        with open(model_path, "r", encoding="utf-8") as f:
            try:
                data = json.load(f)
            except Exception:
                data = {}

        parent = data.get("parent", "")
        has_elements = "elements" in data
        has_obj = "loader" in data or "model" in data or ".obj" in str(data)

        # Check if item is scheduled for conversion
        if item_name in BATCH_MAPPING:
            geom_type, batch_name = BATCH_MAPPING[item_name]
            new_model = build_model_json(item_name, geom_type, batch_name)
            with open(model_path, "w", encoding="utf-8") as f:
                json.dump(new_model, f, indent=2)

            elem_count = len(new_model["elements"])
            converted_count += 1
            audit_rows.append({
                "item_id": item_name,
                "dimension": "3D",
                "format": "JSON_ELEMENTS",
                "batch": batch_name,
                "elements_count": elem_count,
                "poly_faces": elem_count * 6,
                "status": "CONVERTED_3D",
                "notes": f"Volumetric 3D geometry ({geom_type}) with isometric display matrices"
            })
        elif has_elements or has_obj or (parent and not parent.endswith("generated")):
            already_3d_count += 1
            format_type = "OBJ_MESH" if has_obj else ("JSON_BLOCK_CHILD" if "block" in parent else "JSON_ELEMENTS")
            elem_count = len(data.get("elements", []))
            audit_rows.append({
                "item_id": item_name,
                "dimension": "3D",
                "format": format_type,
                "batch": "Armas e Blocos 3D",
                "elements_count": elem_count,
                "poly_faces": elem_count * 6 if elem_count > 0 else 0,
                "status": "APPROVED_3D",
                "notes": f"Existing approved 3D model ({format_type})"
            })
        else:
            audit_rows.append({
                "item_id": item_name,
                "dimension": "2D",
                "format": "MINECRAFT_GENERATED",
                "batch": "Residual",
                "elements_count": 0,
                "poly_faces": 0,
                "status": "LEGACY_2D",
                "notes": "2D flat sprite"
            })

    # Write audit CSV
    AUDIT_CSV.parent.mkdir(parents=True, exist_ok=True)
    fieldnames = ["item_id", "dimension", "format", "batch", "elements_count", "poly_faces", "status", "notes"]
    with open(AUDIT_CSV, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(audit_rows)

    total_3d = sum(1 for r in audit_rows if r["dimension"] == "3D")
    total_2d = sum(1 for r in audit_rows if r["dimension"] == "2D")

    print(f"Total Items Audited: {len(audit_rows)}")
    print(f"Items Converted to 3D in this run: {converted_count}")
    print(f"Pre-existing 3D Items: {already_3d_count}")
    print(f"Total 3D Items: {total_3d} ({(total_3d/len(audit_rows))*100:.1f}%)")
    print(f"Remaining 2D Items: {total_2d}")
    print(f"Audit saved to: {AUDIT_CSV}")

if __name__ == "__main__":
    main()
