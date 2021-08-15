// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.animation;

import com.google.common.collect.Lists;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class MeshAnimationFrame {
    private List<Vector3f> bonePositions;
    private List<Quaternionf> boneRotations;
    private List<Vector3f> boneScales;

    public MeshAnimationFrame(List<Vector3f> bonePositions, List<Quaternionf> boneRotations, List<Vector3f> boneScales) {
        this.bonePositions = Lists.newArrayList(bonePositions);
        this.boneRotations = Lists.newArrayList(boneRotations);
        this.boneScales = Lists.newArrayList(boneScales);
    }

    public Vector3f getPosition(int boneId) {
        return bonePositions.get(boneId);
    }

    public Quaternionf getRotation(int boneId) {
        return boneRotations.get(boneId);
    }

    public Vector3f getBoneScale(int boneId) {
        return boneScales.get(boneId);
    }
}
