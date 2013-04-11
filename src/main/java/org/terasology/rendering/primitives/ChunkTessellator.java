/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import gnu.trove.iterator.TIntIterator;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.terasology.math.Direction;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.world.MiniatureChunk;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.chunks.Chunk;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkTessellator {

    private static int _statVertexArrayUpdateCount = 0;

    private WorldBiomeProvider biomeProvider;

    public enum ChunkVertexFlags {
        BLOCK_HINT_WATER(1),
        BLOCK_HINT_LAVA(2),
        BLOCK_HINT_GRASS(3),
        BLOCK_HINT_WAVING(4),
        BLOCK_HINT_WAVING_BLOCK(5);

        private int value;
        private ChunkVertexFlags(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public ChunkTessellator(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    public ChunkMesh generateMesh(WorldView worldView, Vector3i chunkPos, int meshHeight, int verticalOffset) {
        PerformanceMonitor.startActivity("GenerateMesh");
        ChunkMesh mesh = new ChunkMesh();

        Vector3i chunkOffset = new Vector3i(chunkPos.x * Chunk.SIZE_X, chunkPos.y * Chunk.SIZE_Y, chunkPos.z * Chunk.SIZE_Z);

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                float biomeTemp = biomeProvider.getTemperatureAt(chunkOffset.x + x, chunkOffset.z + z);
                float biomeHumidity = biomeProvider.getHumidityAt(chunkOffset.x + x, chunkOffset.z + z);

                for (int y = verticalOffset; y < verticalOffset + meshHeight; y++) {
                    Block block = worldView.getBlock(x, y, z);

                    if (block == null || block.isInvisible())
                        continue;

                    generateBlockVertices(worldView, mesh, x, y, z, biomeTemp, biomeHumidity);
                }
            }
        }

        generateOptimizedBuffers(worldView, mesh);
        _statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    public ChunkMesh generateMinaturizedMesh(MiniatureChunk miniatureChunk) {
        PerformanceMonitor.startActivity("GenerateMinuatureMesh");
        ChunkMesh mesh = new ChunkMesh();

        MiniatureChunk[] chunks = { miniatureChunk };
        WorldView localWorldView = new WorldView(chunks, Region3i.createFromCenterExtents(Vector3i.zero(), Vector3i.zero()), Vector3i.zero());
        localWorldView.setChunkSize(new Vector3i(MiniatureChunk.CHUNK_SIZE));

        for (int x = 0; x < MiniatureChunk.SIZE_X; x++) {
            for (int z = 0; z < MiniatureChunk.SIZE_Z; z++) {
                for (int y = 0; y < MiniatureChunk.SIZE_Y; y++) {
                    Block block = miniatureChunk.getBlock(x,y,z);

                    if (block == null || block.isInvisible())
                        continue;

                    generateBlockVertices(localWorldView, mesh, x, y, z, 0.0f, 0.0f);
                }
            }
        }

        generateOptimizedBuffers(localWorldView, mesh);
        _statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    private void generateOptimizedBuffers(WorldView worldView, ChunkMesh mesh) {
        PerformanceMonitor.startActivity("OptimizeBuffers");

        for (int j = 0; j < mesh._vertexElements.length; j++) {
            // Vertices double to account for light info
            mesh._vertexElements[j].finalVertices = BufferUtils.createByteBuffer(mesh._vertexElements[j].vertices.size() * 2 * 4 + mesh._vertexElements[j].tex.size() * 4 + mesh._vertexElements[j].flags.size() * 4 + mesh._vertexElements[j].color.size() * 4 + mesh._vertexElements[j].normals.size() * 4);

            int cTex = 0;
            int cColor = 0;
            int cFlags = 0;
            for (int i = 0; i < mesh._vertexElements[j].vertices.size(); i += 3, cTex += 2, cColor += 4, cFlags++) {

                Vector3f vertexPos = new Vector3f(mesh._vertexElements[j].vertices.get(i), mesh._vertexElements[j].vertices.get(i + 1), mesh._vertexElements[j].vertices.get(i + 2));

                mesh._vertexElements[j].finalVertices.putFloat(vertexPos.x);
                mesh._vertexElements[j].finalVertices.putFloat(vertexPos.y);
                mesh._vertexElements[j].finalVertices.putFloat(vertexPos.z);

                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].tex.get(cTex));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].tex.get(cTex + 1));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].flags.get(cFlags));

                float[] result = new float[3];
                Vector3f normal = new Vector3f(mesh._vertexElements[j].normals.get(i), mesh._vertexElements[j].normals.get(i+1), mesh._vertexElements[j].normals.get(i+2));
                calcLightingValuesForVertexPos(worldView, vertexPos, result, normal);

                mesh._vertexElements[j].finalVertices.putFloat(result[0]);
                mesh._vertexElements[j].finalVertices.putFloat(result[1]);
                mesh._vertexElements[j].finalVertices.putFloat(result[2]);

                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor + 1));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor + 2));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor + 3));

                mesh._vertexElements[j].finalVertices.putFloat(normal.x);
                mesh._vertexElements[j].finalVertices.putFloat(normal.y);
                mesh._vertexElements[j].finalVertices.putFloat(normal.z);
            }

            mesh._vertexElements[j].finalIndices = BufferUtils.createIntBuffer(mesh._vertexElements[j].indices.size());
            TIntIterator indexIterator = mesh._vertexElements[j].indices.iterator();
            while (indexIterator.hasNext()) {
                mesh._vertexElements[j].finalIndices.put(indexIterator.next());
            }

            mesh._vertexElements[j].finalVertices.flip();
            mesh._vertexElements[j].finalIndices.flip();
        }
        PerformanceMonitor.endActivity();
    }

    private void calcLightingValuesForVertexPos(WorldView worldView, Vector3f vertexPos, float[] output, Vector3f normal) {
        PerformanceMonitor.startActivity("calcLighting");
        float[] lights = new float[8];
        float[] blockLights = new float[8];
        Block[] blocks = new Block[4];

        PerformanceMonitor.startActivity("gatherLightInfo");
        Direction dir = Direction.inDirection(normal);
        switch (dir) {
            case LEFT:
            case RIGHT:
                blocks[0] = worldView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y + 0.1f), (vertexPos.z + 0.1f));
                blocks[1] = worldView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y + 0.1f), (vertexPos.z - 0.1f));
                blocks[2] = worldView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
                blocks[3] = worldView.getBlock((vertexPos.x + 0.8f * normal.x), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
                break;
            case FORWARD:
            case BACKWARD:
                blocks[0] = worldView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.1f), (vertexPos.z + 0.8f * normal.z));
                blocks[1] = worldView.getBlock((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.8f * normal.z));
                blocks[2] = worldView.getBlock((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.8f * normal.z));
                blocks[3] = worldView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.1f), (vertexPos.z + 0.8f * normal.z));
                break;
            default:
                blocks[0] = worldView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z + 0.1f));
                blocks[1] = worldView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z - 0.1f));
                blocks[2] = worldView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z - 0.1f));
                blocks[3] = worldView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.8f * normal.y), (vertexPos.z + 0.1f));
                break;
        }

        lights[0] = worldView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));
        lights[1] = worldView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        lights[2] = worldView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        lights[3] = worldView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));

        lights[4] = worldView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
        lights[5] = worldView.getSunlight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        lights[6] = worldView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        lights[7] = worldView.getSunlight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));

        blockLights[0] = worldView.getLight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));
        blockLights[1] = worldView.getLight((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        blockLights[2] = worldView.getLight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        blockLights[3] = worldView.getLight((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));

        blockLights[4] = worldView.getLight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
        blockLights[5] = worldView.getLight((vertexPos.x + 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        blockLights[6] = worldView.getLight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z - 0.1f));
        blockLights[7] = worldView.getLight((vertexPos.x - 0.1f), (vertexPos.y - 0.1f), (vertexPos.z + 0.1f));
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

        if (counterLight == 0)
            output[0] = 0;
        else
            output[0] = resultLight / counterLight / 15f;

        if (counterBlockLight == 0)
            output[1] = 0;
        else
            output[1] = resultBlockLight / counterBlockLight / 15f;

        output[2] = (float) resultAmbientOcclusion;
        PerformanceMonitor.endActivity();
    }

    private void generateBlockVertices(WorldView view, ChunkMesh mesh, int x, int y, int z, float temp, float hum) {
        Block block = view.getBlock(x, y, z);
        int vertexFlags = 0;

        // TODO: Needs review since the new per-vertex flags introduce a lot of special scenarios
        if (block.getURI().toString().equals("engine:water")) {
            vertexFlags = ChunkVertexFlags.BLOCK_HINT_WATER.getValue();
        } else if (block.getURI().toString().equals("engine:lava")) {
            vertexFlags = ChunkVertexFlags.BLOCK_HINT_LAVA.getValue();
        } else if (block.isWaving() && block.isDoubleSided()) {
            vertexFlags = ChunkVertexFlags.BLOCK_HINT_WAVING.getValue();
        } else if (block.isWaving() && !block.isDoubleSided()) {
            vertexFlags = ChunkVertexFlags.BLOCK_HINT_WAVING_BLOCK.getValue();
        }

        /*
         * Determine the render process.
         */
        ChunkMesh.RENDER_TYPE renderType = ChunkMesh.RENDER_TYPE.TRANSLUCENT;

        if (!block.isTranslucent())
            renderType = ChunkMesh.RENDER_TYPE.OPAQUE;
        // TODO: Review special case, or alternatively compare uris.
        if (block.getURI().toString().equals("engine:water") || block.getURI().toString().equals("engine:ice"))
            renderType = ChunkMesh.RENDER_TYPE.WATER_AND_ICE;
        if (block.isDoubleSided())
            renderType = ChunkMesh.RENDER_TYPE.BILLBOARD;

        if (block.getMeshPart(BlockPart.CENTER) != null) {
            Vector4f colorOffset = block.calcColorOffsetFor(BlockPart.CENTER, temp, hum);
            block.getMeshPart(BlockPart.CENTER).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), vertexFlags);
        }

        boolean[] drawDir = new boolean[6];

        for (Side side : Side.values()) {
            Vector3i offset = side.getVector3i();
            Block blockToCheck = view.getBlock(x + offset.x, y + offset.y, z + offset.z);
            drawDir[side.ordinal()] = isSideVisibleForBlockTypes(blockToCheck, block, side);
        }

        if (y == 0) {
            drawDir[Side.BOTTOM.ordinal()] = false;
        }

        // If the block is lowered, some more faces may have to be drawn
        if (block.isLiquid()) {
            // Draw horizontal sides if visible from below
            for (Side side : Side.horizontalSides()) {
                Vector3i offset = side.getVector3i();
                Block adjacentBelow = view.getBlock(x + offset.x, y - 1, z + offset.z);
                Block adjacent = view.getBlock(x + offset.x, y, z + offset.z);
                Block below = view.getBlock(x, y - 1, z);

                drawDir[side.ordinal()] |= (isSideVisibleForBlockTypes(adjacentBelow, block, side) && !isSideVisibleForBlockTypes(below, adjacent, side.reverse()));
            }

            // Draw the top if below a non-lowered block
            // TODO: Don't need to render the top if each side and the block above each side are either liquid or opaque solids.
            Block blockToCheck = view.getBlock(x, y + 1, z);
            drawDir[Side.TOP.ordinal()] |= !blockToCheck.isLiquid();

            Block bottomBlock = view.getBlock(x, y - 1, z);
            if (bottomBlock.isLiquid() || bottomBlock.getId() == 0x0) {
                for (Side dir : Side.values()) {
                    if (drawDir[dir.ordinal()]) {
                        Vector4f colorOffset = block.calcColorOffsetFor(BlockPart.fromSide(dir), temp, hum);
                        block.getLoweredLiquidMesh(dir).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), vertexFlags);
                    }
                }
                return;
            }
        }

        for (Side dir : Side.values()) {
            if (drawDir[dir.ordinal()]) {
                Vector4f colorOffset = block.calcColorOffsetFor(BlockPart.fromSide(dir), temp, hum);

                // TODO: Needs review since the new per-vertex flags introduce a lot of special scenarios
                // Don't mask grass on the top or bottom side...
                if (block.getURI().toString().equals("engine:grass")) {
                    vertexFlags = (dir != Side.TOP && dir != Side.BOTTOM) ? ChunkVertexFlags.BLOCK_HINT_GRASS.getValue() : 0;
                }

                block.getMeshPart(BlockPart.fromSide(dir)).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex(), vertexFlags);
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
        if (currentBlock.getMeshPart(BlockPart.fromSide(side)) == null) return false;

        // Liquids can be transparent but there should be no visible adjacent faces
        if (currentBlock.isLiquid() && blockToCheck.isLiquid()) return false;

        // Draw faces adjacent to animated blocks (which are of different types)
        //if (blockToCheck.isWaving() && !blockToCheck.isDoubleSided() && currentBlock.getId() != blockToCheck.getId()) return true;

        return blockToCheck.getId() == 0x0 ||
                !blockToCheck.isFullSide(side.reverse()) ||
                (!currentBlock.isTranslucent() && blockToCheck.isTranslucent());
    }

    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
    }
}
