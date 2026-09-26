"""
Extracts authentic weighted skinning data from GLTF models in C:/model 3d/_processed
for Eye of Cthulhu Phase 1 and Phase 2.
Outputs:
- eye_of_cthulhu_p1.skin.json
- eye_of_cthulhu_p2.skin.json
"""

import json
import struct
from pathlib import Path
from collections import defaultdict

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
TARGET_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/models/entity/boss"

P1_GLTF = Path("C:/model 3d/_processed/06664c90cf3e4d24a74a43dc771ae74f/source/scene.gltf")
P2_GLTF = Path("C:/model 3d/_processed/a53f70fa34284699a57af3d161182611/source/scene.gltf")

PALETTE = [
    "root",         # 0
    "body",         # 1
    "upper_jaw",    # 2
    "lower_jaw",    # 3
    "tendril_01",   # 4
    "tendril_02",   # 5
    "tendril_03",   # 6
    "tendril_04",   # 7
    "tendril_05",   # 8
    "tendril_06",   # 9
    "iris",         # 10
    "pupil",        # 11
    "optic_back"    # 12
]
PALETTE_MAP = {name: i for i, name in enumerate(PALETTE)}

def read_gltf_accessor(data, bin_data, acc_idx):
    acc = data["accessors"][acc_idx]
    bv = data["bufferViews"][acc["bufferView"]]
    offset = bv.get("byteOffset", 0) + acc.get("byteOffset", 0)
    count = acc["count"]
    ctype = acc["componentType"]
    tstr = acc["type"]
    fmt_map = {
        (5121, "VEC4"): ("4B", 4),
        (5123, "VEC4"): ("4H", 8),
        (5125, "VEC4"): ("4I", 16),
        (5126, "VEC4"): ("4f", 16),
        (5126, "VEC3"): ("3f", 12),
        (5126, "VEC2"): ("2f", 8),
        (5123, "SCALAR"): ("H", 2),
        (5125, "SCALAR"): ("I", 4)
    }
    fmt, stride = fmt_map[(ctype, tstr)]
    stride = bv.get("byteStride", stride)
    return [struct.unpack_from(fmt, bin_data, offset + i * stride) for i in range(count)]

