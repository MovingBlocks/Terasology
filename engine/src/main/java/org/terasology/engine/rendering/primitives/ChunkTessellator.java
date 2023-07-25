// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.base.Stopwatch;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.rust.EngineKernel;
import org.terasology.engine.rust.resource.ChunkGeometry;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunks;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 */
public final class ChunkTessellator {

    private static int statVertexArrayUpdateCount;
    private final EngineKernel kernel;

    public ChunkTessellator(EngineKernel kernel) {
        this.kernel = kernel;
    }

    public ChunkGeometry generateMesh(ChunkView chunkView) {
        return generateMesh(chunkView, 1, 0);
    }

    public ChunkGeometry generateMesh(ChunkView chunkView, float scale, int border) {
        PerformanceMonitor.startActivity("GenerateMesh");
        BlockMeshGenerator.ChunkMeshBufferGroup buffer = new BlockMeshGenerator.ChunkMeshBufferGroup();

        final Stopwatch watch = Stopwatch.createStarted();

        // The mesh extends into the borders in the horizontal directions, but not vertically upwards, in order to cover
        // gaps between LOD chunks of different scales, but also avoid multiple overlapping ocean surfaces.
        for (int y = 0; y < Chunks.SIZE_Y * 2; y++) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                for (int x = 0; x < Chunks.SIZE_X; x++) {
                    Block block = chunkView.getBlock(x, y, z);
                    block.getMeshGenerator().generateChunkMesh(chunkView, buffer, x, y, z);
                }
            }
        }

//        if (border != 0) {
//            float totalScale = scale * Chunks.SIZE_X / (Chunks.SIZE_X - 2 * border);
//            for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
//                ChunkMesh.VertexElements elements = mesh.getVertexElements(type);
//                Vector3f pos = new Vector3f();
//                for (int x = 0; x < elements.position.elements(); x++) {
//                    elements.position.get(x, pos);
//                    elements.position.set(x, pos.sub(border, 2 * border, border).mul(totalScale));
//                }
//            }
//        }

        watch.stop();
//        mesh.setTimeToGenerateBlockVertices((int) watch.elapsed(TimeUnit.MILLISECONDS));
        statVertexArrayUpdateCount++;

        ChunkGeometry geometry = this.kernel.resource.createChunkGeometry();
        if (!buffer.opaque.positionBuffer.isEmpty()) {
           geometry.setMeshResource(buffer.opaque.indices.buffer(),
                   buffer.opaque.positionBuffer.buffer(),
                   null,
                   null,
                   null,
                   null);
        }
//        for(BlockMeshGenerator.ChunkMeshBufferGroup.Geometry geom: buffer.geometries) {
//
//        }

        PerformanceMonitor.endActivity();
        return geometry;
    }

    public static int getVertexArrayUpdateCount() {
        return statVertexArrayUpdateCount;
    }
}
