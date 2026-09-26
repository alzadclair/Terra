package com.terraforge.rpg.client.animation.skeletal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Keyframe-based animation clip for skeletal armatures.
 * Holds bone tracks and evaluates interpolated poses at specific timestamps.
 */
public class AnimationClip {
    private final String name;
    private final float duration;
    private final boolean looping;
    private final Map<String, BoneTrack> tracks = new HashMap<>();

    public AnimationClip(String name, float duration, boolean looping) {
        this.name = name;
        this.duration = Math.max(0.001f, duration);
        this.looping = looping;
    }

    public String getName() {
        return name;
    }

    public float getDuration() {
        return duration;
    }

    public boolean isLooping() {
        return looping;
    }

    public AnimationClip addTrack(BoneTrack track) {
        tracks.put(track.getBoneName(), track);
        return this;
    }

    public BoneTrack getTrack(String boneName) {
        return tracks.get(boneName);
    }

    public Map<String, BoneTrack> getTracks() {
        return Collections.unmodifiableMap(tracks);
    }

    public void sample(float time, Map<String, BoneTransform> outTransforms) {
        float sampleTime = time;
        if (looping) {
            sampleTime = (sampleTime % duration + duration) % duration;
        } else {
            sampleTime = Math.max(0.0f, Math.min(sampleTime, duration));
        }

        for (Map.Entry<String, BoneTrack> entry : tracks.entrySet()) {
            BoneTransform transform = outTransforms.computeIfAbsent(entry.getKey(), k -> new BoneTransform());
            entry.getValue().sample(sampleTime, transform);
        }
    }
}
