"""
Blender Python script — Render 4-view previews of Eye of Cthulhu candidates.
Run headless:  blender --background --python preview_candidates.py

Reads source GLTFs from C:\model 3d (READ ONLY).
Outputs PNG previews to build/eye_pipeline/previews/
"""

import bpy
import os
import sys
import math
import json
from pathlib import Path

# ── Configuration ──────────────────────────────────────────────
PROJECT_ROOT = Path(r"C:\Users\Alza\Desktop\terraforge")
PREVIEW_DIR = PROJECT_ROOT / "build" / "eye_pipeline" / "previews"

CANDIDATES = [
    {
        "id": "06664c90",
        "label": "P1_Rig_CASEOH",
        "gltf": r"C:\model 3d\Eye of Cthulhu Rig\extraido\scene.gltf",
        "note": "P1 rig by NO DONT EAT ME CASEOH — CC-BY-4.0",
    },
    {
        "id": "a53f70fa",
        "label": "P2_Rig_CASEOH",
        "gltf": r"C:\model 3d\Eye of Cthulhu Phase 2\extraido\scene.gltf",
        "note": "P2 rig by NO DONT EAT ME CASEOH — CC-BY-4.0",
    },
    {
        "id": "2526baa2",
        "label": "pedrohmm123_fanart",
        "gltf": r"C:\model 3d\Eye of Cthulhu - pedrohmm123\extraido\scene.gltf",
        "note": "Stylized fanart by pedrohmm123 — CC-BY-4.0",
    },
    {
        "id": "ddf28611",
        "label": "PREFERIDO_Ahmed",
        "gltf": r"C:\model 3d\Eye of Cthulhu - PREFERIDO\extraido\scene.gltf",
        "note": "High-poly vertex-color model by AhmedMahmoudmetwally100 — Sketchfab Standard",
    },
    {
        "id": "b52f9611",
        "label": "Zaza_EyeOfCthulhu",
        "gltf": r"C:\model 3d\_processed\b52f961111eb46d7a11341965e66a0c0\source\scene.gltf",
        "note": "Polyhedral sphere by Zaza — CC-BY-4.0",
    },
    {
        "id": "fb42d665",
        "label": "shovelsquid_Eye",
        "gltf": r"C:\model 3d\_processed\fb42d6656c694d15a7910b1ac8d044ed\source\scene.gltf",
        "note": "Eye with tentacles + skeleton by shovelsquid — CC-BY-4.0",
    },
    {
        "id": "546bc88e",
        "label": "JJJimmyJam_EyeTerraria",
        "gltf": r"C:\model 3d\_processed\546bc88e319f499e9066b51c89101dc1\source\scene.gltf",
        "note": "Eye Terraria by JJJimmyJam — CC-BY-4.0",
    },
    {
        "id": "6b31f455",
        "label": "probablymodelmaker_Eyeball",
        "gltf": r"C:\model 3d\_processed\6b31f4550b5743e2b7a6d58d0359b6ce\source\scene.gltf",
        "note": "Eyeball by probablymodelmaker — CC-BY-4.0",
    },
]

# Camera angles: (name, rotation_euler_degrees_xyz)
VIEWS = [
    ("front",  (90, 0, 0)),
    ("side",   (90, 0, 90)),
    ("back",   (90, 0, 180)),
    ("top",    (0, 0, 0)),
]

# ── Helpers ────────────────────────────────────────────────────

def clear_scene():
    """Remove all objects from the scene."""
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.delete(use_global=False)
    # Remove orphan data
    for block in bpy.data.meshes:
        bpy.data.meshes.remove(block)
    for block in bpy.data.materials:
        bpy.data.materials.remove(block)
    for block in bpy.data.textures:
        bpy.data.textures.remove(block)
    for block in bpy.data.images:
        bpy.data.images.remove(block)
    for block in bpy.data.armatures:
        bpy.data.armatures.remove(block)
    for block in bpy.data.cameras:
        bpy.data.cameras.remove(block)
    for block in bpy.data.lights:
        bpy.data.lights.remove(block)


