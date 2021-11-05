// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.skeletalmesh;

import com.google.common.base.Preconditions;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;

public class BoneWeight {
    private float[] biases;
    private int[] boneIndices;

    public BoneWeight(float[] biases, int[] boneIndices) {
        Preconditions.checkArgument(biases.length == boneIndices.length);
        this.biases = biases;
        this.boneIndices = boneIndices;
    }

    public BoneWeight(TFloatList biases, TIntList boneIndices) {
        Preconditions.checkArgument(biases.size() == boneIndices.size());
        this.biases = biases.toArray();
        this.boneIndices = boneIndices.toArray();
    }

    public void normalise() {
        float totalBias = 0;
        for (float bias : biases) {
            totalBias += bias;
        }
        for (int i = 0; i < biases.length; ++i) {
            biases[i] /= totalBias;
        }
    }

    public int jointCount() {
        return biases.length;
    }

    public int getJoint(int weight) {
        return boneIndices[weight];
    }

    public float getBias(int weight) {
        return biases[weight];
    }
}

