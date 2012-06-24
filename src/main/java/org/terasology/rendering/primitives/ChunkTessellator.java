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

import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.ScalarType;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TShortIterator;
import org.lwjgl.BufferUtils;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.WorldBiomeProvider;
import org.terasology.logic.world.WorldView;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.performanceMonitor.PerformanceMonitor;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkTessellator {

    private static final int FLOAT_BYTES = 4;
    private static final int INT_BYTES = 4;
    private static int _statVertexArrayUpdateCount = 0;

    private WorldBiomeProvider biomeProvider;

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

    private void generateOptimizedBuffers(WorldView worldView, ChunkMesh mesh) {
        PerformanceMonitor.startActivity("OptimizeBuffers");

        generateBulletBuffers(mesh);

        for (int j = 0; j < mesh._vertexElements.length; j++) {
            // Vertices double to account for light info
            mesh._vertexElements[j].finalVertices = BufferUtils.createByteBuffer(mesh._vertexElements[j].vertices.size() * 2 * 4 + mesh._vertexElements[j].tex.size() * 4 + mesh._vertexElements[j].color.size() * 4 + mesh._vertexElements[j].normals.size() * 4);

            int cTex = 0;
            int cColor = 0;
            for (int i = 0; i < mesh._vertexElements[j].vertices.size(); i += 3, cTex += 3, cColor += 4) {

                Vector3f vertexPos = new Vector3f(mesh._vertexElements[j].vertices.get(i), mesh._vertexElements[j].vertices.get(i + 1), mesh._vertexElements[j].vertices.get(i + 2));

                mesh._vertexElements[j].finalVertices.putFloat(vertexPos.x);
                mesh._vertexElements[j].finalVertices.putFloat(vertexPos.y);
                mesh._vertexElements[j].finalVertices.putFloat(vertexPos.z);

                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].tex.get(cTex));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].tex.get(cTex + 1));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].tex.get(cTex + 2));

                float[] result = new float[3];
                calcLightingValuesForVertexPos(worldView, vertexPos, result);

                mesh._vertexElements[j].finalVertices.putFloat(result[0]);
                mesh._vertexElements[j].finalVertices.putFloat(result[1]);
                mesh._vertexElements[j].finalVertices.putFloat(result[2]);

                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor + 1));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor + 2));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].color.get(cColor + 3));

                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].normals.get(i));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].normals.get(i + 1));
                mesh._vertexElements[j].finalVertices.putFloat(mesh._vertexElements[j].normals.get(i + 2));
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

    private void generateBulletBuffers(ChunkMesh mesh) {
        mesh._indexedMesh = new IndexedMesh();
        mesh._indexedMesh.vertexBase = BufferUtils.createByteBuffer(mesh._vertexElements[0].vertices.size() * FLOAT_BYTES);
        mesh._indexedMesh.triangleIndexBase = BufferUtils.createByteBuffer(mesh._vertexElements[0].indices.size() * INT_BYTES);
        mesh._indexedMesh.triangleIndexStride = 3 * INT_BYTES;
        mesh._indexedMesh.vertexStride = 3 * FLOAT_BYTES;
        mesh._indexedMesh.numVertices = mesh._vertexElements[0].vertices.size() / 3;
        mesh._indexedMesh.numTriangles = mesh._vertexElements[0].indices.size() / 3;
        mesh._indexedMesh.indexType = ScalarType.INTEGER;

        TIntIterator indexIterator = mesh._vertexElements[0].indices.iterator();
        while (indexIterator.hasNext()) {
            mesh._indexedMesh.triangleIndexBase.putInt(indexIterator.next());
        }
        TFloatIterator vertIterator = mesh._vertexElements[0].vertices.iterator();
        while (vertIterator.hasNext()) {
            mesh._indexedMesh.vertexBase.putFloat(vertIterator.next());
        }
    }

    private void calcLightingValuesForVertexPos(WorldView worldView, Vector3f vertexPos, float[] output) {
        PerformanceMonitor.startActivity("calcLighting");
        float[] lights = new float[8];
        float[] blockLights = new float[8];
        Block[] blocks = new Block[4];

        PerformanceMonitor.startActivity("gatherLightInfo");
        blocks[0] = worldView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));
        blocks[1] = worldView.getBlock((vertexPos.x + 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        blocks[2] = worldView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z - 0.1f));
        blocks[3] = worldView.getBlock((vertexPos.x - 0.1f), (vertexPos.y + 0.8f), (vertexPos.z + 0.1f));

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

                if (b.isCastsShadows() && b.getBlockForm() != Block.BLOCK_FORM.BILLBOARD) {
                    occCounter++;
                } else if (b.isCastsShadows() && b.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                    occCounterBillboard++;
                }
            }
        }

        double resultAmbientOcclusion = (Math.pow(0.60, occCounter) + Math.pow(0.86, occCounterBillboard)) / 2.0;

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

    private void generateBlockVertices(WorldView view, ChunkMesh mesh, int x, int y, int z, double temp, double hum) {
        Block block = view.getBlock(x, y, z);

        /*
         * Determine the render process.
         */
        ChunkMesh.RENDER_TYPE renderType = ChunkMesh.RENDER_TYPE.TRANSLUCENT;

        if (!block.isTransparent())
            renderType = ChunkMesh.RENDER_TYPE.OPAQUE;
        if (block.getTitle().equals("Water") || block.getTitle().equals("Ice"))
            renderType = ChunkMesh.RENDER_TYPE.WATER_AND_ICE;
        if (block.getBlockForm() == Block.BLOCK_FORM.BILLBOARD)
            renderType = ChunkMesh.RENDER_TYPE.BILLBOARD;

        Block.BLOCK_FORM blockForm = block.getBlockForm();

        if (block.getCenterMesh() != null) {
            Vector4f colorOffset = block.calcColorOffsetFor(Side.TOP, temp, hum);
            block.getCenterMesh().appendTo(mesh, x, y, z, colorOffset, renderType.getIndex());
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
        if (blockForm == Block.BLOCK_FORM.LOWERED_BLOCK) {
            // Draw horizontal sides if visible from below
            for (Side side : Side.horizontalSides()) {
                Vector3i offset = side.getVector3i();
                Block blockToCheck = view.getBlock(x + offset.x, y - 1, z + offset.z);
                drawDir[side.ordinal()] |= isSideVisibleForBlockTypes(blockToCheck, block, side);
            }

            // Draw the top if below a non-lowered block
            // TODO: Don't need to render the top if each side and the block above each side are either liquid or opaque solids.
            Block blockToCheck = view.getBlock(x, y + 1, z);
            drawDir[Side.TOP.ordinal()] |= blockToCheck.getBlockForm() != Block.BLOCK_FORM.LOWERED_BLOCK;

            Block bottomBlock = view.getBlock(x, y - 1, z);
            if (bottomBlock.getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK || bottomBlock.getId() == 0x0) {
                for (Side dir : Side.values()) {
                    if (drawDir[dir.ordinal()]) {
                        Vector4f colorOffset = block.calcColorOffsetFor(dir, temp, hum);
                        block.getLoweredSideMesh(dir).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex());
                    }
                }
                return;
            }
        }

        for (Side dir : Side.values()) {
            if (drawDir[dir.ordinal()]) {
                Vector4f colorOffset = block.calcColorOffsetFor(dir, temp, hum);
                block.getSideMesh(dir).appendTo(mesh, x, y, z, colorOffset, renderType.getIndex());
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
        if (currentBlock.getSideMesh(side) == null) return false;

        // Liquids can be transparent but there should be no visible adjacent faces
        // !!! In comparison to leaves !!!
        if (currentBlock.isLiquid() && blockToCheck.isLiquid()) return false;

        return blockToCheck.getId() == 0x0 ||
                !blockToCheck.isBlockingSide(side.reverse()) ||
                (!currentBlock.isTranslucent() && blockToCheck.isTranslucent());
    }

    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
    }
}
