import bpy
import bmesh
import json
import os
import math
from mathutils import Vector

P1_GLTF = r"C:\model 3d\_processed\06664c90cf3e4d24a74a43dc771ae74f\source\scene.gltf"
P2_GLTF = r"C:\model 3d\_processed\a53f70fa34284699a57af3d161182611\source\scene.gltf"

P1_JSON_OUT = r"C:\Users\Alza\Desktop\terraforge\src\main\resources\assets\terraforge_rpg\models\entity\boss\eye_of_cthulhu_p1.skin.json"
P2_JSON_OUT = r"C:\Users\Alza\Desktop\terraforge\src\main\resources\assets\terraforge_rpg\models\entity\boss\eye_of_cthulhu_p2.skin.json"

# In P1:
# 0: root, 1: body, 2: upper_jaw, 3: lower_jaw, 4: tendril_01, 5: tendril_02,
# 6: tendril_03, 7: tendril_04, 8: tendril_05, 9: tendril_06, 10: iris, 11: pupil, 12: optic_back
BONE_MAP_P1 = {
    "_rootJoint": 0,
    "root_00": 1,
    "tentacle_outer_01": 12,
    "tentacle_outer_end_017": 12,
    "tendril_1_0_02": 4, "tendril_1_1_03": 4, "tendril_1_2_04": 4, "tendril_1_2_end_018": 4,
    "tendril_2_0_08": 5, "tendril_2_1_09": 5, "tendril_2_2_010": 5, "tendril_2_2_end_020": 5,
    "tendril_3_0_011": 6, "tendril_3_1_012": 6, "tendril_3_2_013": 6, "tendril_3_2_end_021": 6,
    "tendril_4_0_014": 7, "tendril_4_1_015": 7, "tendril_4_2_016": 7, "tendril_4_2_end_022": 7,
    "tendril_5_0_05": 8, "tendril_5_1_06": 8,
    "tendril_5_2_07": 9, "tendril_5_2_end_019": 9
}

# In P2:
# 0: root, 1: body, 2: upper_jaw, 3: lower_jaw, 4: tendril_01, 5: tendril_02,
# 6: tendril_03, 7: tendril_04, 8: tendril_05, 9: tendril_06, 10: optic_back, 11: iris, 12: pupil
BONE_MAP_P2 = {
    "_rootJoint": 0,
    "root_00": 1,
    "jaw_upper_01": 2, "jaw_upper_end_018": 2,
    "jaw_lower_02": 3, "jaw_lower_end_019": 3,
    "tendril_1_0_03": 4, "tendril_1_1_04": 4, "tendril_1_2_05": 4, "tendril_1_2_end_020": 4,
    "tendril_2_0_09": 5, "tendril_2_1_010": 5, "tendril_2_2_011": 5, "tendril_2_2_end_022": 5,
    "tendril_3_0_012": 6, "tendril_3_1_013": 6, "tendril_3_2_014": 6, "tendril_3_2_end_023": 6,
    "tendril_4_0_015": 7, "tendril_4_1_016": 7, "tendril_4_2_017": 7, "tendril_4_2_end_024": 7,
    "tendril_5_0_06": 8, "tendril_5_1_07": 8,
    "tendril_5_2_08": 9, "tendril_5_2_end_021": 9
}

