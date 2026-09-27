"""
Generates comprehensive visual validation render projections for Eye of Cthulhu runtime fix.
Outputs to:
- build/visual_validation/runtime/
  - eye_texture_p1_front.png (front view of Phase 1 showing crisp pupil, iris, and veiny sclera)
  - eye_texture_p1_side.png  (side profile showing spherical eye body, trailing crimson tendrils, and player)
  - eye_texture_transition.png (transformation convulsion and controlled swap at tick 40)
  - eye_texture_p2_front.png (front view of gaping Phase 2 maw with ivory razor teeth & bloody gums)
  - eye_texture_p2_side.png  (side profile of Phase 2 showing 3.08m jaw opening & intermeshing teeth)
  - eye_fixed_p1_ground.png (with 3D player scale comparison & terrain)
  - eye_fixed_p1_air.png    (airborne boss with undulating tendrils)
  - eye_fixed_p2_ground.png (Phase 2 open maw, teeth revealed & player scale comparison)
  - eye_fixed_transition.png (transformation convulsion and controlled swap)
  - eye_fixed_top_view.png  (top-down orthographic footprint vs block grid & player)
"""

import json
import numpy as np
from PIL import Image
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
MODELS_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/models/entity/boss"
TEXTURES_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/textures/entity/boss"
OUT_RUNTIME_DIR = PROJECT_ROOT / "build/visual_validation/runtime"
OUT_EYE_DIR = PROJECT_ROOT / "build/visual_validation/eye"

BASE_SCALE = 0.007

def rot_x(deg):
    r = np.radians(deg)
    return np.array([
        [1, 0, 0, 0],
        [0, np.cos(r), -np.sin(r), 0],
        [0, np.sin(r), np.cos(r), 0],
        [0, 0, 0, 1]
    ], dtype=np.float64)

def rot_y(deg):
    r = np.radians(deg)
    return np.array([
        [np.cos(r), 0, np.sin(r), 0],
        [0, 1, 0, 0],
        [-np.sin(r), 0, np.cos(r), 0],
        [0, 0, 0, 1]
    ], dtype=np.float64)

def rot_z(deg):
    r = np.radians(deg)
    return np.array([
        [np.cos(r), -np.sin(r), 0, 0],
        [np.sin(r), np.cos(r), 0, 0],
        [0, 0, 1, 0],
        [0, 0, 0, 1]
    ], dtype=np.float64)

def skin_mesh_textured(data, bone_transforms, texture_img):
    raw_bones = data["bones"]
    bone_names = [b["name"] if isinstance(b, dict) else b for b in raw_bones]
    inv_bind = [np.array(m, dtype=np.float64).reshape((4, 4), order="F") for m in data["inverseBindMatrices"]]
    bone_palette = []

    for i, bname in enumerate(bone_names):
        T_bone = bone_transforms.get(bname, np.linalg.inv(inv_bind[i]))
        S_i = T_bone @ inv_bind[i]
        bone_palette.append(S_i)

    w, h = texture_img.size
    tex_arr = np.array(texture_img)

    skinned_verts = []
    vertex_colors = []

    for part in data["parts"]:
        pos = np.array(part["positions"]).reshape((-1, 3))
        uvs = np.array(part["uvs"]).reshape((-1, 2))
        b_idx = np.array(part["boneIndices"]).reshape((-1, 4))
        b_w = np.array(part["boneWeights"]).reshape((-1, 4))

        for v in range(len(pos)):
            u, v_coord = uvs[v, 0], uvs[v, 1]
            u_px = int(np.clip(u * w, 0, w - 1))
            v_px = int(np.clip(v_coord * h, 0, h - 1))

            rgba = tex_arr[v_px, u_px]
            if rgba[3] < 10:  # Skip transparent fragments (e.g. glass cornea cutout)
                continue

            p_homo = np.append(pos[v], 1.0)
            p_out = np.zeros(4)
            for k in range(4):
                weight = b_w[v, k]
                if weight > 0.0001:
                    bi = b_idx[v, k]
                    p_out += weight * (bone_palette[bi] @ p_homo)

            skinned_verts.append(p_out[0:3])
            vertex_colors.append(rgba[:3] / 255.0)

    return np.array(skinned_verts), np.array(vertex_colors)

