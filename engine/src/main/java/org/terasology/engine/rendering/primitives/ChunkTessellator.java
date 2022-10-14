// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.base.Stopwatch;
import org.joml.Vector3f;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunks;

import java.util.concurrent.TimeUnit;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 */
public final class ChunkTessellator {

    private static int statVertexArrayUpdateCount;

    public ChunkTessellator() {

    }

    public ChunkMesh generateMesh(ChunkView chunkView) {
        return generateMesh(chunkView, 1, 0);
    }

    public ChunkMesh generateMesh(ChunkView chunkView, float scale, int border) {
        PerformanceMonitor.startActivity("GenerateMesh");
        ChunkMeshImpl mesh = new ChunkMeshImpl();

        final Stopwatch watch = Stopwatch.createStarted();

        // The mesh extends into the borders in the horizontal directions, but not vertically upwards, in order to cover
        // gaps between LOD chunks of different scales, but also avoid multiple overlapping ocean surfaces.
        for (int y = 0; y < Chunks.SIZE_Y - border * 2; y++) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                for (int x = 0; x < Chunks.SIZE_X; x++) {
                    Block block = chunkView.getBlock(x, y, z);
                    block.getMeshGenerator().generateChunkMesh(chunkView, mesh, x, y, z);
                }
            }
        }

        if (border != 0) {
            float totalScale = scale * Chunks.SIZE_X / (Chunks.SIZE_X - 2 * border);
            for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
                ChunkMesh.VertexElements elements = mesh.getVertexElements(type);
                Vector3f pos = new Vector3f();
                for (int x = 0; x < elements.position.elements(); x++) {
                    elements.position.get(x, pos);
                    elements.position.set(x, pos.sub(border, 2 * border, border).mul(totalScale));
                }
            }
        }

        watch.stop();
        mesh.setTimeToGenerateBlockVertices((int) watch.elapsed(TimeUnit.MILLISECONDS));
        statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    public static int getVertexArrayUpdateCount() {
        return statVertexArrayUpdateCount;
    }
}
