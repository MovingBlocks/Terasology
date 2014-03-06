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
package org.terasology.rendering.primitives;

import com.google.common.collect.Maps;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.rendering.VertexBufferObjectUtil;

import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * Chunk meshes are used to store the vertex data of tessellated chunks.
 */
@SuppressWarnings("PointlessArithmeticExpression")
public class ChunkMesh {

    /**
     * Possible rendering types.
     */
    public enum RenderType {
        OPAQUE(0),
        TRANSLUCENT(1),
        BILLBOARD(2),
        WATER_AND_ICE(3);

        private int meshIndex;

        private RenderType(int index) {
            meshIndex = index;
        }

        public int getIndex() {
            return meshIndex;
        }
    }

    public enum RenderPhase {
        OPAQUE,
        ALPHA_REJECT,
        REFRACTIVE,
        Z_PRE_PASS
    }

    /* CONST */
    public static final int SIZE_VERTEX = 3;
    public static final int SIZE_TEX0 = 3;
    public static final int SIZE_TEX1 = 3;
    public static final int SIZE_COLOR = 1;
    public static final int SIZE_NORMAL = 3;

    private static final int OFFSET_VERTEX = 0;
    private static final int OFFSET_TEX_0 = OFFSET_VERTEX + SIZE_VERTEX * 4;
    private static final int OFFSET_TEX_1 = OFFSET_TEX_0 + SIZE_TEX0 * 4;
    private static final int OFFSET_COLOR = OFFSET_TEX_1 + SIZE_TEX1 * 4;
    private static final int OFFSET_NORMAL = OFFSET_COLOR + SIZE_COLOR * 4;
    private static final int STRIDE = OFFSET_NORMAL + SIZE_NORMAL * 4;

    /* VERTEX DATA */
    private final int[] vertexBuffers = new int[4];
    private final int[] idxBuffers = new int[4];
    private final int[] vertexCount = new int[4];

    /* STATS */
    private int triangleCount = -1;

    /* TEMPORARY DATA */
    private Map<RenderType, VertexElements> vertexElements = Maps.newEnumMap(RenderType.class);

    private boolean disposed;

    /* CONCURRENCY */
    private ReentrantLock lock = new ReentrantLock();

    /* MEASUREMENTS */
    private int timeToGenerateBlockVertices;
    private int timeToGenerateOptimizedBuffers;

    private GLBufferPool bufferPool;

    public ChunkMesh(GLBufferPool bufferPool) {
        this.bufferPool = bufferPool;
        for (RenderType type : RenderType.values()) {
            vertexElements.put(type, new VertexElements());
        }
    }

    public VertexElements getVertexElements(RenderType renderType) {
        return vertexElements.get(renderType);
    }

    public boolean isGenerated() {
        return vertexElements == null;
    }

    /**
     * Generates the VBOs from the pre calculated arrays.
     *
     * @return True if something was generated
     */
    public boolean generateVBOs() {
        if (lock.tryLock()) {
            try {
                // IMPORTANT: A mesh can only be generated once.
                if (vertexElements == null || disposed) {
                    return false;
                }

                for (RenderType type : RenderType.values()) {
                    generateVBO(type);
                }

                // Free unused space on the heap
                vertexElements = null;
                // Calculate the final amount of triangles
                triangleCount = (vertexCount[0] + vertexCount[1] + vertexCount[2] + vertexCount[3]) / 3;
            } finally {
                lock.unlock();
            }

            return true;
        }

        return false;
    }

    private void generateVBO(RenderType type) {
        VertexElements elements = vertexElements.get(type);
        int id = type.getIndex();
        if (!disposed && elements.finalIndices.limit() > 0 && elements.finalVertices.limit() > 0) {
            vertexBuffers[id] = bufferPool.get("chunkMesh");
            idxBuffers[id] = bufferPool.get("chunkMesh");
            vertexCount[id] = elements.finalIndices.limit();

            VertexBufferObjectUtil.bufferVboElementData(idxBuffers[id], elements.finalIndices, GL15.GL_STATIC_DRAW);
            VertexBufferObjectUtil.bufferVboData(vertexBuffers[id], elements.finalVertices, GL15.GL_STATIC_DRAW);
        } else {
            vertexBuffers[id] = 0;
            idxBuffers[id] = 0;
            vertexCount[id] = 0;
        }

    }