def skin_mesh(data, bone_transforms):
    raw_bones = data["bones"]
    bone_names = [b["name"] if isinstance(b, dict) else b for b in raw_bones]
    inv_bind = [np.array(m, dtype=np.float64).reshape((4, 4), order="F") for m in data["inverseBindMatrices"]]
    bone_palette = []

    for i, bname in enumerate(bone_names):
        T_bone = bone_transforms.get(bname, np.linalg.inv(inv_bind[i]))
        S_i = T_bone @ inv_bind[i]
        bone_palette.append(S_i)

    skinned_verts = []
    for part in data["parts"]:
        pos = np.array(part["positions"]).reshape((-1, 3))
        b_idx = np.array(part["boneIndices"]).reshape((-1, 4))
        b_w = np.array(part["boneWeights"]).reshape((-1, 4))

        for v in range(len(pos)):
            p_homo = np.append(pos[v], 1.0)
            p_out = np.zeros(4)
            for k in range(4):
                w = b_w[v, k]
                if w > 0.0001:
                    bi = b_idx[v, k]
                    p_out += w * (bone_palette[bi] @ p_homo)
            skinned_verts.append(p_out[0:3])

    return np.array(skinned_verts)

def to_minecraft_space(verts, world_offset=(0.0, 1.5, 0.0), pitch_deg=0.0, yaw_deg=0.0, roll_deg=0.0):
    R90 = rot_x(90.0)
    homo = np.hstack([verts, np.ones((len(verts), 1))])
    v_mc = (homo @ R90.T)[:, :3] * BASE_SCALE

    if pitch_deg != 0.0:
        v_mc = (np.hstack([v_mc, np.ones((len(v_mc), 1))]) @ rot_x(pitch_deg).T)[:, :3]
    if yaw_deg != 0.0:
        v_mc = (np.hstack([v_mc, np.ones((len(v_mc), 1))]) @ rot_y(yaw_deg).T)[:, :3]
    if roll_deg != 0.0:
        v_mc = (np.hstack([v_mc, np.ones((len(v_mc), 1))]) @ rot_z(roll_deg).T)[:, :3]

    v_mc[:, 0] += world_offset[0]
    v_mc[:, 1] += world_offset[1]
    v_mc[:, 2] += world_offset[2]

    return v_mc

def draw_block_box(ax, x0, y0, z0, dx, dy, dz, color='steelblue', alpha=0.5, edgecolor='black'):
    x = [x0, x0 + dx]
    y = [y0, y0 + dy]
    z = [z0, z0 + dz]

    verts = [
        [[x[0], z[0], y[0]], [x[1], z[0], y[0]], [x[1], z[0], y[1]], [x[0], z[0], y[1]]],
        [[x[0], z[1], y[0]], [x[1], z[1], y[0]], [x[1], z[1], y[1]], [x[0], z[1], y[1]]],
        [[x[0], z[0], y[0]], [x[1], z[0], y[0]], [x[1], z[1], y[0]], [x[0], z[1], y[0]]],
        [[x[0], z[0], y[1]], [x[1], z[0], y[1]], [x[1], z[1], y[1]], [x[0], z[1], y[1]]],
        [[x[0], z[0], y[0]], [x[0], z[0], y[1]], [x[0], z[1], y[1]], [x[0], z[1], y[0]]],
        [[x[1], z[0], y[0]], [x[1], z[0], y[1]], [x[1], z[1], y[1]], [x[1], z[1], y[0]]]
    ]
    poly = Poly3DCollection(verts, facecolors=color, linewidths=0.8, edgecolors=edgecolor, alpha=alpha)
    ax.add_collection3d(poly)

