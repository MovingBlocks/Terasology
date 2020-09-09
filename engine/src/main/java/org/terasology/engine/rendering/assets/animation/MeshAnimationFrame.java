// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.animation;

import com.google.common.collect.Lists;

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Collection;
import java.util.List;

/**
 */
public class MeshAnimationFrame {
    private final List<Vector3f> bonePositions;
    private final List<Quat4f> boneRotations;

    public MeshAnimationFrame(Collection<Vector3f> bonePositions, Collection<Quat4f> boneRotations) {
        this.bonePositions = Lists.newArrayList(bonePositions);
        this.boneRotations = Lists.newArrayList(boneRotations);
    }

    public Vector3f getPosition(int boneId) {
        return bonePositions.get(boneId);
    }

    public Quat4f getRotation(int boneId) {
        return boneRotations.get(boneId);
    }
}
