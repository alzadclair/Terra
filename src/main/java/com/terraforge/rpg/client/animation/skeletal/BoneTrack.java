package com.terraforge.rpg.client.animation.skeletal;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Keyframe animation track for a specific bone with linear & spherical (SLERP) interpolation.
 */
public class BoneTrack {
    private final String boneName;
    private final List<Keyframe> keyframes = new ArrayList<>();

    public BoneTrack(String boneName) {
        this.boneName = boneName;
    }

    public String getBoneName() {
        return boneName;
    }

    public BoneTrack addKeyframe(Keyframe kf) {
        keyframes.add(kf);
        keyframes.sort(Comparator.comparingDouble(k -> k.time));
        return this;
    }

    public BoneTrack addKeyframe(float time, float tx, float ty, float tz, Quaternionf rot, float sx, float sy, float sz) {
        return addKeyframe(new Keyframe(time, tx, ty, tz, rot, sx, sy, sz));
    }

    public List<Keyframe> getKeyframes() {
        return keyframes;
    }

    public void sample(float time, BoneTransform out) {
        sample(time, out.translation, out.rotation, out.scale);
    }

    public void sample(float time, Vector3f outPos, Quaternionf outRot, Vector3f outScale) {
        if (keyframes.isEmpty()) {
            return;
        }

        if (keyframes.size() == 1 || time <= keyframes.get(0).time) {
            Keyframe first = keyframes.get(0);
            outPos.set(first.translation);
            outRot.set(first.rotation);
            outScale.set(first.scale);
            return;
        }

        Keyframe last = keyframes.get(keyframes.size() - 1);
        if (time >= last.time) {
            outPos.set(last.translation);
            outRot.set(last.rotation);
            outScale.set(last.scale);
            return;
        }

        // Find surrounding keyframes
        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe k0 = keyframes.get(i);
            Keyframe k1 = keyframes.get(i + 1);

            if (time >= k0.time && time <= k1.time) {
                float segmentDuration = k1.time - k0.time;
                float alpha = segmentDuration > 0.0001f ? (time - k0.time) / segmentDuration : 0.0f;

                // Position: linear interpolation (LERP)
                k0.translation.lerp(k1.translation, alpha, outPos);

                // Rotation: spherical linear interpolation (SLERP)
                k0.rotation.slerp(k1.rotation, alpha, outRot);

                // Scale: linear interpolation (LERP)
                k0.scale.lerp(k1.scale, alpha, outScale);
                return;
            }
        }
    }
}
