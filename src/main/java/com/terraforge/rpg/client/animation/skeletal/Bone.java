package com.terraforge.rpg.client.animation.skeletal;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a bone in a skeletal hierarchy.
 * Tracks bind pose matrices and animated delta local transforms.
 * Computes:
 *   localMatrix = bindLocalMatrix * animLocalMatrix
 *   worldMatrix = parentWorld * localMatrix
 *   skinMatrix  = worldMatrix * invBindWorldMatrix
 * Under rest pose (anim delta = identity), skinMatrix is exactly the identity matrix.
 */
public class Bone {
    private final String name;
    private final Bone parent;
    private final List<Bone> children = new ArrayList<>();

    // Bind pose local transform (for legacy or procedural rigs)
    private final Vector3f bindPos = new Vector3f();
    private final Quaternionf bindRot = new Quaternionf();
    private final Vector3f bindScale = new Vector3f(1.0f, 1.0f, 1.0f);
    private boolean bindMatricesExplicit = false;

    // Bind pose matrices (source of truth from GLTF skin data)
    public final Matrix4f bindLocalMatrix = new Matrix4f();
    public final Matrix4f bindWorldMatrix = new Matrix4f();
    public final Matrix4f invBindWorldMatrix = new Matrix4f();

    // Animated delta transform (defaults: pos=(0,0,0), rot=identity, scale=(1,1,1))
    public final Vector3f animPos = new Vector3f();
    public final Quaternionf animRot = new Quaternionf();
    public final Vector3f animScale = new Vector3f(1.0f, 1.0f, 1.0f);

    // Aliases for backwards compatibility with existing animation tracks & tests
    public final Vector3f localPos = animPos;
    public final Quaternionf localRot = animRot;
    public final Vector3f localScale = animScale;

    // Computed runtime matrices
    public final Matrix4f animLocalMatrix = new Matrix4f();
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

    public boolean hasExplicitBindMatrices() {
        return bindMatricesExplicit;
    }

    public void setExplicitBindMatrices(Matrix4f local, Matrix4f world, Matrix4f invWorld) {
        this.bindLocalMatrix.set(local);
        this.bindWorldMatrix.set(world);
        this.invBindWorldMatrix.set(invWorld);
        this.bindMatricesExplicit = true;
        resetToBindPose();
    }

    public void setBindPose(float x, float y, float z, Quaternionf rot, float sx, float sy, float sz) {
        this.bindPos.set(x, y, z);
        this.bindRot.set(rot);
        this.bindScale.set(sx, sy, sz);
        this.bindLocalMatrix.identity()
                .translate(bindPos)
                .rotate(bindRot)
                .scale(bindScale);
        this.bindMatricesExplicit = false;
        resetToBindPose();
    }

    public void initBindPose() {
        if (!bindMatricesExplicit) {
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
        }

        for (Bone child : children) {
            child.initBindPose();
        }
    }

    public void resetToBindPose() {
        this.animPos.set(0.0f, 0.0f, 0.0f);
        this.animRot.identity();
        this.animScale.set(1.0f, 1.0f, 1.0f);
        this.animLocalMatrix.identity();

        this.localMatrix.set(bindLocalMatrix);

        if (parent != null) {
            this.worldMatrix.set(parent.worldMatrix).mul(localMatrix);
        } else {
            this.worldMatrix.set(localMatrix);
        }

        this.skinMatrix.set(worldMatrix).mul(invBindWorldMatrix);
    }

    public void updateMatrices() {
        animLocalMatrix.identity()
                .translate(animPos)
                .rotate(animRot)
                .scale(animScale);

        localMatrix.set(bindLocalMatrix).mul(animLocalMatrix);

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
