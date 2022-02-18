// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.chunk;

import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.MutableChunkMesh;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChunkMeshInfo {

    public final int totalFinalVertices;
    public final int totalFinalIndices;
    public final int totalTriangles;
    public final int totalTimeToGenerateBlockVertices;
    public final int totalTimeToGenerateOptimizedBuffers;

    public ChunkMeshInfo(MutableChunkMesh mesh) {
        checkNotNull(mesh, "The parameter 'mesh' must not be null");

        int vertices = 0;
        int indices = 0;

        if (mesh.hasVertexElements()) {
            for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
                final MutableChunkMesh.VertexElements element = mesh.getVertexElements(type);
                vertices += element.buffer.elements();
                indices += element.indices.indices();
            }
        }

        this.totalFinalVertices = vertices;
        this.totalFinalIndices = indices;
        this.totalTriangles = indices / 3;
        this.totalTimeToGenerateBlockVertices = mesh.getTimeToGenerateBlockVertices();
        this.totalTimeToGenerateOptimizedBuffers = mesh.getTimeToGenerateOptimizedBuffers();
    }
}

