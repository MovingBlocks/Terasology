// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.base.Stopwatch;
import gnu.trove.iterator.TIntIterator;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.rendering.RenderMath;
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
        ChunkMesh mesh = new ChunkMesh();

        final Stopwatch watch = Stopwatch.createStarted();

        // The mesh extends into the borders in the horizontal directions, but not vertically upwards, in order to cover
        // gaps between LOD chunks of different scales, but also avoid multiple overlapping ocean surfaces.
        for (int x = 0; x < Chunks.SIZE_X; x++) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                for (int y = 0; y < Chunks.SIZE_Y - border * 2; y++) {
                    Block block = chunkView.getBlock(x, y, z);
                    if (block != null && block.getMeshGenerator() != null) {
                        block.getMeshGenerator().generateChunkMesh(chunkView, mesh, x, y, z);
                    }
                }
            }
        }
        watch.stop();

        mesh.setTimeToGenerateBlockVertices((int) watch.elapsed(TimeUnit.MILLISECONDS));

        watch.reset().start();
//        generateOptimizedBuffers(chunkView, mesh, scale, border);
        watch.stop();
        mesh.setTimeToGenerateOptimizedBuffers((int) watch.elapsed(TimeUnit.MILLISECONDS));
        statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

//    public void generateOptimizedBuffers(ChunkView chunkView, ChunkMesh mesh, float scale, float border) {
//        PerformanceMonitor.startActivity("OptimizeBuffers");
//
//        for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
//            ChunkMesh.VertexElements elements = mesh.getVertexElements(type);
//            // Vertices double to account for light info
//            elements.finalVertices = BufferUtils.createIntBuffer(
//                    elements.vertices.size() + /* POSITION */
//                    elements.tex.size() + /* TEX0.xy (texture coords) */
//                    elements.flags.size() + /* TEX0.z (flags) */
//                    elements.frames.size() + /* TEX0.w (animation frame counts) */
//                    elements.vertexCount * 3 + /* TEX1 (lighting data) */
//                    elements.color.size() / 4 + /* COLOR */
//                    elements.normals.size() /* NORMALS */
//            );
//
//            for (int i = 0; i < elements.vertexCount; i++) {
//                Vector3f vertexPos = new Vector3f(
//                        elements.vertices.get(i * 3),
//                        elements.vertices.get(i * 3 + 1),
//                        elements.vertices.get(i * 3 + 2));
//
//                /* POSITION */
//                float totalScale = scale * Chunks.SIZE_X / (Chunks.SIZE_X - 2 * border);
//                elements.finalVertices.put(Float.floatToIntBits((vertexPos.x - border) * totalScale));
//                elements.finalVertices.put(Float.floatToIntBits((vertexPos.y - 2 * border) * totalScale));
//                elements.finalVertices.put(Float.floatToIntBits((vertexPos.z - border) * totalScale));
//
//                /* UV0 - TEX DATA 0.xy */
//                elements.finalVertices.put(Float.floatToIntBits(elements.tex.get(i * 2)));
//                elements.finalVertices.put(Float.floatToIntBits(elements.tex.get(i * 2 + 1)));
//
//                /* FLAGS - TEX DATA 0.z */
//                elements.finalVertices.put(Float.floatToIntBits(elements.flags.get(i)));
//
//                /* ANIMATION FRAME COUNT - TEX DATA 0.w*/
//                elements.finalVertices.put(Float.floatToIntBits(elements.frames.get(i)));
//
//                /* LIGHTING DATA / TEX DATA 1 */
//                elements.finalVertices.put(Float.floatToIntBits(elements.sunlight.get(i)));
//                elements.finalVertices.put(Float.floatToIntBits(elements.blocklight.get(i)));
//                elements.finalVertices.put(Float.floatToIntBits(elements.ambientOcclusion.get(i)));
//
//                /* PACKED COLOR */
//                final int packedColor = RenderMath.packColor(
//                        elements.color.get(i * 4),
//                        elements.color.get(i * 4 + 1),
//                        elements.color.get(i * 4 + 2),
//                        elements.color.get(i * 4 + 3));
//                elements.finalVertices.put(packedColor);
//
//                /* NORMALS */
//                elements.finalVertices.put(Float.floatToIntBits(elements.normals.get(i * 3)));
//                elements.finalVertices.put(Float.floatToIntBits(elements.normals.get(i * 3 + 1)));
//                elements.finalVertices.put(Float.floatToIntBits(elements.normals.get(i * 3 + 2)));
//            }
//
//            elements.finalIndices = BufferUtils.createIntBuffer(elements.indices.size());
//            TIntIterator indexIterator = elements.indices.iterator();
//            while (indexIterator.hasNext()) {
//                elements.finalIndices.put(indexIterator.next());
//            }
//
//            elements.finalVertices.flip();
//            elements.finalIndices.flip();
//        }
//        PerformanceMonitor.endActivity();
//    }

    public static int getVertexArrayUpdateCount() {
        return statVertexArrayUpdateCount;
    }
}
