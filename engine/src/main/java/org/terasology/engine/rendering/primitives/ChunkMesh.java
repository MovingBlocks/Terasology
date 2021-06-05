// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.collect.Maps;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexFloatAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexIntegerAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.gestalt.module.sandbox.API;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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

    /* VERTEX DATA */
    public final int[] vertexBuffers = new int[4];
    public final int[] idxBuffers = new int[4];
    public final int[] vertexCount = new int[4];
    public final int[] vaoCount = new int[4];

    /* STATS */
    private int triangleCount = -1;

    /* TEMPORARY DATA */
    private VertexElements[] vertexElements = new VertexElements[RenderType.values().length];

    private boolean disposed;

    /* CONCURRENCY */
    private ReentrantLock lock = new ReentrantLock();

    /* MEASUREMENTS */
    private int timeToGenerateBlockVertices;
    private int timeToGenerateOptimizedBuffers;

    public ChunkMesh() {
        for (RenderType type : RenderType.values()) {
            vertexElements[type.ordinal()] = new VertexElements();
        }
    }

    public VertexElements getVertexElements(RenderType renderType) {
        return vertexElements[renderType.ordinal()];
    }

    public boolean hasVertexElements() {
        return vertexElements != null;
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

                // Make sure that if it has already been generated, the previous buffers are freed
                dispose();
                disposed = false;

                for (RenderType type : RenderType.values()) {
                    generateVBO(type);
                }

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
        VertexElements elements = vertexElements[type.ordinal()];
        int id = type.getIndex();
        if (!disposed && elements.buffer.elements() > 0) {
            vertexBuffers[id] = GL15.glGenBuffers();
            idxBuffers[id] = GL15.glGenBuffers();
            vaoCount[id] = GL30.glGenVertexArrays();

            GL30.glBindVertexArray(vaoCount[id]);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffers[id]);
            elements.buffer.writeBuffer(buffer -> {
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW);
            });

            for (VertexResource.VertexDefinition definition : elements.buffer.definitions()) {
                GL30.glEnableVertexAttribArray(definition.location);
                if (definition.location == VertexElements.FLAGS_INDEX) {
                    GL30.glVertexAttribIPointer(definition.location, definition.attribute.count,
                            definition.attribute.mapping.glType, elements.buffer.inStride(), definition.offset);
                } else {
                    GL30.glVertexAttribPointer(definition.location, definition.attribute.count,
                            definition.attribute.mapping.glType, false, elements.buffer.inStride(), definition.offset);
                }
            }

            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, idxBuffers[id]);
            elements.indices.writeBuffer((buffer) -> GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW));
            vertexCount[id] = elements.indices.indices();

            GL30.glBindVertexArray(0);

        } else {
            vertexBuffers[id] = 0;
            idxBuffers[id] = 0;
            vertexCount[id] = 0;
        }
    }

    /**
     * Save space by removing the data that was used to construct the mesh, but
     * after discardData is called, the mesh can't be serialized, so it shouldn't
     * be used in contexts where that might be necessary.
     */
    public void discardData() {
        vertexElements = null;
    }

    private void renderVbo(int id) {
        if (lock.tryLock()) {
            try {
                if (vertexBuffers[id] <= 0 || disposed) {
                    return;
                }

                GL30.glBindVertexArray(vaoCount[id]);
                GL30.glDrawElements(GL30.GL_TRIANGLES, vertexCount[id], GL30.GL_UNSIGNED_INT, 0);
                GL30.glBindVertexArray(0);
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
        chunkMaterial.setFloat3("chunkPositionWorld", chunkPosition, true);
        chunkMaterial.setFloat("animated", chunkIsAnimated ? 1.0f : 0.0f, true);
    }

    public int render(RenderPhase type) {
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
        return triangleCount();
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
                        GL15.glDeleteBuffers(id);
                        vertexBuffers[i] = 0;
                    }

                    id = idxBuffers[i];
                    if (id != 0) {
                        GL15.glDeleteBuffers(id);
                        idxBuffers[i] = 0;
                    }

                    id = vaoCount[i];
                    if (id != 0) {
                        GL30.glDeleteVertexArrays(id);
                        vaoCount[i] = 0;
                    }
                }

                disposed = true;
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

        public static final int VERTEX_INDEX = 0; // vec3
        public static final int NORMAL_INDEX = 1;  // vec3
        public static final int UV0_INDEX = 2;  // vec3

        public static final int FLAGS_INDEX = 3;  // int
        public static final int FRAME_INDEX = 4; // float

        public static final int SUNLIGHT_INDEX = 5; // float
        public static final int BLOCK_INDEX = 6; // float
        public static final int AMBIENT_OCCLUSION_INDEX = 7; // float

        public final VertexResource buffer;
        public final IndexResource indices = new IndexResource();

        public final VertexAttributeBinding<Vector3fc, Vector3f> position;
        public final VertexAttributeBinding<Vector3fc, Vector3f> normals;
        public final VertexAttributeBinding<Vector2fc, Vector2f> uv0;

        // color data is unused something to consider later
        // public final VertexAttributeBinding<Colorc, Color> color;

        public final VertexIntegerAttributeBinding flags;
        public final VertexIntegerAttributeBinding frames;

        public final VertexFloatAttributeBinding sunlight;         // this could be changed to a single byte
        public final VertexFloatAttributeBinding blockLight;       // this could be changed to a single byte
        public final VertexFloatAttributeBinding ambientOcclusion; // this could be changed to a single byte
        public  int vertexCount;


        VertexElements() {
            VertexResourceBuilder builder = new VertexResourceBuilder();
            position = builder.add(VERTEX_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
            normals = builder.add(NORMAL_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
            uv0 = builder.add(UV0_INDEX, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);

            flags = builder.add(FLAGS_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);
            frames = builder.add(FRAME_INDEX, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);

            sunlight = builder.add(SUNLIGHT_INDEX, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);
            blockLight = builder.add(BLOCK_INDEX, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);
            ambientOcclusion = builder.add(AMBIENT_OCCLUSION_INDEX, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);

            buffer = builder.build();
        }
    }
}