    private void renderVbo(int id) {
        if (lock.tryLock()) {
            try {
                if (vertexBuffers[id] <= 0 || disposed) {
                    return;
                }

                glEnableClientState(GL_VERTEX_ARRAY);
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                glEnableClientState(GL_COLOR_ARRAY);
                glEnableClientState(GL_NORMAL_ARRAY);

                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, idxBuffers[id]);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffers[id]);

                glVertexPointer(SIZE_VERTEX, GL11.GL_FLOAT, STRIDE, OFFSET_VERTEX);

                GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
                glTexCoordPointer(SIZE_TEX0, GL11.GL_FLOAT, STRIDE, OFFSET_TEX_0);

                GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
                glTexCoordPointer(SIZE_TEX1, GL11.GL_FLOAT, STRIDE, OFFSET_TEX_1);

                glColorPointer(SIZE_COLOR * 4, GL11.GL_UNSIGNED_BYTE, STRIDE, OFFSET_COLOR);

                glNormalPointer(GL11.GL_FLOAT, STRIDE, OFFSET_NORMAL);

                GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount[id], GL11.GL_UNSIGNED_INT, 0);


                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

                glDisableClientState(GL_NORMAL_ARRAY);
                glDisableClientState(GL_COLOR_ARRAY);
                glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                glDisableClientState(GL_VERTEX_ARRAY);

            } finally {
                lock.unlock();
            }
        }
    }

    public void render(RenderPhase type) {
        switch (type) {
            case OPAQUE:
                renderVbo(0);
                break;
            case ALPHA_REJECT:
                renderVbo(1);
                glDisable(GL_CULL_FACE);
                renderVbo(2);
                glEnable(GL_CULL_FACE);
                break;
            case REFRACTIVE:
                renderVbo(3);
                break;
            default:
                break;
        }
    }

    public void dispose() {
        lock.lock();
        try {
            if (!disposed) {
                for (int i = 0; i < vertexBuffers.length; i++) {
                    int id = vertexBuffers[i];
                    if (id != 0) {
                        bufferPool.dispose(id);
                        vertexBuffers[i] = 0;
                    }

                    id = idxBuffers[i];
                    if (id != 0) {
                        bufferPool.dispose(id);
                        idxBuffers[i] = 0;
                    }
                }

                disposed = true;
                vertexElements = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public int triangleCount(RenderPhase phase) {
        if (phase == RenderPhase.OPAQUE) {
            return vertexCount[0] / 3;
        } else if (phase == RenderPhase.ALPHA_REJECT) {
            return (vertexCount[1] + vertexCount[2]) / 3;
        } else {
            return vertexCount[3] / 3;
        }
    }

    public int triangleCount() {
        return triangleCount;
    }

    public boolean isEmpty() {
        return triangleCount == 0;
    }

    public void setTimeToGenerateBlockVertices(int timeToGenerateBlockVertices) {
        this.timeToGenerateBlockVertices = timeToGenerateBlockVertices;
    }

    public int getTimeToGenerateBlockVertices() {
        return timeToGenerateBlockVertices;
    }

    public void setTimeToGenerateOptimizedBuffers(int timeToGenerateOptimizedBuffers) {
        this.timeToGenerateOptimizedBuffers = timeToGenerateOptimizedBuffers;
    }

    public int getTimeToGenerateOptimizedBuffers() {
        return timeToGenerateOptimizedBuffers;
    }

    /**
     * Data structure for storing vertex data. Abused like a "struct" in C/C++. Just sad.
     */
    public static class VertexElements {

        public final TFloatList normals;
        public final TFloatList vertices;
        public final TFloatList tex;
        public final TFloatList color;
        public final TIntList indices;
        public final TIntList flags;
        public int vertexCount;

        public IntBuffer finalVertices;
        public IntBuffer finalIndices;

        public VertexElements() {
            vertexCount = 0;
            normals = new TFloatArrayList();
            vertices = new TFloatArrayList();
            tex = new TFloatArrayList();
            color = new TFloatArrayList();
            indices = new TIntArrayList();
            flags = new TIntArrayList();
        }
    }
}
