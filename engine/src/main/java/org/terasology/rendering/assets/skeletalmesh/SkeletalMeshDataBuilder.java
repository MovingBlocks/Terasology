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

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.math.AABB;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.assets.mesh.MeshData;

import java.util.List;

/**
 */
public class SkeletalMeshDataBuilder {

    private List<Bone> bones = Lists.newArrayList();
    private List<BoneWeight> weights = Lists.newArrayList();
    private List<Vector2f> uvs = Lists.newArrayList();
    private TIntList vertexStartWeights = new TIntArrayList();
    private TIntList vertexWeightCounts = new TIntArrayList();
    private TIntList indices = new TIntArrayList();
    private MeshBuilder.TextureMapper textureMapper;
    private Vector3f minOfAABB = null;
    private Vector3f maxOfAABB = null;

    public SkeletalMeshDataBuilder() {

    }

    public SkeletalMeshDataBuilder addBone(Bone bone) {
        if (!bones.contains(bone)) {
            bones.add(bone);
        }
        return this;
    }

    public SkeletalMeshDataBuilder addWeight(BoneWeight boneWeight) {
        Vector3f pos = boneWeight.getPosition();
        if (minOfAABB == null) {
            minOfAABB = new Vector3f(pos);
        } else {
            minOfAABB.min(pos);
        }
        if (maxOfAABB == null) {
            maxOfAABB = new Vector3f(pos);
        } else {
            maxOfAABB.max(pos);
        }
        weights.add(boneWeight);
        return this;
    }

    public SkeletalMeshDataBuilder addMesh(Bone bone, MeshBuilder builder) {
        return addMesh(bone, builder.getMeshData());
    }

    public SkeletalMeshDataBuilder addBox(Bone bone, Vector3f offset, Vector3f size, float u, float v) {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.setTextureMapper(textureMapper);
        meshBuilder.addBox(offset, size, u, v);
        return addMesh(bone, meshBuilder);
    }

    public SkeletalMeshDataBuilder addMesh(Bone bone, MeshData data) {
        TFloatList meshVertices = data.getVertices();
        TIntList meshIndices = data.getIndices();
        TFloatList texCoord0 = data.getTexCoord0();
        int weightsStart = weights.size();
        addBone(bone);
        for (int i = 0; i < meshVertices.size() / 3; i++) {
            float x = meshVertices.get(i * 3);
            float y = meshVertices.get(i * 3 + 1);
            float z = meshVertices.get(i * 3 + 2);
            Vector3f pos = new Vector3f(x, y, z);
            BoneWeight weight = new BoneWeight(pos, 1, bone.getIndex());
            // TODO Meshes may contain normal vectors and we may copy them to the weight here
            //   - but they are recalculated later on in either case. needs some rework
            addWeight(weight);
            vertexStartWeights.add(weightsStart + i);
            vertexWeightCounts.add(1);
            uvs.add(new Vector2f(texCoord0.get(i * 2), texCoord0.get(i * 2 + 1)));
        }

        for (int i = 0; i < meshIndices.size(); i++) {
            indices.add(meshIndices.get(i) + weightsStart);
        }
        return this;
    }

    public void setTextureMapper(MeshBuilder.TextureMapper textureMapper) {
        this.textureMapper = textureMapper;
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
        AABB staticAabb;
        if (minOfAABB != null && maxOfAABB != null) {
            staticAabb = AABB.createMinMax(minOfAABB, maxOfAABB);
        } else {
            staticAabb = AABB.createEmpty();
        }
        // TODO: More validation

        return new SkeletalMeshData(bones, weights, uvs, vertexStartWeights, vertexWeightCounts, indices, staticAabb);
    }
}
