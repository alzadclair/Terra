import os
import json
from PIL import Image, ImageDraw

BASE_DIR = r"c:\Users\Alza\Desktop\terraforge\src\main\resources\assets\terraforge_rpg"
MODELS_ITEM_DIR = os.path.join(BASE_DIR, "models", "item")
MODELS_BLOCK_DIR = os.path.join(BASE_DIR, "models", "block")
BLOCKSTATES_DIR = os.path.join(BASE_DIR, "blockstates")
TEXTURES_ITEM_DIR = os.path.join(BASE_DIR, "textures", "item")
TEXTURES_BLOCK_DIR = os.path.join(BASE_DIR, "textures", "block")
TEXTURES_ENTITY_DIR = os.path.join(BASE_DIR, "textures", "entity")

for d in [
    MODELS_ITEM_DIR, MODELS_BLOCK_DIR, BLOCKSTATES_DIR,
    TEXTURES_ITEM_DIR, TEXTURES_BLOCK_DIR,
    os.path.join(TEXTURES_ENTITY_DIR, "slime"),
    os.path.join(TEXTURES_ENTITY_DIR, "zombie"),
    os.path.join(TEXTURES_ENTITY_DIR, "eye"),
    os.path.join(TEXTURES_ENTITY_DIR, "boss"),
    os.path.join(TEXTURES_ENTITY_DIR, "goblin"),
    os.path.join(TEXTURES_ENTITY_DIR, "npc"),
    os.path.join(TEXTURES_ENTITY_DIR, "projectile")
]:
    os.makedirs(d, exist_ok=True)

def write_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def create_item_model(item_id):
    path = os.path.join(MODELS_ITEM_DIR, f"{item_id}.json")
    write_json(path, {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"terraforge_rpg:item/{item_id}"
        }
    })

def create_block_assets(block_id, main_color, accent_color, ore_color=None):
    bs_path = os.path.join(BLOCKSTATES_DIR, f"{block_id}.json")
    write_json(bs_path, {"variants": {"": {"model": f"terraforge_rpg:block/{block_id}"}}})
    bm_path = os.path.join(MODELS_BLOCK_DIR, f"{block_id}.json")
    write_json(bm_path, {"parent": "minecraft:block/cube_all", "textures": {"all": f"terraforge_rpg:block/{block_id}"}})
    im_path = os.path.join(MODELS_ITEM_DIR, f"{block_id}.json")
    write_json(im_path, {"parent": f"terraforge_rpg:block/{block_id}"})

    img = Image.new("RGBA", (16, 16), main_color)
    draw = ImageDraw.Draw(img)
    for x in range(16):
        for y in range(16):
            if (x * 7 + y * 13) % 5 == 0:
                draw.point((x, y), fill=accent_color)
            elif (x * 3 + y * 11) % 7 == 0:
                darker = (max(0, main_color[0] - 22), max(0, main_color[1] - 22), max(0, main_color[2] - 22), 255)
                draw.point((x, y), fill=darker)
    if ore_color:
        vein_pixels = [
            (4, 3), (5, 3), (5, 4), (6, 4), (10, 8), (11, 8), (11, 9), (12, 9),
            (3, 11), (4, 11), (4, 12), (9, 4), (10, 4), (7, 13), (8, 13)
        ]
        ore_highlight = (min(255, ore_color[0] + 50), min(255, ore_color[1] + 50), min(255, ore_color[2] + 50), 255)
        for (vx, vy) in vein_pixels:
            draw.point((vx, vy), fill=ore_color)
            if (vx + vy) % 2 == 0:
                draw.point((min(15, vx + 1), vy), fill=ore_highlight)

    img.save(os.path.join(TEXTURES_BLOCK_DIR, f"{block_id}.png"))

