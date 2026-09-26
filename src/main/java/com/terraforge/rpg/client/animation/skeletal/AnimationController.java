package com.terraforge.rpg.client.animation.skeletal;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller managing clip playback, timing, and cross-fade blending between skeletal animation clips.
 */
public class AnimationController {
    private final Map<String, AnimationClip> clips = new HashMap<>();

    private AnimationClip currentClip;
    private float currentTime = 0.0f;

    private AnimationClip previousClip;
    private float previousTime = 0.0f;

    private boolean isBlending = false;
    private float blendFactor = 1.0f; // 0.0 = full previous, 1.0 = full current
    private float blendDuration = 0.2f;

    // Temporary sample buffers to avoid allocations per frame
    private final Map<String, BoneTransform> currentSampleMap = new HashMap<>();
    private final Map<String, BoneTransform> prevSampleMap = new HashMap<>();
    private final BoneTransform tempTransform = new BoneTransform();
    private final BoneTransform defaultTransform = new BoneTransform();

    public AnimationController registerClip(AnimationClip clip) {
        clips.put(clip.getName(), clip);
        return this;
    }

    public AnimationClip getClip(String name) {
        return clips.get(name);
    }

    public boolean hasClip(String name) {
        return clips.containsKey(name);
    }

    public String getCurrentClipName() {
        return currentClip != null ? currentClip.getName() : "";
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public boolean isBlending() {
        return isBlending;
    }

    public void play(String clipName, boolean restartIfSame) {
        if (!restartIfSame && currentClip != null && currentClip.getName().equals(clipName)) {
            return;
        }

        AnimationClip target = clips.get(clipName);
        if (target == null) {
            return;
        }

        this.currentClip = target;
        this.currentTime = 0.0f;
        this.previousClip = null;
        this.isBlending = false;
        this.blendFactor = 1.0f;
    }

    public void crossFade(String clipName, float duration) {
        if (currentClip != null && currentClip.getName().equals(clipName)) {
            return;
        }

        AnimationClip target = clips.get(clipName);
        if (target == null) {
            return;
        }

        if (currentClip == null || duration <= 0.001f) {
            play(clipName, true);
            return;
        }

        this.previousClip = this.currentClip;
        this.previousTime = this.currentTime;
        this.currentClip = target;
        this.currentTime = 0.0f;
        this.blendDuration = Math.max(0.01f, duration);
        this.blendFactor = 0.0f;
        this.isBlending = true;
    }

    public void update(float deltaTime) {
        if (currentClip != null) {
            currentTime += deltaTime;
        }

        if (isBlending && previousClip != null) {
            previousTime += deltaTime;
            blendFactor += deltaTime / blendDuration;
            if (blendFactor >= 1.0f) {
                blendFactor = 1.0f;
                isBlending = false;
                previousClip = null;
            }
        }
    }

    public void apply(Skeleton skeleton) {
        if (skeleton == null || currentClip == null) {
            return;
        }

        currentSampleMap.clear();
        currentClip.sample(currentTime, currentSampleMap);

        if (isBlending && previousClip != null) {
            prevSampleMap.clear();
            previousClip.sample(previousTime, prevSampleMap);

            for (Bone bone : skeleton.getAllBones().values()) {
                String name = bone.getName();

                BoneTransform prevTrans = prevSampleMap.get(name);
                if (prevTrans == null) {
                    defaultTransform.set(bone.getBindPos(), bone.getBindRot(), bone.getBindScale());
                    prevTrans = defaultTransform;
                }

                BoneTransform currTrans = currentSampleMap.get(name);
                if (currTrans == null) {
                    if (prevTrans == defaultTransform) {
                        bone.resetToBindPose();
                        continue;
                    }
                    defaultTransform.set(bone.getBindPos(), bone.getBindRot(), bone.getBindScale());
                    currTrans = defaultTransform;
                }

                prevTrans.lerp(currTrans, blendFactor, tempTransform);
                bone.localPos.set(tempTransform.translation);
                bone.localRot.set(tempTransform.rotation);
                bone.localScale.set(tempTransform.scale);
            }
        } else {
            for (Bone bone : skeleton.getAllBones().values()) {
                BoneTransform transform = currentSampleMap.get(bone.getName());
                if (transform != null) {
                    bone.localPos.set(transform.translation);
                    bone.localRot.set(transform.rotation);
                    bone.localScale.set(transform.scale);
                } else {
                    bone.resetToBindPose();
                }
            }
        }

        skeleton.updateMatrices();
    }
}
