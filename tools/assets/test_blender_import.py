import bpy
import os

gltf = r"C:\model 3d\_processed\19e9e3d27fc841359b8ed0df5b42fd83\source\scene.gltf"
bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.gltf(filepath=gltf)
print("Imported objects:", [o.name for o in bpy.data.objects])
for o in bpy.data.objects:
    if o.type == 'MESH':
        print(f"Mesh: {o.name}, verts: {len(o.data.vertices)}, polygons: {len(o.data.polygons)}")
