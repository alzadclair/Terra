"""
Generate a 128x128 Lihzahrd Temple Golem texture for TerraForge RPG.
Colors:
- Rich warm Lihzahrd stone / terracotta brick (hex #8B4513, #A0522D, #B86230, #6E3310)
- Mossy green patches in cracks (#3B5323, #4A6B2F)
- Golden carved ornamentation (#DAA520, #FFD700, #B8860B)
- Solar orange / molten core & glowing eyes (#FF4500, #FF8C00, #FFA500, #FFFF70)
"""
import os
import math
import random
from PIL import Image, ImageDraw

def create_golem_texture(output_path: str):
    width, height = 128, 128
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Base brick texture palette
    c_stone_dark = (90, 42, 18, 255)
    c_stone_mid = (142, 70, 32, 255)
    c_stone_light = (175, 92, 45, 255)
    c_stone_highlight = (205, 115, 60, 255)
    c_mortar = (55, 26, 12, 255)
    c_moss = (58, 82, 35, 255)
    c_gold = (218, 165, 32, 255)
    c_gold_bright = (255, 215, 0, 255)
    c_solar_bright = (255, 240, 100, 255)
    c_solar_orange = (255, 120, 20, 255)
    c_solar_red = (210, 40, 10, 255)

    random.seed(42)

    # Fill background areas with noisy temple brick
    for y in range(height):
        for x in range(width):
            # Brick pattern
            brick_h = 4
            brick_w = 8
            row = y // brick_h
            col = (x + (row % 2) * 4) // brick_w
            
            is_edge = (y % brick_h == 0) or ((x + (row % 2) * 4) % brick_w == 0)
            
            if is_edge:
                color = c_mortar
            else:
                noise = random.randint(-15, 15)
                r = min(255, max(0, c_stone_mid[0] + noise))
                g = min(255, max(0, c_stone_mid[1] + noise // 2))
                b = min(255, max(0, c_stone_mid[2] + noise // 3))
                # Occasional moss
                if random.random() < 0.04:
                    color = c_moss
                else:
                    color = (r, g, b, 255)
            img.putpixel((x, y), color)

    # Decorate Head region: (0, 0) to (64, 36)
    # Head front face is at offset around x=14 to x=30, y=14 to y=28 (size 16x14)
    # Let's paint Golem face: glowing golden solar eyes and mouth slit
    for y in range(16, 26):
        for x in range(18, 28):
            # Left Eye: x in [19, 21], y in [18, 20]
            if 19 <= x <= 21 and 18 <= y <= 20:
                img.putpixel((x, y), c_solar_bright if (x == 20 and y == 19) else c_solar_orange)
            # Right Eye: x in [24, 26], y in [18, 20]
            elif 24 <= x <= 26 and 18 <= y <= 20:
                img.putpixel((x, y), c_solar_bright if (x == 25 and y == 19) else c_solar_orange)
            # Mouth / grill: y in [23, 24], x in [20, 25]
            elif 20 <= x <= 25 and 23 <= y <= 24:
                img.putpixel((x, y), c_solar_red if (x in (21, 24)) else c_mortar)
            # Gold brow / forehead crest
            elif 18 <= x <= 27 and y == 17:
                img.putpixel((x, y), c_gold_bright)

    # Decorate Body region: (0, 36) to (64, 72)
    # Front face of body: x in [16, 40], y in [52, 70]
    # Lihzahrd Sun Altar disk / core in center of chest
    center_x, center_y = 28, 61
    for dy in range(-6, 7):
        for dx in range(-6, 7):
            dist = math.hypot(dx, dy)
            px = center_x + dx
            py = center_y + dy
            if 0 <= px < width and 0 <= py < height:
                if dist <= 2.2:
                    img.putpixel((px, py), c_solar_bright)
                elif dist <= 3.8:
                    img.putpixel((px, py), c_solar_orange)
                elif dist <= 5.2:
                    img.putpixel((px, py), c_gold_bright)
                elif dist <= 6.2:
                    img.putpixel((px, py), c_mortar)

    # Decorate Fists (64, 28 to 128, 64) with golden knuckled plates
    for fist_base_x in (74, 106):
        for y in range(40, 52):
            for x in range(fist_base_x, fist_base_x + 10):
                if y in (42, 43) or x in (fist_base_x + 1, fist_base_x + 8):
                    img.putpixel((x, y), c_gold)
                elif y in (46, 47) and fist_base_x + 3 <= x <= fist_base_x + 6:
                    img.putpixel((x, y), c_solar_orange)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"Generated Golem texture at {output_path} ({width}x{height})")

if __name__ == "__main__":
    out = "src/main/resources/assets/terraforge_rpg/textures/entity/boss/golem.png"
    create_golem_texture(out)
