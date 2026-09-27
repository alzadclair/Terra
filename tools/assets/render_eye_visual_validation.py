"""
Generates visual validation render projections for Eye of Cthulhu skinning.
Outputs to:
- build/visual_validation/eye/
- build/visual_validation/runtime/
"""

import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
MODELS_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/models/entity/boss"
OUT_EYE_DIR = PROJECT_ROOT / "build/visual_validation/eye"
OUT_RUNTIME_DIR = PROJECT_ROOT / "build/visual_validation/runtime"

def rot_x(deg):
    r = np.radians(deg)
    return np.array([
        [1, 0, 0, 0],
        [0, np.cos(r), -np.sin(r), 0],
        [0, np.sin(r), np.cos(r), 0],
        [0, 0, 0, 1]
    ], dtype=np.float64)

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

def render_projection(verts, title, out_path, color='crimson'):
    fig = plt.figure(figsize=(10, 8), dpi=150)
    ax = fig.add_subplot(111, projection='3d')

    # Subsample points for clean high-density point cloud
    step = max(1, len(verts) // 2500)
    sub = verts[::step]

    # Camera perspective
    ax.scatter(sub[:, 0], sub[:, 2], sub[:, 1], c=color, s=2.5, alpha=0.6, edgecolors='none')

    ax.set_title(title, fontsize=14, fontweight='bold', pad=15)
    ax.set_xlabel('X (Lateral)', fontsize=10)
    ax.set_ylabel('Z (Depth)', fontsize=10)
    ax.set_zlabel('Y (Vertical)', fontsize=10)

    # Set consistent axis limits
    ax.set_xlim(-150, 150)
    ax.set_ylim(-200, 300)
    ax.set_zlim(-150, 150)

    ax.view_init(elev=20, azim=45)
    ax.grid(True, linestyle=':', alpha=0.5)

    plt.tight_layout()
    out_path.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(out_path, dpi=150)
    plt.close()
    print(f"Rendered: {out_path}")

def main():
    OUT_EYE_DIR.mkdir(parents=True, exist_ok=True)
    OUT_RUNTIME_DIR.mkdir(parents=True, exist_ok=True)

    with open(MODELS_DIR / "eye_of_cthulhu_p1.skin.json", "r") as f:
        p1_data = json.load(f)
    with open(MODELS_DIR / "eye_of_cthulhu_p2.skin.json", "r") as f:
        p2_data = json.load(f)

    raw_p2_bones = [b["name"] if isinstance(b, dict) else b for b in p2_data["bones"]]
    inv_p2 = [np.array(m, dtype=np.float64).reshape((4, 4), order="F") for m in p2_data["inverseBindMatrices"]]
    bind_p2 = {bname: np.linalg.inv(inv_p2[i]) for i, bname in enumerate(raw_p2_bones)}

    # 1. Phase 1 Rest Pose
    v_p1_rest = skin_mesh(p1_data, {})
    render_projection(v_p1_rest, "Eye of Cthulhu Phase 1 — Rest Pose (Identity Skinning)",
                      OUT_EYE_DIR / "eye_p1_rest_pose.png", color='royalblue')
    render_projection(v_p1_rest, "Eye of Cthulhu Phase 1 — Runtime Rest Pose",
                      OUT_RUNTIME_DIR / "eye_p1_runtime_rest.png", color='royalblue')

    # 2. Phase 2 Rest Pose
    v_p2_rest = skin_mesh(p2_data, {})
    render_projection(v_p2_rest, "Eye of Cthulhu Phase 2 — Rest Pose (Neutral Cornea Maw)",
                      OUT_EYE_DIR / "eye_p2_rest_pose.png", color='darkred')
    render_projection(v_p2_rest, "Eye of Cthulhu Phase 2 — Runtime Neutral Rest",
                      OUT_RUNTIME_DIR / "eye_p2_runtime_rest.png", color='darkred')

    # 3. Phase 2 Bite Pose (Articulated Maw)
    p2_bite_transforms = dict(bind_p2)
    # Rotate upper jaw +35 deg around its hinge pivot [0.018, 12.302, 0.144]
    p2_bite_transforms["upper_jaw"] = bind_p2["upper_jaw"] @ rot_x(35.0)
    # Rotate lower jaw -35 deg around its hinge pivot [0.018, 10.149, -4.324]
    p2_bite_transforms["lower_jaw"] = bind_p2["lower_jaw"] @ rot_x(-35.0)

    v_p2_bite = skin_mesh(p2_data, p2_bite_transforms)
    render_projection(v_p2_bite, "Eye of Cthulhu Phase 2 — Bite Pose (+35°/-35° Articulated Maw)",
                      OUT_EYE_DIR / "eye_p2_bite_pose.png", color='firebrick')
    render_projection(v_p2_bite, "Eye of Cthulhu Phase 2 — Runtime Articulated Bite (+35°/-35°)",
                      OUT_RUNTIME_DIR / "eye_p2_runtime_bite.png", color='firebrick')

    # 4. Phase 2 Tendril Wave
    p2_wave_transforms = dict(bind_p2)
    p2_wave_transforms["tendril_01"] = bind_p2["tendril_01"] @ rot_x(25.0)
    p2_wave_transforms["tendril_02"] = bind_p2["tendril_02"] @ rot_x(-20.0)
    p2_wave_transforms["tendril_03"] = bind_p2["tendril_03"] @ rot_x(30.0)
    p2_wave_transforms["tendril_04"] = bind_p2["tendril_04"] @ rot_x(-25.0)
    p2_wave_transforms["tendril_05"] = bind_p2["tendril_05"] @ rot_x(20.0)

    v_p2_wave = skin_mesh(p2_data, p2_wave_transforms)
    render_projection(v_p2_wave, "Eye of Cthulhu Phase 2 — Tendril Flexion Wave",
                      OUT_EYE_DIR / "eye_p2_tendril_wave.png", color='purple')
    render_projection(v_p2_wave, "Eye of Cthulhu Phase 2 — Runtime Tendril Flexion",
                      OUT_RUNTIME_DIR / "eye_p2_runtime_tendrils.png", color='purple')

if __name__ == "__main__":
    main()