def import_gltf(filepath):
    """Import a GLTF/GLB file."""
    bpy.ops.import_scene.gltf(filepath=filepath)


def get_scene_bounds():
    """Get bounding box of all mesh objects in the scene."""
    min_co = [float('inf')] * 3
    max_co = [float('-inf')] * 3
    
    for obj in bpy.data.objects:
        if obj.type == 'MESH':
            # Get world-space bounding box
            for corner in obj.bound_box:
                world_co = obj.matrix_world @ bpy.types.Object.bl_rna.properties['bound_box'].fixed_type.bl_rna  # fallback
                # Simpler approach
                pass
    
    # Use depsgraph for accurate bounds
    for obj in bpy.data.objects:
        if obj.type == 'MESH':
            bbox = [obj.matrix_world @ bpy.mathutils.Vector(corner) for corner in obj.bound_box]
            for co in bbox:
                for i in range(3):
                    min_co[i] = min(min_co[i], co[i])
                    max_co[i] = max(max_co[i], co[i])
    
    return min_co, max_co


def setup_camera_and_light(label):
    """Create camera and light suitable for the imported model."""
    import mathutils
    
    # Compute scene bounds
    min_co = [float('inf')] * 3
    max_co = [float('-inf')] * 3
    
    for obj in bpy.data.objects:
        if obj.type == 'MESH':
            for corner in obj.bound_box:
                world_co = obj.matrix_world @ mathutils.Vector(corner)
                for i in range(3):
                    min_co[i] = min(min_co[i], world_co[i])
                    max_co[i] = max(max_co[i], world_co[i])
    
    if min_co[0] == float('inf'):
        # No mesh found
        center = mathutils.Vector((0, 0, 0))
        size = 2.0
    else:
        center = mathutils.Vector(((min_co[i] + max_co[i]) / 2 for i in range(3)))
        size = max(max_co[i] - min_co[i] for i in range(3))
    
    if size < 0.001:
        size = 2.0
    
    # Create camera
    cam_data = bpy.data.cameras.new(name='PreviewCam')
    cam_data.type = 'ORTHO'
    cam_data.ortho_scale = size * 1.4
    cam_obj = bpy.data.objects.new('PreviewCam', cam_data)
    bpy.context.scene.collection.objects.link(cam_obj)
    bpy.context.scene.camera = cam_obj
    
    # Create sun light
    light_data = bpy.data.lights.new(name='PreviewSun', type='SUN')
    light_data.energy = 3.0
    light_obj = bpy.data.objects.new('PreviewSun', light_data)
    light_obj.rotation_euler = (math.radians(50), math.radians(30), math.radians(20))
    bpy.context.scene.collection.objects.link(light_obj)
    
    # Fill light
    fill_data = bpy.data.lights.new(name='FillLight', type='SUN')
    fill_data.energy = 1.0
    fill_obj = bpy.data.objects.new('FillLight', fill_data)
    fill_obj.rotation_euler = (math.radians(120), math.radians(-30), math.radians(-60))
    bpy.context.scene.collection.objects.link(fill_obj)
    
    return cam_obj, center, size


def render_view(cam_obj, center, size, view_name, rotation_deg, output_path):
    """Position camera for the given view and render."""
    import mathutils
    
    rx, ry, rz = [math.radians(d) for d in rotation_deg]
    cam_obj.rotation_euler = (rx, ry, rz)
    
    # Position camera based on view
    dist = size * 2.0
    if view_name == "front":
        cam_obj.location = (center[0], center[1] - dist, center[2])
    elif view_name == "side":
        cam_obj.location = (center[0] + dist, center[1], center[2])
    elif view_name == "back":
        cam_obj.location = (center[0], center[1] + dist, center[2])
    elif view_name == "top":
        cam_obj.location = (center[0], center[1], center[2] + dist)
    
    # Render settings
    scene = bpy.context.scene
    scene.render.resolution_x = 512
    scene.render.resolution_y = 512
    scene.render.film_transparent = True
    scene.render.image_settings.file_format = 'PNG'
    scene.render.filepath = str(output_path)
    
    # Use EEVEE for speed
    scene.render.engine = 'BLENDER_EEVEE'
    
    bpy.ops.render.render(write_still=True)
    print(f"  Rendered: {output_path.name}")


