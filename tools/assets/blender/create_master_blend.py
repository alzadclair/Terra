"""
Blender script to create the master .blend file:
tools/assets/blender/eye_cthulhu_master.blend

Contains:
- Collection "Eye_Phase_1": All P1 non-LOD meshes (body, iris, pupil, stalk, glass) with original armature
- Collection "Eye_Phase_2": All P2 non-LOD meshes (maw body, teeth, inner teeth, stalk) with original armature
- Preserves all original materials, UVs, and vertex groups.
"""

import bpy
import os
from pathlib import Path

PROJECT_ROOT = Path(r"C:\Users\Alza\Desktop\terraforge")
RAW_DIR = PROJECT_ROOT / "build" / "eye_pipeline" / "raw"
OUTPUT_BLEND = PROJECT_ROOT / "tools" / "assets" / "blender" / "eye_cthulhu_master.blend"

def clear_all():
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.delete(use_global=False)
    for c in list(bpy.data.collections):
        if c.name != "Scene Collection":
            bpy.data.collections.remove(c)

def setup_master():
    clear_all()
    OUTPUT_BLEND.parent.mkdir(parents=True, exist_ok=True)
    
    # 1. Import P1
    p1_gltf = RAW_DIR / "p1_caseoh" / "scene.gltf"
    if p1_gltf.exists():
        p1_col = bpy.data.collections.new("Eye_Phase_1")
        bpy.context.scene.collection.children.link(p1_col)
        
        bpy.ops.import_scene.gltf(filepath=str(p1_gltf))
        
        # Move imported objects to P1 collection and remove LODs
        to_remove = []
        for obj in list(bpy.context.scene.collection.objects):
            if "lod1" in obj.name.lower():
                to_remove.append(obj)
            else:
                p1_col.objects.link(obj)
                bpy.context.scene.collection.objects.unlink(obj)
                
        for obj in to_remove:
            bpy.data.objects.remove(obj, do_unlink=True)
        print("P1 collection populated and LODs removed.")
    
    # 2. Import P2
    p2_gltf = RAW_DIR / "p2_caseoh" / "scene.gltf"
    if p2_gltf.exists():
        p2_col = bpy.data.collections.new("Eye_Phase_2")
        bpy.context.scene.collection.children.link(p2_col)
        
        bpy.ops.import_scene.gltf(filepath=str(p2_gltf))
        
        to_remove = []
        for obj in list(bpy.context.scene.collection.objects):
            if "lod1" in obj.name.lower():
                to_remove.append(obj)
            else:
                p2_col.objects.link(obj)
                bpy.context.scene.collection.objects.unlink(obj)
                
        for obj in to_remove:
            bpy.data.objects.remove(obj, do_unlink=True)
        print("P2 collection populated and LODs removed.")
    
    # Save master blend file
    bpy.ops.wm.save_as_mainfile(filepath=str(OUTPUT_BLEND))
    print(f"Master file saved: {OUTPUT_BLEND} ({OUTPUT_BLEND.stat().st_size:,} bytes)")

if __name__ == "__main__":
    setup_master()
