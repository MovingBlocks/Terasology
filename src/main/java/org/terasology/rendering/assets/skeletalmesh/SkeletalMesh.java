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

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.VertexBufferObjectManager;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * @author Immortius
 */
public class SkeletalMesh implements Asset {
    private AssetUri uri;
    private Bone rootBone;
    private Map<String, Bone> boneLookup = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private List<Vector2f> uvs = Lists.newArrayList();
    private List<BoneWeight> weights = Lists.newArrayList();
    private TIntList vertexStartWeights;
    private TIntList vertexWeightCounts;
    private TIntList indices;

    private int vboPosBuffer = 0;
    private int vboUVBuffer = 0;
    private int vboIndexBuffer = 0;

    public SkeletalMesh(AssetUri uri) {
        this.uri = uri;
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
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

        vboPosBuffer = VertexBufferObjectManager.getInstance().getVboId();
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

    public List<Vector3f> getVertexPositions(List<Vector3f> bonePositions, List<Quat4f> boneRotations) {
        List<Vector3f> results = Lists.newArrayList();
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

    public static final int TEX_COORD_SIZE = 2;

    public void preRender() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        // if (hasNormal) glEnableClientState(GL_NORMAL_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUVBuffer);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glTexCoordPointer(2, GL11.GL_FLOAT, TEX_COORD_SIZE * 4, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexBuffer);

        //if (hasNormal) glNormalPointer(GL11.GL_FLOAT, stride, normalOffset);
    }

    public void postRender() {
        //if (hasNormal) glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void doRender(List<Vector3f> verts) {
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(verts.size() * 3);
        for (Vector3f vert : verts) {
            vertBuffer.put(vert.x);
            vertBuffer.put(vert.y);
            vertBuffer.put(vert.z);
        }
        vertBuffer.flip();
        VertexBufferObjectManager.getInstance().bufferVboData(vboPosBuffer, vertBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPosBuffer);
        glVertexPointer(3, GL_FLOAT, 3 * 4, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        preRender();
        doRender(getBindPoseVertexPositions());
        postRender();
    }

    public void render(List<Vector3f> bonePositions, List<Quat4f> boneRotations) {
        preRender();
        doRender(getVertexPositions(bonePositions, boneRotations));
        postRender();
    }

    public int getVertexCount() {
        return vertexStartWeights.size();
    }
}
