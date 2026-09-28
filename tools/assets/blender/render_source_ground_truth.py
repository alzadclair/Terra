import bpy
import os
import sys
import math
from mathutils import Vector, Euler

BLENDER_EXE = r"C:\Program Files\Blender Foundation\Blender 5.2\blender.exe"
P1_GLTF = r"C:\model 3d\_processed\06664c90cf3e4d24a74a43dc771ae74f\source\scene.gltf"
P2_GLTF = r"C:\model 3d\_processed\a53f70fa34284699a57af3d161182611\source\scene.gltf"
OUT_DIR = r"C:\Users\Alza\Desktop\terraforge\build\eye_pipeline\source_blender"

os.makedirs(OUT_DIR, exist_ok=True)

def setup_camera_and_lights():
    # Clear default lights and camera
    bpy.ops.object.select_all(action='DESELECT')
    for obj in list(bpy.data.objects):
        if obj.type in ('CAMERA', 'LIGHT'):
            bpy.data.objects.remove(obj, do_unlink=True)

    # Add sun light
    light_data = bpy.data.lights.new(name="Sun", type='SUN')
    light_data.energy = 3.0
    light_obj = bpy.data.objects.new(name="Sun", object_data=light_data)
    bpy.context.collection.objects.link(light_obj)
    light_obj.rotation_euler = Euler((math.radians(45), math.radians(30), math.radians(45)), 'XYZ')

    # Add second fill light
    fill_data = bpy.data.lights.new(name="Fill", type='SUN')
    fill_data.energy = 1.5
    fill_obj = bpy.data.objects.new(name="Fill", object_data=fill_data)
    bpy.context.collection.objects.link(fill_obj)
    fill_obj.rotation_euler = Euler((math.radians(-30), math.radians(-60), math.radians(-120)), 'XYZ')

    # Add camera
    cam_data = bpy.data.cameras.new(name="RenderCam")
    cam_data.lens = 50
    cam_obj = bpy.data.objects.new(name="RenderCam", object_data=cam_data)
    bpy.context.collection.objects.link(cam_obj)
    bpy.context.scene.camera = cam_obj
    return cam_obj

def get_scene_bounds(mesh_objects):
    min_co = Vector((float('inf'), float('inf'), float('inf')))
    max_co = Vector((float('-inf'), float('-inf'), float('-inf')))
    for obj in mesh_objects:
        for corner in obj.bound_box:
            world_corner = obj.matrix_world @ Vector(corner)
            min_co.x = min(min_co.x, world_corner.x)
            min_co.y = min(min_co.y, world_corner.y)
            min_co.z = min(min_co.z, world_corner.z)
            max_co.x = max(max_co.x, world_corner.x)
            max_co.y = max(max_co.y, world_corner.y)
            max_co.z = max(max_co.z, world_corner.z)
    center = (min_co + max_co) / 2.0
    size = max_co - min_co
    radius = max(size.x, size.y, size.z) / 2.0
    return center, max(radius, 0.1)

def position_camera(cam, center, radius, view_name):
    dist = radius * 2.8
    if view_name == 'front':
        # In GLTF/Blender import: often -Y is forward or +Y is forward, +Z up
        # Let's position camera at front (-Y if facing -Y, or +Y if facing +Y)
        # We will check orientation
        cam.location = center + Vector((0, -dist, 0))
        cam.rotation_euler = Euler((math.radians(90), 0, 0), 'XYZ')
    elif view_name == 'side':
        cam.location = center + Vector((dist, 0, 0))
        cam.rotation_euler = Euler((math.radians(90), 0, math.radians(90)), 'XYZ')
    elif view_name == 'back':
        cam.location = center + Vector((0, dist, 0))
        cam.rotation_euler = Euler((math.radians(90), 0, math.radians(180)), 'XYZ')
    elif view_name == 'top':
        cam.location = center + Vector((0, 0, dist))
        cam.rotation_euler = Euler((0, 0, math.radians(-90)), 'XYZ')

def render_phase(gltf_path, prefix):
    # Clear existing scene
    bpy.ops.wm.read_factory_settings(use_empty=True)
    scene = bpy.context.scene
    scene.render.resolution_x = 1024
    scene.render.resolution_y = 1024
    scene.render.film_transparent = True
    scene.render.image_settings.file_format = 'PNG'

    # Try workbench or cycles/eevee
    try:
        scene.render.engine = 'BLENDER_EEVEE_NEXT'
    except Exception:
        scene.render.engine = 'CYCLES'
        scene.cycles.device = 'CPU'
        scene.cycles.samples = 32

    # Import GLTF
    print(f"Importing {gltf_path}...")
    bpy.ops.import_scene.gltf(filepath=gltf_path)

    # Inspect imported objects
    mesh_objs = [o for o in bpy.data.objects if o.type == 'MESH']
    print(f"Imported {len(mesh_objs)} mesh objects for {prefix}:")
    for o in mesh_objs:
        print(f"  Mesh obj: {o.name}, parent={o.parent.name if o.parent else None}, matrix_world pos={o.matrix_world.translation}, materials={[m.name if m else None for m in o.data.materials]}, verts={len(o.data.vertices)}, polys={len(o.data.polygons)}")

    center, radius = get_scene_bounds(mesh_objs)
    print(f"Scene bounds center={center}, radius={radius}")

    cam = setup_camera_and_lights()

    # Determine facing by looking at iris/pupil or body center vs stalk
    # If stalk is at +Y or -Y or +Z
    # Let's inspect where meshes are located
    views = ['front', 'side', 'back', 'top']
    for v in views:
        position_camera(cam, center, radius, v)
        out_path = os.path.join(OUT_DIR, f"{prefix}_{v}.png")
        scene.render.filepath = out_path
        bpy.ops.render.render(write_still=True)
        print(f"Saved {out_path}")

print("=== Rendering P1 Ground Truth ===")
render_phase(P1_GLTF, "p1")

print("=== Rendering P2 Ground Truth ===")
render_phase(P2_GLTF, "p2")

print("Done ground truth rendering.")
