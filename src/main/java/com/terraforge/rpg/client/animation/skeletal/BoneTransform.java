package com.terraforge.rpg.client.animation.skeletal;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Encapsulates position, rotation, and scale for a single bone at an instant in time.
 */
public class BoneTransform {
    public final Vector3f translation = new Vector3f();
    public final Quaternionf rotation = new Quaternionf();
    public final Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

    public BoneTransform() {}

    public BoneTransform(Vector3f translation, Quaternionf rotation, Vector3f scale) {
        this.translation.set(translation);
        this.rotation.set(rotation);
        this.scale.set(scale);
    }

    public BoneTransform(float tx, float ty, float tz, Quaternionf rot, float sx, float sy, float sz) {
        this.translation.set(tx, ty, tz);
        this.rotation.set(rot);
        this.scale.set(sx, sy, sz);
    }

    public void set(BoneTransform other) {
        this.translation.set(other.translation);
        this.rotation.set(other.rotation);
        this.scale.set(other.scale);
    }

    public void set(Vector3f pos, Quaternionf rot, Vector3f scl) {
        this.translation.set(pos);
        this.rotation.set(rot);
        this.scale.set(scl);
    }

    public void lerp(BoneTransform target, float alpha, BoneTransform out) {
        this.translation.lerp(target.translation, alpha, out.translation);
        this.rotation.slerp(target.rotation, alpha, out.rotation);
        this.scale.lerp(target.scale, alpha, out.scale);
    }
}
