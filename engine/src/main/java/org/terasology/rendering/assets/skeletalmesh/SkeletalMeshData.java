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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.assets.AssetData;
import org.terasology.math.AABB;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SkeletalMeshData implements AssetData {

    private Bone rootBone;
    private Map<String, Bone> boneLookup = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private List<Vector2f> uvs;
    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<BoneWeight> weights = Lists.newArrayList();
    private TIntList indices = new TIntArrayList();
    private AABB staticAABB;

    public SkeletalMeshData(List<Bone> bones, List<Vector3f> vertices, List<Vector3f> normals, List<BoneWeight> weights, List<Vector2f> uvs, TIntList indices, AABB staticAABB) {
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
     * Provides the positions of all vertices of the mesh, transformed based on the transformation matrices of all bones
     * @param boneTransforms A transformation matrix for each bone in the skeletal mesh
     * @return The positions of each vertex
     */
    public List<Vector3f> getVertexPositions(List<Matrix4f> boneTransforms) {
        List<Vector3f> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f pos = new Vector3f(vertices.get(i));
            Matrix4f skinMat = new Matrix4f();
            BoneWeight weight = weights.get(i);
            for (int w = 0; w < weight.jointCount(); w++) {
                Matrix4f jointMat = new Matrix4f(boneTransforms.get(weight.getJoint(w)));
                jointMat.mul(weight.getBias(w));
                skinMat.add(jointMat);
            }
            skinMat.transformPoint(pos);
            results.add(pos);
        }
        return results;
    }

    /**
     * Provides the normals of all vertices of the mesh, transformed based on the transformation matrices of all bones
     * @param boneTransforms A transformation matrix for each bone in the skeletal mesh
     * @return The normals of each vertex
     */
    public List<Vector3f> getVertexNormals(List<Matrix4f> boneTransforms) {
        List<Vector3f> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < normals.size(); i++) {
            Vector3f norm = new Vector3f(normals.get(i));
            Matrix4f skinMat = new Matrix4f();
            BoneWeight weight = weights.get(i);
            for (int w = 0; w < weight.jointCount(); w++) {
                Matrix4f jointMat = new Matrix4f(boneTransforms.get(weight.getJoint(w)));
                jointMat.mul(weight.getBias(w));
                skinMat.add(jointMat);
            }
            skinMat.transformVector(norm);
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
    public AABB getStaticAABB() {
        return staticAABB;
    }

    private void calculateNormals() {
        // TODO: Better algorithm (take into account triangle size and angles
        List<Vector3f> vertices = getBindPoseVertexPositions();
        List<Vector3f> normals = Lists.newArrayListWithCapacity(vertices.size());
        for (int i = 0; i < vertices.size(); ++i) {
            normals.add(new Vector3f());
        }
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f norm = new Vector3f();
        for (int i = 0; i < indices.size() / 3; ++i) {
            Vector3f baseVert = vertices.get(indices.get(i * 3));
            v1.sub(vertices.get(indices.get(i * 3 + 1)), baseVert);
            v2.sub(vertices.get(indices.get(i * 3 + 2)), baseVert);
            v1.normalize();
            v2.normalize();
            norm.cross(v1, v2);
            normals.get(indices.get(i * 3)).add(norm);
            normals.get(indices.get(i * 3 + 1)).add(norm);
            normals.get(indices.get(i * 3 + 2)).add(norm);
        }

        normals.forEach(Vector3f::normalize);

        this.normals = normals;
    }
}
