import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from PIL import Image
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
MODELS_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/models/entity/boss"
TEXTURES_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/textures/entity/boss"
OUT_DIR = PROJECT_ROOT / "build/visual_validation/runtime"
OUT_DIR.mkdir(parents=True, exist_ok=True)

with open(MODELS_DIR / "eye_of_cthulhu_p1.skin.json", "r", encoding="utf-8") as f:
    p1_data = json.load(f)

img = Image.open(TEXTURES_DIR / "eye_of_cthulhu_p1.png").convert("RGBA")
w_tex, h_tex = img.size
tex_arr = np.array(img)

BASE_SCALE = 0.007

def rot_x(deg):
    r = np.radians(deg)
    c, s = np.cos(r), np.sin(r)
    return np.array([[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]], dtype=np.float64)

def rot_y(deg):
    r = np.radians(deg)
    c, s = np.cos(r), np.sin(r)
    return np.array([[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]], dtype=np.float64)

# In Minecraft space, model is rotated 90 deg around X (rot_x(90)) then scaled by BASE_SCALE
R90 = rot_x(90.0)

raw_bones = [b["name"] if isinstance(b, dict) else b for b in p1_data["bones"]]
inv_bind = [np.array(m, dtype=np.float64).reshape((4, 4), order="F") for m in p1_data["inverseBindMatrices"]]
bind_p1 = {bname: np.linalg.inv(inv_bind[i]) for i, bname in enumerate(raw_bones)}
bone_palette = [bind_p1[bname] @ inv_bind[i] for i, bname in enumerate(raw_bones)]

def get_part_mesh(part_name, honor_alpha=False):
    matching_parts = [p for p in p1_data["parts"] if part_name == "all" or p["name"] == part_name]
    tri_verts = []
    tri_colors = []

    for part in matching_parts:
        pos = np.array(part["positions"]).reshape((-1, 3))
        uvs = np.array(part["uvs"]).reshape((-1, 2))
        b_idx = np.array(part["boneIndices"]).reshape((-1, 4))
        b_w = np.array(part["boneWeights"]).reshape((-1, 4))
        indices = part["indices"]

        # Skin vertices
        skinned = np.zeros_like(pos)
        v_colors = np.zeros((len(pos), 4))

        for v in range(len(pos)):
            p_homo = np.append(pos[v], 1.0)
            p_out = np.zeros(4)
            for k in range(4):
                weight = b_w[v, k]
                if weight > 0.0001:
                    bi = b_idx[v, k]
                    p_out += weight * (bone_palette[bi] @ p_homo)
            skinned[v] = p_out[:3]

            u, v_coord = uvs[v, 0], uvs[v, 1]
            u_px = int(np.clip(u * w_tex, 0, w_tex - 1))
            v_px = int(np.clip(v_coord * h_tex, 0, h_tex - 1))
            v_colors[v] = tex_arr[v_px, u_px] / 255.0

        # Transform to Minecraft world space:
        # Minecraft model: cornea is towards -Z, stalk towards +Z, top is +Y
        homo = np.hstack([skinned, np.ones((len(skinned), 1))])
        mc_pos = (homo @ R90.T)[:, :3] * BASE_SCALE
        # Model translation: translate(0, 1.5, 0)
        mc_pos[:, 1] += 1.5

        for i in range(0, len(indices), 3):
            i0, i1, i2 = indices[i], indices[i+1], indices[i+2]
            c0, c1, c2 = v_colors[i0], v_colors[i1], v_colors[i2]
            avg_alpha = (c0[3] + c1[3] + c2[3]) / 3.0
            if honor_alpha and avg_alpha < 0.1:
                continue
            avg_col = (c0[:3] + c1[:3] + c2[:3]) / 3.0
            tri_verts.append([mc_pos[i0], mc_pos[i1], mc_pos[i2]])
            tri_colors.append(avg_col)

    return tri_verts, tri_colors

def render_debug_view(tri_verts, tri_colors, title, out_path):
    fig = plt.figure(figsize=(9, 8), dpi=150)
    ax = fig.add_subplot(111, projection='3d')

    if len(tri_verts) > 0:
        # Matplotlib 3D axes: ax_X = X, ax_Y = Z, ax_Z = Y (so Y is vertical)
        plot_tris = []
        for tri in tri_verts:
            plot_tris.append([[pt[0], pt[2], pt[1]] for pt in tri])
        
        poly = Poly3DCollection(plot_tris, facecolors=tri_colors, edgecolors='gray', linewidths=0.2, alpha=0.95)
        ax.add_collection3d(poly)

    ax.set_title(title, fontsize=12, fontweight='bold', pad=12)
    ax.set_xlabel('X (Lateral)', fontsize=9)
    ax.set_ylabel('Z (Forward/Back)', fontsize=9)
    ax.set_zlabel('Y (Height)', fontsize=9)

    ax.set_xlim(-2.5, 2.5)
    ax.set_ylim(-3.0, 3.0)
    ax.set_zlim(0.0, 3.5)

    # Front-quarter angle looking at the front of the eye (-Z is front)
    ax.view_init(elev=15, azim=-60)
    ax.grid(True, linestyle=':', alpha=0.4)

    plt.tight_layout()
    plt.savefig(out_path, dpi=150)
    plt.close()
    print(f"Saved: {out_path} ({len(tri_verts)} tris)")

submeshes = ["body", "glass", "pupil", "iris", "stalk", "all"]
for sub in submeshes:
    # Notice: honor_alpha=False to see what geometry exists when rendered opaque
    verts, cols = get_part_mesh(sub, honor_alpha=False)
    name = f"eye_debug_{sub}_only.png" if sub != "all" else "eye_debug_all.png"
    render_debug_view(verts, cols, f"Eye of Cthulhu Debug — {sub.upper()} ONLY", OUT_DIR / name)
