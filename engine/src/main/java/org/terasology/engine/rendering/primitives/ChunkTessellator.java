// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.base.Stopwatch;
import gnu.trove.iterator.TIntIterator;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.terasology.engine.core.subsystem.lwjgl.GLBufferPool;
import org.terasology.engine.math.Direction;
import org.terasology.math.TeraMath;
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

    private GLBufferPool bufferPool;

    public ChunkTessellator(GLBufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public ChunkMesh generateMesh(ChunkView chunkView) {
        return generateMesh(chunkView, 1, 0);
    }

    public ChunkMesh generateMesh(ChunkView chunkView, float scale, int border) {
        PerformanceMonitor.startActivity("GenerateMesh");
        ChunkMesh mesh = new ChunkMesh(bufferPool);

        final Stopwatch watch = Stopwatch.createStarted();

        // The mesh extends into the borders in the horizontal directions, but not vertically upwards, in order to cover gaps between LOD chunks of different scales, but also avoid multiple overlapping ocean surfaces.
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
        generateOptimizedBuffers(chunkView, mesh, scale, border);
        watch.stop();
        mesh.setTimeToGenerateOptimizedBuffers((int) watch.elapsed(TimeUnit.MILLISECONDS));
        statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    private void generateOptimizedBuffers(ChunkView chunkView, ChunkMesh mesh, float scale, float border) {
        PerformanceMonitor.startActivity("OptimizeBuffers");

        for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
            ChunkMesh.VertexElements elements = mesh.getVertexElements(type);
            // Vertices double to account for light info
            elements.finalVertices = BufferUtils.createIntBuffer(
                    elements.vertices.size() + /* POSITION */
                    elements.tex.size() + /* TEX0.xy (texture coords) */
                    elements.flags.size() + /* TEX0.z (flags) */
                    elements.frames.size() + /* TEX0.w (animation frame counts) */
                    elements.vertexCount * 3 + /* TEX1 (lighting data) */
                    elements.color.size() + /* COLOR */
                    elements.normals.size() /* NORMALS */
            );

            for (int i = 0; i < elements.vertexCount; i++) {
                Vector3f vertexPos = new Vector3f(
                        elements.vertices.get(i * 3),
                        elements.vertices.get(i * 3 + 1),
                        elements.vertices.get(i * 3 + 2));

                /* POSITION */
                float totalScale = scale * Chunks.SIZE_X / (Chunks.SIZE_X - 2 * border);
                elements.finalVertices.put(Float.floatToIntBits((vertexPos.x - border) * totalScale));
                elements.finalVertices.put(Float.floatToIntBits((vertexPos.y - 2 * border) * totalScale));
                elements.finalVertices.put(Float.floatToIntBits((vertexPos.z - border) * totalScale));

                /* UV0 - TEX DATA 0.xy */
                elements.finalVertices.put(Float.floatToIntBits(elements.tex.get(i * 2)));
                elements.finalVertices.put(Float.floatToIntBits(elements.tex.get(i * 2 + 1)));

                /* FLAGS - TEX DATA 0.z */
                elements.finalVertices.put(Float.floatToIntBits(elements.flags.get(i)));
                
                /* ANIMATION FRAME COUNT - TEX DATA 0.w*/
                elements.finalVertices.put(Float.floatToIntBits(elements.frames.get(i)));

                float[] result = new float[3];
                Vector3f normal = new Vector3f(elements.normals.get(i * 3), elements.normals.get(i * 3 + 1), elements.normals.get(i * 3 + 2));
                calcLightingValuesForVertexPos(chunkView, vertexPos, result, normal);

                /* LIGHTING DATA / TEX DATA 1 */
                elements.finalVertices.put(Float.floatToIntBits(result[0]));
                elements.finalVertices.put(Float.floatToIntBits(result[1]));
                elements.finalVertices.put(Float.floatToIntBits(result[2]));

                /* PACKED COLOR */
                final int packedColor = RenderMath.packColor(
                        elements.color.get(i * 4),
                        elements.color.get(i * 4 + 1),
                        elements.color.get(i * 4 + 2),
                        elements.color.get(i * 4 + 3));
                elements.finalVertices.put(packedColor);

                /* NORMALS */
                elements.finalVertices.put(Float.floatToIntBits(normal.x));
                elements.finalVertices.put(Float.floatToIntBits(normal.y));
                elements.finalVertices.put(Float.floatToIntBits(normal.z));
            }

            elements.finalIndices = BufferUtils.createIntBuffer(elements.indices.size());
            TIntIterator indexIterator = elements.indices.iterator();
            while (indexIterator.hasNext()) {
                elements.finalIndices.put(indexIterator.next());
            }

            elements.finalVertices.flip();
            elements.finalIndices.flip();
        }
        PerformanceMonitor.endActivity();
    }

    private void calcLightingValuesForVertexPos(ChunkView chunkView, Vector3f vertexPos, float[] output, Vector3f normal) {
        PerformanceMonitor.startActivity("calcLighting");
        float[] lights = new float[8];
        float[] blockLights = new float[8];
        Block[] blocks = new Block[4];

        PerformanceMonitor.startActivity("gatherLightInfo");
        Direction dir = Direction.inDirection(normal);
        switch (dir) {
            case LEFT:
            case RIGHT:
                blocks[0] = chunkView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y + 0.1f), (vertexPos.z + 0.1f));
                blocks[1] = chunkView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y + 0.1f), (vertexPos.z - 0.1f));
                blocks[2] = chunkView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
                blocks[3] = chunkView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
                break;
            case FORWARD:
            case BACKWARD:
                blocks[0] = chunkView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.1f), (vertexPos.z + 0.8f * normal.z));
                blocks[1] = chunkView.getBlock((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.8f * normal.z));
                blocks[2] = chunkView.getBlock((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.8f * normal.z));
                blocks[3] = chunkView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.1f), (vertexPos.z + 0.8f * normal.z));
                break;
            default:
                blocks[0] = chunkView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z + 0.1f));
                blocks[1] = chunkView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z - 0.1f));
                blocks[2] = chunkView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z - 0.1f));
                blocks[3] = chunkView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z + 0.1f));
        }

        lights[0] = chunkView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));
        lights[1] = chunkView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        lights[2] = chunkView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        lights[3] = chunkView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));

        lights[4] = chunkView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
        lights[5] = chunkView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        lights[6] = chunkView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        lights[7] = chunkView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));

        blockLights[0] = chunkView.getLight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));
        blockLights[1] = chunkView.getLight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        blockLights[2] = chunkView.getLight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        blockLights[3] = chunkView.getLight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));

        blockLights[4] = chunkView.getLight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
        blockLights[5] = chunkView.getLight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        blockLights[6] = chunkView.getLight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        blockLights[7] = chunkView.getLight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
        PerformanceMonitor.endActivity();

        float resultLight = 0;
        float resultBlockLight = 0;
        int counterLight = 0;
        int counterBlockLight = 0;

        int occCounter = 0;
        int occCounterBillboard = 0;
        for (int i = 0; i < 8; i++) {
            if (lights[i] > 0) {
                resultLight += lights[i];
                counterLight++;
            }
            if (blockLights[i] > 0) {
                resultBlockLight += blockLights[i];
                counterBlockLight++;
            }

            if (i < 4) {
                Block b = blocks[i];

                if (b.isShadowCasting() && !b.isTranslucent()) {
                    occCounter++;
                } else if (b.isShadowCasting()) {
                    occCounterBillboard++;
                }
            }
        }

        double resultAmbientOcclusion = (TeraMath.pow(0.40, occCounter) + TeraMath.pow(0.80, occCounterBillboard)) / 2.0;

        if (counterLight == 0) {
            output[0] = 0;
        } else {
            output[0] = resultLight / counterLight / 15f;
        }

        if (counterBlockLight == 0) {
            output[1] = 0;
        } else {
            output[1] = resultBlockLight / counterBlockLight / 15f;
        }

        output[2] = (float) resultAmbientOcclusion;
        PerformanceMonitor.endActivity();
    }

    public static int getVertexArrayUpdateCount() {
        return statVertexArrayUpdateCount;
    }
}