def collect_mesh_stats():
    """Collect mesh statistics for the report."""
    total_verts = 0
    total_tris = 0
    mesh_names = []
    material_names = []
    has_uv = False
    has_vertex_colors = False
    has_armature = False
    
    for obj in bpy.data.objects:
        if obj.type == 'MESH':
            mesh = obj.data
            total_verts += len(mesh.vertices)
            total_tris += len(mesh.loop_triangles) if hasattr(mesh, 'loop_triangles') else len(mesh.polygons)
            mesh_names.append(obj.name)
            
            if mesh.uv_layers:
                has_uv = True
            if mesh.color_attributes:
                has_vertex_colors = True
            
            for mat in mesh.materials:
                if mat and mat.name not in material_names:
                    material_names.append(mat.name)
        
        elif obj.type == 'ARMATURE':
            has_armature = True
    
    # Recalculate triangles properly
    for obj in bpy.data.objects:
        if obj.type == 'MESH':
            mesh = obj.data
            mesh.calc_loop_triangles()
            total_tris = sum(len(obj.data.loop_triangles) for obj in bpy.data.objects if obj.type == 'MESH')
            break
    
    return {
        "total_vertices": total_verts,
        "total_triangles": total_tris,
        "mesh_count": len(mesh_names),
        "mesh_names": mesh_names[:20],  # cap for readability
        "material_count": len(material_names),
        "material_names": material_names[:20],
        "has_uv": has_uv,
        "has_vertex_colors": has_vertex_colors,
        "has_armature": has_armature,
    }


# ── Main ───────────────────────────────────────────────────────

def main():
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    
    all_stats = {}
    
    for candidate in CANDIDATES:
        cid = candidate["id"]
        label = candidate["label"]
        gltf_path = candidate["gltf"]
        
        print(f"\n{'='*60}")
        print(f"Processing: {label} ({cid})")
        print(f"  Source: {gltf_path}")
        print(f"{'='*60}")
        
        if not os.path.exists(gltf_path):
            print(f"  SKIPPED — file not found: {gltf_path}")
            all_stats[cid] = {"label": label, "error": "file not found"}
            continue
        
        # Clear and import
        clear_scene()
        try:
            import_gltf(gltf_path)
        except Exception as e:
            print(f"  IMPORT ERROR: {e}")
            all_stats[cid] = {"label": label, "error": str(e)}
            continue
        
        # Collect stats
        stats = collect_mesh_stats()
        stats["label"] = label
        stats["note"] = candidate["note"]
        all_stats[cid] = stats
        
        print(f"  Vertices: {stats['total_vertices']}")
        print(f"  Triangles: {stats['total_triangles']}")
        print(f"  Meshes: {stats['mesh_count']} — {stats['mesh_names']}")
        print(f"  Materials: {stats['material_count']} — {stats['material_names']}")
        print(f"  UV: {stats['has_uv']}")
        print(f"  Vertex Colors: {stats['has_vertex_colors']}")
        print(f"  Armature: {stats['has_armature']}")
        
        # Setup rendering
        cam_obj, center, size = setup_camera_and_light(label)
        
        # Render 4 views
        for view_name, rotation in VIEWS:
            out_path = PREVIEW_DIR / f"{label}_{view_name}.png"
            try:
                render_view(cam_obj, center, size, view_name, rotation, out_path)
            except Exception as e:
                print(f"  RENDER ERROR ({view_name}): {e}")
    
    # Write stats summary
    stats_path = PREVIEW_DIR / "candidate_stats.json"
    with open(str(stats_path), 'w') as f:
        json.dump(all_stats, f, indent=2, default=str)
    print(f"\nStats written to: {stats_path}")
    
    print("\n" + "="*60)
    print("ALL CANDIDATE PREVIEWS COMPLETE")
    print("="*60)


if __name__ == "__main__":
    main()
