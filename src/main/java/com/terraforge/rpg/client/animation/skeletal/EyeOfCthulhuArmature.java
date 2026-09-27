package com.terraforge.rpg.client.animation.skeletal;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Master Rig armature and animation definitions for Eye of Cthulhu.
 * Canonical Master Rig:
 * root
 * ├── body
 * │   ├── iris
 * │   │   └── pupil
 * │   ├── optic_back
 * │   │   ├── tendril_01
 * │   │   ├── tendril_02
 * │   │   ├── tendril_03
 * │   │   ├── tendril_04
 * │   │   ├── tendril_05
 * │   │   └── tendril_06
 * │   └── jaw_root (Phase 2)
 * │       ├── upper_jaw
 * │       │   └── teeth_upper
 * │       └── lower_jaw
 * │           └── teeth_lower
 */
public final class EyeOfCthulhuArmature {

    private EyeOfCthulhuArmature() {}

    private static Quaternionf rot(float xDeg, float yDeg, float zDeg) {
        return new Quaternionf().rotationXYZ(
                (float) Math.toRadians(xDeg),
                (float) Math.toRadians(yDeg),
                (float) Math.toRadians(zDeg)
        );
    }

    public static Skeleton createArmature() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.getResourceManager() != null) {
                com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData data =
                        com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader.getOrLoad(
                                com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader.SKIN_EYE_P2
                        );
                if (data != null && !data.getBones().isEmpty()) {
                    return EyeSkeletonFactory.create(data);
                }
            }
        } catch (Throwable ignored) {
        }

        Bone root = new Bone("root", null);
        root.setBindPose(0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone body = new Bone("body", root);
        body.setBindPose(0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone iris = new Bone("iris", body);
        iris.setBindPose(0.0f, 0.0f, 0.4f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone pupil = new Bone("pupil", iris);
        pupil.setBindPose(0.0f, 0.0f, 0.05f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone opticBack = new Bone("optic_back", body);
        opticBack.setBindPose(0.0f, -222.278f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone jawRoot = new Bone("jaw_root", body);
        jawRoot.setBindPose(0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        // Authentic upper jaw hinge pivot from GLTF: [0.018, 12.302, 0.144]
        Bone upperJaw = new Bone("upper_jaw", jawRoot);
        upperJaw.setBindPose(0.018f, 12.302f, 0.144f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone teethUpper = new Bone("teeth_upper", upperJaw);
        teethUpper.setBindPose(0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        // Authentic lower jaw hinge pivot from GLTF: [0.018, 10.149, -4.324]
        Bone lowerJaw = new Bone("lower_jaw", jawRoot);
        lowerJaw.setBindPose(0.018f, 10.149f, -4.324f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone teethLower = new Bone("teeth_lower", lowerJaw);
        teethLower.setBindPose(0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        // Authentic tendril base pivots from GLTF (all trailing bases at Y = 247.499)
        Bone tendril1 = new Bone("tendril_01", body);
        tendril1.setBindPose(1.544f, 247.499f, 87.141f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone tendril2 = new Bone("tendril_02", body);
        tendril2.setBindPose(-83.905f, 247.499f, -0.993f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone tendril3 = new Bone("tendril_03", body);
        tendril3.setBindPose(-25.397f, 247.499f, -79.170f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone tendril4 = new Bone("tendril_04", body);
        tendril4.setBindPose(68.315f, 247.499f, -76.759f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone tendril5 = new Bone("tendril_05", body);
        tendril5.setBindPose(78.023f, 247.499f, 51.949f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        Bone tendril6 = new Bone("tendril_06", body);
        tendril6.setBindPose(74.930f, 438.405f, 50.909f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);

        return new Skeleton(root);
    }

    public static Skeleton createArmature(com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData meshData) {
        return EyeSkeletonFactory.create(meshData);
    }

    public static AnimationController createController() {
        AnimationController controller = new AnimationController();

        // 1. SPAWN (3.0s, non-looping)
        AnimationClip spawn = new AnimationClip("spawn", 3.0f, false);
        BoneTrack spBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, -2.5f, 0.0f, rot(0, 0, 0), 0.3f, 0.3f, 0.3f)
                .addKeyframe(1.5f, 0.0f, -0.8f, 0.0f, rot(5, 0, 0), 0.7f, 0.7f, 0.7f)
                .addKeyframe(3.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack spPupil = new BoneTrack("pupil")
                .addKeyframe(0.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 0.1f, 0.1f, 0.1f)
                .addKeyframe(2.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 0.4f, 0.4f, 0.4f)
                .addKeyframe(3.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        spawn.addTrack(spBody).addTrack(spPupil);
        addTendrilWave(spawn, 3.0f, 25.0f, false);
        controller.registerClip(spawn);

        // 2. IDLE (2.0s, looping)
        AnimationClip idle = new AnimationClip("idle", 2.0f, true);
        BoneTrack idBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.0f, 0.0f, 0.15f, 0.0f, rot(0, 0, 0), 1.02f, 0.98f, 1.02f)
                .addKeyframe(2.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack idPupil = new BoneTrack("pupil")
                .addKeyframe(0.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.03f, 0.05f, 0.0f, rot(0, 5, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.5f, -0.03f, 0.05f, 0.0f, rot(0, -5, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(2.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        idle.addTrack(idBody).addTrack(idPupil);
        addTendrilWave(idle, 2.0f, 12.0f, true);
        controller.registerClip(idle);

        // 3. HOVER (1.5s, looping)
        AnimationClip hover = new AnimationClip("hover", 1.5f, true);
        BoneTrack hvBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.75f, 0.0f, 0.08f, 0.0f, rot(2, 0, 0), 1.01f, 1.0f, 1.01f)
                .addKeyframe(1.5f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        hover.addTrack(hvBody);
        addTendrilWave(hover, 1.5f, 8.0f, true);
        controller.registerClip(hover);

        // 4. LOOK (1.0s, non-looping)
        AnimationClip look = new AnimationClip("look", 1.0f, false);
        BoneTrack lkBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.3f, 0.0f, 0.0f, 0.0f, rot(8, 22, -4), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.7f, 0.0f, 0.0f, 0.0f, rot(8, 22, -4), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack lkPupil = new BoneTrack("pupil")
                .addKeyframe(0.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.3f, 0.05f, 0.07f, 0.0f, rot(0, 15, 0), 1.1f, 1.1f, 1.0f)
                .addKeyframe(0.7f, 0.05f, 0.07f, 0.0f, rot(0, 15, 0), 1.1f, 1.1f, 1.0f)
                .addKeyframe(1.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        look.addTrack(lkBody).addTrack(lkPupil);
        controller.registerClip(look);

        // 5. SUMMON_SERVANT (1.2s, non-looping)
        AnimationClip summon = new AnimationClip("summon_servant", 1.2f, false);
        BoneTrack smBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.4f, 0.0f, 0.0f, -0.1f, rot(-5, 0, 0), 1.12f, 1.12f, 0.85f)
                .addKeyframe(0.6f, 0.0f, 0.0f, 0.25f, rot(5, 0, 0), 0.9f, 0.9f, 1.25f)
                .addKeyframe(1.2f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack smPupil = new BoneTrack("pupil")
                .addKeyframe(0.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.4f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 0.5f, 0.5f, 1.0f)
                .addKeyframe(0.6f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.4f, 1.4f, 1.0f)
                .addKeyframe(1.2f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        summon.addTrack(smBody).addTrack(smPupil);
        addTendrilWave(summon, 1.2f, 30.0f, false);
        controller.registerClip(summon);

        // 6. CHARGE_PREPARE (0.8s, non-looping)
        AnimationClip chgPrep = new AnimationClip("charge_prepare", 0.8f, false);
        BoneTrack cpBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.0f, 0.1f, -0.4f, rot(-12, 0, 0), 0.95f, 0.95f, 1.15f)
                .addKeyframe(0.8f, 0.0f, 0.15f, -0.7f, rot(-18, 0, 0), 0.9f, 0.9f, 1.2f);
        BoneTrack cpPupil = new BoneTrack("pupil")
                .addKeyframe(0.0f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.8f, 0.0f, 0.05f, 0.0f, rot(0, 0, 0), 1.3f, 1.3f, 1.0f);
        chgPrep.addTrack(cpBody).addTrack(cpPupil);
        addTendrilBack(chgPrep, 0.8f, -25.0f);
        controller.registerClip(chgPrep);

        // 7. CHARGE (1.0s, looping)
        AnimationClip charge = new AnimationClip("charge", 1.0f, true);
        BoneTrack cgBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.3f, rot(0, 0, 0), 0.88f, 0.88f, 1.35f)
                .addKeyframe(0.25f, 0.0f, 0.0f, 0.35f, rot(0, 0, 8), 0.88f, 0.88f, 1.35f)
                .addKeyframe(0.5f, 0.0f, 0.0f, 0.3f, rot(0, 0, 0), 0.88f, 0.88f, 1.35f)
                .addKeyframe(0.75f, 0.0f, 0.0f, 0.35f, rot(0, 0, -8), 0.88f, 0.88f, 1.35f)
                .addKeyframe(1.0f, 0.0f, 0.0f, 0.3f, rot(0, 0, 0), 0.88f, 0.88f, 1.35f);
        charge.addTrack(cgBody);
        addTendrilBack(charge, 1.0f, -40.0f);
        controller.registerClip(charge);

        // 8. CHARGE_RECOVER (0.6s, non-looping)
        AnimationClip chgRec = new AnimationClip("charge_recover", 0.6f, false);
        BoneTrack crBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.3f, rot(0, 0, 0), 0.88f, 0.88f, 1.35f)
                .addKeyframe(0.25f, 0.0f, -0.05f, 0.0f, rot(10, 0, 0), 1.2f, 1.2f, 0.8f)
                .addKeyframe(0.6f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        chgRec.addTrack(crBody);
        addTendrilForwardOvershoot(chgRec, 0.6f);
        controller.registerClip(chgRec);

        // 9. HURT (0.4s, non-looping)
        AnimationClip hurt = new AnimationClip("hurt", 0.4f, false);
        BoneTrack htBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.1f, 0.0f, 0.15f, -0.35f, rot(-15, 8, -6), 0.95f, 1.05f, 0.95f)
                .addKeyframe(0.25f, 0.0f, 0.08f, -0.15f, rot(-5, -4, 3), 1.02f, 0.98f, 1.02f)
                .addKeyframe(0.4f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        hurt.addTrack(htBody);
        controller.registerClip(hurt);

        // 10. PHASE_TRANSITION (3.0s, non-looping)
        AnimationClip pTrans = new AnimationClip("phase_transition", 3.0f, false);
        BoneTrack ptBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.05f, -0.05f, 0.0f, rot(10, 25, 45), 1.1f, 1.1f, 1.1f)
                .addKeyframe(1.0f, -0.05f, 0.05f, 0.0f, rot(-15, -40, 120), 0.9f, 0.9f, 0.9f)
                .addKeyframe(1.5f, 0.08f, 0.0f, 0.0f, rot(20, 90, 240), 1.15f, 1.15f, 1.15f)
                .addKeyframe(2.0f, 0.0f, 0.0f, 0.1f, rot(0, 180, 360), 1.25f, 1.25f, 1.25f)
                .addKeyframe(3.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack ptUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.2f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(2.0f, 0.0f, 0.0f, 0.0f, rot(45, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(3.0f, 0.0f, 0.0f, 0.0f, rot(25, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack ptLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.2f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(2.0f, 0.0f, 0.0f, 0.0f, rot(-45, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(3.0f, 0.0f, 0.0f, 0.0f, rot(-25, 0, 0), 1.0f, 1.0f, 1.0f);
        pTrans.addTrack(ptBody).addTrack(ptUpJaw).addTrack(ptLoJaw);
        addTendrilWave(pTrans, 3.0f, 40.0f, false);
        controller.registerClip(pTrans);

        // 11. PHASE2_IDLE (1.5s, looping)
        AnimationClip p2Idle = new AnimationClip("phase2_idle", 1.5f, true);
        BoneTrack p2iBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.75f, 0.0f, 0.18f, 0.0f, rot(3, 0, 0), 1.03f, 0.97f, 1.03f)
                .addKeyframe(1.5f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack p2iUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(20, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.35f, 0.0f, 0.0f, 0.0f, rot(32, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.75f, 0.0f, 0.0f, 0.0f, rot(18, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.15f, 0.0f, 0.0f, 0.0f, rot(28, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.5f, 0.0f, 0.0f, 0.0f, rot(20, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack p2iLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-20, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.35f, 0.0f, 0.0f, 0.0f, rot(-32, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.75f, 0.0f, 0.0f, 0.0f, rot(-18, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.15f, 0.0f, 0.0f, 0.0f, rot(-28, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.5f, 0.0f, 0.0f, 0.0f, rot(-20, 0, 0), 1.0f, 1.0f, 1.0f);
        p2Idle.addTrack(p2iBody).addTrack(p2iUpJaw).addTrack(p2iLoJaw);
        addTendrilWave(p2Idle, 1.5f, 18.0f, true);
        controller.registerClip(p2Idle);

        // 12. PHASE2_CHARGE_PREPARE (0.6s, non-looping)
        AnimationClip p2Cp = new AnimationClip("phase2_charge_prepare", 0.6f, false);
        BoneTrack p2cpBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.6f, 0.0f, 0.2f, -0.8f, rot(-18, 0, 0), 0.9f, 0.9f, 1.25f);
        BoneTrack p2cpUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(20, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.6f, 0.0f, 0.0f, 0.0f, rot(55, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack p2cpLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-20, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.6f, 0.0f, 0.0f, 0.0f, rot(-55, 0, 0), 1.0f, 1.0f, 1.0f);
        p2Cp.addTrack(p2cpBody).addTrack(p2cpUpJaw).addTrack(p2cpLoJaw);
        addTendrilBack(p2Cp, 0.6f, -30.0f);
        controller.registerClip(p2Cp);

        // 13. PHASE2_CHARGE (0.8s, looping)
        AnimationClip p2Cg = new AnimationClip("phase2_charge", 0.8f, true);
        BoneTrack p2cgBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.4f, rot(0, 0, 0), 0.85f, 0.85f, 1.4f)
                .addKeyframe(0.2f, 0.0f, 0.0f, 0.45f, rot(0, 0, 10), 0.85f, 0.85f, 1.4f)
                .addKeyframe(0.4f, 0.0f, 0.0f, 0.4f, rot(0, 0, 0), 0.85f, 0.85f, 1.4f)
                .addKeyframe(0.6f, 0.0f, 0.0f, 0.45f, rot(0, 0, -10), 0.85f, 0.85f, 1.4f)
                .addKeyframe(0.8f, 0.0f, 0.0f, 0.4f, rot(0, 0, 0), 0.85f, 0.85f, 1.4f);
        BoneTrack p2cgUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(40, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.4f, 0.0f, 0.0f, 0.0f, rot(52, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.8f, 0.0f, 0.0f, 0.0f, rot(40, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack p2cgLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-40, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.4f, 0.0f, 0.0f, 0.0f, rot(-52, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.8f, 0.0f, 0.0f, 0.0f, rot(-40, 0, 0), 1.0f, 1.0f, 1.0f);
        p2Cg.addTrack(p2cgBody).addTrack(p2cgUpJaw).addTrack(p2cgLoJaw);
        addTendrilBack(p2Cg, 0.8f, -45.0f);
        controller.registerClip(p2Cg);

        // 14. PHASE2_BITE (0.5s, non-looping)
        AnimationClip bite = new AnimationClip("phase2_bite", 0.5f, false);
        BoneTrack btBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.2f, 0.0f, 0.0f, 0.3f, rot(8, 0, 0), 1.05f, 0.95f, 1.15f)
                .addKeyframe(0.5f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack btUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(55, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.2f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)   // SNAP SHUT!
                .addKeyframe(0.35f, 0.0f, 0.0f, 0.0f, rot(15, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.0f, 0.0f, 0.0f, rot(20, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack btLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-55, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.2f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f) // SNAP SHUT!
                .addKeyframe(0.35f, 0.0f, 0.0f, 0.0f, rot(-15, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.0f, 0.0f, 0.0f, rot(-20, 0, 0), 1.0f, 1.0f, 1.0f);
        bite.addTrack(btBody).addTrack(btUpJaw).addTrack(btLoJaw);
        controller.registerClip(bite);

        // 15. ENRAGE (1.0s, looping)
        AnimationClip enrage = new AnimationClip("enrage", 1.0f, true);
        BoneTrack erBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.02f, -0.02f, 0.0f, rot(5, -6, 4), 1.05f, 1.05f, 1.05f)
                .addKeyframe(0.25f, -0.03f, 0.03f, 0.02f, rot(-6, 7, -5), 0.96f, 0.96f, 0.96f)
                .addKeyframe(0.5f, 0.04f, -0.01f, -0.02f, rot(7, -5, 6), 1.06f, 1.06f, 1.06f)
                .addKeyframe(0.75f, -0.02f, 0.04f, 0.01f, rot(-5, 6, -4), 0.97f, 0.97f, 0.97f)
                .addKeyframe(1.0f, 0.02f, -0.02f, 0.0f, rot(5, -6, 4), 1.05f, 1.05f, 1.05f);
        BoneTrack erUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(15, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.12f, 0.0f, 0.0f, 0.0f, rot(48, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.25f, 0.0f, 0.0f, 0.0f, rot(8, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.37f, 0.0f, 0.0f, 0.0f, rot(50, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.0f, 0.0f, 0.0f, rot(15, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.62f, 0.0f, 0.0f, 0.0f, rot(48, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.75f, 0.0f, 0.0f, 0.0f, rot(8, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.87f, 0.0f, 0.0f, 0.0f, rot(50, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.0f, 0.0f, 0.0f, 0.0f, rot(15, 0, 0), 1.0f, 1.0f, 1.0f);
        BoneTrack erLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-15, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.12f, 0.0f, 0.0f, 0.0f, rot(-48, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.25f, 0.0f, 0.0f, 0.0f, rot(-8, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.37f, 0.0f, 0.0f, 0.0f, rot(-50, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.5f, 0.0f, 0.0f, 0.0f, rot(-15, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.62f, 0.0f, 0.0f, 0.0f, rot(-48, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.75f, 0.0f, 0.0f, 0.0f, rot(-8, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.87f, 0.0f, 0.0f, 0.0f, rot(-50, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.0f, 0.0f, 0.0f, 0.0f, rot(-15, 0, 0), 1.0f, 1.0f, 1.0f);
        enrage.addTrack(erBody).addTrack(erUpJaw).addTrack(erLoJaw);
        addTendrilWave(enrage, 1.0f, 50.0f, true);
        controller.registerClip(enrage);

        // 16. DEATH (2.5s, non-looping)
        AnimationClip death = new AnimationClip("death", 2.5f, false);
        BoneTrack dtBody = new BoneTrack("body")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(0.8f, 0.0f, -0.4f, 0.0f, rot(45, 120, 90), 0.95f, 0.95f, 0.95f)
                .addKeyframe(1.6f, 0.0f, -1.5f, 0.0f, rot(110, 260, 200), 0.75f, 0.75f, 0.75f)
                .addKeyframe(2.5f, 0.0f, -3.0f, 0.0f, rot(160, 360, 310), 0.4f, 0.4f, 0.4f);
        BoneTrack dtUpJaw = new BoneTrack("upper_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(20, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.2f, 0.0f, 0.0f, 0.0f, rot(5, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(2.5f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 0.6f, 0.6f, 0.6f);
        BoneTrack dtLoJaw = new BoneTrack("lower_jaw")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-20, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.2f, 0.0f, 0.0f, 0.0f, rot(-40, 0, 0), 1.0f, 1.0f, 1.0f)
                .addKeyframe(2.5f, 0.0f, 0.0f, 0.0f, rot(-50, 0, 0), 0.6f, 0.6f, 0.6f);
        death.addTrack(dtBody).addTrack(dtUpJaw).addTrack(dtLoJaw);
        addTendrilCurl(death, 2.5f);
        controller.registerClip(death);

        return controller;
    }

    private static void addTendrilWave(AnimationClip clip, float duration, float maxAngle, boolean loop) {
        String[] tendrils = {"tendril_01", "tendril_02", "tendril_03", "tendril_04", "tendril_05", "tendril_06"};
        float[] baseOffsets = {0.0f, 0.3f, 0.6f, 0.9f, 1.2f, 1.5f};

        for (int i = 0; i < tendrils.length; i++) {
            BoneTrack track = new BoneTrack(tendrils[i]);
            float offset = baseOffsets[i % baseOffsets.length];
            int steps = 5;
            for (int s = 0; s <= steps; s++) {
                float t = (float) s / steps * duration;
                float phase = (t / duration * (float) Math.PI * 2.0f) + offset;
                float pitch = (float) Math.sin(phase) * maxAngle;
                float yaw = (float) Math.cos(phase) * (maxAngle * 0.4f);
                track.addKeyframe(t, 0.0f, 0.0f, 0.0f, rot(pitch, yaw, 0), 1.0f, 1.0f, 1.0f);
            }
            clip.addTrack(track);
        }
    }

    private static void addTendrilBack(AnimationClip clip, float duration, float angle) {
        String[] tendrils = {"tendril_01", "tendril_02", "tendril_03", "tendril_04", "tendril_05", "tendril_06"};
        for (String tendril : tendrils) {
            BoneTrack track = new BoneTrack(tendril)
                    .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                    .addKeyframe(duration * 0.5f, 0.0f, 0.0f, -0.1f, rot(angle, 0, 0), 0.9f, 0.9f, 1.1f)
                    .addKeyframe(duration, 0.0f, 0.0f, -0.1f, rot(angle, 0, 0), 0.9f, 0.9f, 1.1f);
            clip.addTrack(track);
        }
    }

    private static void addTendrilForwardOvershoot(AnimationClip clip, float duration) {
        String[] tendrils = {"tendril_01", "tendril_02", "tendril_03", "tendril_04", "tendril_05", "tendril_06"};
        for (String tendril : tendrils) {
            BoneTrack track = new BoneTrack(tendril)
                    .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(-35, 0, 0), 1.0f, 1.0f, 1.0f)
                    .addKeyframe(duration * 0.4f, 0.0f, 0.0f, 0.1f, rot(25, 0, 0), 1.0f, 1.0f, 1.0f)
                    .addKeyframe(duration, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f);
            clip.addTrack(track);
        }
    }

    private static void addTendrilCurl(AnimationClip clip, float duration) {
        String[] tendrils = {"tendril_01", "tendril_02", "tendril_03", "tendril_04", "tendril_05", "tendril_06"};
        for (String tendril : tendrils) {
            BoneTrack track = new BoneTrack(tendril)
                    .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, rot(0, 0, 0), 1.0f, 1.0f, 1.0f)
                    .addKeyframe(duration * 0.5f, 0.0f, -0.1f, 0.0f, rot(45, 15, 0), 0.9f, 0.9f, 0.9f)
                    .addKeyframe(duration, 0.0f, -0.2f, 0.0f, rot(70, 30, 0), 0.6f, 0.6f, 0.6f);
            clip.addTrack(track);
        }
    }
}
