// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.skeletalmesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.joml.geom.AABBf;
import org.terasology.engine.rendering.assets.mesh.MeshBuilder;
import org.terasology.engine.rendering.assets.mesh.MeshData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SkeletalMeshDataBuilder {

    private List<Bone> bones = new ArrayList<>();
    private List<BoneWeight> weights = new ArrayList<>();
    private List<Vector2f> uvs = new ArrayList<>();
    private List<Vector3f> vertices = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private TIntList indices = new TIntArrayList();

    public SkeletalMeshDataBuilder() {

    }

    public SkeletalMeshDataBuilder addBone(Bone bone) {
        if (!bones.contains(bone)) {
            bones.add(bone);
        }
        return this;
    }

    public SkeletalMeshDataBuilder addVertices(List<Vector3f> vertices) {
        this.vertices.addAll(vertices);
        return this;
    }

    public SkeletalMeshDataBuilder addNormals(List<Vector3f> normals) {
        this.normals.addAll(normals);
        return this;
    }

    public SkeletalMeshDataBuilder addWeights(List<BoneWeight> weights) {
        this.weights.addAll(weights);
        return this;
    }

    public SkeletalMeshDataBuilder addMesh(Bone bone, MeshBuilder builder) {
        return addMesh(bone, builder.getMeshData());
    }

    public SkeletalMeshDataBuilder addMesh(Bone bone, StandardMeshData data) {
        TFloatList meshVertices = data.getVertices();
        TIntList meshIndices = data.getIndices();
        TFloatList texCoord0 = data.uv0;
        int weightsStart = weights.size();
        addBone(bone);
        for (int i = 0; i < meshVertices.size() / 3; i++) {
            float x = meshVertices.get(i * 3);
            float y = meshVertices.get(i * 3 + 1);
            float z = meshVertices.get(i * 3 + 2);
            Vector3f pos = new Vector3f(x, y, z);
            BoneWeight weight = new BoneWeight(new float[]{1}, new int[]{bone.getIndex()});
            // TODO Copy mesh normals
            vertices.add(pos);
            weights.add(weight);
            uvs.add(new Vector2f(texCoord0.get(i * 2), texCoord0.get(i * 2 + 1)));
        }

        for (int i = 0; i < meshIndices.size(); i++) {
            indices.add(meshIndices.get(i) + weightsStart);
        }
        return this;
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

        Vector3f minOfAABB = new Vector3f(vertices.get(0));
        Vector3f maxOfAABB = new Vector3f(vertices.get(0));
        for (Vector3f vert : vertices) {
            minOfAABB.min(vert);
            maxOfAABB.max(vert);
        }

        if (rootBones == 0) {
            throw new IllegalStateException("Cannot create a skeleton with no root bones");
        } else if (rootBones > 1) {
            throw new IllegalStateException("Cannot create a skeleton with multiple root bones");
        }
        AABBf staticAabb = new AABBf(minOfAABB, maxOfAABB);
        return new SkeletalMeshData(bones, vertices, normals, weights, uvs, indices, staticAabb);
    }

}
