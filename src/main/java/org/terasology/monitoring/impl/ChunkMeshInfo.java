package org.terasology.monitoring.impl;

import org.terasology.rendering.primitives.ChunkMesh;

import com.google.common.base.Preconditions;

public class ChunkMeshInfo {
    
    public final int totalFinalVertices;
    public final int totalFinalIndices;
    public final int totalTriangles;
    public final int totalTimeToGenerateBlockVertices;
    public final int totalTimeToGenerateOptimizedBuffers;
    
    public ChunkMeshInfo(ChunkMesh[] mesh) {
        Preconditions.checkNotNull(mesh, "The parameter 'mesh' must not be null");
        
        int totalFinalVertices = 0;
        int totalFinalIndices = 0;
        int totalTimeToGenerateBlockVertices = 0;
        int totalTimeToGenerateOptimizedBuffers = 0;

        for (int i = 0; i < mesh.length; i++) {
            final ChunkMesh segment = Preconditions.checkNotNull(mesh[i], "Chunk mesh segment #" + i + " must not be null");
            if (segment._vertexElements != null)
                for (int j = 0; j < segment._vertexElements.length; j++) {
                    final ChunkMesh.VertexElements element = Preconditions.checkNotNull(segment._vertexElements[j], "Vertex element #" + j + " of chunk mesh segment #" + i + " must not be null");
                    totalFinalVertices += element.finalVertices.limit();
                    totalFinalIndices += element.finalIndices.limit();
                }
            totalTimeToGenerateBlockVertices += segment.timeToGenerateBlockVertices;
            totalTimeToGenerateOptimizedBuffers +=segment.timeToGenerateOptimizedBuffers;
        }
        
        this.totalFinalVertices = totalFinalVertices;
        this.totalFinalIndices = totalFinalIndices;
        this.totalTriangles = totalFinalIndices / 3;
        this.totalTimeToGenerateBlockVertices = totalTimeToGenerateBlockVertices;
        this.totalTimeToGenerateOptimizedBuffers = totalTimeToGenerateOptimizedBuffers;
    }
}

