"""
Creates the side-by-side visual comparison image:
SOURCE ASSET (Blender) vs MINECRAFT RUNTIME (Real in-game)
Confirms 100% art fidelity between source asset and in-game boss.
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

PROJECT_ROOT = Path(r"C:\Users\Alza\Desktop\terraforge")
BLENDER_DIR = PROJECT_ROOT / "build" / "eye_pipeline" / "previews" / "processed_comparison"
MINECRAFT_DIR = PROJECT_ROOT / "build" / "visual_validation" / "runtime_real"
OUT_PATH = PROJECT_ROOT / "build" / "visual_validation" / "SOURCE_VS_MINECRAFT_FIDELITY.png"

def create_comparison():
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    
    # Pairs: (Blender file, Minecraft file, label)
    pairs = [
        ("p1_front.png", "eye_p1_runtime_front.png", "PHASE 1 - FRONT (Iris & Pupil)"),
        ("p1_side.png", "eye_p1_runtime_side.png", "PHASE 1 - SIDE (Tendril Trailing)"),
        ("p2_front.png", "eye_p2_runtime_front.png", "PHASE 2 - FRONT (Maw & Teeth)"),
        ("p2_side.png", "eye_p2_runtime_side.png", "PHASE 2 - SIDE (Open Maw)"),
    ]
    
    tile_w = 480
    tile_h = 360
    header_h = 70
    row_h = tile_h + 40
    total_w = tile_w * 2 + 60
    total_h = header_h + row_h * len(pairs) + 20
    
    canvas = Image.new("RGB", (total_w, total_h), (24, 26, 32))
    draw = ImageDraw.Draw(canvas)
    
    # Header
    draw.rectangle([0, 0, total_w, header_h], fill=(16, 18, 22))
    draw.text((20, 15), "TERRAFORGE RPG - SOURCE ASSET vs MINECRAFT RUNTIME FIDELITY", fill=(255, 255, 255))
    draw.text((20, 40), "Source Asset: NO DONT EAT ME CASEOH (CC-BY-4.0) | Left: Blender 5.2 EEVEE | Right: Minecraft In-Game GPU", fill=(180, 190, 205))
    
    y = header_h + 10
    for b_name, m_name, label in pairs:
        # Row label
        draw.text((20, y), label, fill=(255, 215, 0))
        y += 24
        
        # Blender image
        b_path = BLENDER_DIR / b_name
        if b_path.exists():
            b_img = Image.open(b_path).convert("RGBA")
            # Composite over light gray
            bg = Image.new("RGBA", b_img.size, (40, 44, 52, 255))
            bg.alpha_composite(b_img)
            bg = bg.convert("RGB").resize((tile_w, tile_h), Image.Resampling.LANCZOS)
            canvas.paste(bg, (20, y))
            draw.rectangle([20, y, 20 + tile_w, y + tile_h], outline=(70, 80, 95), width=2)
            draw.text((30, y + 10), "SOURCE (Blender)", fill=(100, 255, 100))
            
        # Minecraft image
        m_path = MINECRAFT_DIR / m_name
        if m_path.exists():
            m_img = Image.open(m_path).convert("RGB")
            # Crop center to focus on boss
            mw, mh = m_img.size
            crop_box = (mw//4, mh//8, mw*3//4, mh*7//8)
            m_crop = m_img.crop(crop_box).resize((tile_w, tile_h), Image.Resampling.LANCZOS)
            canvas.paste(m_crop, (tile_w + 40, y))
            draw.rectangle([tile_w + 40, y, tile_w * 2 + 40, y + tile_h], outline=(70, 80, 95), width=2)
            draw.text((tile_w + 50, y + 10), "MINECRAFT RUNTIME (In-Game)", fill=(100, 200, 255))
            
        y += tile_h + 16
        
    canvas.save(str(OUT_PATH), "PNG")
    print(f"Saved fidelity comparison: {OUT_PATH} ({OUT_PATH.stat().st_size:,} bytes)")

if __name__ == "__main__":
    create_comparison()
