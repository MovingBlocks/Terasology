/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.assets.skeletalmesh;

import com.google.common.base.Preconditions;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;

/**
 */
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