def extract_p2():
    print("Extracting Eye of Cthulhu Phase 2...")
    with open(P2_GLTF, "r", encoding="utf-8") as f:
        data = json.load(f)
    with open(P2_GLTF.parent / "scene.bin", "rb") as f:
        bin_data = f.read()

    skin = data["skins"][0]
    joints = skin["joints"]
    joint_names = [data["nodes"][j].get("name", "") for j in joints]

    def map_joint(jname):
        if "upper" in jname: return "upper_jaw"
        if "lower" in jname: return "lower_jaw"
        if "tendril_1" in jname: return "tendril_01"
        if "tendril_2" in jname: return "tendril_02"
        if "tendril_3" in jname: return "tendril_03"
        if "tendril_4" in jname: return "tendril_04"
        if "tendril_5_2" in jname: return "tendril_06"
        if "tendril_5" in jname: return "tendril_05"
        if "root_00" in jname: return "body"
        return "root"

    # Meshes: 1 (stalk), 3 (body), 5 (teeth), 7 (inner teeth)
    mesh_configs = [
        (1, "stalk", "stalk"),
        (3, "body", "body"),
        (5, "teeth", "teeth"),
        (7, "inner_teeth", "teeth")
    ]

    parts_json = []

    for m_idx, part_name, tex_type in mesh_configs:
        m = data["meshes"][m_idx]
        prim = m["primitives"][0]
        pos_list = read_gltf_accessor(data, bin_data, prim["attributes"]["POSITION"])
        norm_list = read_gltf_accessor(data, bin_data, prim["attributes"]["NORMAL"])
        uv_list = read_gltf_accessor(data, bin_data, prim["attributes"]["TEXCOORD_0"])
        j_list = read_gltf_accessor(data, bin_data, prim["attributes"]["JOINTS_0"])
        w_list = read_gltf_accessor(data, bin_data, prim["attributes"]["WEIGHTS_0"])
        ind_list = read_gltf_accessor(data, bin_data, prim["indices"]) if "indices" in prim else []

        flat_pos = []
        flat_norm = []
        flat_uv = []
        flat_b_idx = []
        flat_b_w = []

        for p, n, uv, j_vec, w_vec in zip(pos_list, norm_list, uv_list, j_list, w_list):
            # Coordinate conversion: Blender/FBX GLTF to Minecraft space
            # x -> x, y -> -z, z -> y
            x_m = p[0]
            y_m = -p[2]
            z_m = p[1]
            flat_pos.extend([round(x_m, 4), round(y_m, 4), round(z_m, 4)])

            nx_m = n[0]
            ny_m = -n[2]
            nz_m = n[1]
            flat_norm.extend([round(nx_m, 4), round(ny_m, 4), round(nz_m, 4)])

            # UV mapping into atlas (2048x1024)
            u, v = uv[0], uv[1]
            if tex_type == "body":
                u_atlas = u * 0.5
                v_atlas = v
            elif tex_type == "stalk":
                u_atlas = 0.5 + u * 0.5
                v_atlas = v
            elif tex_type == "teeth":
                u_atlas = 0.5 + u * 0.5
                v_atlas = v * 0.5
            else:
                u_atlas = u
                v_atlas = v
            flat_uv.extend([round(u_atlas, 4), round(v_atlas, 4)])

            # Bone weights
            influences = defaultdict(float)
            for j, w in zip(j_vec, w_vec):
                if w > 0.0001:
                    bname = map_joint(joint_names[j])
                    influences[bname] += w

            # Sort and take top 4
            top4 = sorted(influences.items(), key=lambda x: x[1], reverse=True)[:4]
            total_w = sum(w for _, w in top4)
            if total_w < 0.0001:
                top4 = [("body", 1.0)]
                total_w = 1.0

            b_indices = []
            b_weights = []
            for bname, w in top4:
                b_indices.append(PALETTE_MAP[bname])
                b_weights.append(round(w / total_w, 4))

            # Pad to 4
            while len(b_indices) < 4:
                b_indices.append(0)
                b_weights.append(0.0)

            # Re-normalize sum to exact 1.0
            diff = 1.0 - sum(b_weights)
            b_weights[0] = round(b_weights[0] + diff, 4)

            flat_b_idx.extend(b_indices)
            flat_b_w.extend(b_weights)

        # Indices (triangles)
        flat_ind = []
        for idx_entry in ind_list:
            flat_ind.append(idx_entry[0] if isinstance(idx_entry, tuple) else idx_entry)

        parts_json.append({
            "name": part_name,
            "vertexCount": len(pos_list),
            "positions": flat_pos,
            "normals": flat_norm,
            "uvs": flat_uv,
            "boneIndices": flat_b_idx,
            "boneWeights": flat_b_w,
            "indices": flat_ind
        })

    out_json = {
        "name": "eye_of_cthulhu_p2",
        "texture": "textures/entity/boss/eye_of_cthulhu_p2.png",
        "bones": PALETTE,
        "parts": parts_json
    }

    out_file = TARGET_DIR / "eye_of_cthulhu_p2.skin.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(out_json, f, separators=(",", ":"))
    print(f"Generated {out_file} ({out_file.stat().st_size} bytes)")

