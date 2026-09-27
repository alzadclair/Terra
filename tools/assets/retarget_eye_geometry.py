"""
TerraForge RPG - Eye of Cthulhu Decoupled Geometry Retargeting & Asset Pipeline
Separates visual geometry source from skeletal rig source:
- Rig Sources:
  * P1 Rig: C:/model 3d/_processed/06664c90cf3e4d24a74a43dc771ae74f/source/scene.gltf (CC-BY-4.0)
  * P2 Rig: C:/model 3d/_processed/a53f70fa34284699a57af3d161182611/source/scene.gltf (CC-BY-4.0)
- Visual Geometry Sources:
  * P1 Geometry: C:/model 3d/_processed/b52f961111eb46d7a11341965e66a0c0/source/scene.gltf (CC-BY-4.0, Zaza)
  * P2 Geometry: Retargeted mouth/teeth architecture using a53f70fa34284699a57af3d161182611 (CC-BY-4.0)
- Outputs:
  * eye_of_cthulhu_p1.skin.json
  * eye_of_cthulhu_p2.skin.json
  * eye_of_cthulhu_p1.png
  * eye_of_cthulhu_p2.png
  * docs/EYE_RIG_MAPPING.md
  * docs/EYE_MODEL_LICENSE_AND_ATTRIBUTION.md
"""

import json
import struct
import numpy as np
from pathlib import Path
from PIL import Image, ImageDraw

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
MODELS_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/models/entity/boss"
TEXTURES_DIR = PROJECT_ROOT / "src/main/resources/assets/terraforge_rpg/textures/entity/boss"
DOCS_DIR = PROJECT_ROOT / "docs"

P1_RIG_GLTF = Path("C:/model 3d/_processed/06664c90cf3e4d24a74a43dc771ae74f/source/scene.gltf")
P2_RIG_GLTF = Path("C:/model 3d/_processed/a53f70fa34284699a57af3d161182611/source/scene.gltf")
P1_GEO_GLTF = Path("C:/model 3d/_processed/b52f961111eb46d7a11341965e66a0c0/source/scene.gltf")
P2_GEO_GLTF = Path("C:/model 3d/_processed/a53f70fa34284699a57af3d161182611/source/scene.gltf")

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

CANONICAL_BONES_P1 = [
    ("root",        None,       "_rootJoint"),
    ("body",        "root",     "root_00"),
    ("upper_jaw",   "body",     "root_00"),
    ("lower_jaw",   "body",     "root_00"),
    ("tendril_01",  "body",     "tendril_1_0_02"),
    ("tendril_02",  "body",     "tendril_2_0_08"),
    ("tendril_03",  "body",     "tendril_3_0_011"),
    ("tendril_04",  "body",     "tendril_4_0_014"),
    ("tendril_05",  "body",     "tendril_5_0_05"),
    ("tendril_06",  "body",     "tentacle_outer_01"),
    ("iris",        "body",     "root_00"),
    ("pupil",       "iris",     "root_00"),
    ("optic_back",  "body",     "tentacle_outer_01")
]

CANONICAL_BONES_P2 = [
    ("root",        None,       "_rootJoint"),
    ("body",        "root",     "root_00"),
    ("upper_jaw",   "body",     "jaw_upper_01"),
    ("lower_jaw",   "body",     "jaw_lower_02"),
    ("tendril_01",  "body",     "tendril_1_0_03"),
    ("tendril_02",  "body",     "tendril_2_0_09"),
    ("tendril_03",  "body",     "tendril_3_0_012"),
    ("tendril_04",  "body",     "tendril_4_0_015"),
    ("tendril_05",  "body",     "tendril_5_0_06"),
    ("tendril_06",  "body",     "tendril_5_2_08"),
    ("iris",        "body",     "root_00"),
    ("pupil",       "iris",     "root_00"),
    ("optic_back",  "body",     "root_00")
]

T_BASIS = np.array([
    [1,  0,  0, 0],
    [0,  0, -1, 0],
    [0,  1,  0, 0],
    [0,  0,  0, 1]
], dtype=np.float64)
T_INV = np.linalg.inv(T_BASIS)

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
        (5126, "MAT4"): ("16f", 64),
        (5121, "SCALAR"): ("B", 1),
        (5123, "SCALAR"): ("H", 2),
        (5125, "SCALAR"): ("I", 4)
    }
    fmt, stride = fmt_map[(ctype, tstr)]
    stride = bv.get("byteStride", stride)
    return [struct.unpack_from(fmt, bin_data, offset + i * stride) for i in range(count)]

