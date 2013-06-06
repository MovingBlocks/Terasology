/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.VertexBufferObjectManager;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Immortius
 */
public class SkeletalMesh implements Asset {
    private static final int TEX_COORD_SIZE = 2;
    private static final int VECTOR3_SIZE = 3;
    private static final int STRIDE = 24;
    private static final int NORMAL_OFFSET = VECTOR3_SIZE * 4;

    private AssetUri uri;
    private Bone rootBone;
    private Map<String, Bone> boneLookup = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private List<Vector2f> uvs = Lists.newArrayList();
    private List<BoneWeight> weights = Lists.newArrayList();
    private TIntList vertexStartWeights;
    private TIntList vertexWeightCounts;
    private TIntList indices;

    private int vboPosNormBuffer = 0;
    private int vboUVBuffer = 0;
    private int vboIndexBuffer = 0;

    public SkeletalMesh(AssetUri uri) {
        this.uri = uri;
        vboPosNormBuffer = VertexBufferObjectManager.getInstance().getVboId();
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
        // TODO: dispose VBOs
    }

    public void addBone(Bone bone) {
        if (bone.getParent() == null && rootBone == null) {
            rootBone = bone;
        }
        boneLookup.put(bone.getName(), bone);
        bones.add(bone);
    }

    public Collection<Bone> bones() {
        return bones;
    }

    public void calculateNormals() {
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

        for (Vector3f normal : normals) {
            normal.normalize();
        }

        Quat4f inverseRot = new Quat4f();
        for (int vertIndex = 0; vertIndex < vertices.size(); ++vertIndex) {
            Vector3f normal = normals.get(vertIndex);
            for (int weightIndex = 0; weightIndex < vertexWeightCounts.get(vertIndex); ++weightIndex) {
                BoneWeight weight = weights.get(weightIndex + vertexStartWeights.get(vertIndex));
                inverseRot.inverse(bones.get(weight.getBoneIndex()).getObjectRotation());
                QuaternionUtil.quatRotate(inverseRot, normal, norm);
                weight.setNormal(norm);
            }
        }
    }

    public void setIndices(TIntList indices) {
        this.indices = new TIntArrayList(indices);

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
        indexBuffer.put(indices.toArray());
        indexBuffer.flip();
        vboIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        VertexBufferObjectManager.getInstance().bufferVboElementData(vboIndexBuffer, indexBuffer, GL15.GL_STATIC_DRAW);
    }

    public Bone getRootBone() {
        return rootBone;
    }

    public void setUvs(Collection<Vector2f> uvs) {
        this.uvs.clear();
        this.uvs.addAll(uvs);

        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(uvs.size() * 2);
        for (Vector2f uv : uvs) {
            uvBuffer.put(uv.x);
            uvBuffer.put(uv.y);
        }
        uvBuffer.flip();
        vboUVBuffer = VertexBufferObjectManager.getInstance().getVboId();
        VertexBufferObjectManager.getInstance().bufferVboData(vboUVBuffer, uvBuffer, GL15.GL_STATIC_DRAW);
    }

    public void setWeights(Collection<BoneWeight> weights) {
        this.weights.clear();
        this.weights.addAll(weights);
    }

    public void setVertexWeights(TIntList vertexStartWeight, TIntList vertexWeightCount) {
        this.vertexStartWeights = vertexStartWeight;
        this.vertexWeightCounts = vertexWeightCount;
    }

    public List<Vector3f> getBindPoseVertexPositions() {
        List<Vector3f> positions = Lists.newArrayListWithCapacity(bones.size());
        List<Quat4f> rotations = Lists.newArrayListWithCapacity(bones().size());
        for (Bone bone : bones) {
            positions.add(bone.getObjectPosition());
            rotations.add(bone.getObjectRotation());
        }
        return getVertexPositions(positions, rotations);
    }

    public List<Vector3f> getBindPoseVertexNormals() {
        List<Vector3f> positions = Lists.newArrayListWithCapacity(bones.size());
        List<Quat4f> rotations = Lists.newArrayListWithCapacity(bones().size());
        for (Bone bone : bones) {
            positions.add(bone.getObjectPosition());
            rotations.add(bone.getObjectRotation());
        }
        return getVertexNormals(positions, rotations);
    }

    public List<Vector3f> getVertexPositions(List<Vector3f> bonePositions, List<Quat4f> boneRotations) {
        List<Vector3f> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < vertexStartWeights.size(); ++i) {
            Vector3f vertexPos = new Vector3f();
            for (int weightIndexOffset = 0; weightIndexOffset < vertexWeightCounts.get(i); ++weightIndexOffset) {
                int weightIndex = vertexStartWeights.get(i) + weightIndexOffset;
                BoneWeight weight = weights.get(weightIndex);

                Vector3f current = QuaternionUtil.quatRotate(boneRotations.get(weight.getBoneIndex()), weight.getPosition(), new Vector3f());
                current.add(bonePositions.get(weight.getBoneIndex()));
                current.scale(weight.getBias());
                vertexPos.add(current);
            }
            results.add(vertexPos);
        }
        return results;
    }

    public List<Vector3f> getVertexNormals(List<Vector3f> bonePositions, List<Quat4f> boneRotations) {
        List<Vector3f> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < vertexStartWeights.size(); ++i) {
            Vector3f vertexNorm = new Vector3f();
            for (int weightIndexOffset = 0; weightIndexOffset < vertexWeightCounts.get(i); ++weightIndexOffset) {
                int weightIndex = vertexStartWeights.get(i) + weightIndexOffset;
                BoneWeight weight = weights.get(weightIndex);

                Vector3f current = QuaternionUtil.quatRotate(boneRotations.get(weight.getBoneIndex()), weight.getNormal(), new Vector3f());
                current.scale(weight.getBias());
                vertexNorm.add(current);
            }
            results.add(vertexNorm);
        }
        return results;
    }

    public void preRender() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUVBuffer);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glTexCoordPointer(2, GL11.GL_FLOAT, TEX_COORD_SIZE * 4, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexBuffer);
    }

    public void postRender() {
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void doRender(List<Vector3f> verts, List<Vector3f> normals) {
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(verts.size() * 6);
        for (int i = 0; i < verts.size(); ++i) {
            Vector3f vert = verts.get(i);
            vertBuffer.put(vert.x);
            vertBuffer.put(vert.y);
            vertBuffer.put(vert.z);
            Vector3f norm = normals.get(i);
            vertBuffer.put(norm.x);
            vertBuffer.put(norm.y);
            vertBuffer.put(norm.z);
        }
        vertBuffer.flip();
        VertexBufferObjectManager.getInstance().bufferVboData(vboPosNormBuffer, vertBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPosNormBuffer);
        glVertexPointer(VECTOR3_SIZE, GL_FLOAT, STRIDE, 0);
        glNormalPointer(GL_FLOAT, STRIDE, NORMAL_OFFSET);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        preRender();
        doRender(getBindPoseVertexPositions(), getBindPoseVertexNormals());
        postRender();
    }

    public void render(List<Vector3f> bonePositions, List<Quat4f> boneRotations) {
        preRender();
        doRender(getVertexPositions(bonePositions, boneRotations), getVertexNormals(bonePositions, boneRotations));
        postRender();
    }

    public int getVertexCount() {
        return vertexStartWeights.size();
    }

    public Bone getBone(String name) {
        return boneLookup.get(name);
    }
}