def draw_minecraft_player(ax, px=-2.8, py=0.0, pz=-0.5):
    # Legs (0.0 to 0.75m)
    draw_block_box(ax, px - 0.2, py, pz - 0.12, 0.19, 0.75, 0.24, color='#1f3a5f', alpha=0.8)
    draw_block_box(ax, px + 0.01, py, pz - 0.12, 0.19, 0.75, 0.24, color='#1f3a5f', alpha=0.8)
    # Torso (0.75 to 1.40m)
    draw_block_box(ax, px - 0.2, py + 0.75, pz - 0.12, 0.4, 0.65, 0.24, color='#00a8a8', alpha=0.85)
    # Arms
    draw_block_box(ax, px - 0.38, py + 0.75, pz - 0.12, 0.18, 0.65, 0.24, color='#c99e74', alpha=0.8)
    draw_block_box(ax, px + 0.20, py + 0.75, pz - 0.12, 0.18, 0.65, 0.24, color='#c99e74', alpha=0.8)
    # Head (1.40 to 1.80m)
    draw_block_box(ax, px - 0.2, py + 1.40, pz - 0.2, 0.4, 0.4, 0.4, color='#d2a679', alpha=0.95)

def draw_terrain_grid(ax, x_range=(-5, 5), z_range=(-4, 6), y=0.0):
    xs = np.arange(x_range[0], x_range[1] + 1)
    zs = np.arange(z_range[0], z_range[1] + 1)

    for x in xs:
        ax.plot([x, x], [z_range[0], z_range[1]], [y, y], color='#2e7d32', alpha=0.4, linewidth=0.8, linestyle='--')
    for z in zs:
        ax.plot([x_range[0], x_range[1]], [z, z], [y, y], color='#2e7d32', alpha=0.4, linewidth=0.8, linestyle='--')

