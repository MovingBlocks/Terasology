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
import org.lwjgl.BufferUtils;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.logic.world.Chunk;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockManager;
import org.terasology.performanceMonitor.PerformanceMonitor;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.EnumMap;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkTessellator {

    private static final boolean GENERATE_PHYSICS_MESHES = (Boolean) ConfigurationManager.getInstance().getConfig().get("Physics.generatePhysicsMeshes");

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

                    Block.BLOCK_FORM blockForm = block.getBlockForm();

                    if (blockForm != Block.BLOCK_FORM.BILLBOARD)
                        generateBlockVertices(mesh, x, y, z, biomeTemp, biomeHumidity);
                    else
                        generateBillboardVertices(mesh, x, y, z, biomeTemp, biomeHumidity);
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
        mesh._indexedMesh = null;

        if (GENERATE_PHYSICS_MESHES) {
            mesh._indexedMesh = new IndexedMesh();
            mesh._indexedMesh.vertexBase = BufferUtils.createByteBuffer(mesh._vertexElements[0].quads.size() * 4);
            mesh._indexedMesh.triangleIndexBase = BufferUtils.createByteBuffer(mesh._vertexElements[0].quads.size() * 4);
            mesh._indexedMesh.triangleIndexStride = 12;
            mesh._indexedMesh.vertexStride = 12;
            mesh._indexedMesh.numVertices = mesh._vertexElements[0].quads.size() / 3;
            mesh._indexedMesh.numTriangles = mesh._vertexElements[0].quads.size() / 6;
            mesh._indexedMesh.indexType = ScalarType.INTEGER;
        }
        /* ------------- */

        for (int j = 0; j < mesh._vertexElements.length; j++) {
            mesh._vertexElements[j].vertices = BufferUtils.createFloatBuffer(mesh._vertexElements[j].quads.size() * 2 + mesh._vertexElements[j].tex.size() + mesh._vertexElements[j].color.size() + mesh._vertexElements[j].normals.size());
            mesh._vertexElements[j].indices = BufferUtils.createIntBuffer(mesh._vertexElements[j].quads.size());

            int cTex = 0;
            int cColor = 0;
            int cIndex = 0;
            for (int i = 0; i < mesh._vertexElements[j].quads.size(); i += 3, cTex += 3, cColor += 4) {

                if (i % 4 == 0) {
                    mesh._vertexElements[j].indices.put(cIndex);
                    mesh._vertexElements[j].indices.put(cIndex + 1);
                    mesh._vertexElements[j].indices.put(cIndex + 2);

                    mesh._vertexElements[j].indices.put(cIndex + 2);
                    mesh._vertexElements[j].indices.put(cIndex + 3);
                    mesh._vertexElements[j].indices.put(cIndex);

                    /* BULLET PHYSICS */
                    if (j == 0 && GENERATE_PHYSICS_MESHES) {
                        mesh._indexedMesh.triangleIndexBase.putInt(cIndex);
                        mesh._indexedMesh.triangleIndexBase.putInt(cIndex + 1);
                        mesh._indexedMesh.triangleIndexBase.putInt(cIndex + 2);

                        mesh._indexedMesh.triangleIndexBase.putInt(cIndex + 2);
                        mesh._indexedMesh.triangleIndexBase.putInt(cIndex + 3);
                        mesh._indexedMesh.triangleIndexBase.putInt(cIndex);
                    }
                    /* ------------- */

                    cIndex += 4;
                }

                Vector3f vertexPos = new Vector3f(mesh._vertexElements[j].quads.get(i), mesh._vertexElements[j].quads.get(i + 1), mesh._vertexElements[j].quads.get(i + 2));

                mesh._vertexElements[j].vertices.put(vertexPos.x);
                mesh._vertexElements[j].vertices.put(vertexPos.y);
                mesh._vertexElements[j].vertices.put(vertexPos.z);

                /* BULLET PHYSICS */
                if (j == 0 && GENERATE_PHYSICS_MESHES) {
                    mesh._indexedMesh.vertexBase.putFloat(vertexPos.x);
                    mesh._indexedMesh.vertexBase.putFloat(vertexPos.y);
                    mesh._indexedMesh.vertexBase.putFloat(vertexPos.z);
                }
                /* ------------ */

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex + 1));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex + 2));

                Double[] result = new Double[3];
                calcLightingValuesForVertexPos(vertexPos, result);

                mesh._vertexElements[j].vertices.put(result[0].floatValue());
                mesh._vertexElements[j].vertices.put(result[1].floatValue());
                mesh._vertexElements[j].vertices.put(result[2].floatValue());

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 1));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 2));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 3));

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].normals.get(i));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].normals.get(i + 1));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].normals.get(i + 2));
            }

            mesh._vertexElements[j].vertices.flip();
            mesh._vertexElements[j].indices.flip();
        }
        PerformanceMonitor.endActivity();
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

    /**
     * Generates the billboard vertices for a given local block position.
     *
     * @param mesh The active mesh
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
     * @param temp The temperature
     * @param hum  The humidity
     */
    private void generateBillboardVertices(ChunkMesh mesh, int x, int y, int z, double temp, double hum) {
        byte blockId = _chunk.getBlock(x, y, z);
        Block block = BlockManager.getInstance().getBlock(blockId);

        /*
         * First side of the billboard
         */
        Vector4f colorBillboardOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);
        Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.FRONT).x, block.calcTextureOffsetFor(Block.SIDE.FRONT).y, 0);

        Vector3f p1 = new Vector3f(-0.5f, -0.5f, 0.5f);
        Vector3f p2 = new Vector3f(0.5f, -0.5f, -0.5f);
        Vector3f p3 = new Vector3f(0.5f, 0.5f, -0.5f);
        Vector3f p4 = new Vector3f(-0.5f, 0.5f, 0.5f);
        Vector3f normal = new Vector3f(0, 0, 1);

        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p1), normal);
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p2), normal);
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p3), normal);
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p4), normal);
        addBlockTextureData(mesh._vertexElements[2], texOffset, normal);

        /*
        * Second side of the billboard
        */
        colorBillboardOffset = block.calcColorOffsetFor(Block.SIDE.BACK, temp, hum);
        texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.BACK).x, block.calcTextureOffsetFor(Block.SIDE.BACK).y, 0);

        p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, -0.5f, 0.5f);
        p3 = new Vector3f(0.5f, 0.5f, 0.5f);
        p4 = new Vector3f(-0.5f, 0.5f, -0.5f);
        normal = new Vector3f(0, 0, 1);

        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p1), normal);
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p2), normal);
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p3), normal);
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p4), normal);
        addBlockTextureData(mesh._vertexElements[2], texOffset, normal);
    }

    private void generateBlockVertices(ChunkMesh mesh, int x, int y, int z, double temp, double hum) {
        PerformanceMonitor.startActivity("GenerateBlock");
        byte blockId = _chunk.getBlock(x, y, z);
        Block block = BlockManager.getInstance().getBlock(blockId);

        /*
         * Determine the render process.
         */
        ChunkMesh.RENDER_TYPE renderType = ChunkMesh.RENDER_TYPE.BILLBOARD_AND_TRANSLUCENT;

        if (!block.isTranslucent())
            renderType = ChunkMesh.RENDER_TYPE.OPAQUE;
        if (block.getTitle().equals("Water") || block.getTitle().equals("Ice"))
            renderType = ChunkMesh.RENDER_TYPE.WATER_AND_ICE;

        boolean drawFront, drawBack, drawLeft, drawRight, drawTop, drawBottom;

        byte blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y + 1, _chunk.getBlockWorldPosZ(z));
        drawTop = isSideVisibleForBlockTypes(blockToCheckId, blockId);
        blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y, _chunk.getBlockWorldPosZ(z - 1));
        drawFront = isSideVisibleForBlockTypes(blockToCheckId, blockId);
        blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y, _chunk.getBlockWorldPosZ(z + 1));
        drawBack = isSideVisibleForBlockTypes(blockToCheckId, blockId);
        blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x - 1), y, _chunk.getBlockWorldPosZ(z));
        drawLeft = isSideVisibleForBlockTypes(blockToCheckId, blockId);
        blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + 1), y, _chunk.getBlockWorldPosZ(z));
        drawRight = isSideVisibleForBlockTypes(blockToCheckId, blockId);

        // Don't draw anything "below" the world
        if (y > 0) {
            blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y - 1, _chunk.getBlockWorldPosZ(z));
            drawBottom = isSideVisibleForBlockTypes(blockToCheckId, blockId);
        } else {
            drawBottom = false;
        }

        Block.BLOCK_FORM blockForm = block.getBlockForm();

        // If the block is lowered, some more faces have to be drawn
        if (blockForm == Block.BLOCK_FORM.LOWERED_BLOCK) {
            blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y - 1, _chunk.getBlockWorldPosZ(z - 1));
            drawFront = isSideVisibleForBlockTypes(blockToCheckId, blockId) || drawFront;
            blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y - 1, _chunk.getBlockWorldPosZ(z + 1));
            drawBack = isSideVisibleForBlockTypes(blockToCheckId, blockId) || drawBack;
            blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x - 1), y - 1, _chunk.getBlockWorldPosZ(z));
            drawLeft = isSideVisibleForBlockTypes(blockToCheckId, blockId) || drawLeft;
            blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + 1), y - 1, _chunk.getBlockWorldPosZ(z));
            drawRight = isSideVisibleForBlockTypes(blockToCheckId, blockId) || drawRight;
            blockToCheckId = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y + 1, _chunk.getBlockWorldPosZ(z));
            drawTop = (BlockManager.getInstance().getBlock(blockToCheckId).getBlockForm() != Block.BLOCK_FORM.LOWERED_BLOCK) || drawTop;
        }

        if (drawTop) {
            Vector3f p1 = new Vector3f(-0.5f, 0.5f, 0.5f);
            Vector3f p2 = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f p3 = new Vector3f(0.5f, 0.5f, -0.5f);
            Vector3f p4 = new Vector3f(-0.5f, 0.5f, -0.5f);

            Vector3f norm = new Vector3f(0, 1, 0);

            Vector4f colorOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);

            Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.TOP).x, block.calcTextureOffsetFor(Block.SIDE.TOP).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);
        }

        if (drawFront) {

            Vector3f p1 = new Vector3f(-0.5f, 0.5f, -0.5f);
            Vector3f p2 = new Vector3f(0.5f, 0.5f, -0.5f);
            Vector3f p3 = new Vector3f(0.5f, -0.5f, -0.5f);
            Vector3f p4 = new Vector3f(-0.5f, -0.5f, -0.5f);

            Vector3f norm = new Vector3f(0, 0, -1);

            Vector4f colorOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);

            Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.FRONT).x, block.calcTextureOffsetFor(Block.SIDE.FRONT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);
        }

        if (drawBack) {
            Vector3f p1 = new Vector3f(-0.5f, -0.5f, 0.5f);
            Vector3f p2 = new Vector3f(0.5f, -0.5f, 0.5f);
            Vector3f p3 = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f p4 = new Vector3f(-0.5f, 0.5f, 0.5f);

            Vector3f norm = new Vector3f(0, 0, 1);

            Vector4f colorOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);

            Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.BACK).x, block.calcTextureOffsetFor(Block.SIDE.BACK).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);
        }

        if (drawLeft) {
            Vector3f p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
            Vector3f p2 = new Vector3f(-0.5f, -0.5f, 0.5f);
            Vector3f p3 = new Vector3f(-0.5f, 0.5f, 0.5f);
            Vector3f p4 = new Vector3f(-0.5f, 0.5f, -0.5f);

            Vector3f norm = new Vector3f(-1, 0, 0);

            Vector4f colorOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);

            Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.LEFT).x, block.calcTextureOffsetFor(Block.SIDE.LEFT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);
        }

        if (drawRight) {
            Vector3f p1 = new Vector3f(0.5f, 0.5f, -0.5f);
            Vector3f p2 = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f p3 = new Vector3f(0.5f, -0.5f, 0.5f);
            Vector3f p4 = new Vector3f(0.5f, -0.5f, -0.5f);

            Vector3f norm = new Vector3f(1, 0, 0);

            Vector4f colorOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);

            Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.RIGHT).x, block.calcTextureOffsetFor(Block.SIDE.RIGHT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);
        }

        if (drawBottom) {
            Vector3f p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
            Vector3f p2 = new Vector3f(0.5f, -0.5f, -0.5f);
            Vector3f p3 = new Vector3f(0.5f, -0.5f, 0.5f);
            Vector3f p4 = new Vector3f(-0.5f, -0.5f, 0.5f);

            Vector3f norm = new Vector3f(0, -1, 0);

            Vector4f colorOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);

            Vector3f texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.BOTTOM).x, block.calcTextureOffsetFor(Block.SIDE.BOTTOM).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);
        }
        PerformanceMonitor.endActivity();
    }

    private void generateVerticesForBlockSide(ChunkMesh mesh, int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm, Vector4f colorOffset, Vector3f texOffset, ChunkMesh.RENDER_TYPE renderType, Block.BLOCK_FORM blockForm) {
        PerformanceMonitor.startActivity("GenerateBlockSide");
        int vertexElementsId = 0;

        switch (renderType) {
            case BILLBOARD_AND_TRANSLUCENT:
                vertexElementsId = 1;
                break;
            case WATER_AND_ICE:
                vertexElementsId = 3;
                break;
        }

        switch (blockForm) {
            case CACTUS:
                generateCactusSide(p1, p2, p3, p4, norm);
                break;
            case LOWERED_BLOCK:
                generateLoweredBlock(x, y, z, p1, p2, p3, p4, norm);
                break;
        }

        addBlockTextureData(mesh._vertexElements[vertexElementsId], texOffset, norm);

        addBlockVertexData(mesh._vertexElements[vertexElementsId], colorOffset, moveVectorToChunkSpace(x, y, z, p1), norm);
        addBlockVertexData(mesh._vertexElements[vertexElementsId], colorOffset, moveVectorToChunkSpace(x, y, z, p2), norm);
        addBlockVertexData(mesh._vertexElements[vertexElementsId], colorOffset, moveVectorToChunkSpace(x, y, z, p3), norm);
        addBlockVertexData(mesh._vertexElements[vertexElementsId], colorOffset, moveVectorToChunkSpace(x, y, z, p4), norm);
        PerformanceMonitor.endActivity();
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

    private Vector3f moveVectorToChunkSpace(int cPosX, int cPosY, int cPosZ, Vector3f offset) {
        offset.x += cPosX;
        offset.y += cPosY;
        offset.z += cPosZ;

        return offset;
    }

    private void generateLoweredBlock(int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm) {
        byte bottomBlock = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y - 1, _chunk.getBlockWorldPosZ(z));
        boolean lowerBottom = BlockManager.getInstance().getBlock(bottomBlock).getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK || bottomBlock == 0x0;

        if (norm.x == 1.0f) {
            p1.y -= 0.1;
            p2.y -= 0.1;

            if (lowerBottom) {
                p3.y -= 0.1;
                p4.y -= 0.1;
            }
        } else if (norm.x == -1.0f) {
            p3.y -= 0.1;
            p4.y -= 0.1;

            if (lowerBottom) {
                p1.y -= 0.1;
                p2.y -= 0.1;
            }
        } else if (norm.z == 1.0f) {
            p3.y -= 0.1;
            p4.y -= 0.1;

            if (lowerBottom) {
                p1.y -= 0.1;
                p2.y -= 0.1;
            }
        } else if (norm.z == -1.0f) {
            p1.y -= 0.1;
            p2.y -= 0.1;

            if (lowerBottom) {
                p3.y -= 0.1;
                p4.y -= 0.1;
            }
        } else if (norm.y == 1.0f) {
            p1.y -= 0.1;
            p2.y -= 0.1;
            p3.y -= 0.1;
            p4.y -= 0.1;
        } else if (norm.y == -1.0f) {
            if (lowerBottom) {
                p1.y -= 0.1;
                p2.y -= 0.1;
                p3.y -= 0.1;
                p4.y -= 0.1;
            }
        }
    }

    private void generateCactusSide(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm) {
        if (norm.x == 1.0f || norm.x == -1.0f) {
            p1.x -= 0.0625 * norm.x;
            p2.x -= 0.0625 * norm.x;
            p3.x -= 0.0625 * norm.x;
            p4.x -= 0.0625 * norm.x;
        } else if (norm.z == 1.0f || norm.z == -1.0f) {
            p1.z -= 0.0625 * norm.z;
            p2.z -= 0.0625 * norm.z;
            p3.z -= 0.0625 * norm.z;
            p4.z -= 0.0625 * norm.z;
        }
    }

    private void addBlockTextureData(ChunkMesh.VertexElements vertexElements, Vector3f texOffset, Vector3f norm) {
        /*
        * Rotate the texture coordinates according to the
        * orientation of the plane.
        */
        if (norm.z == 1 || norm.x == -1) {
            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(1.0f);

            vertexElements.tex.add(texOffset.x + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(texOffset.y + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(1.0f);

            vertexElements.tex.add(texOffset.x + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(texOffset.y);
            vertexElements.tex.add(1.0f);

            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y);
            vertexElements.tex.add(1.0f);
        } else {
            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y);
            vertexElements.tex.add(1.0f);

            vertexElements.tex.add(texOffset.x + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(texOffset.y);
            vertexElements.tex.add(1.0f);

            vertexElements.tex.add(texOffset.x + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(texOffset.y + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(1.0f);

            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y + Block.TEXTURE_OFFSET_WIDTH);
            vertexElements.tex.add(1.0f);
        }
    }

    private void addBlockVertexData(ChunkMesh.VertexElements vertexElements, Vector4f colorOffset, Vector3f vertex, Vector3f normal) {
        PerformanceMonitor.startActivity("AddBlockVertexData");
        vertexElements.color.add(colorOffset.x);
        vertexElements.color.add(colorOffset.y);
        vertexElements.color.add(colorOffset.z);
        vertexElements.color.add(colorOffset.w);
        vertexElements.quads.add(vertex.x);
        vertexElements.quads.add(vertex.y);
        vertexElements.quads.add(vertex.z);
        vertexElements.normals.add(normal.x);
        vertexElements.normals.add(normal.y);
        vertexElements.normals.add(normal.z);
        PerformanceMonitor.endActivity();
    }

    /**
     * Returns true if the side should be rendered adjacent to the second side provided.
     *
     * @param blockToCheck The block to check
     * @param currentBlock The current block
     * @return True if the side is visible for the given block types
     */
    private boolean isSideVisibleForBlockTypes(byte blockToCheck, byte currentBlock) {
        PerformanceMonitor.startActivity("CheckSideVisibility");
        Block bCheck = BlockManager.getInstance().getBlock(blockToCheck);
        Block cBlock = BlockManager.getInstance().getBlock(currentBlock);

        try
        {
            return bCheck.getId() == 0x0 || cBlock.isDisableTessellation() || bCheck.getBlockForm() == Block.BLOCK_FORM.BILLBOARD || !cBlock.isTranslucent() && bCheck.isTranslucent() || (bCheck.getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK && cBlock.getBlockForm() != Block.BLOCK_FORM.LOWERED_BLOCK);
        }
        finally
        {
            PerformanceMonitor.endActivity();
        }
    }

    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
    }

}
