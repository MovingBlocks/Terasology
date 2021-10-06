// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.primitives;

import org.joml.Vector3fc;
import org.lwjgl.opengl.GL30;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;

import java.lang.ref.WeakReference;

public class ChunkMeshImpl implements ChunkMesh {

    /* VERTEX DATA */
    private final int[] vertexBuffers = new int[4];
    private final int[] idxBuffers = new int[4];
    private final int[] vertexCount = new int[4];
    private final int[] vaoCount = new int[4];

    /* STATS */
    private int triangleCount = -1;

    /* TEMPORARY DATA */
    private VertexElements[] vertexElements = new VertexElements[ChunkMesh.RenderType.values().length];

    private boolean disposed;

    /* MEASUREMENTS */
    private int timeToGenerateBlockVertices;
    private int timeToGenerateOptimizedBuffers;

    public ChunkMeshImpl() {
        for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
            vertexElements[type.ordinal()] = new VertexElements();
        }
    }

    @Override
    public VertexElements getVertexElements(ChunkMesh.RenderType renderType) {
        return vertexElements[renderType.ordinal()];
    }

    @Override
    public boolean hasVertexElements() {
        return vertexElements != null;
    }

    @Override
    public boolean updateMesh() {

        // IMPORTANT: A mesh can only be generated once.
        if (vertexElements == null || disposed) {
            return false;
        }

        // Make sure that if it has already been generated, the previous buffers are freed
        dispose();
        disposed = false;

        for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
            generateVBO(type);
        }

        // Calculate the final amount of triangles
        triangleCount = (vertexCount[0] + vertexCount[1] + vertexCount[2] + vertexCount[3]) / 3;

        return true;
    }

    private void generateVBO(ChunkMesh.RenderType type) {
        VertexElements elements = vertexElements[type.ordinal()];
        int id = type.getIndex();
        if (!disposed && elements.buffer.elements() > 0) {
            vertexBuffers[id] = GL30.glGenBuffers();
            idxBuffers[id] = GL30.glGenBuffers();
            vaoCount[id] = GL30.glGenVertexArrays();

            GL30.glBindVertexArray(vaoCount[id]);

            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexBuffers[id]);
            elements.buffer.writeBuffer(buffer -> GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW));

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
     * Save space by removing the data that was used to construct the mesh, but after discardData is called, the mesh can't be serialized,
     * so it shouldn't be used in contexts where that might be necessary.
     */
    @Override
    public void discardData() {
        vertexElements = null;
    }

    private void renderVbo(int id) {
        if (vertexBuffers[id] <= 0 || disposed) {
            return;
        }

        GL30.glBindVertexArray(vaoCount[id]);
        GL30.glDrawElements(GL30.GL_TRIANGLES, vertexCount[id], GL30.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Updates a given material with information such as the World position of a chunk and whether it is animated.
     *
     * @param chunkMaterial a Material instance to be updated
     * @param chunkPosition a Vector3f instance holding the world coordinates of a chunk
     * @param chunkIsAnimated a boolean: true if the chunk is animated, false otherwise
     */
    @Override
    public void updateMaterial(Material chunkMaterial, Vector3fc chunkPosition, boolean chunkIsAnimated) {
        chunkMaterial.setFloat3("chunkPositionWorld", chunkPosition, true);
        chunkMaterial.setFloat("animated", chunkIsAnimated ? 1.0f : 0.0f, true);
    }

    public int render(ChunkMesh.RenderPhase type) {
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
     * Disposes of all the data stored in an instance of this class and the associated data stored in the GLBufferPool instance provided on
     * construction.
     * <p>
     * ChunkMesh instances cannot be un-disposed.
     */
    @Override
    public void dispose() {
        if (!disposed) {
            for (int i = 0; i < vertexBuffers.length; i++) {
                int id = vertexBuffers[i];
                if (id != 0) {
                    GL30.glDeleteBuffers(id);
                    vertexBuffers[i] = 0;
                }

                id = idxBuffers[i];
                if (id != 0) {
                    GL30.glDeleteBuffers(id);
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
    }

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public int triangleCount(ChunkMesh.RenderPhase phase) {
        if (phase == ChunkMesh.RenderPhase.OPAQUE) {
            return vertexCount[0] / 3;
        } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
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

}