def draw_coin(item_id, border_c, fill_c, shine_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((3, 3, 12, 12), fill=fill_c, outline=border_c)
    draw.point((5, 5), fill=shine_c)
    draw.point((6, 5), fill=shine_c)
    draw.point((5, 6), fill=shine_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_sword(item_id, blade_c, edge_c, guard_c, grip_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Diagonal sword from bottom-left to top-right
    for i in range(4, 14):
        draw.point((i, 15 - i), fill=blade_c)
        draw.point((i - 1, 15 - i), fill=edge_c)
        draw.point((i, 16 - i), fill=edge_c)
    draw.point((14, 1), fill=edge_c)
    # Crossguard
    draw.point((4, 10), fill=guard_c)
    draw.point((3, 11), fill=guard_c)
    draw.point((5, 12), fill=guard_c)
    draw.point((6, 11), fill=guard_c)
    # Hilt / Grip
    draw.point((2, 13), fill=grip_c)
    draw.point((1, 14), fill=grip_c)
    draw.point((0, 15), fill=guard_c) # Pommel
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_bow(item_id, wood_c, wood_light, string_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    bow_pts = [(13, 2), (11, 4), (9, 7), (8, 9), (7, 11), (5, 13), (3, 14)]
    for (x, y) in bow_pts:
        draw.point((x, y), fill=wood_c)
        draw.point((x + 1, y), fill=wood_light)
    # String
    for i in range(2, 15):
        draw.point((2 + (i // 5), i), fill=string_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_staff(item_id, shaft_c, gem_c, gem_glow):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for i in range(2, 14):
        draw.point((i, 15 - i), fill=shaft_c)
    # Gem orb at top
    draw.ellipse((11, 1, 14, 4), fill=gem_c)
    draw.point((12, 2), fill=gem_glow)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_arrow(item_id, tip_c, shaft_c, feather_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for i in range(3, 13):
        draw.point((i, 15 - i), fill=shaft_c)
    # Tip
    draw.point((13, 1), fill=tip_c)
    draw.point((14, 1), fill=tip_c)
    draw.point((14, 2), fill=tip_c)
    draw.point((13, 3), fill=tip_c)
    draw.point((12, 2), fill=tip_c)
    # Fletching
    draw.point((2, 14), fill=feather_c)
    draw.point((1, 13), fill=feather_c)
    draw.point((3, 15), fill=feather_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_bullet(item_id, shell_c, tip_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((5, 7, 9, 12), fill=shell_c)
    draw.polygon([(5, 7), (9, 7), (7, 4)], fill=tip_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_crystal(item_id, base_c, glow_c, outline_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Heart shape or crystal shape
    draw.polygon([(4, 4), (7, 2), (8, 4), (9, 2), (12, 4), (8, 13)], fill=base_c, outline=outline_c)
    draw.point((6, 5), fill=glow_c)
    draw.point((7, 5), fill=glow_c)
    draw.point((6, 6), fill=glow_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_potion(item_id, liquid_c, liquid_shine):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Flask neck & cork
    draw.rectangle((7, 2, 8, 3), fill=(180, 140, 90, 255))
    draw.rectangle((6, 4, 9, 6), fill=(200, 220, 240, 200))
    # Body
    draw.ellipse((3, 6, 12, 14), fill=liquid_c, outline=(220, 240, 255, 255))
    draw.point((5, 9), fill=liquid_shine)
    draw.point((6, 9), fill=liquid_shine)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_bar(item_id, bar_c, shine_c, shadow_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon([(2, 10), (5, 6), (13, 6), (10, 10)], fill=shine_c)
    draw.polygon([(2, 10), (10, 10), (11, 13), (3, 13)], fill=bar_c)
    draw.polygon([(10, 10), (13, 6), (14, 9), (11, 13)], fill=shadow_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_soul(item_id, core_c, glow_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((4, 4, 11, 11), fill=glow_c)
    draw.ellipse((5, 5, 10, 10), fill=core_c)
    draw.point((7, 6), fill=(255, 255, 255, 255))
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_armor_piece(item_id, piece_type, base_c, highlight_c, dark_c):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    if "helmet" in piece_type:
        draw.rectangle((4, 3, 11, 11), fill=base_c, outline=dark_c)
        draw.rectangle((5, 7, 10, 8), fill=(20, 20, 30, 255)) # Visor
        draw.point((5, 4), fill=highlight_c)
    elif "chestplate" in piece_type or "scalemail" in piece_type or "breastplate" in piece_type:
        draw.polygon([(3, 3), (12, 3), (14, 7), (11, 13), (4, 13), (1, 7)], fill=base_c, outline=dark_c)
        draw.point((6, 5), fill=highlight_c)
        draw.point((7, 5), fill=highlight_c)
    elif "leggings" in piece_type or "greaves" in piece_type:
        draw.rectangle((4, 3, 11, 6), fill=base_c, outline=dark_c)
        draw.rectangle((4, 7, 7, 13), fill=base_c, outline=dark_c)
        draw.rectangle((8, 7, 11, 13), fill=base_c, outline=dark_c)
        draw.point((5, 8), fill=highlight_c)
        draw.point((9, 8), fill=highlight_c)
    elif "boots" in piece_type:
        draw.rectangle((3, 7, 6, 13), fill=base_c, outline=dark_c)
        draw.rectangle((9, 7, 12, 13), fill=base_c, outline=dark_c)
        draw.point((4, 8), fill=highlight_c)
        draw.point((10, 8), fill=highlight_c)
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

def draw_generic_icon(item_id, primary_c, secondary_c, shape="circle"):
    create_item_model(item_id)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    if shape == "circle":
        draw.ellipse((3, 3, 12, 12), fill=primary_c, outline=secondary_c)
        draw.point((6, 6), fill=(255, 255, 255, 255))
    elif shape == "square":
        draw.rectangle((3, 3, 12, 12), fill=primary_c, outline=secondary_c)
        draw.point((5, 5), fill=(255, 255, 255, 255))
    elif shape == "diamond":
        draw.polygon([(8, 2), (14, 8), (8, 14), (2, 8)], fill=primary_c, outline=secondary_c)
        draw.point((8, 6), fill=(255, 255, 255, 255))
    img.save(os.path.join(TEXTURES_ITEM_DIR, f"{item_id}.png"))

# 1. COINS
draw_coin("copper_coin", (130, 70, 30, 255), (184, 115, 51, 255), (230, 160, 100, 255))
draw_coin("silver_coin", (100, 110, 120, 255), (192, 197, 206, 255), (245, 250, 255, 255))
draw_coin("gold_coin", (160, 100, 10, 255), (234, 179, 8, 255), (254, 245, 140, 255))
draw_coin("platinum_coin", (10, 120, 150, 255), (6, 182, 212, 255), (180, 245, 255, 255))

# 2. WEAPONS
draw_sword("copper_shortsword", (184, 115, 51, 255), (220, 150, 80, 255), (120, 70, 30, 255), (90, 50, 20, 255))
draw_bow("wooden_bow", (139, 90, 43, 255), (180, 120, 60, 255), (230, 230, 230, 255))
draw_staff("wand_of_sparking", (150, 40, 30, 255), (245, 180, 30, 255), (255, 240, 120, 255))
draw_staff("slime_staff", (120, 80, 40, 255), (40, 140, 240, 220), (140, 210, 255, 255))
draw_generic_icon("wooden_yoyo", (139, 90, 43, 255), (80, 50, 20, 255), "circle")
draw_generic_icon("leather_whip", (120, 70, 30, 255), (70, 40, 15, 255), "diamond")

# 3. AMMUNITION
draw_arrow("wooden_arrow", (120, 120, 120, 255), (139, 90, 43, 255), (230, 230, 230, 255))
draw_arrow("flaming_arrow", (245, 100, 20, 255), (139, 90, 43, 255), (255, 220, 60, 255))
draw_arrow("jester_arrow", (180, 70, 240, 255), (230, 230, 230, 255), (250, 240, 80, 255))
draw_arrow("unholy_arrow", (100, 30, 140, 255), (50, 20, 80, 255), (160, 60, 220, 255))
draw_bullet("musket_ball", (140, 145, 155, 255), (180, 185, 195, 255))
draw_bullet("meteor_shot", (210, 80, 20, 255), (255, 180, 40, 255))

# 4. BOSS SUMMON ITEMS
draw_generic_icon("suspicious_looking_eye", (240, 240, 240, 255), (180, 20, 30, 255), "circle")
draw_generic_icon("slime_crown", (60, 140, 240, 255), (240, 200, 30, 255), "square")
draw_generic_icon("guide_voodoo_doll", (140, 90, 50, 255), (180, 30, 30, 255), "diamond")
draw_generic_icon("mechanical_eye", (160, 170, 180, 255), (230, 30, 30, 255), "circle")
draw_generic_icon("mechanical_worm", (170, 175, 185, 255), (100, 105, 115, 255), "diamond")
draw_generic_icon("mechanical_skull", (180, 185, 195, 255), (220, 20, 20, 255), "square")
draw_generic_icon("celestial_sigil", (240, 210, 40, 255), (60, 180, 240, 255), "diamond")
draw_generic_icon("lihzahrd_power_cell", (230, 180, 30, 255), (140, 100, 10, 255), "square")

# 5. CRAFTING MATERIALS & SOULS
draw_bar("hallowed_bar", (220, 230, 245, 255), (255, 255, 255, 255), (160, 175, 200, 255))
draw_bar("luminite_bar", (40, 160, 160, 255), (120, 240, 240, 255), (20, 90, 100, 255))
draw_soul("soul_of_sight", (60, 230, 120, 255), (140, 255, 180, 180))
draw_soul("soul_of_might", (40, 120, 240, 255), (120, 190, 255, 180))
draw_soul("soul_of_fright", (230, 30, 60, 255), (255, 120, 140, 180))
draw_generic_icon("beetle_husk", (40, 80, 160, 255), (100, 160, 240, 255), "diamond")
draw_generic_icon("pwnhammer", (240, 190, 30, 255), (160, 110, 10, 255), "square")

# 6. EMBLEMS
draw_generic_icon("warrior_emblem", (220, 60, 30, 255), (140, 30, 10, 255), "diamond")
draw_generic_icon("ranger_emblem", (50, 180, 60, 255), (20, 100, 30, 255), "diamond")
draw_generic_icon("sorcerer_emblem", (60, 120, 240, 255), (20, 60, 160, 255), "diamond")
draw_generic_icon("summoner_emblem", (180, 60, 230, 255), (100, 20, 140, 255), "diamond")

# 7. CRYSTALS & POTIONS
draw_crystal("life_crystal", (225, 29, 72, 255), (251, 113, 133, 255), (159, 18, 57, 255))
draw_crystal("life_fruit", (245, 158, 11, 255), (253, 230, 138, 255), (180, 83, 9, 255))
draw_crystal("mana_crystal", (14, 165, 233, 255), (125, 211, 252, 255), (3, 105, 161, 255))
draw_potion("lesser_mana_potion", (37, 99, 235, 220), (147, 197, 253, 255))
draw_potion("mana_potion", (29, 78, 216, 230), (191, 219, 254, 255))

# 8. THE 10 SPECIAL ACCESSORIES
draw_generic_icon("phoenix_wings", (249, 115, 22, 255), (194, 65, 12, 255), "diamond")
draw_generic_icon("thunder_fragment", (250, 204, 21, 255), (161, 98, 7, 255), "diamond")
draw_generic_icon("void_heart", (76, 29, 149, 255), (192, 132, 252, 255), "circle")
draw_generic_icon("titan_core", (55, 65, 81, 255), (239, 68, 68, 255), "square")
draw_generic_icon("arcane_prism", (168, 85, 247, 255), (236, 72, 153, 255), "diamond")
draw_generic_icon("guardian_seal", (245, 158, 11, 255), (71, 85, 105, 255), "diamond")
draw_generic_icon("blood_crystal", (190, 18, 60, 255), (244, 63, 94, 255), "diamond")
draw_generic_icon("time_gear", (217, 119, 6, 255), (120, 53, 15, 255), "circle")
draw_generic_icon("predator_eye", (234, 179, 8, 255), (17, 24, 39, 255), "circle")
draw_generic_icon("gravity_sigil", (99, 102, 241, 255), (165, 180, 252, 255), "diamond")

# 9. UTILITY & FISHING
draw_generic_icon("magic_mirror", (56, 189, 248, 255), (148, 163, 184, 255), "circle")
draw_generic_icon("temple_key", (245, 158, 11, 255), (180, 83, 9, 255), "square")
draw_generic_icon("portal_gun", (241, 245, 249, 255), (56, 189, 248, 255), "square")
draw_generic_icon("wood_fishing_pole", (139, 90, 43, 255), (180, 120, 60, 255), "diamond")
draw_generic_icon("reinforced_fishing_pole", (100, 116, 139, 255), (148, 163, 184, 255), "diamond")
draw_generic_icon("golden_fishing_rod", (234, 179, 8, 255), (253, 224, 71, 255), "diamond")
draw_generic_icon("monarch_butterfly", (249, 115, 22, 255), (17, 24, 39, 255), "diamond")
draw_generic_icon("worm", (244, 114, 182, 255), (157, 23, 77, 255), "circle")
draw_generic_icon("enchanted_nightcrawler", (56, 189, 248, 255), (30, 58, 138, 255), "circle")
draw_generic_icon("master_bait", (34, 197, 94, 255), (21, 128, 61, 255), "circle")
draw_generic_icon("truffle_worm", (59, 130, 246, 255), (96, 165, 250, 255), "circle")
draw_generic_icon("wooden_crate", (139, 90, 43, 255), (90, 50, 20, 255), "square")
draw_generic_icon("iron_crate", (148, 163, 184, 255), (71, 85, 105, 255), "square")
draw_generic_icon("golden_crate", (234, 179, 8, 255), (161, 98, 7, 255), "square")
draw_generic_icon("grappling_hook", (100, 116, 139, 255), (203, 213, 225, 255), "diamond")
draw_generic_icon("ivy_whip", (34, 197, 94, 255), (21, 128, 61, 255), "diamond")
draw_generic_icon("slimy_saddle", (59, 130, 246, 255), (139, 90, 43, 255), "square")

# 10. ARMOR SETS
armor_sets = [
    ("copper", (184, 115, 51, 255), (217, 144, 88, 255), (120, 70, 30, 255), ["helmet", "chestplate", "leggings", "boots"]),
    ("shadow", (88, 28, 135, 255), (147, 51, 234, 255), (59, 7, 100, 255), ["helmet", "scalemail", "greaves", "boots"]),
    ("crimson", (159, 18, 57, 255), (225, 29, 72, 255), (76, 5, 25, 255), ["helmet", "scalemail", "greaves", "boots"]),
    ("molten", (67, 20, 7, 255), (249, 115, 22, 255), (26, 10, 5, 255), ["helmet", "breastplate", "greaves", "boots"])
]
for prefix, base, light, dark, pieces in armor_sets:
    for p in pieces:
        draw_armor_piece(f"{prefix}_{p}", p, base, light, dark)

# 11. EXPANDED & ENDGAME ARSENAL
draw_sword("nights_edge", (107, 33, 168, 255), (168, 85, 247, 255), (88, 28, 135, 255), (59, 7, 100, 255))
draw_sword("excalibur", (234, 179, 8, 255), (254, 240, 138, 255), (202, 138, 4, 255), (161, 98, 7, 255))
draw_sword("terra_blade", (22, 163, 74, 255), (74, 222, 128, 255), (234, 179, 8, 255), (20, 83, 45, 255))
draw_sword("meowmere", (244, 114, 182, 255), (251, 207, 232, 255), (56, 189, 248, 255), (219, 39, 119, 255))
draw_bow("tsunami", (30, 64, 175, 255), (56, 189, 248, 255), (147, 197, 253, 255))
draw_generic_icon("minishark", (100, 116, 139, 255), (51, 65, 85, 255), "square")
draw_generic_icon("megashark", (71, 85, 105, 255), (30, 41, 59, 255), "square")
draw_generic_icon("water_bolt", (37, 99, 235, 255), (191, 219, 254, 255), "square")
draw_generic_icon("space_gun", (226, 232, 240, 255), (6, 182, 212, 255), "square")
draw_generic_icon("hermes_boots", (180, 83, 9, 255), (253, 224, 71, 255), "diamond")
draw_generic_icon("band_of_regeneration", (225, 29, 72, 255), (148, 163, 184, 255), "circle")
draw_generic_icon("terraspark_boots", (16, 185, 129, 255), (245, 158, 11, 255), "diamond")

# 12. BLOCKS & ORES
stone = (120, 120, 120, 255)
stone_dark = (90, 90, 90, 255)
create_block_assets("copper_ore", stone, stone_dark, (184, 115, 51, 255))
create_block_assets("tin_ore", stone, stone_dark, (168, 162, 158, 255))
create_block_assets("ash_block", (45, 45, 48, 255), (30, 30, 32, 255))
create_block_assets("hellstone_ore", (50, 15, 10, 255), (35, 10, 5, 255), (249, 115, 22, 255))
create_block_assets("ebonstone_block", (60, 35, 80, 255), (40, 20, 55, 255))
create_block_assets("crimstone_block", (90, 20, 25, 255), (60, 10, 15, 255))
create_block_assets("demon_altar", (45, 25, 60, 255), (130, 40, 180, 255))
create_block_assets("crimson_altar", (80, 20, 30, 255), (180, 30, 50, 255))
create_block_assets("cobalt_ore", stone, stone_dark, (37, 99, 235, 255))
create_block_assets("palladium_ore", stone, stone_dark, (234, 88, 12, 255))
create_block_assets("mythril_ore", stone, stone_dark, (5, 150, 105, 255))
create_block_assets("orichalcum_ore", stone, stone_dark, (219, 39, 119, 255))
create_block_assets("adamantite_ore", stone, stone_dark, (225, 29, 72, 255))
create_block_assets("titanium_ore", stone, stone_dark, (148, 163, 184, 255))
create_block_assets("luminite_ore", (15, 23, 42, 255), (30, 41, 59, 255), (6, 182, 212, 255))

# 13. ENTITY TEXTURES (64x64 RGBA)
def create_entity_texture(subfolder, filename, base_c, accent_c):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 63, 63), fill=base_c)
    draw.rectangle((8, 8, 24, 24), fill=accent_c)
    draw.rectangle((36, 8, 52, 24), fill=accent_c)
    draw.rectangle((16, 36, 48, 52), fill=accent_c)
    folder = os.path.join(TEXTURES_ENTITY_DIR, subfolder)
    os.makedirs(folder, exist_ok=True)
    img.save(os.path.join(folder, filename))

# Slimes
create_entity_texture("slime", "green_slime.png", (34, 197, 94, 200), (22, 101, 52, 255))
create_entity_texture("slime", "blue_slime.png", (59, 130, 246, 200), (30, 64, 175, 255))
create_entity_texture("slime", "king_slime.png", (37, 99, 235, 210), (234, 179, 8, 255))
create_entity_texture("slime", "slime_mount.png", (59, 130, 246, 200), (139, 90, 43, 255))

# Zombies & Goblins
create_entity_texture("zombie", "terra_zombie.png", (74, 107, 70, 255), (40, 60, 40, 255))
for g in ["goblin_peon", "goblin_thief", "goblin_warrior", "goblin_sorcerer"]:
    create_entity_texture("goblin", f"{g}.png", (84, 130, 53, 255), (120, 53, 15, 255))

# NPCs
for npc in ["guide", "merchant", "nurse", "goblin_tinkerer"]:
    create_entity_texture("npc", f"{npc}.png", (180, 140, 110, 255), (60, 80, 120, 255))

# Flying Eyes
create_entity_texture("eye", "demon_eye.png", (240, 240, 240, 255), (185, 28, 28, 255))
create_entity_texture("eye", "servant_of_cthulhu.png", (240, 240, 240, 255), (153, 27, 27, 255))

# Bosses
create_entity_texture("boss", "eye_of_cthulhu_p1.png", (240, 240, 240, 255), (185, 28, 28, 255))
create_entity_texture("boss", "eye_of_cthulhu_p2.png", (153, 27, 27, 255), (254, 242, 242, 255))
create_entity_texture("boss", "wall_of_flesh.png", (136, 19, 55, 255), (225, 29, 72, 255))
create_entity_texture("boss", "the_hungry.png", (159, 18, 57, 255), (244, 63, 94, 255))
create_entity_texture("boss", "retinazer.png", (160, 175, 190, 255), (220, 38, 38, 255))
create_entity_texture("boss", "spazmatism.png", (160, 175, 190, 255), (22, 163, 74, 255))
create_entity_texture("boss", "destroyer_probe.png", (148, 163, 184, 255), (239, 68, 68, 255))
create_entity_texture("boss", "the_destroyer.png", (100, 116, 139, 255), (220, 38, 38, 255))
create_entity_texture("boss", "skeletron_prime.png", (180, 185, 195, 255), (220, 38, 38, 255))
create_entity_texture("boss", "plantera.png", (236, 72, 153, 255), (22, 163, 74, 255))
create_entity_texture("boss", "golem.png", (180, 83, 9, 255), (245, 158, 11, 255))
create_entity_texture("boss", "duke_fishron.png", (14, 165, 233, 255), (244, 63, 94, 255))
create_entity_texture("boss", "moon_lord.png", (15, 118, 110, 255), (45, 212, 191, 255))

# Projectiles
create_entity_texture("projectile", "projectile.png", (254, 240, 138, 255), (234, 179, 8, 255))
create_entity_texture("projectile", "yoyo.png", (139, 90, 43, 255), (245, 245, 245, 255))
create_entity_texture("projectile", "hook.png", (100, 116, 139, 255), (203, 213, 225, 255))

print("ALL ASSETS SUCCESSFULLY GENERATED!")
