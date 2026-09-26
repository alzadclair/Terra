import bpy
import os

gltf = r"C:\model 3d\_processed\19e9e3d27fc841359b8ed0df5b42fd83\source\scene.gltf"
bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.gltf(filepath=gltf)

out_obj = r"c:\Users\Alza\Desktop\terraforge\art_work\meowmere_test.obj"
os.makedirs(os.path.dirname(out_obj), exist_ok=True)
bpy.ops.wm.obj_export(filepath=out_obj)
print("Exported OBJ to:", out_obj)
print("Files in art_work:", os.listdir(os.path.dirname(out_obj)))
