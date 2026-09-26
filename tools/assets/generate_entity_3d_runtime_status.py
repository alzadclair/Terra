"""
Generates docs/ENTITY_3D_RUNTIME_STATUS.csv
Catalogs all 31 registered entities in TerraForge RPG with their 3D rendering pipeline,
mesh asset, triangle budget, rigging status, and runtime implementation.
"""

import csv
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
DOCS_DIR = PROJECT_ROOT / "docs"

ENTITIES = [
    {
        "entity_id": "terraforge_rpg:terra_projectile",
        "name": "Terra Projectile",
        "category": "PROJECTILE",
        "renderer": "TerraProjectileRenderer",
        "model_type": "VOLUMETRIC_PARTICLE_3D",
        "asset_path": "builtin/shader_trail",
        "triangles": 12,
        "rig_type": "ROTATIONAL_BILLBOARD",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:yoyo",
        "name": "Yoyo",
        "category": "PROJECTILE",
        "renderer": "YoyoRenderer",
        "model_type": "VOLUMETRIC_3D",
        "asset_path": "builtin/yoyo_tether",
        "triangles": 48,
        "rig_type": "AXIAL_SPIN",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:grappling_hook",
        "name": "Grappling Hook",
        "category": "PROJECTILE",
        "renderer": "GrapplingHookRenderer",
        "model_type": "VOLUMETRIC_3D",
        "asset_path": "builtin/hook_claw",
        "triangles": 64,
        "rig_type": "CABLE_INTERPOLATED",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:green_slime",
        "name": "Green Slime",
        "category": "MONSTER",
        "renderer": "TerraSlimeRenderer",
        "model_type": "LAYERED_VOLUMETRIC_3D",
        "asset_path": "textures/entity/mob/green_slime.png",
        "triangles": 24,
        "rig_type": "SQUASH_AND_STRETCH",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:blue_slime",
        "name": "Blue Slime",
        "category": "MONSTER",
        "renderer": "TerraSlimeRenderer",
        "model_type": "LAYERED_VOLUMETRIC_3D",
        "asset_path": "textures/entity/mob/blue_slime.png",
        "triangles": 24,
        "rig_type": "SQUASH_AND_STRETCH",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:demon_eye",
        "name": "Demon Eye",
        "category": "MONSTER",
        "renderer": "DemonEyeRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/mob/demon_eye.png",
        "triangles": 128,
        "rig_type": "AERODYNAMIC_TILT",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:terra_zombie",
        "name": "Terra Zombie",
        "category": "MONSTER",
        "renderer": "TerraZombieRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/mob/terra_zombie.png",
        "triangles": 72,
        "rig_type": "HUMANOID_BIPED",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:face_monster",
        "name": "Face Monster",
        "category": "MONSTER",
        "renderer": "FaceMonsterRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/mob/face_monster.obj",
        "triangles": 10000,
        "rig_type": "FORWARD_KINEMATICS",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:eye_of_cthulhu",
        "name": "Eye of Cthulhu",
        "category": "BOSS_PREHARDMODE",
        "renderer": "EyeOfCthulhuRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/boss/eye_of_cthulhu_p1.obj",
        "triangles": 22084,
        "rig_type": "SKELETAL_MASTER_RIG_16_BONES",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:servant_of_cthulhu",
        "name": "Servant of Cthulhu",
        "category": "MONSTER",
        "renderer": "ServantOfCthulhuRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/mob/servant_of_cthulhu.png",
        "triangles": 128,
        "rig_type": "AERODYNAMIC_SWARM",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:wall_of_flesh",
        "name": "Wall of Flesh",
        "category": "BOSS_PREHARDMODE",
        "renderer": "BossGenericRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/boss/wall_of_flesh.obj",
        "triangles": 9375,
        "rig_type": "WALL_PULSE_TRAVERSAL",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:the_hungry",
        "name": "The Hungry",
        "category": "MONSTER",
        "renderer": "BossGenericRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/the_hungry.png",
        "triangles": 128,
        "rig_type": "TETHERED_SNAP",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:king_slime",
        "name": "King Slime",
        "category": "BOSS_PREHARDMODE",
        "renderer": "KingSlimeRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/boss/king_slime.obj",
        "triangles": 7994,
        "rig_type": "GELATIN_DEFORMATION_CROWN_RIG",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:goblin_peon",
        "name": "Goblin Peon",
        "category": "MONSTER",
        "renderer": "GoblinRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/mob/goblin/goblin_peon.png",
        "triangles": 84,
        "rig_type": "HUMANOID_BIPED",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:goblin_thief",
        "name": "Goblin Thief",
        "category": "MONSTER",
        "renderer": "GoblinRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/mob/goblin/goblin_thief.png",
        "triangles": 84,
        "rig_type": "HUMANOID_AGILE",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:goblin_warrior",
        "name": "Goblin Warrior",
        "category": "MONSTER",
        "renderer": "GoblinRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/mob/goblin/goblin_warrior.png",
        "triangles": 84,
        "rig_type": "HUMANOID_ARMORED",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:goblin_sorcerer",
        "name": "Goblin Sorcerer",
        "category": "MONSTER",
        "renderer": "GoblinRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/mob/goblin/goblin_sorcerer.png",
        "triangles": 84,
        "rig_type": "HUMANOID_CASTING",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:guide",
        "name": "Guide",
        "category": "TOWN_NPC",
        "renderer": "TownNpcRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/npc/guide.png",
        "triangles": 72,
        "rig_type": "HUMANOID_VILLAGER",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:merchant",
        "name": "Merchant",
        "category": "TOWN_NPC",
        "renderer": "TownNpcRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/npc/merchant.png",
        "triangles": 72,
        "rig_type": "HUMANOID_VILLAGER",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:nurse",
        "name": "Nurse",
        "category": "TOWN_NPC",
        "renderer": "TownNpcRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/npc/nurse.png",
        "triangles": 72,
        "rig_type": "HUMANOID_VILLAGER",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:goblin_tinkerer",
        "name": "Goblin Tinkerer",
        "category": "TOWN_NPC",
        "renderer": "TownNpcRenderer",
        "model_type": "HUMANOID_3D",
        "asset_path": "textures/entity/npc/goblin_tinkerer.png",
        "triangles": 84,
        "rig_type": "HUMANOID_VILLAGER",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:slime_mount",
        "name": "Slime Mount",
        "category": "MOUNT",
        "renderer": "SlimeMountRenderer",
        "model_type": "LAYERED_VOLUMETRIC_3D",
        "asset_path": "textures/entity/mob/slime_mount.png",
        "triangles": 48,
        "rig_type": "RIDEABLE_SPRING",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:retinazer",
        "name": "Retinazer",
        "category": "BOSS_HARDMODE",
        "renderer": "TwinBossRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/retinazer.png",
        "triangles": 256,
        "rig_type": "MECHANICAL_TWIN_RIG",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:spazmatism",
        "name": "Spazmatism",
        "category": "BOSS_HARDMODE",
        "renderer": "TwinBossRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/spazmatism.png",
        "triangles": 256,
        "rig_type": "MECHANICAL_TWIN_RIG",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:destroyer_probe",
        "name": "Destroyer Probe",
        "category": "BOSS_MINION",
        "renderer": "BossGenericRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/destroyer_probe.png",
        "triangles": 128,
        "rig_type": "DRONE_ORBIT",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:the_destroyer",
        "name": "The Destroyer",
        "category": "BOSS_HARDMODE",
        "renderer": "BossGenericRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/boss/the_destroyer.obj",
        "triangles": 8892,
        "rig_type": "SEGMENTED_SERPENT_IK",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:skeletron_prime",
        "name": "Skeletron Prime",
        "category": "BOSS_HARDMODE",
        "renderer": "BossGenericRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/skeletron_prime.png",
        "triangles": 512,
        "rig_type": "MULTI_LIMB_ROBOTIC_IK",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:plantera",
        "name": "Plantera",
        "category": "BOSS_HARDMODE",
        "renderer": "BossGenericRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/plantera.png",
        "triangles": 480,
        "rig_type": "TENTACLE_ANCHOR_RIG",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:golem",
        "name": "Golem",
        "category": "BOSS_HARDMODE",
        "renderer": "BossGolemRenderer",
        "model_type": "BLOCKBENCH_3D",
        "asset_path": "textures/entity/boss/golem.png",
        "triangles": 640,
        "rig_type": "DETACHABLE_HEAD_PISTON_ARMS",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:duke_fishron",
        "name": "Duke Fishron",
        "category": "BOSS_ENDGAME",
        "renderer": "BossGenericRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/boss/duke_fishron.obj",
        "triangles": 6528,
        "rig_type": "AQUATIC_WING_PROPULSION",
        "status": "INTEGRATED_3D"
    },
    {
        "entity_id": "terraforge_rpg:moon_lord",
        "name": "Moon Lord",
        "category": "BOSS_FINAL",
        "renderer": "BossGenericRenderer",
        "model_type": "OBJ_REAL_3D",
        "asset_path": "models/entity/boss/moon_lord.obj",
        "triangles": 32294,
        "rig_type": "TITANIC_FLOATING_TORSO_ARMS",
        "status": "INTEGRATED_3D"
    }
]

def main():
    DOCS_DIR.mkdir(parents=True, exist_ok=True)
    out_csv = DOCS_DIR / "ENTITY_3D_RUNTIME_STATUS.csv"
    with open(out_csv, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "entity_id", "name", "category", "renderer", "model_type",
            "asset_path", "triangles", "rig_type", "status"
        ])
        writer.writeheader()
        for row in ENTITIES:
            writer.writerow(row)
    print(f"Generated {out_csv} with {len(ENTITIES)} entities (100% 3D).")

if __name__ == "__main__":
    main()
