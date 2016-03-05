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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.GameThread;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.math.AABB;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.VertexBufferObjectUtil;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;

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
 */
public class OpenGLSkeletalMesh extends SkeletalMesh {

    private static final int TEX_COORD_SIZE = 2;
    private static final int VECTOR3_SIZE = 3;
    private static final int STRIDE = 24;
    private static final int NORMAL_OFFSET = VECTOR3_SIZE * 4;

    private static final Logger logger = LoggerFactory.getLogger(OpenGLSkeletalMesh.class);

    private SkeletalMeshData data;

    private Vector3f scale;
    private Vector3f translate;

    private DisposalAction disposalAction;

    public OpenGLSkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType, SkeletalMeshData data, GLBufferPool bufferPool) {
        super(urn, assetType);
        disposalAction = new DisposalAction(urn, bufferPool);
        getDisposalHook().setDisposeAction(disposalAction);
        reload(data);
    }

    public void setScaleTranslate(Vector3f newScale, Vector3f newTranslate) {
        this.scale = newScale;
        this.translate = newTranslate;
    }

    @Override
    protected void doReload(SkeletalMeshData newData) {
        try {
            GameThread.synch(() -> {
                this.data = newData;

                if (disposalAction.vboPosNormBuffer == 0) {
                    disposalAction.vboPosNormBuffer = disposalAction.bufferPool.get(getUrn().toString());
                }

                IntBuffer indexBuffer = BufferUtils.createIntBuffer(newData.getIndices().size());
                indexBuffer.put(newData.getIndices().toArray());
                indexBuffer.flip();
                if (disposalAction.vboIndexBuffer == 0) {
                    disposalAction.vboIndexBuffer = disposalAction.bufferPool.get(getUrn().toString());
                }
                VertexBufferObjectUtil.bufferVboElementData(disposalAction.vboIndexBuffer, indexBuffer, GL15.GL_STATIC_DRAW);

                FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(newData.getUVs().size() * 2);
                for (Vector2f uv : newData.getUVs()) {
                    uvBuffer.put(uv.x);
                    uvBuffer.put(uv.y);
                }
                uvBuffer.flip();

                if (disposalAction.vboUVBuffer == 0) {
                    disposalAction.vboUVBuffer = disposalAction.bufferPool.get(getUrn().toString());
                }
                VertexBufferObjectUtil.bufferVboData(disposalAction.vboUVBuffer, uvBuffer, GL15.GL_STATIC_DRAW);
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    public void preRender() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, disposalAction.vboUVBuffer);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glTexCoordPointer(2, GL11.GL_FLOAT, TEX_COORD_SIZE * 4, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, disposalAction.vboIndexBuffer);
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
            vertBuffer.put(vert.x * scale.x + translate.x);
            vertBuffer.put(vert.y * scale.y + translate.y);
            vertBuffer.put(vert.z * scale.z + translate.z);
            Vector3f norm = normals.get(i);
            vertBuffer.put(norm.x);
            vertBuffer.put(norm.y);
            vertBuffer.put(norm.z);
        }
        vertBuffer.flip();
        VertexBufferObjectUtil.bufferVboData(disposalAction.vboPosNormBuffer, vertBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, disposalAction.vboPosNormBuffer);
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

    @Override
    public AABB getStaticAabb() {
        return data.getStaticAABB();
    }

    private static class DisposalAction implements Runnable {

        private final ResourceUrn urn;
        private GLBufferPool bufferPool;

        private int vboPosNormBuffer;
        private int vboUVBuffer;
        private int vboIndexBuffer;

        public DisposalAction(ResourceUrn urn, GLBufferPool bufferPool) {
            this.urn = urn;
            this.bufferPool = bufferPool;
        }

        @Override
        public void run() {
            try {
                GameThread.synch(() -> {
                    if (vboIndexBuffer != 0) {
                        bufferPool.dispose(vboIndexBuffer);
                        vboIndexBuffer = 0;
                    }
                    if (vboPosNormBuffer != 0) {
                        bufferPool.dispose(vboPosNormBuffer);
                        vboPosNormBuffer = 0;
                    }
                    if (vboUVBuffer != 0) {
                        bufferPool.dispose(vboUVBuffer);
                        vboUVBuffer = 0;
                    }
                });
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
