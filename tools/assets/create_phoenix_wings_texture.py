"""
Generates high-resolution 64x64 fiery phoenix wings texture for PhoenixWingsModel.
Includes flame gradients, feather edges, glowing gold spine anchor, and incandescent wing tips.
"""

from PIL import Image, ImageDraw
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
TARGET_PATH = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/textures/entity/phoenix_wings.png"

def create_wings_texture():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # 1. Root harness / anchor (top left: 0..16, 0..16)
    for y in range(16):
        for x in range(16):
            if 4 <= x <= 12 and 4 <= y <= 12:
                # Gold/brass harness with crimson core
                if (x == 4 or x == 12 or y == 4 or y == 12):
                    im.putpixel((x, y), (235, 175, 45, 255))
                elif (x == 8 and y == 8):
                    im.putpixel((x, y), (255, 240, 180, 255))
                else:
                    im.putpixel((x, y), (180, 40, 20, 255))

    # 2. Left Wing (X: 0..32, Y: 16..64)
    for y in range(16, 64):
        v = (y - 16) / 48.0
        for x in range(0, 32):
            u = x / 32.0
            dist_tip = (u * 0.7 + v * 0.3)
            # Radiant fire gradient
            if dist_tip > 0.85:
                # Brilliant yellow-white flame crest
                r, g, b = 255, 245, 160
            elif dist_tip > 0.65:
                # Blazing orange
                r, g, b = 255, 140, 25
            elif dist_tip > 0.35:
                # Crimson-scarlet feather body
                r, g, b = 215, 45, 20
            else:
                # Deep molten dark ember base
                r, g, b = 135, 20, 25

            # Feather feathering pattern
            feather_fringe = ((x + y) % 4 == 0)
            if feather_fringe:
                r = min(255, r + 20)
                g = min(255, g + 25)

            im.putpixel((x, y), (r, g, b, 255))

    # 3. Right Wing (X: 32..64, Y: 16..64) - Mirror
    for y in range(16, 64):
        for x in range(32, 64):
            src_x = 63 - x
            pix = im.getpixel((src_x, y))
            im.putpixel((x, y), pix)

    TARGET_PATH.parent.mkdir(parents=True, exist_ok=True)
    im.save(TARGET_PATH, "PNG")
    print(f"Generated Phoenix Wings texture: {TARGET_PATH} (64x64 RGBA)")

if __name__ == "__main__":
    create_wings_texture()
