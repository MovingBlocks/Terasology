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
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.math.geom.Vector3f;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.VertexBufferObjectUtil;
import org.terasology.rendering.assets.material.Material;
import org.terasology.world.chunks.ChunkConstants;

import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * Chunk meshes store, manipulate and render the vertex data of tessellated chunks.
 */
@SuppressWarnings("PointlessArithmeticExpression")
public class ChunkMesh {

    /**
     * Possible rendering types.
     */
    @API
    public enum RenderType {
        OPAQUE(0),
        TRANSLUCENT(1),
        BILLBOARD(2),
        WATER_AND_ICE(3);

        private int meshIndex;

        RenderType(int index) {
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

    // some constants
    private static final int SIZE_VERTEX = 3;   // vertices have 3 positional components, x,y,z
    private static final int SIZE_TEX0 = 4;     // the first texture has 4 components, u,v, flags, animation frame count
    private static final int SIZE_TEX1 = 3;     // the second texture stores lighting, with components: light, block light, ambient occlusion
    private static final int SIZE_COLOR = 1;    // the color field's 4 components are packed into 1 float-sized field.
    private static final int SIZE_NORMAL = 3;   // normals are 3-dimensional vectors with u,v,t components

    // offset to the beginning of each data field, from the start of the data regarding an individual vertex
    private static final int OFFSET_VERTEX = 0;
    private static final int OFFSET_TEX_0 = OFFSET_VERTEX + SIZE_VERTEX * 4;
    private static final int OFFSET_TEX_1 = OFFSET_TEX_0 + SIZE_TEX0 * 4;
    private static final int OFFSET_COLOR = OFFSET_TEX_1 + SIZE_TEX1 * 4;
    private static final int OFFSET_NORMAL = OFFSET_COLOR + SIZE_COLOR * 4;
    private static final int STRIDE = OFFSET_NORMAL + SIZE_NORMAL * 4;
    // the STRIDE, above, is the gap between the beginnings of the data regarding two consecutive vertices

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

    /**
     * Updates a given material with information such as the World position of a chunk and whether it is animated.
     *
     * @param chunkMaterial a Material instance to be updated
     * @param chunkPosition a Vector3f instance holding the world coordinates of a chunk
     * @param chunkIsAnimated a boolean: true if the chunk is animated, false otherwise
     */
    public void updateMaterial(Material chunkMaterial, Vector3fc chunkPosition, boolean chunkIsAnimated) {
        chunkMaterial.setFloat3("chunkPositionWorld",
                chunkPosition.x() * ChunkConstants.SIZE_X,
                chunkPosition.y() * ChunkConstants.SIZE_Y,
                chunkPosition.z() * ChunkConstants.SIZE_Z,
                true);
        chunkMaterial.setFloat("animated", chunkIsAnimated ? 1.0f : 0.0f, true);
    }

    /**
     * Renders the phase-appropriate mesh of the chunk. I.e. if the RenderPhase is OPAQUE only the opaque mesh
     * is rendered: other meshes stored in an instance of this class are rendered in separate rendering steps.
     *
     * @param phase a RenderPhase value
     * @param chunkPosition a Vector3f storing the world position of the chunk.
     * @param cameraPosition a Vector3f storing the world position of the point of view from which the chunk is rendered.
     * @return Returns an integer representing the number of triangles rendered.
     */
    public int render(ChunkMesh.RenderPhase phase, Vector3fc chunkPosition, Vector3fc cameraPosition) {
        GL11.glPushMatrix();

        // chunkPositionRelativeToCamera = chunkCoordinates * chunkDimensions - cameraCoordinate
        final Vector3f chunkPositionRelativeToCamera =
                new Vector3f(chunkPosition.x() * ChunkConstants.SIZE_X - cameraPosition.x(),
                        chunkPosition.y() * ChunkConstants.SIZE_Y - cameraPosition.y(),
                        chunkPosition.z() * ChunkConstants.SIZE_Z - cameraPosition.z());
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);

        render(phase);  // this is where the chunk is actually rendered

        GL11.glPopMatrix();

        return triangleCount();
    }

    private void render(RenderPhase type) {
        switch (type) {
            case OPAQUE:
                renderVbo(0);
                break;
            case ALPHA_REJECT:
                renderVbo(1);
                renderVbo(2);
                break;
            case REFRACTIVE:
                renderVbo(3);
                break;
            default:
                break;
        }
    }

    /**
     * Disposes of all the data stored in an instance of this class and
     * the associated data stored in the GLBufferPool instance provided on construction.
     *
     * ChunkMesh instances cannot be un-disposed.
     */
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

    private int triangleCount() {
        return triangleCount;
    }

    /**
     * Returns true if an instance of this class stores no triangles.
     *
     * @return True if no triangles are stored in the instance, false otherwise.
     */
    public boolean isEmpty() {
        return triangleCount == 0;
    }

    void setTimeToGenerateBlockVertices(int timeToGenerateBlockVertices) {
        this.timeToGenerateBlockVertices = timeToGenerateBlockVertices;
    }

    public int getTimeToGenerateBlockVertices() {
        return timeToGenerateBlockVertices;
    }

    void setTimeToGenerateOptimizedBuffers(int timeToGenerateOptimizedBuffers) {
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
        public final TIntList frames;
        public int vertexCount;

        public IntBuffer finalVertices;
        public IntBuffer finalIndices;

        VertexElements() {
            vertexCount = 0;
            normals = new TFloatArrayList();
            vertices = new TFloatArrayList();
            tex = new TFloatArrayList();
            color = new TFloatArrayList();
            indices = new TIntArrayList();
            flags = new TIntArrayList();
            frames = new TIntArrayList();
        }
    }
}