def export_phase(gltf_path, out_json_path, parts_config, bone_map, is_phase2):
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.ops.import_scene.gltf(filepath=gltf_path)

    # Read existing bones definition to preserve hierarchy and inverse bind matrices
    with open(out_json_path, "r", encoding="utf-8") as f:
        existing_data = json.load(f)
    bones = existing_data["bones"]

    exported_parts = []

    for p_cfg in parts_config:
        part_name = p_cfg["name"]
        mesh_keyword = p_cfg["keyword"]
        render_mode = p_cfg["renderMode"]
        texture = p_cfg["texture"]

        target_obj = None
        for o in bpy.data.objects:
            if o.type == 'MESH':
                name_l = o.data.name.lower()
                if mesh_keyword in name_l and 'lod1' not in name_l:
                    target_obj = o
                    break

        if not target_obj:
            raise RuntimeError(f"Could not find mesh object for keyword '{mesh_keyword}' in {gltf_path}")

        print(f"Exporting part '{part_name}' from obj {target_obj.name} (mesh {target_obj.data.name})...")

        # Evaluate and triangulate
        bm = bmesh.new()
        bm.from_mesh(target_obj.data)
        bmesh.ops.triangulate(bm, faces=bm.faces[:])
        temp_mesh = bpy.data.meshes.new("temp_tri")
        bm.to_mesh(temp_mesh)
        bm.free()

        uv_layer = temp_mesh.uv_layers.active
        if not uv_layer:
            raise RuntimeError(f"No UV layer found on {target_obj.data.name}")

        vg_names = [vg.name for vg in target_obj.vertex_groups]

        positions = []
        normals = []
        uvs = []
        bone_indices = []
        bone_weights = []
        indices = []

        vertex_map = {}

        for poly in temp_mesh.polygons:
            for l_idx in poly.loop_indices:
                loop = temp_mesh.loops[l_idx]
                v_idx = loop.vertex_index
                vert = temp_mesh.vertices[v_idx]

                pos = vert.co
                norm = loop.normal

                uv = uv_layer.data[l_idx].uv
                u = round(float(uv[0]), 5)
                v = round(1.0 - float(uv[1]), 5)

                # Skin weights mapping
                weight_map = {}
                for g in vert.groups:
                    if g.group < len(vg_names):
                        g_name = vg_names[g.group]
                        b_idx = bone_map.get(g_name, 1)
                        if not is_phase2:
                            if part_name == "stalk" and g_name == "root_00":
                                # Central optic stalk in Phase 1 maps to optic_back (12)
                                b_idx = 12
                            elif part_name == "iris":
                                b_idx = 10
                            elif part_name == "pupil":
                                b_idx = 11
                            elif part_name == "glass":
                                b_idx = 1
                        weight_map[b_idx] = weight_map.get(b_idx, 0.0) + float(g.weight)

                if not weight_map:
                    if not is_phase2:
                        if part_name == "iris":
                            weight_map[10] = 1.0
                        elif part_name == "pupil":
                            weight_map[11] = 1.0
                        elif part_name == "stalk":
                            weight_map[12] = 1.0
                        else:
                            weight_map[1] = 1.0
                    else:
                        weight_map[1] = 1.0

                sorted_weights = sorted(weight_map.items(), key=lambda x: x[1], reverse=True)[:4]
                total_w = sum(w for _, w in sorted_weights)

                b_idxs = [0, 0, 0, 0]
                b_ws = [0.0, 0.0, 0.0, 0.0]

                if total_w < 1e-6:
                    def_b = 10 if part_name == "iris" else (11 if part_name == "pupil" else (12 if part_name == "stalk" else 1))
                    b_idxs[0] = def_b
                    b_ws[0] = 1.0
                else:
                    for k, (bi, bw) in enumerate(sorted_weights):
                        b_idxs[k] = bi
                        b_ws[k] = round(bw / total_w, 4)
                    while len(b_idxs) < 4:
                        b_idxs.append(0)
                        b_ws.append(0.0)
                    diff = round(1.0 - sum(b_ws), 4)
                    b_ws[0] = round(b_ws[0] + diff, 4)

                key = (
                    v_idx,
                    round(float(norm[0]), 3),
                    round(float(norm[1]), 3),
                    round(float(norm[2]), 3),
                    u,
                    v
                )

                if key not in vertex_map:
                    new_idx = len(positions) // 3
                    vertex_map[key] = new_idx
                    positions.extend([round(float(pos[0]), 4), round(float(pos[1]), 4), round(float(pos[2]), 4)])
                    normals.extend([round(float(norm[0]), 4), round(float(norm[1]), 4), round(float(norm[2]), 4)])
                    uvs.extend([u, v])
                    bone_indices.extend(b_idxs[:4])
                    bone_weights.extend(b_ws[:4])

                indices.append(vertex_map[key])

        bpy.data.meshes.remove(temp_mesh)

        vertex_count = len(positions) // 3
        print(f"  Part '{part_name}': {vertex_count} vertices, {len(indices)//3} triangles")

        exported_parts.append({
            "name": part_name,
            "renderMode": render_mode,
            "texture": texture,
            "vertexCount": vertex_count,
            "positions": positions,
            "normals": normals,
            "uvs": uvs,
            "boneIndices": bone_indices,
            "boneWeights": bone_weights,
            "indices": indices
        })

    out_obj = {
        "name": "eye_of_cthulhu_p2" if is_phase2 else "eye_of_cthulhu_p1",
        "texture": "textures/entity/boss/eye_of_cthulhu_p2.png" if is_phase2 else "textures/entity/boss/eye_of_cthulhu_p1.png",
        "bones": bones,
        "parts": exported_parts
    }

    with open(out_json_path, "w", encoding="utf-8") as f:
        json.dump(out_obj, f)
    print(f"Saved: {out_json_path}")

# P1 Part order: stalk, glass, body, pupil, iris (matching EyeSubmeshAndAudioHotfixTest expectation)
P1_PARTS = [
    {"name": "stalk", "keyword": "stalk", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p1_stalk.png"},
    {"name": "glass", "keyword": "glass", "renderMode": "TRANSLUCENT", "texture": "textures/entity/boss/eye/p1_glass.png"},
    {"name": "body", "keyword": "body_boss_eye_cthulhu_v2", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p1_body.png"},
    {"name": "pupil", "keyword": "pupil", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p1_pupil.png"},
    {"name": "iris", "keyword": "iris", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p1_iris.png"}
]

# P2 Part order: stalk, body, teeth, inner_teeth
P2_PARTS = [
    {"name": "stalk", "keyword": "teethy_stalk", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p2_stalk.png"},
    {"name": "body", "keyword": "teethy_body_final", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p2_body.png"},
    {"name": "teeth", "keyword": "teeth_boss_eye_cthulhu_teeth", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p2_teeth.png"},
    {"name": "inner_teeth", "keyword": "inner_teeth_boss_eye_cthulhu_teeth", "renderMode": "OPAQUE", "texture": "textures/entity/boss/eye/p2_inner_teeth.png"}
]

print("=== EXPORTING P1 FROM BLENDER ===")
export_phase(P1_GLTF, P1_JSON_OUT, P1_PARTS, BONE_MAP_P1, is_phase2=False)

print("=== EXPORTING P2 FROM BLENDER ===")
export_phase(P2_GLTF, P2_JSON_OUT, P2_PARTS, BONE_MAP_P2, is_phase2=True)

print("Blender runtime skin export complete.")
