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
package org.terasology.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.VertexBufferObjectManager;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;

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

/**
 * @author Immortius
 */
public class OpenGLSkeletalMesh extends AbstractAsset<SkeletalMeshData> implements SkeletalMesh {

    private static final int TEX_COORD_SIZE = 2;
    private static final int VECTOR3_SIZE = 3;
    private static final int STRIDE = 24;
    private static final int NORMAL_OFFSET = VECTOR3_SIZE * 4;

    private SkeletalMeshData data;

    private int vboPosNormBuffer = 0;
    private int vboUVBuffer = 0;
    private int vboIndexBuffer = 0;

    public OpenGLSkeletalMesh(AssetUri uri, SkeletalMeshData data) {
        super(uri);
        reload(data);
    }

    @Override
    public void reload(SkeletalMeshData data) {
        dispose();

        this.data = data;

        vboPosNormBuffer = VertexBufferObjectManager.getInstance().getVboId();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(data.getIndices().size());
        indexBuffer.put(data.getIndices().toArray());
        indexBuffer.flip();
        vboIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        VertexBufferObjectManager.getInstance().bufferVboElementData(vboIndexBuffer, indexBuffer, GL15.GL_STATIC_DRAW);

        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(data.getUVs().size() * 2);
        for (Vector2f uv : data.getUVs()) {
            uvBuffer.put(uv.x);
            uvBuffer.put(uv.y);
        }
        uvBuffer.flip();
        vboUVBuffer = VertexBufferObjectManager.getInstance().getVboId();
        VertexBufferObjectManager.getInstance().bufferVboData(vboUVBuffer, uvBuffer, GL15.GL_STATIC_DRAW);
    }

    @Override
    public void dispose() {
        if (vboIndexBuffer != 0) {
            VertexBufferObjectManager.getInstance().putVboId(vboIndexBuffer);
            vboIndexBuffer = 0;
        }
        if (vboPosNormBuffer != 0) {
            VertexBufferObjectManager.getInstance().putVboId(vboPosNormBuffer);
            vboPosNormBuffer = 0;
        }
        if (vboUVBuffer != 0) {
            VertexBufferObjectManager.getInstance().putVboId(vboUVBuffer);
            vboUVBuffer = 0;
        }
    }

    @Override
    public boolean isDisposed() {
        return vboPosNormBuffer == 0 && vboUVBuffer == 0 && vboIndexBuffer == 0;
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

        GL11.glDrawElements(GL11.GL_TRIANGLES, data.getIndices().size(), GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        preRender();
        doRender(data.getBindPoseVertexPositions(), data.getBindPoseVertexNormals());
        postRender();
    }

    public void render(List<Vector3f> bonePositions, List<Quat4f> boneRotations) {
        preRender();
        doRender(data.getVertexPositions(bonePositions, boneRotations), data.getVertexNormals(bonePositions, boneRotations));
        postRender();
    }

    @Override
    public int getVertexCount() {
        return data.getVertexCount();
    }

    @Override
    public Collection<Bone> getBones() {
        return data.getBones();
    }

    @Override
    public Bone getBone(String boneName) {
        return data.getBone(boneName);
    }
}
