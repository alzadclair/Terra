import os
from PIL import Image

SRC_P1_TEX = r"C:\model 3d\_processed\06664c90cf3e4d24a74a43dc771ae74f\source\textures"
SRC_P2_TEX = r"C:\model 3d\_processed\a53f70fa34284699a57af3d161182611\source\textures"
DEST_DIR = r"C:\Users\Alza\Desktop\terraforge\src\main\resources\assets\terraforge_rpg\textures\entity\boss\eye"

os.makedirs(DEST_DIR, exist_ok=True)

# 1. P1 Body, Iris, Pupil
v2 = Image.open(os.path.join(SRC_P1_TEX, "boss_eye_cthulhu_v2_baseColor.png")).convert("RGBA")
v2.save(os.path.join(DEST_DIR, "p1_body.png"))
v2.save(os.path.join(DEST_DIR, "p1_iris.png"))
v2.save(os.path.join(DEST_DIR, "p1_pupil.png"))
print("Saved p1_body, p1_iris, p1_pupil (1024x1024)")

# 2. P1 Stalk
stalk_p1 = Image.open(os.path.join(SRC_P1_TEX, "boss_eye_cthulhu_stalk_v2_baseColor.png")).convert("RGBA")
stalk_p1.save(os.path.join(DEST_DIR, "p1_stalk.png"))
print("Saved p1_stalk (1024x1024)")

# 3. P1 Glass (translucent cornea)
glass = Image.open(os.path.join(SRC_P1_TEX, "boss_eye_cthulhu_glass_v2_baseColor.png")).convert("RGBA")
# Apply subtle cornea translucency (alpha ~ 35 out of 255)
r, g, b, _ = glass.split()
a = Image.new("L", glass.size, 35)
glass_translucent = Image.merge("RGBA", (r, g, b, a))
glass_translucent.save(os.path.join(DEST_DIR, "p1_glass.png"))
print("Saved p1_glass (128x128 with alpha=35)")

# 4. P2 Body
body_p2 = Image.open(os.path.join(SRC_P2_TEX, "boss_eye_cthulhu_phase2_v2_baseColor.png")).convert("RGBA")
body_p2.save(os.path.join(DEST_DIR, "p2_body.png"))
print("Saved p2_body (1024x1024)")

# 5. P2 Stalk
stalk_p2 = Image.open(os.path.join(SRC_P2_TEX, "boss_eye_cthulhu_stalk_v2_baseColor.png")).convert("RGBA")
stalk_p2.save(os.path.join(DEST_DIR, "p2_stalk.png"))
print("Saved p2_stalk (1024x1024)")

# 6. P2 Teeth & Inner Teeth
teeth = Image.open(os.path.join(SRC_P2_TEX, "boss_eye_cthulhu_teeth_baseColor.png")).convert("RGBA")
teeth.save(os.path.join(DEST_DIR, "p2_teeth.png"))
teeth.save(os.path.join(DEST_DIR, "p2_inner_teeth.png"))
print("Saved p2_teeth, p2_inner_teeth (1024x1024)")

print("All per-part textures installed successfully.")
