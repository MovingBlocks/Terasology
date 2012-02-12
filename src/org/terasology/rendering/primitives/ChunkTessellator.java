/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import org.lwjgl.BufferUtils;
import org.terasology.logic.world.Chunk;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
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

    private final Chunk _chunk;
    private static int _statVertexArrayUpdateCount = 0;

    public ChunkTessellator(Chunk chunk) {
        _chunk = chunk;
    }

    public ChunkMesh generateMesh(int meshHeight, int verticalOffset) {
        PerformanceMonitor.startActivity("GenerateMesh");
        ChunkMesh mesh = new ChunkMesh();

        for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z++) {
                double biomeTemp = _chunk.getParent().getTemperatureAt(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosZ(z));
                double biomeHumidity = _chunk.getParent().getHumidityAt(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosZ(z));

                for (int y = verticalOffset; y < verticalOffset + meshHeight; y++) {
                    byte blockType = _chunk.getBlock(x, y, z);
                    Block block = BlockManager.getInstance().getBlock(blockType);

                    if (block.isInvisible())
                        continue;

                    generateBlockVertices(mesh, x, y, z, biomeTemp, biomeHumidity);
                }
            }
        }

        generateOptimizedBuffers(mesh);
        _statVertexArrayUpdateCount++;

        PerformanceMonitor.endActivity();
        return mesh;
    }

    private void generateOptimizedBuffers(ChunkMesh mesh) {
        PerformanceMonitor.startActivity("OptimizeBuffers");

        generateBulletBuffers(mesh);

        for (int j = 0; j < mesh._vertexElements.length; j++) {
            // Vertices double to account for light info
            mesh._vertexElements[j].finalVertices = BufferUtils.createFloatBuffer(mesh._vertexElements[j].vertices.size() * 2 + mesh._vertexElements[j].tex.size() + mesh._vertexElements[j].color.size() + mesh._vertexElements[j].normals.size());

            int cTex = 0;
            int cColor = 0;
            for (int i = 0; i < mesh._vertexElements[j].vertices.size(); i += 3, cTex += 3, cColor += 4) {

                Vector3f vertexPos = new Vector3f(mesh._vertexElements[j].vertices.get(i), mesh._vertexElements[j].vertices.get(i + 1), mesh._vertexElements[j].vertices.get(i + 2));

                mesh._vertexElements[j].finalVertices.put(vertexPos.x);
                mesh._vertexElements[j].finalVertices.put(vertexPos.y);
                mesh._vertexElements[j].finalVertices.put(vertexPos.z);

                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].tex.get(cTex));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].tex.get(cTex + 1));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].tex.get(cTex + 2));

                Double[] result = new Double[3];
                calcLightingValuesForVertexPos(vertexPos, result);

                mesh._vertexElements[j].finalVertices.put(result[0].floatValue());
                mesh._vertexElements[j].finalVertices.put(result[1].floatValue());
                mesh._vertexElements[j].finalVertices.put(result[2].floatValue());

                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].color.get(cColor));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].color.get(cColor + 1));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].color.get(cColor + 2));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].color.get(cColor + 3));

                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].normals.get(i));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].normals.get(i + 1));
                mesh._vertexElements[j].finalVertices.put(mesh._vertexElements[j].normals.get(i + 2));
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

    private void calcLightingValuesForVertexPos(Vector3f vertexPos, Double[] output) {
        PerformanceMonitor.startActivity("calcLighting");
        double[] lights = new double[8];
        double[] blockLights = new double[8];
        byte[] blocks = new byte[4];

        Vector3f vertexWorldPos = moveVectorFromChunkSpaceToWorldSpace(vertexPos);

        PerformanceMonitor.startActivity("gatherLightInfo");
        blocks[0] = _chunk.getParent().getBlockAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z + 0.1f));
        blocks[1] = _chunk.getParent().getBlockAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z - 0.1f));
        blocks[2] = _chunk.getParent().getBlockAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z - 0.1f));
        blocks[3] = _chunk.getParent().getBlockAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z + 0.1f));

        lights[0] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.SUN);
        lights[1] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.SUN);
        lights[2] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.SUN);
        lights[3] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.SUN);

        lights[4] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.SUN);
        lights[5] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.SUN);
        lights[6] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.SUN);
        lights[7] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.SUN);

        blockLights[0] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[1] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[2] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[3] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y + 0.8f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.BLOCK);

        blockLights[4] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[5] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x + 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[6] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z - 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[7] = _chunk.getParent().getLightAtPosition((vertexWorldPos.x - 0.1f), (vertexWorldPos.y - 0.1f), (vertexWorldPos.z + 0.1f), Chunk.LIGHT_TYPE.BLOCK);
        PerformanceMonitor.endActivity();

        double resultLight = 0;
        double resultBlockLight = 0;
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
                Block b = BlockManager.getInstance().getBlock(blocks[i]);

                if (b.isCastsShadows() && b.getBlockForm() != Block.BLOCK_FORM.BILLBOARD) {
                    occCounter++;
                } else if (b.isCastsShadows() && b.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                    occCounterBillboard++;
                }
            }
        }

        double resultAmbientOcclusion = (Math.pow(0.70, occCounter) + Math.pow(0.92, occCounterBillboard)) / 2.0;

        if (counterLight == 0)
            output[0] = (double) 0;
        else
            output[0] = resultLight / counterLight / 15f;

        if (counterBlockLight == 0)
            output[1] = (double) 0;
        else
            output[1] = resultBlockLight / counterBlockLight / 15f;

        output[2] = resultAmbientOcclusion;
        PerformanceMonitor.endActivity();
    }

    private void generateBlockVertices(ChunkMesh mesh, int x, int y, int z, double temp, double hum) {
        byte blockId = _chunk.getBlock(x, y, z);
        Block block = BlockManager.getInstance().getBlock(blockId);

        /*
         * Determine the render process.
         */
        ChunkMesh.RENDER_TYPE renderType = ChunkMesh.RENDER_TYPE.TRANSLUCENT;

        if (!block.isTranslucent())
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
            byte blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + offset.x), y + offset.y, _chunk.getBlockWorldPosZ(z + offset.z));
            drawDir[side.ordinal()] = isSideVisibleForBlockTypes(blockToCheckId, blockId, side);
        }

        if (y == 0) {
            drawDir[Side.BOTTOM.ordinal()] = false;
        }

        // If the block is lowered, some more faces may have to be drawn
        if (blockForm == Block.BLOCK_FORM.LOWERED_BLOCK) {
            // Draw horizontal sides if visible from below
            for (Side side : Side.horizontalSides()) {
                Vector3i offset = side.getVector3i();
                byte blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + offset.x), y - 1, _chunk.getBlockWorldPosZ(z + offset.z));
                drawDir[side.ordinal()] |= isSideVisibleForBlockTypes(blockToCheckId, blockId, side);
            }

            // Draw the top if below a non-lowered block
            // TODO: Don't need to render the top if each side and the block above each side are either liquid or opaque solids.
            byte blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y + 1, _chunk.getBlockWorldPosZ(z));
            drawDir[Side.TOP.ordinal()] |= (BlockManager.getInstance().getBlock(blockToCheckId).getBlockForm() != Block.BLOCK_FORM.LOWERED_BLOCK);

            byte bottomBlock = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y - 1, _chunk.getBlockWorldPosZ(z));
            if (BlockManager.getInstance().getBlock(bottomBlock).getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK || bottomBlock == 0x0) {
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

    private Vector3f moveVectorFromChunkSpaceToWorldSpace(Vector3f offset) {
        double offsetX = _chunk.getPosition().x * Chunk.CHUNK_DIMENSION_X;
        double offsetY = _chunk.getPosition().y * Chunk.CHUNK_DIMENSION_Y;
        double offsetZ = _chunk.getPosition().z * Chunk.CHUNK_DIMENSION_Z;

        offset.x += offsetX;
        offset.y += offsetY;
        offset.z += offsetZ;

        return offset;
    }

    /**
     * Returns true if the side should be rendered adjacent to the second side provided.
     *
     * @param blockToCheck The block to check
     * @param currentBlock The current block
     * @return True if the side is visible for the given block types
     */
    private boolean isSideVisibleForBlockTypes(byte blockToCheck, byte currentBlock, Side side) {
        Block cBlock = BlockManager.getInstance().getBlock(currentBlock);
        if (cBlock.getSideMesh(side) == null) return false;
        Block bCheck = BlockManager.getInstance().getBlock(blockToCheck);
        return bCheck.getId() == 0x0 || !bCheck.isBlockingSide(side.reverse()) || !cBlock.isTranslucent() && bCheck.isTranslucent() || (bCheck.getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK && cBlock.getBlockForm() != Block.BLOCK_FORM.LOWERED_BLOCK);
    }

    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
    }

}
