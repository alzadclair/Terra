package com.terraforge.rpg.client.animation.skeletal;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Keyframe representing translation, rotation, and scale at a specific timestamp.
 */
public class Keyframe {
    public final float time; // seconds
    public final Vector3f translation;
    public final Quaternionf rotation;
    public final Vector3f scale;

    public Keyframe(float time, Vector3f translation, Quaternionf rotation, Vector3f scale) {
        this.time = time;
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Keyframe(float time, float tx, float ty, float tz, Quaternionf rot, float sx, float sy, float sz) {
        this.time = time;
        this.translation = new Vector3f(tx, ty, tz);
        this.rotation = new Quaternionf(rot);
        this.scale = new Vector3f(sx, sy, sz);
    }
}
