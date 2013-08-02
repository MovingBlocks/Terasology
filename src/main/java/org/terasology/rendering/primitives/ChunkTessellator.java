/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import gnu.trove.iterator.TIntIterator;
import org.lwjgl.BufferUtils;
import org.terasology.math.Direction;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.RenderMath;
import org.terasology.world.ChunkView;
import org.terasology.world.MiniatureChunk;
import org.terasology.world.RegionalChunkView;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.chunks.Chunk;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkTessellator {

    private static int statVertexArrayUpdateCount = 0;

    private WorldBiomeProvider biomeProvider;

    public ChunkTessellator(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    public ChunkMesh generateMesh(ChunkView chunkView, Vector3i chunkPos, int meshHeight, int verticalOffset) {
        PerformanceMonitor.startActivity("GenerateMesh");
        ChunkMesh mesh = new ChunkMesh();

        Vector3i chunkOffset = new Vector3i(chunkPos.x * Chunk.SIZE_X, chunkPos.y * Chunk.SIZE_Y, chunkPos.z * Chunk.SIZE_Z);
        final Stopwatch watch = new Stopwatch();
        watch.start();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                float biomeTemp = biomeProvider.getTemperatureAt(chunkOffset.x + x, chunkOffset.z + z);
                float biomeHumidity = biomeProvider.getHumidityAt(chunkOffset.x + x, chunkOffset.z + z);

                for (int y = verticalOffset; y < verticalOffset + meshHeight; y++) {
                    Block block = chunkView.getBlock(x, y, z);

                    if (block != null && !block.isInvisible()) {
                        generateBlockVertices(chunkView, mesh, x, y, z, biomeTemp, biomeHumidity);
                    }
                }
            }
        }
        watch.stop();

        mesh.setTimeToGenerateBlockVertices((int) watch.elapsedMillis());

        watch.reset().start();
        generateOptimizedBuffers(chunkView, mesh);
        watch.stop();
        mesh.setTimeToGenerateOptimizedBuffers((int) watch.elapsedMillis());
        statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    public ChunkMesh generateMinaturizedMesh(MiniatureChunk miniatureChunk) {
        PerformanceMonitor.startActivity("GenerateMinuatureMesh");
        ChunkMesh mesh = new ChunkMesh();

        MiniatureChunk[] chunks = {miniatureChunk};
        RegionalChunkView localChunkView = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(Vector3i.zero(), Vector3i.zero()), Vector3i.zero());
        localChunkView.setChunkSize(new Vector3i(MiniatureChunk.CHUNK_SIZE));

        for (int x = 0; x < MiniatureChunk.SIZE_X; x++) {
            for (int z = 0; z < MiniatureChunk.SIZE_Z; z++) {
                for (int y = 0; y < MiniatureChunk.SIZE_Y; y++) {
                    Block block = miniatureChunk.getBlock(x, y, z);

                    if (block != null && !block.isInvisible()) {
                        generateBlockVertices(localChunkView, mesh, x, y, z, 0.0f, 0.0f);
                    }
                }
            }
        }

        generateOptimizedBuffers(localChunkView, mesh);
        statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    private void generateOptimizedBuffers(ChunkView chunkView, ChunkMesh mesh) {
        PerformanceMonitor.startActivity("OptimizeBuffers");

        for (int j = 0; j < mesh.vertexElements.length; j++) {
            // Vertices double to account for light info
            mesh.vertexElements[j].finalVertices = BufferUtils.createByteBuffer(
                    mesh.vertexElements[j].vertices.size() * 4 + /* POSITION */
                            mesh.vertexElements[j].tex.size() * 4 + /* TEX0 (UV0 and flags) */
                            mesh.vertexElements[j].tex.size() * 4 + /* TEX1 (lighting data) */
                            mesh.vertexElements[j].flags.size() * 4 + /* FLAGS */
                            mesh.vertexElements[j].color.size() * 4 + /* COLOR */
                            mesh.vertexElements[j].normals.size() * 4 /* NORMALS */
            );

            int cTex = 0;
            int cColor = 0;
            int cFlags = 0;
            for (int i = 0; i < mesh.vertexElements[j].vertices.size(); i += 3, cTex += 2, cColor += 4, cFlags++) {
                Vector3f vertexPos = new Vector3f(mesh.vertexElements[j].vertices.get(i), mesh.vertexElements[j].vertices.get(i + 1), mesh.vertexElements[j].vertices.get(i + 2));

                /* POSITION */
                mesh.vertexElements[j].finalVertices.putFloat(vertexPos.x);
                mesh.vertexElements[j].finalVertices.putFloat(vertexPos.y);
                mesh.vertexElements[j].finalVertices.putFloat(vertexPos.z);

                /* UV0 - TEX DATA 0 */
                mesh.vertexElements[j].finalVertices.putFloat(mesh.vertexElements[j].tex.get(cTex));
                mesh.vertexElements[j].finalVertices.putFloat(mesh.vertexElements[j].tex.get(cTex + 1));

                /* FLAGS */
                mesh.vertexElements[j].finalVertices.putFloat(mesh.vertexElements[j].flags.get(cFlags));

                float[] result = new float[3];
                Vector3f normal = new Vector3f(mesh.vertexElements[j].normals.get(i), mesh.vertexElements[j].normals.get(i + 1), mesh.vertexElements[j].normals.get(i + 2));
                calcLightingValuesForVertexPos(chunkView, vertexPos, result, normal);

                /* LIGHTING DATA / TEX DATA 1 */
                mesh.vertexElements[j].finalVertices.putFloat(result[0]);
                mesh.vertexElements[j].finalVertices.putFloat(result[1]);
                mesh.vertexElements[j].finalVertices.putFloat(result[2]);

                /* PACKED COLOR */
                final int packedColor = RenderMath.packColor(
                        mesh.vertexElements[j].color.get(cColor),
                        mesh.vertexElements[j].color.get(cColor + 1),
                        mesh.vertexElements[j].color.get(cColor + 2),
                        mesh.vertexElements[j].color.get(cColor + 3));
                mesh.vertexElements[j].finalVertices.putInt(packedColor);

                /* NORMALS */
                mesh.vertexElements[j].finalVertices.putFloat(normal.x);
                mesh.vertexElements[j].finalVertices.putFloat(normal.y);
                mesh.vertexElements[j].finalVertices.putFloat(normal.z);
            }

            mesh.vertexElements[j].finalIndices = BufferUtils.createIntBuffer(mesh.vertexElements[j].indices.size());
            TIntIterator indexIterator = mesh.vertexElements[j].indices.iterator();
            while (indexIterator.hasNext()) {
                mesh.vertexElements[j].finalIndices.put(indexIterator.next());
            }

            mesh.vertexElements[j].finalVertices.flip();
            mesh.vertexElements[j].finalIndices.flip();
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

        double resultAmbientOcclusion = (Math.pow(0.40, occCounter) + Math.pow(0.80, occCounterBillboard)) / 2.0;

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

    private void generateBlockVertices(ChunkView view, ChunkMesh mesh, int x, int y, int z, float temp, float hum) {
        Block block = view.getBlock(x, y, z);

        // TODO: Needs review - too much hardcoded special cases and corner cases resulting from this.
        ChunkVertexFlag vertexFlag = ChunkVertexFlag.NORMAL;
        if (block.isWater()) {
            vertexFlag = ChunkVertexFlag.WATER;
        } else if (block.isLava()) {
            vertexFlag = ChunkVertexFlag.LAVA;
        } else if (block.isWaving() && block.isDoubleSided()) {
            vertexFlag = ChunkVertexFlag.WAVING;
        } else if (block.isWaving() && !block.isDoubleSided()) {
            vertexFlag = ChunkVertexFlag.WAVING_BLOCK;
        }

        // Gather adjacent blocks
        Map<Side, Block> adjacentBlocks = Maps.newEnumMap(Side.class);
        for (Side side : Side.values()) {
            Vector3i offset = side.getVector3i();
            Block blockToCheck = view.getBlock(x + offset.x, y + offset.y, z + offset.z);
            adjacentBlocks.put(side, blockToCheck);
        }

        BlockAppearance blockAppearance = block.getAppearance(adjacentBlocks);

        /*
         * Determine the render process.
         */
        ChunkMesh.RenderType renderType = ChunkMesh.RenderType.TRANSLUCENT;

        if (!block.isTranslucent()) {
            renderType = ChunkMesh.RenderType.OPAQUE;
        }
        // TODO: Review special case, or alternatively compare uris.
        if (block.isWater() || block.getURI().toString().equals("engine:ice")) {
            renderType = ChunkMesh.RenderType.WATER_AND_ICE;
        }
        if (block.isDoubleSided()) {
            renderType = ChunkMesh.RenderType.BILLBOARD;
        }

        if (blockAppearance.getPart(BlockPart.CENTER) != null) {
            Vector4f colorOffset = block.calcColorOffsetFor(BlockPart.CENTER, temp, hum);
            blockAppearance.getPart(BlockPart.CENTER).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), vertexFlag);
        }

        boolean[] drawDir = new boolean[6];

        for (Side side : Side.values()) {
            drawDir[side.ordinal()] = blockAppearance.getPart(BlockPart.fromSide(side)) != null && isSideVisibleForBlockTypes(adjacentBlocks.get(side), block, side);
        }

        if (y == 0) {
            drawDir[Side.BOTTOM.ordinal()] = false;
        }

        // If the block is lowered, some more faces may have to be drawn
        if (block.isLiquid()) {
            Block bottomBlock = adjacentBlocks.get(Side.BOTTOM);
            // Draw horizontal sides if visible from below
            for (Side side : Side.horizontalSides()) {
                Vector3i offset = side.getVector3i();
                Block adjacentBelow = view.getBlock(x + offset.x, y - 1, z + offset.z);
                Block adjacent = adjacentBlocks.get(side);

                drawDir[side.ordinal()] |= (blockAppearance.getPart(BlockPart.fromSide(side)) != null && isSideVisibleForBlockTypes(adjacentBelow, block, side) && !isSideVisibleForBlockTypes(bottomBlock, adjacent, side.reverse()));
            }

            // Draw the top if below a non-lowered block
            // TODO: Don't need to render the top if each side and the block above each side are either liquid or opaque solids.
            Block blockToCheck = adjacentBlocks.get(Side.TOP);
            drawDir[Side.TOP.ordinal()] |= !blockToCheck.isLiquid();

            if (bottomBlock.isLiquid() || bottomBlock.isInvisible()) {
                for (Side dir : Side.values()) {
                    if (drawDir[dir.ordinal()]) {
                        Vector4f colorOffset = block.calcColorOffsetFor(BlockPart.fromSide(dir), temp, hum);
                        block.getLoweredLiquidMesh(dir).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), vertexFlag);
                    }
                }
                return;
            }
        }

        for (Side dir : Side.values()) {
            if (drawDir[dir.ordinal()]) {
                Vector4f colorOffset = block.calcColorOffsetFor(BlockPart.fromSide(dir), temp, hum);
                // TODO: Needs review since the new per-vertex flags introduce a lot of special scenarios - probably a per-side setting?
                if (block.getURI().toString().equals("engine:grass") && dir != Side.TOP && dir != Side.BOTTOM) {
                    blockAppearance.getPart(BlockPart.fromSide(dir)).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), ChunkVertexFlag.COLOR_MASK);
                } else {
                    blockAppearance.getPart(BlockPart.fromSide(dir)).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), vertexFlag);
                }
            }
        }
    }

    /**
     * Returns true if the side should be rendered adjacent to the second side provided.
     *
     * @param blockToCheck The block to check
     * @param currentBlock The current block
     * @return True if the side is visible for the given block types
     */
    private boolean isSideVisibleForBlockTypes(Block blockToCheck, Block currentBlock, Side side) {
        // Liquids can be transparent but there should be no visible adjacent faces
        if (currentBlock.isLiquid() && blockToCheck.isLiquid()) {
            return false;
        }

        if (currentBlock.isWaving() != blockToCheck.isWaving()) {
            return true;
        }

        return blockToCheck.isInvisible() ||
                !blockToCheck.isFullSide(side.reverse()) ||
                (!currentBlock.isTranslucent() && blockToCheck.isTranslucent());
    }

    public static int getVertexArrayUpdateCount() {
        return statVertexArrayUpdateCount;
    }
}
