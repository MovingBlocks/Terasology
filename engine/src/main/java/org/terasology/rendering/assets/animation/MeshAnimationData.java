// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.animation;

import com.google.common.collect.ImmutableList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.assets.AssetData;
import org.terasology.joml.geom.AABBf;

import java.util.List;

/**
 */
public class MeshAnimationData implements AssetData {

    public static final int NO_PARENT = -1;

    private List<String> boneNames;
    private TIntList boneParent;
    private List<MeshAnimationFrame> frames;
    private float timePerFrame;
    private AABBf aabb;

    /**
     * @param boneNames    The names of the bones this animation expects
     * @param boneParents  The indices of the parent of each bone in the boneNames list, NO_PARENT for no parent.
     * @param aabb A bounding box that contains the object in all animation stops.
     * @param frames
     * @param timePerFrame
     */
    public MeshAnimationData(List<String> boneNames, TIntList boneParents, List<MeshAnimationFrame> frames,
                             float timePerFrame, AABBf aabb) {
        if (boneNames.size() != boneParents.size()) {
            throw new IllegalArgumentException("Bone names and boneParent indices must align");
        }
        this.boneNames = ImmutableList.copyOf(boneNames);
        this.boneParent = new TIntArrayList(boneParents);
        this.frames = ImmutableList.copyOf(frames);
        this.timePerFrame = timePerFrame;
        this.aabb = aabb;
    }

    public List<String> getBoneNames() {
        return boneNames;
    }

    public TIntList getBoneParent() {
        return boneParent;
    }

    public List<MeshAnimationFrame> getFrames() {
        return frames;
    }

    public float getTimePerFrame() {
        return timePerFrame;
    }

    public AABBf getAabb() {
        return aabb;
    }
}
