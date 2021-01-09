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

import com.google.common.collect.Lists;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
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
import org.terasology.engine.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.math.AABB;
import org.terasology.rendering.VertexBufferObjectUtil;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 */
public class OpenGLMesh extends Mesh {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLMesh.class);

    private static final int FLOAT_SIZE = 4;
    private AABB aabb;

    private MeshData data;

    private int stride;
    private int vertexOffset;
    private int texCoord0Offset;
    private int texCoord1Offset;
    private int colorOffset;
    private int normalOffset;

    private boolean hasTexCoord0;
    private boolean hasTexCoord1;
    private boolean hasColor;
    private boolean hasNormal;
    private int indexCount;

    private DisposalAction disposalAction;

    public OpenGLMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, GLBufferPool bufferPool, MeshData data, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType);
        this.disposalAction = new DisposalAction(urn, bufferPool);
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    @Override
    protected void doReload(MeshData newData) {
        try {
            GameThread.synch(() -> buildMesh(newData));
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    @Override
    public AABB getAABB() {
        return aabb;
    }

    @Override
    public TFloatList getVertices() {
        return data.getVertices();
    }

    public void preRender() {
        if (!isDisposed()) {
            glEnableClientState(GL_VERTEX_ARRAY);
            if (hasTexCoord0 || hasTexCoord1) {
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            }
            if (hasColor) {
                glEnableClientState(GL_COLOR_ARRAY);
            }
            if (hasNormal) {
                glEnableClientState(GL_NORMAL_ARRAY);
            }

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, disposalAction.vboVertexBuffer);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, disposalAction.vboIndexBuffer);

            glVertexPointer(VERTEX_SIZE, GL11.GL_FLOAT, stride, vertexOffset);

            if (hasTexCoord0) {
                GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
                glTexCoordPointer(TEX_COORD_0_SIZE, GL11.GL_FLOAT, stride, texCoord0Offset);
            }

            if (hasTexCoord1) {
                GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
                glTexCoordPointer(TEX_COORD_1_SIZE, GL11.GL_FLOAT, stride, texCoord1Offset);
            }

            if (hasColor) {
                glColorPointer(COLOR_SIZE, GL11.GL_FLOAT, stride, colorOffset);
            }
            if (hasNormal) {
                glNormalPointer(GL11.GL_FLOAT, stride, normalOffset);
            }
        } else {
            logger.error("Attempted to render disposed mesh: {}", getUrn());
        }
    }

    public void postRender() {
        if (!isDisposed()) {
            if (hasNormal) {
                glDisableClientState(GL_NORMAL_ARRAY);
            }
            if (hasColor) {
                glDisableClientState(GL_COLOR_ARRAY);
            }
            if (hasTexCoord0 || hasTexCoord1) {
                glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            }
            glDisableClientState(GL_VERTEX_ARRAY);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            logger.error("Attempted to render disposed mesh: {}", getUrn());
        }
    }

    public void doRender() {
        if (!isDisposed()) {
            GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        } else {
            logger.error("Attempted to render disposed mesh: {}", getUrn());
        }
    }

    @Override
    public void render() {
        if (!isDisposed()) {
            preRender();
            doRender();
            postRender();
        } else {
            logger.error("Attempted to render disposed mesh: {}", getUrn());
        }
    }

    private void buildMesh(MeshData newData) {
        this.data = newData;

        List<TFloatIterator> parts = Lists.newArrayList();
        TIntList partSizes = new TIntArrayList();
        int vertexCount = newData.getVertices().size() / VERTEX_SIZE;
        int vertexSize = VERTEX_SIZE;
        parts.add(newData.getVertices().iterator());
        partSizes.add(VERTEX_SIZE);

        if (newData.getTexCoord0() != null && newData.getTexCoord0().size() / TEX_COORD_0_SIZE == vertexCount) {
            parts.add(newData.getTexCoord0().iterator());
            partSizes.add(TEX_COORD_0_SIZE);
            texCoord0Offset = vertexSize * FLOAT_SIZE;
            vertexSize += TEX_COORD_0_SIZE;
            hasTexCoord0 = true;
        }
        if (newData.getTexCoord1() != null && newData.getTexCoord1().size() / TEX_COORD_1_SIZE == vertexCount) {
            parts.add(newData.getTexCoord1().iterator());
            partSizes.add(TEX_COORD_1_SIZE);
            texCoord1Offset = vertexSize * FLOAT_SIZE;
            vertexSize += TEX_COORD_1_SIZE;
            hasTexCoord1 = true;
        }
        if (newData.getNormals() != null && newData.getNormals().size() / NORMAL_SIZE == vertexCount) {
            parts.add(newData.getNormals().iterator());
            partSizes.add(NORMAL_SIZE);
            normalOffset = vertexSize * FLOAT_SIZE;
            vertexSize += NORMAL_SIZE;
            hasNormal = true;
        }
        if (newData.getColors() != null && newData.getColors().size() / COLOR_SIZE == vertexCount) {
            parts.add(newData.getColors().iterator());
            partSizes.add(COLOR_SIZE);
            colorOffset = vertexSize * FLOAT_SIZE;
            vertexSize += COLOR_SIZE;
            hasColor = true;
        }
        stride = vertexSize * FLOAT_SIZE;
        indexCount = newData.getIndices().size();

        createVertexBuffer(parts, partSizes, vertexCount, vertexSize);
        createIndexBuffer(newData.getIndices());

        aabb = AABB.createEncompasing(newData.getVertices());
    }

    private void createVertexBuffer(List<TFloatIterator> parts, TIntList partSizes, int vertexCount, int vertexSize) {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexSize * vertexCount);
        for (int v = 0; v < vertexCount; ++v) {
            for (int partIndex = 0; partIndex < parts.size(); ++partIndex) {
                TFloatIterator part = parts.get(partIndex);
                for (int i = 0; i < partSizes.get(partIndex); ++i) {
                    vertexBuffer.put(part.next());
                }
            }
        }
        vertexBuffer.flip();
        if (disposalAction.vboVertexBuffer == 0) {
            disposalAction.vboVertexBuffer = disposalAction.bufferPool.get(getUrn().toString());
        }
        VertexBufferObjectUtil.bufferVboData(disposalAction.vboVertexBuffer, vertexBuffer, GL15.GL_STATIC_DRAW);
        vertexBuffer.flip();
    }

    private void createIndexBuffer(TIntList indexList) {
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexList.size());
        TIntIterator iterator = indexList.iterator();
        while (iterator.hasNext()) {
            indexBuffer.put(iterator.next());
        }
        indexBuffer.flip();

        if (disposalAction.vboIndexBuffer == 0) {
            disposalAction.vboIndexBuffer = disposalAction.bufferPool.get(getUrn().toString());
        }
        VertexBufferObjectUtil.bufferVboElementData(disposalAction.vboIndexBuffer, indexBuffer, GL15.GL_STATIC_DRAW);
        indexBuffer.flip();
    }

    private static class DisposalAction implements Runnable {

        private final ResourceUrn urn;
        private final GLBufferPool bufferPool;

        private int vboVertexBuffer;
        private int vboIndexBuffer;

        DisposalAction(ResourceUrn urn, GLBufferPool bufferPool) {
            this.urn = urn;
            this.bufferPool = bufferPool;
        }

        @Override
        public void run() {
            try {
                GameThread.synch(() -> {
                    if (vboVertexBuffer != 0) {
                        bufferPool.dispose(vboVertexBuffer);
                        vboVertexBuffer = 0;
                    }
                    if (vboIndexBuffer != 0) {
                        bufferPool.dispose(vboIndexBuffer);
                        vboIndexBuffer = 0;
                    }
                });
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