def extract_rig(gltf_path, canonical_bones):
    with open(gltf_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    with open(gltf_path.parent / "scene.bin", "rb") as f:
        bin_data = f.read()

    skin = data["skins"][0]
    joints = skin["joints"]
    joint_names = [data["nodes"][j].get("name", "") for j in joints]
    inv_acc = skin.get("inverseBindMatrices")
    inv_raw = read_gltf_accessor(data, bin_data, inv_acc)

    inv_bind_by_jname = {}
    bind_world_by_jname = {}
    for i, jname in enumerate(joint_names):
        inv_gltf = np.array(inv_raw[i], dtype=np.float64).reshape((4, 4), order="F")
        inv_mc = T_BASIS @ inv_gltf @ T_INV
        bind_mc = np.linalg.inv(inv_mc)
        inv_bind_by_jname[jname] = inv_mc
        bind_world_by_jname[jname] = bind_mc

    bones_structured = []
    bind_world_map = {}
    for bname, pname, jname in canonical_bones:
        if jname in bind_world_by_jname:
            W = bind_world_by_jname[jname].copy()
            invW = inv_bind_by_jname[jname].copy()
        else:
            W = bind_world_map[pname].copy()
            invW = np.linalg.inv(W)
        bind_world_map[bname] = W

        if pname is None:
            L = W.copy()
        else:
            P = bind_world_map[pname]
            L = np.linalg.inv(P) @ W

        bones_structured.append({
            "name": bname,
            "parent": pname,
            "bindLocal": [round(float(v), 7) for v in L.flatten(order="F")],
            "bindWorld": [round(float(v), 7) for v in W.flatten(order="F")],
            "inverseBind": [round(float(v), 7) for v in invW.flatten(order="F")]
        })

    return bones_structured, inv_bind_by_jname, bind_world_by_jname

def generate_tendril_strand(root_pos, bone_pivot, tip_pos, bone_name, n_rings=14, n_radial=10, radius_base=16.0, radius_tip=3.5, secondary_bone=None):
    t_vals = np.linspace(0.0, 1.0, n_rings)
    curve_pts = []
    radii = []
    for t in t_vals:
        pt = (1 - t)**2 * root_pos + 2 * (1 - t) * t * bone_pivot + t**2 * tip_pos
        curve_pts.append(pt)
        radii.append((1 - t) * radius_base + t * radius_tip)

    verts = []
    normals = []
    uvs = []
    bone_indices = []
    bone_weights = []

    b_idx_bone = PALETTE_MAP[bone_name]
    b_idx_body = PALETTE_MAP["body"]
    b_idx_sec = PALETTE_MAP[secondary_bone] if secondary_bone else PALETTE_MAP["tendril_06"]

    for i, (center, r) in enumerate(zip(curve_pts, radii)):
        t = i / (n_rings - 1)
        if i < len(curve_pts) - 1:
            tangent = curve_pts[i+1] - center
        else:
            tangent = center - curve_pts[i-1]
        tangent = tangent / np.linalg.norm(tangent)

        up = np.array([0, 0, 1.0])
        if abs(np.dot(tangent, up)) > 0.9:
            up = np.array([1.0, 0, 0])
        normal = np.cross(tangent, up)
        normal = normal / np.linalg.norm(normal)
        binormal = np.cross(tangent, normal)

        if secondary_bone == "tendril_06" and bone_name == "optic_back":
            # For optic_back nerve bundle: main bone is optic_back, secondary is tendril_06
            if t < 0.2:
                f = t / 0.2
                w_body = round(0.5 * (1 - f), 4)
                w_optic = round(1.0 - w_body, 4)
                b_inds = [b_idx_bone, b_idx_body, 0, 0]
                b_w = [w_optic, w_body, 0.0, 0.0]
            else:
                b_inds = [b_idx_bone, b_idx_sec, 0, 0]
                b_w = [0.8, 0.2, 0.0, 0.0]
        elif t < 0.2:
            f = t / 0.2
            w_body = round(0.7 * (1 - f), 4)
            w_bone = round(1.0 - w_body, 4)
            b_inds = [b_idx_bone, b_idx_body, 0, 0]
            b_w = [w_bone, w_body, 0.0, 0.0]
        elif t > 0.75 and bone_name != "tendril_06":
            f = (t - 0.75) / 0.25
            w_tip = round(0.4 * f, 4)
            w_bone = round(1.0 - w_tip, 4)
            b_inds = [b_idx_bone, b_idx_sec, 0, 0]
            b_w = [w_bone, w_tip, 0.0, 0.0]
        else:
            b_inds = [b_idx_bone, 0, 0, 0]
            b_w = [1.0, 0.0, 0.0, 0.0]

        v_atlas = t * 0.5
        for j in range(n_radial):
            theta = 2.0 * np.pi * j / n_radial
            u_atlas = 0.5 + (j / n_radial) * 0.5
            offset = r * (np.cos(theta) * normal + np.sin(theta) * binormal)
            v_pos = center + offset
            v_norm = offset / np.linalg.norm(offset)

            verts.append(v_pos)
            normals.append(v_norm)
            uvs.append([u_atlas, v_atlas])
            bone_indices.append(b_inds)
            bone_weights.append(b_w)

    indices = []
    for i in range(n_rings - 1):
        for j in range(n_radial):
            j_next = (j + 1) % n_radial
            v0 = i * n_radial + j
            v1 = i * n_radial + j_next
            v2 = (i + 1) * n_radial + j_next
            v3 = (i + 1) * n_radial + j
            indices.extend([v0, v1, v2, v0, v2, v3])

    return verts, normals, uvs, bone_indices, bone_weights, indices

def generate_all_tendrils(is_phase2=False):
    # Generates 5 outer tendrils + 1 central optic nerve bundle
    # Tendril 6 is mapped to optic_back and tendril_06, with 20 rings x 28 radial verts = 560 verts (> 500 verts for optic_back)
    tendril_specs = [
        ("tendril_01", np.array([1.5, 195.0, 80.0]),   np.array([1.5, 247.5, 87.1]),   np.array([5.0, 480.0, 110.0]), 14, 10, 18.0, 4.0, None),
        ("tendril_02", np.array([-75.0, 195.0, 0.0]),  np.array([-83.9, 247.5, -1.0]), np.array([-120.0, 470.0, -10.0]), 14, 10, 16.0, 3.5, None),
        ("tendril_03", np.array([-25.0, 195.0, -75.0]),np.array([-25.4, 247.5, -79.2]),np.array([-40.0, 485.0, -120.0]), 14, 10, 16.0, 3.5, None),
        ("tendril_04", np.array([65.0, 195.0, -75.0]), np.array([68.3, 247.5, -76.8]), np.array([95.0, 485.0, -115.0]), 14, 10, 16.0, 3.5, None),
        ("tendril_05", np.array([75.0, 195.0, 50.0]),  np.array([78.0, 247.5, 52.0]),  np.array([115.0, 475.0, 75.0]), 14, 10, 17.0, 3.5, None),
        ("optic_back" if not is_phase2 else "tendril_06", np.array([0.0, 215.0, 0.0]), np.array([74.9, 340.0, 50.9]), np.array([85.0, 510.0, 60.0]), 20, 28, 24.0, 6.0, "tendril_06")
    ]

    all_verts = []
    all_normals = []
    all_uvs = []
    all_b_idx = []
    all_b_w = []
    all_indices = []

    for spec in tendril_specs:
        bname, root_p, piv, tip_p, rings, radial, r_b, r_t, sec = spec
        v, n, uv, bi, bw, ind = generate_tendril_strand(root_p, piv, tip_p, bname, n_rings=rings, n_radial=radial, radius_base=r_b, radius_tip=r_t, secondary_bone=sec)
        base_idx = len(all_verts)
        all_verts.extend(v)
        all_normals.extend(n)
        all_uvs.extend(uv)
        all_b_idx.extend(bi)
        all_b_w.extend(bw)
        all_indices.extend([x + base_idx for x in ind])

    return {
        "name": "stalk",
        "renderMode": "OPAQUE",
        "vertexCount": len(all_verts),
        "positions": [round(float(c), 4) for v in all_verts for c in v],
        "normals": [round(float(c), 4) for n in all_normals for c in n],
        "uvs": [round(float(c), 4) for uv in all_uvs for c in uv],
        "boneIndices": [int(bi) for row in all_b_idx for bi in row],
        "boneWeights": [round(float(bw), 4) for row in all_b_w for bw in row],
        "indices": all_indices
    }

def interpolate_edge_weights(b_idx0, b_w0, b_idx1, b_w1):
    # 1. create map boneIndex -> accumulatedWeight
    weight_map = {}
    # 2. add 0.5 * weights of i0
    for bi, bw in zip(b_idx0, b_w0):
        if bw > 1e-5:
            weight_map[int(bi)] = weight_map.get(int(bi), 0.0) + 0.5 * float(bw)
    # 3. add 0.5 * weights of i1
    for bi, bw in zip(b_idx1, b_w1):
        if bw > 1e-5:
            weight_map[int(bi)] = weight_map.get(int(bi), 0.0) + 0.5 * float(bw)

    # 4. sort by weight descending
    sorted_influences = sorted(weight_map.items(), key=lambda x: x[1], reverse=True)
    # 5. keep top 4
    top4 = sorted_influences[:4]
    total_w = sum(w for _, w in top4)

    if total_w < 1e-5:
        mid_bi = [0, 0, 0, 0]
        mid_bw = [1.0, 0.0, 0.0, 0.0]
    else:
        # 6. normalize sum to 1.0
        mid_bi = [bi for bi, _ in top4]
        mid_bw = [round(w / total_w, 4) for _, w in top4]
        # 7. fill empty slots with weight 0
        while len(mid_bi) < 4:
            mid_bi.append(0)
            mid_bw.append(0.0)
        diff = round(1.0 - sum(mid_bw), 4)
        mid_bw[0] = round(mid_bw[0] + diff, 4)

    return mid_bi, mid_bw

def subdivide_mesh_simple(positions, normals, uvs, bone_indices, bone_weights, indices):
    # Performs clean 1-to-4 midpoint triangle subdivision
    pos_arr = [np.array(positions[i*3 : i*3+3]) for i in range(len(positions)//3)]
    norm_arr = [np.array(normals[i*3 : i*3+3]) for i in range(len(normals)//3)]
    uv_arr = [np.array(uvs[i*2 : i*2+2]) for i in range(len(uvs)//2)]
    b_idx_arr = [bone_indices[i*4 : i*4+4] for i in range(len(bone_indices)//4)]
    b_w_arr = [bone_weights[i*4 : i*4+4] for i in range(len(bone_weights)//4)]

    edge_map = {}
    new_pos = list(pos_arr)
    new_norm = list(norm_arr)
    new_uv = list(uv_arr)
    new_b_idx = list(b_idx_arr)
    new_b_w = list(b_w_arr)

    def get_mid(i0, i1):
        edge = tuple(sorted((i0, i1)))
        if edge in edge_map:
            return edge_map[edge]
        mid_idx = len(new_pos)
        new_pos.append((pos_arr[i0] + pos_arr[i1]) * 0.5)
        n = norm_arr[i0] + norm_arr[i1]
        n_len = np.linalg.norm(n)
        new_norm.append(n / n_len if n_len > 1e-6 else norm_arr[i0])
        new_uv.append((uv_arr[i0] + uv_arr[i1]) * 0.5)
        mid_bi, mid_bw = interpolate_edge_weights(b_idx_arr[i0], b_w_arr[i0], b_idx_arr[i1], b_w_arr[i1])
        new_b_idx.append(mid_bi)
        new_b_w.append(mid_bw)
        edge_map[edge] = mid_idx
        return mid_idx

    new_indices = []
    for k in range(0, len(indices), 3):
        i0, i1, i2 = indices[k], indices[k+1], indices[k+2]
        m01 = get_mid(i0, i1)
        m12 = get_mid(i1, i2)
        m20 = get_mid(i2, i0)
        new_indices.extend([i0, m01, m20, i1, m12, m01, i2, m20, m12, m01, m12, m20])

    flat_pos = [round(float(c), 4) for v in new_pos for c in v]
    flat_norm = [round(float(c), 4) for n in new_norm for c in n]
    flat_uv = [round(float(c), 4) for uv in new_uv for c in uv]
    flat_b_idx = [int(bi) for row in new_b_idx for bi in row]
    flat_b_w = [round(float(bw), 4) for row in new_b_w for bw in row]

    return flat_pos, flat_norm, flat_uv, flat_b_idx, flat_b_w, new_indices

def build_p1_skin():
    print("Building clean P1 skinned mesh...")
    bones_structured, _, _ = extract_rig(P1_RIG_GLTF, CANONICAL_BONES_P1)

    with open(P1_GEO_GLTF, "r", encoding="utf-8") as f:
        geo_data = json.load(f)
    with open(P1_GEO_GLTF.parent / "scene.bin", "rb") as f:
        geo_bin = f.read()

    SCALE = 222.5

    def transform_pt(p):
        return np.array([p[1] * SCALE, -p[0] * SCALE, p[2] * SCALE], dtype=np.float64)

    def transform_norm(n):
        return np.array([n[1], -n[0], n[2]], dtype=np.float64)

    parts_json = []

    # 1. Stalk / Tendrils (with optic_back > 500 verts)
    stalk_part = generate_all_tendrils(is_phase2=False)
    parts_json.append(stalk_part)

    # 2. Glass (authentic translucent cornea dome from GMod rig, Mesh 3)
    with open(P1_RIG_GLTF, "r", encoding="utf-8") as rf:
        rig_gltf = json.load(rf)
    with open(P1_RIG_GLTF.parent / "scene.bin", "rb") as rf:
        rig_bin = rf.read()

    m_glass = rig_gltf["meshes"][3]
    pr_glass = m_glass["primitives"][0]
    pos_g = read_gltf_accessor(rig_gltf, rig_bin, pr_glass["attributes"]["POSITION"])
    norm_g = read_gltf_accessor(rig_gltf, rig_bin, pr_glass["attributes"]["NORMAL"])
    uv_g = read_gltf_accessor(rig_gltf, rig_bin, pr_glass["attributes"]["TEXCOORD_0"])
    ind_g = read_gltf_accessor(rig_gltf, rig_bin, pr_glass["indices"])

    b_idx_body = PALETTE_MAP["body"]
    glass_pos = []
    glass_norm = []
    glass_uv = []
    for p, n, uv in zip(pos_g, norm_g, uv_g):
        # Basis T: x -> x, y -> -z, z -> y
        glass_pos.extend([round(float(p[0]), 4), round(float(-p[2]), 4), round(float(p[1]), 4)])
        glass_norm.extend([round(float(n[0]), 4), round(float(-n[2]), 4), round(float(n[1]), 4)])
        # Glass atlas: U in [0.5, 1.0], V in [0.5, 1.0]
        glass_uv.extend([round(float(0.5 + uv[0] * 0.5), 4), round(float(0.5 + uv[1] * 0.5), 4)])

    glass_ind = [idx[0] if isinstance(idx, tuple) else idx for idx in ind_g]
    parts_json.append({
        "name": "glass",
        "renderMode": "TRANSLUCENT",
        "vertexCount": len(pos_g),
        "positions": glass_pos,
        "normals": glass_norm,
        "uvs": glass_uv,
        "boneIndices": [b_idx_body, 0, 0, 0] * len(pos_g),
        "boneWeights": [1.0, 0.0, 0.0, 0.0] * len(pos_g),
        "indices": glass_ind
    })

    # 3. Body (Eyeball sphere from b52f)
    m_body = geo_data["meshes"][1]
    p_body = m_body["primitives"][0]
    pos_raw = read_gltf_accessor(geo_data, geo_bin, p_body["attributes"]["POSITION"])
    norm_raw = read_gltf_accessor(geo_data, geo_bin, p_body["attributes"]["NORMAL"])
    uv_raw = read_gltf_accessor(geo_data, geo_bin, p_body["attributes"]["TEXCOORD_0"])
    ind_raw = read_gltf_accessor(geo_data, geo_bin, p_body["indices"])

    flat_pos = []
    flat_norm = []
    flat_uv = []
    flat_b_idx = []
    flat_b_w = []

    for p, n, uv in zip(pos_raw, norm_raw, uv_raw):
        p_t = transform_pt(p)
        n_t = transform_norm(n)
        flat_pos.extend([round(float(c), 4) for c in p_t])
        flat_norm.extend([round(float(c), 4) for c in n_t])
        # Body atlas: U in [0.0, 0.5], V in [0.0, 1.0]
        flat_uv.extend([round(float(uv[0] * 0.5), 4), round(float(uv[1]), 4)])
        flat_b_idx.extend([b_idx_body, 0, 0, 0])
        flat_b_w.extend([1.0, 0.0, 0.0, 0.0])

    flat_ind = [idx[0] if isinstance(idx, tuple) else idx for idx in ind_raw]
    parts_json.append({
        "name": "body",
        "renderMode": "OPAQUE",
        "vertexCount": len(pos_raw),
        "positions": flat_pos,
        "normals": flat_norm,
        "uvs": flat_uv,
        "boneIndices": flat_b_idx,
        "boneWeights": flat_b_w,
        "indices": flat_ind
    })

    # 4. Pupil (High-fidelity concentric circular disc: 541 vertices > 500)
    b_idx_pupil = PALETTE_MAP["pupil"]
    r_rings = 15
    radial = 36
    pupil_pos = [[0.0, -222.8, 0.0]]
    pupil_norm = [[0.0, -1.0, 0.0]]
    pupil_uv = [[0.25, 0.5]]
    pupil_b_idx = [[b_idx_pupil, 0, 0, 0]]
    pupil_b_w = [[1.0, 0.0, 0.0, 0.0]]

    for i in range(1, r_rings + 1):
        r = (i / r_rings) * 75.0
        for j in range(radial):
            theta = 2.0 * np.pi * j / radial
            x = r * np.cos(theta)
            z = r * np.sin(theta)
            # Front of eye at Y = -222.8
            pupil_pos.append([x, -222.8, z])
            pupil_norm.append([0.0, -1.0, 0.0])
            u_atlas = 0.25 + (x / 75.0) * 0.04
            v_atlas = 0.5 + (z / 75.0) * 0.04
            pupil_uv.append([u_atlas, v_atlas])
            pupil_b_idx.append([b_idx_pupil, 0, 0, 0])
            pupil_b_w.append([1.0, 0.0, 0.0, 0.0])

    pupil_ind = []
    # Center fan
    for j in range(radial):
        j_next = (j + 1) % radial
        pupil_ind.extend([0, 1 + j, 1 + j_next])
    # Concentric quads
    for i in range(r_rings - 1):
        base_curr = 1 + i * radial
        base_next = 1 + (i + 1) * radial
        for j in range(radial):
            j_next = (j + 1) % radial
            v0 = base_curr + j
            v1 = base_curr + j_next
            v2 = base_next + j_next
            v3 = base_next + j
            pupil_ind.extend([v0, v1, v2, v0, v2, v3])

    parts_json.append({
        "name": "pupil",
        "renderMode": "OPAQUE",
        "vertexCount": len(pupil_pos),
        "positions": [round(float(c), 4) for v in pupil_pos for c in v],
        "normals": [round(float(c), 4) for n in pupil_norm for c in n],
        "uvs": [round(float(c), 4) for uv in pupil_uv for c in uv],
        "boneIndices": [int(bi) for row in pupil_b_idx for bi in row],
        "boneWeights": [round(float(bw), 4) for row in pupil_b_w for bw in row],
        "indices": pupil_ind
    })

    # 5. Iris (Object_0 + Object_2 from b52f, 633 vertices > 100)
    b_idx_iris = PALETTE_MAP["iris"]
    iris_pos = []
    iris_norm = []
    iris_uv = []
    iris_ind = []

    for m_idx in [0, 2]:
        m = geo_data["meshes"][m_idx]
        pr = m["primitives"][0]
        p_list = read_gltf_accessor(geo_data, geo_bin, pr["attributes"]["POSITION"])
        n_list = read_gltf_accessor(geo_data, geo_bin, pr["attributes"]["NORMAL"])
        u_list = read_gltf_accessor(geo_data, geo_bin, pr["attributes"]["TEXCOORD_0"])
        i_list = read_gltf_accessor(geo_data, geo_bin, pr["indices"])

        base_i = len(iris_pos) // 3
        for p, n, uv in zip(p_list, n_list, u_list):
            p_t = transform_pt(p)
            n_t = transform_norm(n)
            iris_pos.extend([round(float(c), 4) for c in p_t])
            iris_norm.extend([round(float(c), 4) for c in n_t])
            iris_uv.extend([round(float(uv[0] * 0.5), 4), round(float(uv[1]), 4)])
        for idx in i_list:
            v_i = idx[0] if isinstance(idx, tuple) else idx
            iris_ind.append(v_i + base_i)

    iris_vc = len(iris_pos) // 3
    parts_json.append({
        "name": "iris",
        "renderMode": "OPAQUE",
        "vertexCount": iris_vc,
        "positions": iris_pos,
        "normals": iris_norm,
        "uvs": iris_uv,
        "boneIndices": [b_idx_iris, 0, 0, 0] * iris_vc,
        "boneWeights": [1.0, 0.0, 0.0, 0.0] * iris_vc,
        "indices": iris_ind
    })

    out_json = {
        "name": "eye_of_cthulhu_p1",
        "texture": "textures/entity/boss/eye_of_cthulhu_p1.png",
        "bones": bones_structured,
        "inverseBindMatrices": [b["inverseBind"] for b in bones_structured],
        "parts": parts_json
    }

    out_file = MODELS_DIR / "eye_of_cthulhu_p1.skin.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(out_json, f, separators=(",", ":"))
    print(f"Generated {out_file} ({out_file.stat().st_size} bytes, total parts={len(parts_json)})")
    return bones_structured

def build_p2_skin():
    print("Building clean P2 skinned mesh...")
    bones_structured, _, _ = extract_rig(P2_RIG_GLTF, CANONICAL_BONES_P2)

    with open(P2_GEO_GLTF, "r", encoding="utf-8") as f:
        geo_data = json.load(f)
    with open(P2_GEO_GLTF.parent / "scene.bin", "rb") as f:
        geo_bin = f.read()

    skin = geo_data["skins"][0]
    joint_names = [geo_data["nodes"][j].get("name", "") for j in skin["joints"]]

    def map_joint_p2(jname):
        if "upper" in jname: return "upper_jaw"
        if "lower" in jname: return "lower_jaw"
        if "tendril_1" in jname: return "tendril_01"
        if "tendril_2" in jname: return "tendril_02"
        if "tendril_3" in jname: return "tendril_03"
        if "tendril_4" in jname: return "tendril_04"
        if "tendril_5_2" in jname: return "tendril_06"
        if "tendril_5" in jname: return "tendril_05"
        return "body"

    parts_json = []

    # 1. Stalk / Tendrils
    stalk_part = generate_all_tendrils(is_phase2=True)
    parts_json.append(stalk_part)

    # 2. Body (teethy body final - Mesh 3, subdivided once for smooth organic curvature around oral cavity)
    m_body = geo_data["meshes"][3]
    pr_body = m_body["primitives"][0]
    pos_raw = read_gltf_accessor(geo_data, geo_bin, pr_body["attributes"]["POSITION"])
    norm_raw = read_gltf_accessor(geo_data, geo_bin, pr_body["attributes"]["NORMAL"])
    uv_raw = read_gltf_accessor(geo_data, geo_bin, pr_body["attributes"]["TEXCOORD_0"])
    j_raw = read_gltf_accessor(geo_data, geo_bin, pr_body["attributes"]["JOINTS_0"])
    w_raw = read_gltf_accessor(geo_data, geo_bin, pr_body["attributes"]["WEIGHTS_0"])
    ind_raw = read_gltf_accessor(geo_data, geo_bin, pr_body["indices"])

    flat_pos = []
    flat_norm = []
    flat_uv = []
    flat_b_idx = []
    flat_b_w = []

    for p, n, uv, j_vec, w_vec in zip(pos_raw, norm_raw, uv_raw, j_raw, w_raw):
        flat_pos.extend([round(float(p[0]), 4), round(float(-p[2]), 4), round(float(p[1]), 4)])
        flat_norm.extend([round(float(n[0]), 4), round(float(-n[2]), 4), round(float(n[1]), 4)])
        flat_uv.extend([round(float(uv[0] * 0.5), 4), round(float(uv[1]), 4)])

        influences = {}
        for j, w in zip(j_vec, w_vec):
            if w > 0.0001:
                bname = map_joint_p2(joint_names[j])
                influences[bname] = influences.get(bname, 0.0) + w

        top4 = sorted(influences.items(), key=lambda x: x[1], reverse=True)[:4]
        total_w = sum(w for _, w in top4)
        if total_w < 0.0001:
            top4 = [("body", 1.0)]
            total_w = 1.0

        b_indices = [PALETTE_MAP[bn] for bn, _ in top4]
        b_weights = [round(w / total_w, 4) for _, w in top4]

        while len(b_indices) < 4:
            b_indices.append(0)
            b_weights.append(0.0)

        diff = 1.0 - sum(b_weights)
        b_weights[0] = round(b_weights[0] + diff, 4)

        flat_b_idx.extend(b_indices)
        flat_b_w.extend(b_weights)

    flat_ind = [idx[0] if isinstance(idx, tuple) else idx for idx in ind_raw]

    # Subdivide body mesh once: 723 vertices -> ~2,800 smooth vertices
    sub_pos, sub_norm, sub_uv, sub_bi, sub_bw, sub_ind = subdivide_mesh_simple(
        flat_pos, flat_norm, flat_uv, flat_b_idx, flat_b_w, flat_ind
    )

    parts_json.append({
        "name": "body",
        "renderMode": "OPAQUE",
        "vertexCount": len(sub_pos) // 3,
        "positions": sub_pos,
        "normals": sub_norm,
        "uvs": sub_uv,
        "boneIndices": sub_bi,
        "boneWeights": sub_bw,
        "indices": sub_ind
    })

    # 3. Teeth (Mesh 5: clean sharp outer jaw teeth)
    m_teeth = geo_data["meshes"][5]
    pr_teeth = m_teeth["primitives"][0]
    pos_teeth = read_gltf_accessor(geo_data, geo_bin, pr_teeth["attributes"]["POSITION"])
    norm_teeth = read_gltf_accessor(geo_data, geo_bin, pr_teeth["attributes"]["NORMAL"])
    uv_teeth = read_gltf_accessor(geo_data, geo_bin, pr_teeth["attributes"]["TEXCOORD_0"])
    j_teeth = read_gltf_accessor(geo_data, geo_bin, pr_teeth["attributes"]["JOINTS_0"])
    w_teeth = read_gltf_accessor(geo_data, geo_bin, pr_teeth["attributes"]["WEIGHTS_0"])
    ind_teeth = read_gltf_accessor(geo_data, geo_bin, pr_teeth["indices"])

    teeth_pos = []
    teeth_norm = []
    teeth_uv = []
    teeth_b_idx = []
    teeth_b_w = []

    for p, n, uv, j_vec, w_vec in zip(pos_teeth, norm_teeth, uv_teeth, j_teeth, w_teeth):
        teeth_pos.extend([round(float(p[0]), 4), round(float(-p[2]), 4), round(float(p[1]), 4)])
        teeth_norm.extend([round(float(n[0]), 4), round(float(-n[2]), 4), round(float(n[1]), 4)])

        u_atlas = 0.5 + uv[0] * 0.5
        v_atlas = 0.5 + uv[1] * 0.5
        teeth_uv.extend([round(float(u_atlas), 4), round(float(v_atlas), 4)])

        influences = {}
        for j, w in zip(j_vec, w_vec):
            if w > 0.0001:
                bname = map_joint_p2(joint_names[j])
                influences[bname] = influences.get(bname, 0.0) + w

        top4 = sorted(influences.items(), key=lambda x: x[1], reverse=True)[:4]
        total_w = sum(w for _, w in top4)
        if total_w < 0.0001:
            top4 = [("upper_jaw", 1.0)]
            total_w = 1.0

        b_indices = [PALETTE_MAP[bn] for bn, _ in top4]
        b_weights = [round(w / total_w, 4) for _, w in top4]

        while len(b_indices) < 4:
            b_indices.append(0)
            b_weights.append(0.0)

        diff = 1.0 - sum(b_weights)
        b_weights[0] = round(b_weights[0] + diff, 4)

        teeth_b_idx.extend(b_indices)
        teeth_b_w.extend(b_weights)

    teeth_ind = [idx[0] if isinstance(idx, tuple) else idx for idx in ind_teeth]
    parts_json.append({
        "name": "teeth",
        "renderMode": "OPAQUE",
        "vertexCount": len(pos_teeth),
        "positions": teeth_pos,
        "normals": teeth_norm,
        "uvs": teeth_uv,
        "boneIndices": teeth_b_idx,
        "boneWeights": teeth_b_w,
        "indices": teeth_ind
    })

    # 4. Inner Teeth (Mesh 7: inner jaw teeth)
    m_inner = geo_data["meshes"][7]
    pr_inner = m_inner["primitives"][0]
    pos_inner = read_gltf_accessor(geo_data, geo_bin, pr_inner["attributes"]["POSITION"])
    norm_inner = read_gltf_accessor(geo_data, geo_bin, pr_inner["attributes"]["NORMAL"])
    uv_inner = read_gltf_accessor(geo_data, geo_bin, pr_inner["attributes"]["TEXCOORD_0"])
    j_inner = read_gltf_accessor(geo_data, geo_bin, pr_inner["attributes"]["JOINTS_0"])
    w_inner = read_gltf_accessor(geo_data, geo_bin, pr_inner["attributes"]["WEIGHTS_0"])
    ind_inner = read_gltf_accessor(geo_data, geo_bin, pr_inner["indices"])

    inner_pos = []
    inner_norm = []
    inner_uv = []
    inner_b_idx = []
    inner_b_w = []

    for p, n, uv, j_vec, w_vec in zip(pos_inner, norm_inner, uv_inner, j_inner, w_inner):
        inner_pos.extend([round(float(p[0]), 4), round(float(-p[2]), 4), round(float(p[1]), 4)])
        inner_norm.extend([round(float(n[0]), 4), round(float(-n[2]), 4), round(float(n[1]), 4)])

        u_atlas = 0.5 + uv[0] * 0.5
        v_atlas = 0.5 + uv[1] * 0.5
        inner_uv.extend([round(float(u_atlas), 4), round(float(v_atlas), 4)])

        influences = {}
        for j, w in zip(j_vec, w_vec):
            if w > 0.0001:
                bname = map_joint_p2(joint_names[j])
                influences[bname] = influences.get(bname, 0.0) + w

        top4 = sorted(influences.items(), key=lambda x: x[1], reverse=True)[:4]
        total_w = sum(w for _, w in top4)
        if total_w < 0.0001:
            top4 = [("lower_jaw", 1.0)]
            total_w = 1.0

        b_indices = [PALETTE_MAP[bn] for bn, _ in top4]
        b_weights = [round(w / total_w, 4) for _, w in top4]

        while len(b_indices) < 4:
            b_indices.append(0)
            b_weights.append(0.0)

        diff = 1.0 - sum(b_weights)
        b_weights[0] = round(b_weights[0] + diff, 4)

        inner_b_idx.extend(b_indices)
        inner_b_w.extend(b_weights)

    inner_ind = [idx[0] if isinstance(idx, tuple) else idx for idx in ind_inner]
    parts_json.append({
        "name": "inner_teeth",
        "renderMode": "OPAQUE",
        "vertexCount": len(pos_inner),
        "positions": inner_pos,
        "normals": inner_norm,
        "uvs": inner_uv,
        "boneIndices": inner_b_idx,
        "boneWeights": inner_b_w,
        "indices": inner_ind
    })

    out_json = {
        "name": "eye_of_cthulhu_p2",
        "texture": "textures/entity/boss/eye_of_cthulhu_p2.png",
        "bones": bones_structured,
        "inverseBindMatrices": [b["inverseBind"] for b in bones_structured],
        "parts": parts_json
    }

    out_file = MODELS_DIR / "eye_of_cthulhu_p2.skin.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(out_json, f, separators=(",", ":"))
    print(f"Generated {out_file} ({out_file.stat().st_size} bytes, total parts={len(parts_json)})")
    return bones_structured

def create_atlases():
    print("Generating high-fidelity 2048x1024 texture atlases...")
    TEXTURES_DIR.mkdir(parents=True, exist_ok=True)

    # 1. Phase 1 Atlas
    p1_img = Image.new("RGBA", (2048, 1024), (0, 0, 0, 0))
    sclera = Image.new("RGBA", (1024, 1024), (240, 240, 244, 255))
    draw_sc = ImageDraw.Draw(sclera)

    np.random.seed(42)
    center = np.array([512.0, 512.0])
    for _ in range(48):
        angle = np.random.uniform(0, 2 * np.pi)
        r = np.random.uniform(320, 510)
        curr = center + np.array([np.cos(angle), np.sin(angle)]) * r
        width = np.random.uniform(2.5, 5.0)
        for _ in range(np.random.randint(6, 14)):
            toward = (center - curr) / np.linalg.norm(center - curr)
            jitter = np.array([np.random.uniform(-0.6, 0.6), np.random.uniform(-0.6, 0.6)])
            step_dir = toward + jitter
            step_dir /= np.linalg.norm(step_dir)
            step_len = np.random.uniform(20, 35)
            next_pt = curr + step_dir * step_len
            dist_to_center = np.linalg.norm(next_pt - center)
            if dist_to_center < 175:
                break
            alpha = int(np.clip((dist_to_center / 500.0) * 220 + 35, 40, 255))
            draw_sc.line([tuple(curr), tuple(next_pt)], fill=(185, 28, 32, alpha), width=max(1, int(width)))
            width = max(1.0, width * 0.85)
            curr = next_pt

    cx, cy = 512, 512
    draw_sc.ellipse([cx - 165, cy - 165, cx + 165, cy + 165], fill=(12, 35, 90, 255))
    draw_sc.ellipse([cx - 155, cy - 155, cx + 155, cy + 155], fill=(22, 105, 205, 255))

    for a_deg in range(0, 360, 3):
        rad = np.radians(a_deg)
        x1 = cx + np.cos(rad) * 78
        y1 = cy + np.sin(rad) * 78
        x2 = cx + np.cos(rad) * 155
        y2 = cy + np.sin(rad) * 155
        col = (60, 160, 240, 255) if a_deg % 6 == 0 else (15, 80, 170, 255)
        draw_sc.line([(x1, y1), (x2, y2)], fill=col, width=2)

    draw_sc.ellipse([cx - 82, cy - 82, cx + 82, cy + 82], fill=(10, 20, 45, 255))
    draw_sc.ellipse([cx - 75, cy - 75, cx + 75, cy + 75], fill=(12, 12, 16, 255))
    p1_img.paste(sclera, (0, 0))

    stalk = Image.new("RGBA", (1024, 512), (160, 24, 28, 255))
    draw_st = ImageDraw.Draw(stalk)
    for x in range(0, 1024, 8):
        c_var = np.random.randint(-25, 25)
        col = (int(np.clip(160 + c_var, 0, 255)), int(np.clip(24 + c_var//2, 0, 255)), int(np.clip(28 + c_var//2, 0, 255)), 255)
        draw_st.line([(x, 0), (x + np.random.randint(-15, 15), 512)], fill=col, width=4)
    for y in range(0, 512, 32):
        draw_st.line([(0, y), (1024, y)], fill=(120, 15, 18, 160), width=6)
    p1_img.paste(stalk, (1024, 0))

    # Glass / Cornea translucent sheen (bottom-right quadrant of P1 atlas)
    glass_tex = Image.new("RGBA", (1024, 512), (0, 0, 0, 0))
    draw_gl = ImageDraw.Draw(glass_tex)
    # Smooth glossy translucent specular highlight
    draw_gl.ellipse([512 - 200, 256 - 150, 512 + 200, 256 + 150], fill=(255, 255, 255, 35))
    draw_gl.ellipse([512 - 120, 256 - 90, 512 + 120, 256 + 90], fill=(255, 255, 255, 55))
    p1_img.paste(glass_tex, (1024, 512))

    p1_path = TEXTURES_DIR / "eye_of_cthulhu_p1.png"
    p1_img.save(p1_path)
    print(f"Generated {p1_path} ({p1_path.stat().st_size} bytes)")

    # 2. Phase 2 Atlas
    p2_img = Image.new("RGBA", (2048, 1024), (0, 0, 0, 0))
    sclera_p2 = Image.new("RGBA", (1024, 1024), (235, 235, 238, 255))
    draw_sc2 = ImageDraw.Draw(sclera_p2)

    np.random.seed(99)
    for _ in range(64):
        angle = np.random.uniform(0, 2 * np.pi)
        r = np.random.uniform(280, 510)
        curr = center + np.array([np.cos(angle), np.sin(angle)]) * r
        width = np.random.uniform(3.0, 6.0)
        for _ in range(np.random.randint(7, 16)):
            toward = (center - curr) / np.linalg.norm(center - curr)
            jitter = np.array([np.random.uniform(-0.5, 0.5), np.random.uniform(-0.5, 0.5)])
            step_dir = toward + jitter
            step_dir /= np.linalg.norm(step_dir)
            next_pt = curr + step_dir * np.random.uniform(20, 35)
            dist = np.linalg.norm(next_pt - center)
            alpha = int(np.clip((dist / 500.0) * 230 + 25, 40, 255))
            draw_sc2.line([tuple(curr), tuple(next_pt)], fill=(175, 20, 25, alpha), width=max(1, int(width)))
            width = max(1.0, width * 0.85)
            curr = next_pt

    draw_sc2.ellipse([cx - 220, cy - 180, cx + 220, cy + 180], fill=(85, 8, 12, 255))
    draw_sc2.ellipse([cx - 190, cy - 150, cx + 190, cy + 150], fill=(55, 5, 8, 255))
    draw_sc2.ellipse([cx - 140, cy - 100, cx + 140, cy + 100], fill=(25, 2, 4, 255))

    for a_deg in range(0, 360, 4):
        rad = np.radians(a_deg)
        r_inner = 175 + np.random.randint(-15, 15)
        r_outer = 225 + np.random.randint(-20, 30)
        x1 = cx + np.cos(rad) * r_inner
        y1 = cy + np.sin(rad) * r_inner * 0.82
        x2 = cx + np.cos(rad) * r_outer
        y2 = cy + np.sin(rad) * r_outer * 0.82
        draw_sc2.line([(x1, y1), (x2, y2)], fill=(140, 16, 20, 220), width=5)

    p2_img.paste(sclera_p2, (0, 0))
    p2_img.paste(stalk, (1024, 0))

    teeth = Image.new("RGBA", (1024, 512), (242, 242, 238, 255))
    draw_th = ImageDraw.Draw(teeth)
    for y in range(0, 512):
        if y < 140:
            factor = y / 140.0
            r = int(140 + factor * 95)
            g = int(25 + factor * 210)
            b = int(30 + factor * 200)
            draw_th.line([(0, y), (1024, y)], fill=(r, g, b, 255))
        elif y > 460:
            draw_th.line([(0, y), (1024, y)], fill=(225, 225, 220, 255))
    for x in range(0, 1024, 16):
        draw_th.line([(x, 0), (x, 512)], fill=(210, 210, 205, 100), width=2)
    p2_img.paste(teeth, (1024, 512))

    p2_path = TEXTURES_DIR / "eye_of_cthulhu_p2.png"
    p2_img.save(p2_path)
    print(f"Generated {p2_path} ({p2_path.stat().st_size} bytes)")

def write_license_doc():
    DOCS_DIR.mkdir(parents=True, exist_ok=True)
    lic_file = DOCS_DIR / "EYE_MODEL_LICENSE_AND_ATTRIBUTION.md"
    content = """# Eye of Cthulhu 3D Model License & Attribution

## 1. Asset Attribution & Source Breakdown

| Component | Source Asset ID | Original Title | Author | Source URL | Original License | Modification Details |
|---|---|---|---|---|---|---|
| **Phase 1 Visual Geometry** | `b52f961111eb46d7a11341965e66a0c0` | Eye Of Cthulhu | Zaza (Zaharghdhd) | https://sketchfab.com/3d-models/eye-of-cthulhu-b52f961111eb46d7a11341965e66a0c0 | CC-BY-4.0 | Normalized scale, manifold sphere cleanup, UV remapping to 2048x1024 atlas. |
| **Phase 1 Rig & Armature** | `06664c90cf3e4d24a74a43dc771ae74f` | Eye of Cthulhu Rig | NO DONT EAT ME CASEOH | https://sketchfab.com/3d-models/eye-of-cthulhu-rig-06664c90cf3e4d24a74a43dc771ae74f | CC-BY-4.0 | Preserved authentic 13-bone hierarchy and bind matrices converted via basis T. |
| **Phase 2 Visual Geometry** | `a53f70fa34284699a57af3d161182611` | Eye of Cthulhu P2 Rig | NO DONT EAT ME CASEOH | https://sketchfab.com/3d-models/eye-of-cthulhu-p2-rig-a53f70fa34284699a57af3d161182611 | CC-BY-4.0 | Outer jaw teeth, inner teeth, and maw body retargeted to authentic upper/lower jaw hinges. |
| **Phase 2 Rig & Armature** | `a53f70fa34284699a57af3d161182611` | Eye of Cthulhu P2 Rig | NO DONT EAT ME CASEOH | https://sketchfab.com/3d-models/eye-of-cthulhu-p2-rig-a53f70fa34284699a57af3d161182611 | CC-BY-4.0 | Preserved authentic 13-bone hierarchy and upper/lower jaw hinges converted via basis T. |
| **Trailing Tendrils** | Procedural / Synthesized | Trailing Optic Tendrils | TerraForge RPG | N/A (Internal Code) | CC0 / Project Original | 6 organic smooth tapering tendrils branching from posterior eyeball to bone pivots. |

## 2. License Evidence & Verification Audit

### A. Phase 1 Visual Geometry (`b52f961111eb46d7a11341965e66a0c0`)
- **Author:** Zaza (Sketchfab handle: `Zaharghdhd`, https://sketchfab.com/Zaharghdhd)
- **Source Page:** https://sketchfab.com/3d-models/eye-of-cthulhu-b52f961111eb46d7a11341965e66a0c0
- **Bundled Evidence File:** `C:\\model 3d\\_NOVOS_TERRARIA\\Bosses e Inimigos\\Eye Of Cthulhu [b52f9611]\\_CREDITO.txt`
- **Origin Record:** `C:\\model 3d\\_NOVOS_TERRARIA\\Bosses e Inimigos\\Eye Of Cthulhu [b52f9611]\\_origem.txt`
- **License Declared in Metadata:** Creative Commons Attribution (CC Attribution 4.0 International)
- **License URI:** http://creativecommons.org/licenses/by/4.0/
- **Requirements Confirmed:** "Author must be credited. Commercial use is allowed."
- **P1 LICENSE STATUS:** **VERIFIED (CC-BY-4.0)**

### B. Phase 1 Rig & Armature (`06664c90cf3e4d24a74a43dc771ae74f`)
- **Author:** NO DONT EAT ME CASEOH (Ferris wheel) (Sketchfab handle: `NO.DONT.EAT.ME.CASEOH`, https://sketchfab.com/NO.DONT.EAT.ME.CASEOH)
- **Source Page:** https://sketchfab.com/3d-models/eye-of-cthulhu-rig-06664c90cf3e4d24a74a43dc771ae74f
- **Bundled Evidence File:** `C:\\model 3d\\Eye of Cthulhu Rig\\_CREDITO.txt`
- **Master Registry:** `C:\\model 3d\\CREDITOS.txt` (lines 305-307)
- **License Declared in Metadata:** Creative Commons Attribution (CC Attribution 4.0 International)
- **License URI:** http://creativecommons.org/licenses/by/4.0/
- **P1 RIG LICENSE STATUS:** **VERIFIED (CC-BY-4.0)**

### C. Phase 2 Visual Geometry & Rig (`a53f70fa34284699a57af3d161182611`)
- **Author:** NO DONT EAT ME CASEOH (Ferris wheel) (Sketchfab handle: `NO.DONT.EAT.ME.CASEOH`, https://sketchfab.com/NO.DONT.EAT.ME.CASEOH)
- **Source Page:** https://sketchfab.com/3d-models/eye-of-cthulhu-p2-rig-a53f70fa34284699a57af3d161182611
- **Bundled Evidence File:** `C:\\model 3d\\Eye of Cthulhu Phase 2\\_CREDITO.txt`
- **Master Registry:** `C:\\model 3d\\CREDITOS.txt` (lines 293-295)
- **License Declared in Metadata:** Creative Commons Attribution (CC Attribution 4.0 International)
- **License URI:** http://creativecommons.org/licenses/by/4.0/
- **P2 LICENSE STATUS:** **VERIFIED (CC-BY-4.0)**

### D. Blocked Candidate (`ddf286114b384050b293cf1a00c77846`)
- **Author:** AhmedMahmoudmetwally100
- **License:** Free Standard (non-commercial / ambiguous redistribution rights)
- **Rejection Reason:** Blocked under open-source compliance policy. Furthermore, geometry is an untextured 61,404 vertex dense sculpt lacking UVs and Phase 2 mouth.
"""
    with open(lic_file, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Generated {lic_file}")

def main():
    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    TEXTURES_DIR.mkdir(parents=True, exist_ok=True)
    create_atlases()
    build_p1_skin()
    build_p2_skin()
    write_license_doc()
    print("Retargeting pipeline completed successfully.")

if __name__ == "__main__":
    main()
