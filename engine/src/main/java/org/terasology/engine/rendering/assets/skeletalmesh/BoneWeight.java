// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.skeletalmesh;

import org.terasology.math.geom.Vector3f;

/**
 */
public class BoneWeight {
    private final Vector3f position = new Vector3f();
    private final float bias;
    private final int boneIndex;
    private final Vector3f normal = new Vector3f();

    public BoneWeight(Vector3f position, float bias, int boneIndex) {
        this.position.set(position);
        this.bias = bias;
        this.boneIndex = boneIndex;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getBias() {
        return bias;
    }

    public int getBoneIndex() {
        return boneIndex;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(Vector3f normal) {
        this.normal.set(normal);
    }
}

