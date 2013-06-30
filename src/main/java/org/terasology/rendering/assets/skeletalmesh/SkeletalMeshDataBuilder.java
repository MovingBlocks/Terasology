/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * @author Immortius
 */
public class SkeletalMeshDataBuilder {

    private List<Bone> bones = Lists.newArrayList();
    private List<BoneWeight> weights = Lists.newArrayList();
    private List<Vector2f> uvs = Lists.newArrayList();
    private TIntList vertexStartWeights = new TIntArrayList();
    private TIntList vertexWeightCounts = new TIntArrayList();
    private TIntList indices = new TIntArrayList();

    public SkeletalMeshDataBuilder() {

    }

    public SkeletalMeshDataBuilder addBone(Bone bone) {
        bones.add(bone);
        return this;
    }

    public SkeletalMeshDataBuilder addWeight(BoneWeight boneWeight) {
        weights.add(boneWeight);
        return this;
    }

    public void setVertexWeights(TIntList vertexStartWeight, TIntList vertexWeightCount) {
        this.vertexStartWeights.clear();
        this.vertexStartWeights.addAll(vertexStartWeight);
        this.vertexWeightCounts.clear();
        this.vertexWeightCounts.addAll(vertexWeightCount);
    }

    public void setUvs(List<Vector2f> uvs) {
        this.uvs.clear();
        this.uvs.addAll(uvs);
    }

    public void setIndices(TIntList indices) {
        this.indices.clear();
        this.indices.addAll(indices);
    }

    public SkeletalMeshData build() {
        int rootBones = 0;
        for (Bone bone : bones) {
            if (bone.getParent() == null) {
                rootBones++;
            }
        }

        if (rootBones == 0) {
            throw new IllegalStateException("Cannot create a skeleton with no root bones");
        } else if (rootBones > 1) {
            throw new IllegalStateException("Cannot create a skeleton with multiple root bones");
        }

        // TODO: More validation

        return new SkeletalMeshData(bones, weights, uvs, vertexStartWeights, vertexWeightCounts, indices);
    }


}
