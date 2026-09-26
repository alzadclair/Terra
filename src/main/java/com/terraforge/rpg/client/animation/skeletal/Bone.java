package com.terraforge.rpg.client.animation.skeletal;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a bone in a skeletal hierarchy.
 * Tracks bind pose, animated local transforms, and calculates the skinning matrix
 * (worldMatrix * invBindWorldMatrix) to apply skeletal transformations to mesh geometry.
 */
public class Bone {
    private final String name;
    private final Bone parent;
    private final List<Bone> children = new ArrayList<>();

    // Bind pose local transform
    private final Vector3f bindPos = new Vector3f();
    private final Quaternionf bindRot = new Quaternionf();
    private final Vector3f bindScale = new Vector3f(1.0f, 1.0f, 1.0f);

    // Bind pose matrices
    public final Matrix4f bindLocalMatrix = new Matrix4f();
    public final Matrix4f bindWorldMatrix = new Matrix4f();
    public final Matrix4f invBindWorldMatrix = new Matrix4f();

    // Current animated local transform
    public final Vector3f localPos = new Vector3f();
    public final Quaternionf localRot = new Quaternionf();
    public final Vector3f localScale = new Vector3f(1.0f, 1.0f, 1.0f);

    // Computed runtime matrices
    public final Matrix4f localMatrix = new Matrix4f();
    public final Matrix4f worldMatrix = new Matrix4f();
    public final Matrix4f skinMatrix = new Matrix4f();

    public Bone(String name, Bone parent) {
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public String getName() {
        return name;
    }

    public Bone getParent() {
        return parent;
    }

    public List<Bone> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Vector3f getBindPos() {
        return bindPos;
    }

    public Quaternionf getBindRot() {
        return bindRot;
    }

    public Vector3f getBindScale() {
        return bindScale;
    }

    public void setBindPose(float x, float y, float z, Quaternionf rot, float sx, float sy, float sz) {
        this.bindPos.set(x, y, z);
        this.bindRot.set(rot);
        this.bindScale.set(sx, sy, sz);
        resetToBindPose();
    }

    public void initBindPose() {
        bindLocalMatrix.identity()
                .translate(bindPos)
                .rotate(bindRot)
                .scale(bindScale);

        if (parent != null) {
            bindWorldMatrix.set(parent.bindWorldMatrix).mul(bindLocalMatrix);
        } else {
            bindWorldMatrix.set(bindLocalMatrix);
        }

        invBindWorldMatrix.set(bindWorldMatrix).invert();

        for (Bone child : children) {
            child.initBindPose();
        }
    }

    public void resetToBindPose() {
        this.localPos.set(bindPos);
        this.localRot.set(bindRot);
        this.localScale.set(bindScale);
    }

    public void updateMatrices() {
        localMatrix.identity()
                .translate(localPos)
                .rotate(localRot)
                .scale(localScale);

        if (parent != null) {
            worldMatrix.set(parent.worldMatrix).mul(localMatrix);
        } else {
            worldMatrix.set(localMatrix);
        }

        skinMatrix.set(worldMatrix).mul(invBindWorldMatrix);

        for (Bone child : children) {
            child.updateMatrices();
        }
    }
}
