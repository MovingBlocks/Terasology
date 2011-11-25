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
package com.github.begla.blockmania.world.chunk;

import com.bulletphysics.collision.shapes.*;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.configuration.ConfigurationManager;
import org.lwjgl.BufferUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;

/**
 * Generates tessellated chunk meshes from chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkMeshGenerator {

    private static final Double OCCLUSION_INTENS_DEFAULT = (Double) ConfigurationManager.getInstance().getConfig().get("Lighting.occlusionIntensDefault");
    private static final Double OCCLUSION_INTENS_BILLBOARDS = (Double) ConfigurationManager.getInstance().getConfig().get("Lighting.occlusionIntensBillboards");

    private final Chunk _chunk;
    private static int _statVertexArrayUpdateCount = 0;

    public ChunkMeshGenerator(Chunk chunk) {
        _chunk = chunk;
    }

    public ChunkMesh generateMesh() {
        ChunkMesh mesh = new ChunkMesh();

        for (int x = 0; x < Chunk.getChunkDimensionX(); x++) {
            for (int z = 0; z < Chunk.getChunkDimensionZ(); z++) {
                double biomeTemp = _chunk.getParent().getTemperatureAt(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosZ(z));
                double biomeHumidity = _chunk.getParent().getHumidityAt(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosZ(z));

                for (int y = 0; y < Chunk.getChunkDimensionY(); y++) {
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

        return mesh;
    }

    private void generateOptimizedBuffers(ChunkMesh mesh) {
        /* BULLET PHYSICS */
        IndexedMesh indexMesh = new IndexedMesh();
        indexMesh.vertexBase = ByteBuffer.allocate(mesh._vertexElements[0].quads.size() * 4);
        indexMesh.triangleIndexBase = ByteBuffer.allocate(mesh._vertexElements[0].quads.size() * 4);
        indexMesh.triangleIndexStride = 12;
        indexMesh.vertexStride = 12;
        indexMesh.numVertices = mesh._vertexElements[0].quads.size() / 3;
        indexMesh.numTriangles = mesh._vertexElements[0].quads.size() / 6;
        indexMesh.indexType = ScalarType.INTEGER;
        /* ------------- */

        for (int j = 0; j < mesh._vertexElements.length; j++) {
            mesh._vertexElements[j].vertices = BufferUtils.createFloatBuffer(mesh._vertexElements[j].quads.size() * 2 + mesh._vertexElements[j].tex.size() + mesh._vertexElements[j].color.size());
            mesh._vertexElements[j].indices = BufferUtils.createIntBuffer(mesh._vertexElements[j].quads.size());

            int cTex = 0;
            int cColor = 0;
            int cIndex = 0;
            for (int i = 0; i < mesh._vertexElements[j].quads.size(); i += 3, cTex += 2, cColor += 4) {

                if (i % 4 == 0) {
                    mesh._vertexElements[j].indices.put(cIndex);
                    mesh._vertexElements[j].indices.put(cIndex + 1);
                    mesh._vertexElements[j].indices.put(cIndex + 2);

                    mesh._vertexElements[j].indices.put(cIndex + 2);
                    mesh._vertexElements[j].indices.put(cIndex + 3);
                    mesh._vertexElements[j].indices.put(cIndex);

                    /* BULLET PHYSICS */
                    if (j == 0) {
                        indexMesh.triangleIndexBase.putInt(cIndex);
                        indexMesh.triangleIndexBase.putInt(cIndex + 1);
                        indexMesh.triangleIndexBase.putInt(cIndex + 2);

                        indexMesh.triangleIndexBase.putInt(cIndex + 2);
                        indexMesh.triangleIndexBase.putInt(cIndex + 3);
                        indexMesh.triangleIndexBase.putInt(cIndex);
                    }
                    /* ------------- */

                    cIndex += 4;
                }

                Vector3f vertexPos = new Vector3f(mesh._vertexElements[j].quads.get(i), mesh._vertexElements[j].quads.get(i + 1), mesh._vertexElements[j].quads.get(i + 2));

                mesh._vertexElements[j].vertices.put(vertexPos.x);
                mesh._vertexElements[j].vertices.put(vertexPos.y);
                mesh._vertexElements[j].vertices.put(vertexPos.z);

                /* BULLET PHYSICS */
                if (j == 0) {
                    indexMesh.vertexBase.putFloat(vertexPos.x);
                    indexMesh.vertexBase.putFloat(vertexPos.y);
                    indexMesh.vertexBase.putFloat(vertexPos.z);
                }
                /* ------------ */

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex + 1));

                Double[] result = new Double[3];
                calcLightingValuesForVertexPos(vertexPos, result);

                mesh._vertexElements[j].vertices.put(result[0].floatValue());
                mesh._vertexElements[j].vertices.put(result[1].floatValue());
                mesh._vertexElements[j].vertices.put(result[2].floatValue());

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 1));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 2));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 3));
            }

            mesh._vertexElements[j].vertices.flip();
            mesh._vertexElements[j].indices.flip();
        }

        indexMesh.triangleIndexBase.flip();
        indexMesh.vertexBase.flip();

        /* BULLET PHYSICS */
        TriangleIndexVertexArray vertexArray = new TriangleIndexVertexArray();
        vertexArray.addIndexedMesh(indexMesh);

        mesh._bulletMeshShape = new BvhTriangleMeshShape(vertexArray, true);
        /* -------------- */
    }

    private void calcLightingValuesForVertexPos(Vector3f vertexPos, Double[] output) {
        double[] lights = new double[8];
        double[] blockLights = new double[8];
        byte[] blocks = new byte[8];

        Vector3f vertexWorldPos = moveVectorFromChunkSpaceToWorldSpace(vertexPos);

        blocks[0] = _chunk.getParent().getBlock((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z + 0.5f));
        blocks[1] = _chunk.getParent().getBlock((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z - 0.5f));
        blocks[2] = _chunk.getParent().getBlock((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z - 0.5f));
        blocks[3] = _chunk.getParent().getBlock((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z + 0.5f));

        lights[0] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.SUN);
        lights[1] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.SUN);
        lights[2] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.SUN);
        lights[3] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.SUN);

        lights[4] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.SUN);
        lights[5] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.SUN);
        lights[6] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.SUN);
        lights[7] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.SUN);

        blockLights[0] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[1] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[2] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[3] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y + 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.BLOCK);

        blockLights[4] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[5] = _chunk.getParent().getLight((int) (vertexWorldPos.x + 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[6] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z - 0.5f), Chunk.LIGHT_TYPE.BLOCK);
        blockLights[7] = _chunk.getParent().getLight((int) (vertexWorldPos.x - 0.5f), (int) (vertexWorldPos.y - 0.5f), (int) (vertexWorldPos.z + 0.5f), Chunk.LIGHT_TYPE.BLOCK);

        double resultAmbientOcclusion = 1.0;
        double resultLight = 0;
        double resultBlockLight = 0;
        int counterLight = 0;
        int counterBlockLight = 0;

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
                    resultAmbientOcclusion -= OCCLUSION_INTENS_DEFAULT;
                } else if (b.isCastsShadows() && b.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                    resultAmbientOcclusion -= OCCLUSION_INTENS_BILLBOARDS;
                }
            }
        }

        if (counterLight == 0)
            output[0] = (double) 0;
        else
            output[0] = resultLight / counterLight / 15f;

        if (counterBlockLight == 0)
            output[1] = (double) 0;
        else
            output[1] = resultBlockLight / counterBlockLight / 15f;

        output[2] = resultAmbientOcclusion;
    }

    /**
     * Generates the billboard vertices for a given local block position.
     *
     * @param mesh The active mesh
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
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

        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p1));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p2));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p3));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p4));
        addBlockTextureData(mesh._vertexElements[2], texOffset, new Vector3f(0, 0, 1));

        /*
        * Second side of the billboard
        */
        colorBillboardOffset = block.calcColorOffsetFor(Block.SIDE.FRONT, temp, hum);
        texOffset = new Vector3f(block.calcTextureOffsetFor(Block.SIDE.BACK).x, block.calcTextureOffsetFor(Block.SIDE.BACK).y, 0);

        p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, -0.5f, 0.5f);
        p3 = new Vector3f(0.5f, 0.5f, 0.5f);
        p4 = new Vector3f(-0.5f, 0.5f, -0.5f);

        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p1));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p2));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p3));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToChunkSpace(x, y, z, p4));
        addBlockTextureData(mesh._vertexElements[2], texOffset, new Vector3f(0, 0, 1));
    }

    private void generateBlockVertices(ChunkMesh mesh, int x, int y, int z, double temp, double hum) {
        byte blockId = _chunk.getBlock(x, y, z);
        Block block = BlockManager.getInstance().getBlock(blockId);

        /*
         * Determine the render process.
         */
        ChunkMesh.RENDER_TYPE renderType = ChunkMesh.RENDER_TYPE.BILLBOARD_AND_TRANSLUCENT;

        if (!block.isTranslucent())
            renderType = ChunkMesh.RENDER_TYPE.OPAQUE;
        if (block.getTitle().equals("Water"))
            renderType = ChunkMesh.RENDER_TYPE.WATER;
        if (block.getTitle().equals("Lava"))
            renderType = ChunkMesh.RENDER_TYPE.LAVA;

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
    }

    private void  generateVerticesForBlockSide(ChunkMesh mesh, int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm, Vector4f colorOffset, Vector3f texOffset, ChunkMesh.RENDER_TYPE renderType, Block.BLOCK_FORM blockForm) {
        ChunkMesh.VertexElements vertexElements = mesh._vertexElements[0];

        switch (renderType) {
            case BILLBOARD_AND_TRANSLUCENT:
                vertexElements = mesh._vertexElements[1];
                break;
            case WATER:
                vertexElements = mesh._vertexElements[3];
                break;
            case LAVA:
                vertexElements = mesh._vertexElements[4];
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

        addBlockTextureData(vertexElements, texOffset, norm);

        addBlockVertexData(vertexElements, colorOffset, moveVectorToChunkSpace(x, y, z, p1));
        addBlockVertexData(vertexElements, colorOffset, moveVectorToChunkSpace(x, y, z, p2));
        addBlockVertexData(vertexElements, colorOffset, moveVectorToChunkSpace(x, y, z, p3));
        addBlockVertexData(vertexElements, colorOffset, moveVectorToChunkSpace(x, y, z, p4));
    }

    private Vector3f moveVectorFromChunkSpaceToWorldSpace(Vector3f offset) {
        double offsetX = _chunk.getPosition().x * Chunk.getChunkDimensionX();
        double offsetY = _chunk.getPosition().y * Chunk.getChunkDimensionY();
        double offsetZ = _chunk.getPosition().z * Chunk.getChunkDimensionZ();

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
        byte topBlock = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y + 1, _chunk.getBlockWorldPosZ(z));
        byte bottomBlock = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), y - 1, _chunk.getBlockWorldPosZ(z));

        boolean lower = topBlock == 0x0 || BlockManager.getInstance().getBlock(topBlock).getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK;
        boolean lowerBottom = BlockManager.getInstance().getBlock(bottomBlock).getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK || bottomBlock == 0x0;

        if (!lower)
            return;

        if (norm.x == 1.0f) {
            p1.y -= 0.25;
            p2.y -= 0.25;

            if (lowerBottom) {
                p3.y -= 0.25;
                p4.y -= 0.25;
            }
        } else if (norm.x == -1.0f) {
            p3.y -= 0.25;
            p4.y -= 0.25;

            if (lowerBottom) {
                p1.y -= 0.25;
                p2.y -= 0.25;
            }
        } else if (norm.z == 1.0f) {
            p3.y -= 0.25;
            p4.y -= 0.25;

            if (lowerBottom) {
                p1.y -= 0.25;
                p2.y -= 0.25;
            }
        } else if (norm.z == -1.0f) {
            p1.y -= 0.25;
            p2.y -= 0.25;

            if (lowerBottom) {
                p3.y -= 0.25;
                p4.y -= 0.25;
            }
        } else if (norm.y == 1.0f) {
            p1.y -= 0.25;
            p2.y -= 0.25;
            p3.y -= 0.25;
            p4.y -= 0.25;
        } else if (norm.y == -1.0f) {
            if (lowerBottom) {
                p1.y -= 0.25;
                p2.y -= 0.25;
                p3.y -= 0.25;
                p4.y -= 0.25;
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
            vertexElements.tex.add(texOffset.y + 0.0624f);

            vertexElements.tex.add(texOffset.x + 0.0624f);
            vertexElements.tex.add(texOffset.y + 0.0624f);

            vertexElements.tex.add(texOffset.x + 0.0624f);
            vertexElements.tex.add(texOffset.y);

            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y);
        } else {
            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y);

            vertexElements.tex.add(texOffset.x + 0.0624f);
            vertexElements.tex.add(texOffset.y);

            vertexElements.tex.add(texOffset.x + 0.0624f);
            vertexElements.tex.add(texOffset.y + 0.0624f);

            vertexElements.tex.add(texOffset.x);
            vertexElements.tex.add(texOffset.y + 0.0624f);
        }
    }

    private void addBlockVertexData(ChunkMesh.VertexElements vertexElements, Vector4f colorOffset, Vector3f vertex) {
        vertexElements.color.add(colorOffset.x);
        vertexElements.color.add(colorOffset.y);
        vertexElements.color.add(colorOffset.z);
        vertexElements.color.add(colorOffset.w);
        vertexElements.quads.add(vertex.x);
        vertexElements.quads.add(vertex.y);
        vertexElements.quads.add(vertex.z);
    }

    /**
     * Returns true if the side should be rendered adjacent to the second side provided.
     *
     * @param blockToCheck The block to check
     * @param currentBlock The current block
     * @return True if the side is visible for the given block types
     */
    private boolean isSideVisibleForBlockTypes(byte blockToCheck, byte currentBlock) {
        Block bCheck = BlockManager.getInstance().getBlock(blockToCheck);
        Block cBlock = BlockManager.getInstance().getBlock(currentBlock);

        return bCheck.getId() == 0x0 || cBlock.isDisableTessellation() || bCheck.getBlockForm() == Block.BLOCK_FORM.BILLBOARD || (!cBlock.isTranslucent() && bCheck.isTranslucent()) || (bCheck.getBlockForm() == Block.BLOCK_FORM.LOWERED_BLOCK && cBlock.getBlockForm() != Block.BLOCK_FORM.LOWERED_BLOCK);
    }

    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
    }

}
