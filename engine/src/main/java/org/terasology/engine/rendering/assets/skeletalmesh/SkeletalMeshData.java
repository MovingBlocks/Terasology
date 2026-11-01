// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.skeletalmesh;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.joml.geom.AABBf;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SkeletalMeshData implements AssetData {

    private Bone rootBone;
    private Map<String, Bone> boneLookup = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private List<Vector2f> uvs;
    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<BoneWeight> weights = Lists.newArrayList();
    private TIntList indices = new TIntArrayList();
    private AABBf staticAABB;

    public SkeletalMeshData(List<Bone> bones, List<Vector3f> vertices, List<Vector3f> normals,
                            List<BoneWeight> weights, List<Vector2f> uvs, TIntList indices, AABBf staticAABB) {
        for (Bone bone : bones) {
            boneLookup.put(bone.getName(), bone);
            if (bone.getParent() == null) {
                rootBone = bone;
            }
        }
        this.bones.addAll(bones);
        this.weights.addAll(weights);
        this.uvs = ImmutableList.copyOf(uvs);
        this.vertices = ImmutableList.copyOf(vertices);
        this.normals = ImmutableList.copyOf(normals);
        this.indices.addAll(indices);
        this.staticAABB = staticAABB;

        calculateNormals();
    }

    /**
     * @return Information on all bones composing the mesh
     */
    public Collection<Bone> getBones() {
        return bones;
    }

    /**
     * @return Information on the root bone
     */
    public Bone getRootBone() {
        return rootBone;
    }

    /**
     * @return Provides the vertex positions for the default pose
     */
    public List<Vector3f> getBindPoseVertexPositions() {
        Matrix4f[] transforms = new Matrix4f[bones.size()];
        for (Bone bone : bones) {
            transforms[bone.getIndex()] = bone.getObjectTransform();
        }
        return getVertexPositions(Arrays.asList(transforms));
    }

    /**
     * @return Provides the vertex normals for the default pose
     */
    public List<Vector3f> getBindPoseVertexNormals() {
        Matrix4f[] transforms = new Matrix4f[bones.size()];
        for (Bone bone : bones) {
            transforms[bone.getIndex()] = bone.getObjectTransform();
        }
        return getVertexNormals(Arrays.asList(transforms));
    }

    /**
     * Provides the positions of all vertices of the mesh, transformed based on the transformation matrices of all
     * bones
     *
     * @param boneTransforms A transformation matrix for each bone in the skeletal mesh
     * @return The positions of each vertex
     */
    public List<Vector3f> getVertexPositions(List<Matrix4f> boneTransforms) {
        List<Vector3f> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f pos = new Vector3f(vertices.get(i));
            Matrix4f skinMat = new Matrix4f().m00(0).m11(0).m22(0).m33(0);
            BoneWeight weight = weights.get(i);
            for (int w = 0; w < weight.jointCount(); w++) {
                Matrix4f jointMat = new Matrix4f(boneTransforms.get(weight.getJoint(w)));
                jointMat.scale(weight.getBias(w));
                skinMat.add(jointMat);
            }
            pos.mulTransposePosition(skinMat);
            results.add(pos);
        }
        return results;
    }

    /**
     * Provides the normals of all vertices of the mesh, transformed based on the transformation matrices of all bones
     *
     * @param boneTransforms A transformation matrix for each bone in the skeletal mesh
     * @return The normals of each vertex
     */
    public List<Vector3f> getVertexNormals(List<Matrix4f> boneTransforms) {
        List<Vector3f> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < normals.size(); i++) {
            Vector3f norm = new Vector3f(normals.get(i));
            Matrix4f skinMat = new Matrix4f().m00(0).m11(0).m22(0).m33(0);
            BoneWeight weight = weights.get(i);
            for (int w = 0; w < weight.jointCount(); w++) {
                Matrix4f jointMat = new Matrix4f(boneTransforms.get(weight.getJoint(w)));
                jointMat.scale(weight.getBias(w));
                skinMat.add(jointMat);
            }
            norm.mulTransposePosition(skinMat);
            results.add(norm);
        }
        return results;
    }

    /**
     * @return The number of vertices composing the mesh
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * @param name The name of the bone
     * @return Provides information for the named bone
     */
    public Bone getBone(String name) {
        return boneLookup.get(name);
    }

    /**
     * @return The indices instructing how to render the vertices as triangles
     */
    public TIntList getIndices() {
        return indices;
    }

    /**
     * @return The texture coordinate of each vertex
     */
    public List<Vector2f> getUVs() {
        return uvs;
    }

    /**
     * @return A axis-aligned bounding box that surrounds the skeletal mesh given its default pose.
     */
    public AABBf getStaticAABB() {
        return staticAABB;
    }

    private void calculateNormals() {
        // TODO: Better algorithm (take into account triangle size and angles
        List<Vector3f> vertices = getBindPoseVertexPositions();
        List<Vector3f> normals = IntStream.range(0, vertices.size()).mapToObj(i -> new Vector3f()).collect(Collectors.toCollection(() ->
                Lists.newArrayListWithCapacity(vertices.size())));
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f norm = new Vector3f();
        for (int i = 0; i < indices.size() / 3; ++i) {
            Vector3f baseVert = vertices.get(indices.get(i * 3));
            vertices.get(indices.get(i * 3 + 1)).sub(baseVert, v1);
            vertices.get(indices.get(i * 3 + 2)).sub(baseVert, v2);
            v1.normalize();
            v2.normalize();
            v2.cross(v1, norm);
            normals.get(indices.get(i * 3)).add(norm);
            normals.get(indices.get(i * 3 + 1)).add(norm);
            normals.get(indices.get(i * 3 + 2)).add(norm);
        }

        normals.forEach(Vector3f::normalize);

        this.normals = normals;
    }
}
