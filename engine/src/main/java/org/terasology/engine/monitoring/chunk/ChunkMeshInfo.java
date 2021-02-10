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
package org.terasology.monitoring.chunk;

import org.terasology.rendering.primitives.ChunkMesh;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChunkMeshInfo {

    public final int totalFinalVertices;
    public final int totalFinalIndices;
    public final int totalTriangles;
    public final int totalTimeToGenerateBlockVertices;
    public final int totalTimeToGenerateOptimizedBuffers;

    public ChunkMeshInfo(ChunkMesh mesh) {
        checkNotNull(mesh, "The parameter 'mesh' must not be null");

        int vertices = 0;
        int indices = 0;

        if (!mesh.isGenerated()) {
            for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
                final ChunkMesh.VertexElements element = mesh.getVertexElements(type);
                vertices += element.finalVertices.limit();
                indices += element.finalIndices.limit();
            }
        }

        this.totalFinalVertices = vertices;
        this.totalFinalIndices = indices;
        this.totalTriangles = indices / 3;
        this.totalTimeToGenerateBlockVertices = mesh.getTimeToGenerateBlockVertices();
        this.totalTimeToGenerateOptimizedBuffers = mesh.getTimeToGenerateOptimizedBuffers();
    }
}