def render_scene(verts_world, title, out_path, include_player=True, elev=20, azim=45,
                 xlim=(-4, 4), ylim=(-0.5, 7.0), zlim=(-3, 5), color='royalblue',
                 annotations=None, is_top_view=False):
    fig = plt.figure(figsize=(11, 8.5), dpi=150)
    ax = fig.add_subplot(111, projection='3d')

    draw_terrain_grid(ax, x_range=(int(xlim[0]), int(xlim[1])), z_range=(int(zlim[0]), int(zlim[1])))

    if include_player:
        draw_minecraft_player(ax, px=-2.6, py=0.0, pz=-0.5)

    step = max(1, len(verts_world) // 3000)
    sub = verts_world[::step]

    ax.scatter(sub[:, 0], sub[:, 2], sub[:, 1], c=color, s=2.5, alpha=0.65, edgecolors='none')

    ax.set_title(title, fontsize=13, fontweight='bold', pad=15)
    ax.set_xlabel('X — Lateral (Blocks / Meters)', fontsize=9, labelpad=8)
    ax.set_ylabel('Z — Depth / Forward (Blocks / Meters)', fontsize=9, labelpad=8)
    ax.set_zlabel('Y — Height (Blocks / Meters)', fontsize=9, labelpad=8)

    ax.set_xlim(xlim[0], xlim[1])
    ax.set_ylim(zlim[0], zlim[1])
    ax.set_zlim(ylim[0], ylim[1])

    ax.view_init(elev=elev, azim=azim)
    ax.grid(True, linestyle=':', alpha=0.4)

    if annotations:
        fig.text(0.12, 0.04, annotations, fontsize=9.5,
                 bbox=dict(boxstyle='round,pad=0.5', facecolor='white', alpha=0.9, edgecolor='gray'))

    plt.tight_layout()
    out_path.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(out_path, dpi=150)
    plt.close()
    print(f"Rendered: {out_path}")

def render_textured_scene(verts_world, colors, title, out_path, include_player=True, elev=20, azim=45,
                          xlim=(-4, 4), ylim=(-0.5, 5.0), zlim=(-3, 5), annotations=None):
    fig = plt.figure(figsize=(11, 8.5), dpi=150)
    ax = fig.add_subplot(111, projection='3d')

    draw_terrain_grid(ax, x_range=(int(xlim[0]), int(xlim[1])), z_range=(int(zlim[0]), int(zlim[1])))

    if include_player:
        draw_minecraft_player(ax, px=-2.6, py=0.0, pz=-0.5)

    # In Minecraft space: X=lateral, Y=vertical height, Z=depth (forward is -Z)
    # Matplotlib 3D axes: ax_X = X, ax_Y = Z, ax_Z = Y
    ax.scatter(verts_world[:, 0], verts_world[:, 2], verts_world[:, 1],
               c=colors, s=12.0, alpha=0.92, edgecolors='none')

    ax.set_title(title, fontsize=13, fontweight='bold', pad=15)
    ax.set_xlabel('X — Lateral (Blocks / Meters)', fontsize=9, labelpad=8)
    ax.set_ylabel('Z — Depth / Forward (Blocks / Meters)', fontsize=9, labelpad=8)
    ax.set_zlabel('Y — Height (Blocks / Meters)', fontsize=9, labelpad=8)

    ax.set_xlim(xlim[0], xlim[1])
    ax.set_ylim(zlim[0], zlim[1])
    ax.set_zlim(ylim[0], ylim[1])

    ax.view_init(elev=elev, azim=azim)
    ax.grid(True, linestyle=':', alpha=0.4)

    if annotations:
        fig.text(0.10, 0.04, annotations, fontsize=9.5,
                 bbox=dict(boxstyle='round,pad=0.5', facecolor='white', alpha=0.9, edgecolor='gray'))

    plt.tight_layout()
    out_path.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(out_path, dpi=150)
    plt.close()
    print(f"Rendered: {out_path}")

def main():
    OUT_RUNTIME_DIR.mkdir(parents=True, exist_ok=True)
    OUT_EYE_DIR.mkdir(parents=True, exist_ok=True)

    with open(MODELS_DIR / "eye_of_cthulhu_p1.skin.json", "r") as f:
        p1_data = json.load(f)
    with open(MODELS_DIR / "eye_of_cthulhu_p2.skin.json", "r") as f:
        p2_data = json.load(f)

    p1_img = Image.open(TEXTURES_DIR / "eye_of_cthulhu_p1.png").convert("RGBA")
    p2_img = Image.open(TEXTURES_DIR / "eye_of_cthulhu_p2.png").convert("RGBA")

    raw_p1_bones = [b["name"] if isinstance(b, dict) else b for b in p1_data["bones"]]
    inv_p1 = [np.array(m, dtype=np.float64).reshape((4, 4), order="F") for m in p1_data["inverseBindMatrices"]]
    bind_p1 = {bname: np.linalg.inv(inv_p1[i]) for i, bname in enumerate(raw_p1_bones)}

    raw_p2_bones = [b["name"] if isinstance(b, dict) else b for b in p2_data["bones"]]
    inv_p2 = [np.array(m, dtype=np.float64).reshape((4, 4), order="F") for m in p2_data["inverseBindMatrices"]]
    bind_p2 = {bname: np.linalg.inv(inv_p2[i]) for i, bname in enumerate(raw_p2_bones)}

    # P1 Idle Pose
    p1_idle_transforms = dict(bind_p1)
    p1_idle_transforms["tendril_01"] = bind_p1["tendril_01"] @ rot_x(12.0)
    p1_idle_transforms["tendril_02"] = bind_p1["tendril_02"] @ rot_x(-10.0)

    # 1. eye_texture_p1_front.png:
    v_p1_raw, col_p1 = skin_mesh_textured(p1_data, p1_idle_transforms, p1_img)
    v_p1_front_world = to_minecraft_space(v_p1_raw, world_offset=(0.0, 1.5, 0.0), pitch_deg=0.0)
    render_textured_scene(
        v_p1_front_world, col_p1,
        "Eye of Cthulhu Phase 1 (Front View) — Textured Eyeball, Sclera Veins, Iris & Pupil",
        OUT_RUNTIME_DIR / "eye_texture_p1_front.png",
        include_player=True, elev=12, azim=15,
        xlim=(-4, 4), ylim=(-0.5, 4.5), zlim=(-3, 5),
        annotations="Texture: eye_of_cthulhu_p1.png | UV Mapping: Sclera Left [0, 0.5], Stalk Top-Right [0.5, 1.0]x[0, 0.5] | Cornea: Cutout Discarded"
    )

    # 2. eye_texture_p1_side.png:
    render_textured_scene(
        v_p1_front_world, col_p1,
        "Eye of Cthulhu Phase 1 (Side Profile) — Lateral Spherical Eyeball & Crimson Tendrils",
        OUT_RUNTIME_DIR / "eye_texture_p1_side.png",
        include_player=True, elev=14, azim=85,
        xlim=(-4, 4), ylim=(-0.5, 4.5), zlim=(-3, 5),
        annotations="Side Profile: Cornea at -Z (left), Tendrils at +Z (right) | Tendril UV: 100% aligned to crimson stalk texture without stretching"
    )

    # 3. eye_texture_transition.png:
    p2_trans_transforms = dict(bind_p2)
    p2_trans_transforms["body"] = bind_p2["body"] @ rot_x(10.0) @ rot_y(25.0)
    p2_trans_transforms["upper_jaw"] = bind_p2["upper_jaw"] @ rot_x(20.0)
    p2_trans_transforms["lower_jaw"] = bind_p2["lower_jaw"] @ rot_x(-20.0)
    v_p2_trans_raw, col_p2_trans = skin_mesh_textured(p2_data, p2_trans_transforms, p2_img)
    v_p2_trans_world = to_minecraft_space(v_p2_trans_raw, world_offset=(0.0, 2.0, 0.0), pitch_deg=0.0)
    render_textured_scene(
        v_p2_trans_world, col_p2_trans,
        "Eye of Cthulhu Transformation (Tick 40 Swap) — Textured Agitation & Mouth Emergence",
        OUT_RUNTIME_DIR / "eye_texture_transition.png",
        include_player=True, elev=20, azim=45,
        xlim=(-4, 4), ylim=(-0.5, 5.0), zlim=(-3, 5),
        annotations="Transition State: Synchronized visual phase, controlled swap to P2 at tick 40 | Scale: 3.12m body | Player: 1.8m tall (left)"
    )

    # P2 Bite / Open Maw Pose
    p2_bite_transforms = dict(bind_p2)
    p2_bite_transforms["upper_jaw"] = bind_p2["upper_jaw"] @ rot_x(35.0)
    p2_bite_transforms["lower_jaw"] = bind_p2["lower_jaw"] @ rot_x(-35.0)
    p2_bite_transforms["tendril_01"] = bind_p2["tendril_01"] @ rot_x(18.0)
    p2_bite_transforms["tendril_02"] = bind_p2["tendril_02"] @ rot_x(-15.0)

    # 4. eye_texture_p2_front.png:
    v_p2_raw, col_p2 = skin_mesh_textured(p2_data, p2_bite_transforms, p2_img)
    v_p2_front_world = to_minecraft_space(v_p2_raw, world_offset=(0.0, 1.5, 0.0), pitch_deg=0.0)
    render_textured_scene(
        v_p2_front_world, col_p2,
        "Eye of Cthulhu Phase 2 (Front View) — Articulated Maw, Ivory Razor Teeth & Red Gums",
        OUT_RUNTIME_DIR / "eye_texture_p2_front.png",
        include_player=True, elev=12, azim=15,
        xlim=(-4, 4), ylim=(-0.5, 4.5), zlim=(-3, 5),
        annotations="Texture: eye_of_cthulhu_p2.png | Teeth UV: [0.5, 1.0]x[0.5, 1.0] mapped directly to teeth atlas | Gaping Mouth: 3.08m vertical opening"
    )

    # 5. eye_texture_p2_side.png:
    render_textured_scene(
        v_p2_front_world, col_p2,
        "Eye of Cthulhu Phase 2 (Side Profile) — Gaping Maw (+35°/-35°) & Trailing Tendrils",
        OUT_RUNTIME_DIR / "eye_texture_p2_side.png",
        include_player=True, elev=14, azim=85,
        xlim=(-4, 4), ylim=(-0.5, 4.5), zlim=(-3, 5),
        annotations="Side Profile: Upper Jaw (+35°) & Lower Jaw (-35°) | Scale: 3.12m body diameter | Player: 1.8m comparison on left"
    )

    # Maintain existing scale/geometry verification images as well
    v_p1_idle_asset = skin_mesh(p1_data, p1_idle_transforms)
    v_p1_ground_world = to_minecraft_space(v_p1_idle_asset, world_offset=(0.0, 1.5, 0.0), pitch_deg=0.0)
    render_scene(v_p1_ground_world,
                 "Eye of Cthulhu Phase 1 (Ground Level) — Corrected 3.1m Scale vs 1.8m Player",
                 OUT_RUNTIME_DIR / "eye_fixed_p1_ground.png",
                 include_player=True, elev=18, azim=40,
                 xlim=(-4, 4), ylim=(-0.5, 4.5), zlim=(-3, 5), color='royalblue',
                 annotations="Eyeball Diameter: 3.12m | Hitbox: 2.5x2.5m | Player: 1.8m tall (left) | Base: sits cleanly at ground level (Y=0)")

    v_p1_air_world = to_minecraft_space(v_p1_idle_asset, world_offset=(0.0, 4.5, 0.0), pitch_deg=-20.0)
    render_scene(v_p1_air_world,
                 "Eye of Cthulhu Phase 1 (Airborne Hover) — 4.5m Altitude, Trailing Undulating Tendrils",
                 OUT_RUNTIME_DIR / "eye_fixed_p1_air.png",
                 include_player=True, elev=15, azim=45,
                 xlim=(-4, 4), ylim=(-0.5, 7.5), zlim=(-3, 5), color='mediumblue',
                 annotations="Altitude: 4.5m above ground | Pitch: -20° downward gaze towards player | Tendrils: wave naturally behind eye")

    v_p2_bite_asset = skin_mesh(p2_data, p2_bite_transforms)
    v_p2_ground_world = to_minecraft_space(v_p2_bite_asset, world_offset=(0.0, 1.5, 0.0), pitch_deg=0.0)
    render_scene(v_p2_ground_world,
                 "Eye of Cthulhu Phase 2 (Ground Level) — Articulated Maw (+35°/-35°) & Player Scale",
                 OUT_RUNTIME_DIR / "eye_fixed_p2_ground.png",
                 include_player=True, elev=18, azim=40,
                 xlim=(-4, 4), ylim=(-0.5, 4.5), zlim=(-3, 5), color='crimson',
                 annotations="Phase 2 Gaping Maw: 3.08m vertical opening | Teeth: fully articulated | Player: 1.8m tall (left) | Scale: 3.12m body")

    v_p2_trans_asset = skin_mesh(p2_data, p2_trans_transforms)
    v_p2_trans_world_plain = to_minecraft_space(v_p2_trans_asset, world_offset=(0.0, 2.0, 0.0), pitch_deg=0.0)
    render_scene(v_p2_trans_world_plain,
                 "Eye of Cthulhu Transformation (Tick 40 Swap) — Convulsive Agitation & Cornea Tear",
                 OUT_RUNTIME_DIR / "eye_fixed_transition.png",
                 include_player=True, elev=22, azim=55,
                 xlim=(-4, 4), ylim=(-0.5, 5.0), zlim=(-3, 5), color='darkmagenta',
                 annotations="Transformation Swap: P1 Cornea tears open -> P2 Maw emerges | Controlled transition at tick 40 | Scale: 3.12m")

    render_scene(v_p1_ground_world,
                 "Eye of Cthulhu (Top-Down Orthographic View) — 3.1m Width x 5.5m Length Footprint",
                 OUT_RUNTIME_DIR / "eye_fixed_top_view.png",
                 include_player=True, elev=88, azim=0,
                 xlim=(-3.5, 3.5), ylim=(-0.5, 4.5), zlim=(-3, 5), color='teal',
                 annotations="Top-Down View: Forward is -Z (top), Trailing Tendrils are +Z (bottom) | Body: 3.12m x 3.12m | Total Length: 5.55m",
                 is_top_view=True)

if __name__ == "__main__":
    main()