def extract_p1():
    print("Extracting Eye of Cthulhu Phase 1...")
    with open(P1_GLTF, "r", encoding="utf-8") as f:
        data = json.load(f)
    with open(P1_GLTF.parent / "scene.bin", "rb") as f:
        bin_data = f.read()

    skin = data["skins"][0]
    joints = skin["joints"]
    joint_names = [data["nodes"][j].get("name", "") for j in joints]

    def map_joint_p1(jname, part_name, z_val):
        if part_name == "pupil": return "pupil"
        if part_name == "iris": return "iris"
        if "tendril_1" in jname: return "tendril_01"
        if "tendril_2" in jname: return "tendril_02"
        if "tendril_3" in jname: return "tendril_03"
        if "tendril_4" in jname: return "tendril_04"
        if "tendril_5_2" in jname: return "tendril_06"
        if "tendril_5" in jname: return "tendril_05"
        if z_val < -100: return "optic_back"
        return "body"

    # Meshes: 1 (stalk), 3 (glass), 7 (body), 8 (pupil), 9 (iris)
    mesh_configs = [
        (1, "stalk", "stalk"),
        (3, "glass", "body"),
        (7, "body", "body"),
        (8, "pupil", "pupil"),
        (9, "iris", "iris")
    ]

    parts_json = []

    for m_idx, part_name, tex_type in mesh_configs:
        m = data["meshes"][m_idx]
        prim = m["primitives"][0]
        pos_list = read_gltf_accessor(data, bin_data, prim["attributes"]["POSITION"])
        norm_list = read_gltf_accessor(data, bin_data, prim["attributes"]["NORMAL"])
        uv_list = read_gltf_accessor(data, bin_data, prim["attributes"]["TEXCOORD_0"])
        j_list = read_gltf_accessor(data, bin_data, prim["attributes"]["JOINTS_0"])
        w_list = read_gltf_accessor(data, bin_data, prim["attributes"]["WEIGHTS_0"])
        ind_list = read_gltf_accessor(data, bin_data, prim["indices"]) if "indices" in prim else []

        flat_pos = []
        flat_norm = []
        flat_uv = []
        flat_b_idx = []
        flat_b_w = []

        for p, n, uv, j_vec, w_vec in zip(pos_list, norm_list, uv_list, j_list, w_list):
            x_m = p[0]
            y_m = -p[2]
            z_m = p[1]
            flat_pos.extend([round(x_m, 4), round(y_m, 4), round(z_m, 4)])

            nx_m = n[0]
            ny_m = -n[2]
            nz_m = n[1]
            flat_norm.extend([round(nx_m, 4), round(ny_m, 4), round(nz_m, 4)])

            u, v = uv[0], uv[1]
            if tex_type == "stalk":
                u_atlas = 0.5 + u * 0.5
                v_atlas = v
            else:
                u_atlas = u * 0.5
                v_atlas = v
            flat_uv.extend([round(u_atlas, 4), round(v_atlas, 4)])

            influences = defaultdict(float)
            for j, w in zip(j_vec, w_vec):
                if w > 0.0001:
                    bname = map_joint_p1(joint_names[j], part_name, p[2])
                    influences[bname] += w

            top4 = sorted(influences.items(), key=lambda x: x[1], reverse=True)[:4]
            total_w = sum(w for _, w in top4)
            if total_w < 0.0001:
                top4 = [("body", 1.0)]
                total_w = 1.0

            b_indices = []
            b_weights = []
            for bname, w in top4:
                b_indices.append(PALETTE_MAP[bname])
                b_weights.append(round(w / total_w, 4))

            while len(b_indices) < 4:
                b_indices.append(0)
                b_weights.append(0.0)

            diff = 1.0 - sum(b_weights)
            b_weights[0] = round(b_weights[0] + diff, 4)

            flat_b_idx.extend(b_indices)
            flat_b_w.extend(b_weights)

        flat_ind = []
        for idx_entry in ind_list:
            flat_ind.append(idx_entry[0] if isinstance(idx_entry, tuple) else idx_entry)

        parts_json.append({
            "name": part_name,
            "vertexCount": len(pos_list),
            "positions": flat_pos,
            "normals": flat_norm,
            "uvs": flat_uv,
            "boneIndices": flat_b_idx,
            "boneWeights": flat_b_w,
            "indices": flat_ind
        })

    out_json = {
        "name": "eye_of_cthulhu_p1",
        "texture": "textures/entity/boss/eye_of_cthulhu_p1.png",
        "bones": PALETTE,
        "parts": parts_json
    }

    out_file = TARGET_DIR / "eye_of_cthulhu_p1.skin.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(out_json, f, separators=(",", ":"))
    print(f"Generated {out_file} ({out_file.stat().st_size} bytes)")

def main():
    TARGET_DIR.mkdir(parents=True, exist_ok=True)
    extract_p2()
    extract_p1()

if __name__ == "__main__":
    main()
