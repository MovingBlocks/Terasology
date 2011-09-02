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
package com.github.begla.blockmania.world;

import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockAir;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.VectorPool;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.HashMap;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkMeshGenerator {

    private static Vector3f[] _rayLut;
    private static FastRandom _rand = new FastRandom(32);
    private Chunk _chunk;

    static {
        _rayLut = new Vector3f[16];

        for (int i = 0; i < _rayLut.length; i++) {
            _rayLut[i] = RandomDirection();
        }
    }

    private static Vector3f RandomDirection() {
        Vector3f result = new Vector3f();

        while (true) {
            result.x = (float) _rand.randomDouble();
            result.y = (float) _rand.randomDouble();
            result.z = (float) _rand.randomDouble();
            if (result.x * result.x + result.y * result.y + result.z * result.z > 1) continue;

            if (Vector3f.dot(result, new Vector3f(0, 1, 0)) < 0) continue;

            result.normalise();
            return result;
        }
    }

    /**
     *
     */
    public enum RENDER_TYPE {

        /**
         *
         */
        TRANS,
        /**
         *
         */
        OPAQUE,
        /**
         *
         */
        BILLBOARD
    }

    public ChunkMeshGenerator(Chunk chunk) {
        _chunk = chunk;
    }

    public ChunkMesh generateMesh() {
        ChunkMesh mesh = new ChunkMesh();

        HashMap<Vector3f, Integer> indexMap = new HashMap<Vector3f, Integer>();

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    generateBlockVertices(mesh, x, y, z);
                    generateBillboardVertices(mesh, x, y, z);
                }
            }
        }

        generateOptimizedBuffers(mesh);

        return mesh;
    }

    private void generateOptimizedBuffers(ChunkMesh mesh) {
        for (int j = 0; j < 3; j++) {
            mesh._vertexElements[j].vertices = BufferUtils.createFloatBuffer(mesh._vertexElements[j].quads.size() + mesh._vertexElements[j].tex.size() * 2 + mesh._vertexElements[j].color.size());
            mesh._vertexElements[j].indices = BufferUtils.createIntBuffer(mesh._vertexElements[j].quads.size());

            HashMap<Vector3f, Integer> indexLut = new HashMap<Vector3f, Integer>(mesh._vertexElements[j].vertices.capacity());

            int tex = 0;
            int color = 0;
            int idxCounter = 0;
            for (int i = 0; i < mesh._vertexElements[j].quads.size(); i += 3, tex += 2, color += 4) {

                Vector3f vertexPos = VectorPool.getVector(mesh._vertexElements[j].quads.get(i), mesh._vertexElements[j].quads.get(i + 1), mesh._vertexElements[j].quads.get(i + 2));

                // Check if this vertex is a new one
                if (indexLut.containsKey(vertexPos)) {
                    int index = indexLut.get(vertexPos);
                    mesh._vertexElements[j].indices.put(index);
                    continue;
                }

                mesh._vertexElements[j].vertices.put(vertexPos.x);
                mesh._vertexElements[j].vertices.put(vertexPos.y);
                mesh._vertexElements[j].vertices.put(vertexPos.z);

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(tex));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(tex + 1));

                mesh._vertexElements[j].vertices.put(getLightForVertexPos(vertexPos, Chunk.LIGHT_TYPE.SUN) * getOcclusionValue(vertexPos, Chunk.LIGHT_TYPE.SUN));
                mesh._vertexElements[j].vertices.put(getLightForVertexPos(vertexPos, Chunk.LIGHT_TYPE.BLOCK) * getOcclusionValue(vertexPos, Chunk.LIGHT_TYPE.BLOCK));

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(color));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(color + 1));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(color + 2));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(color + 3));

                // Log this vertex
                indexLut.put(vertexPos, idxCounter);
                mesh._vertexElements[j].indices.put(idxCounter++);
            }

            mesh._vertexElements[j].vertices.flip();
            mesh._vertexElements[j].indices.flip();
        }
    }

    private float getLightForVertexPos(Vector3f vertexPos, Chunk.LIGHT_TYPE lightType) {
        float result = 0.0f;

        float[] lights = new float[8];

        lights[0] = _chunk.getParent().getLight((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f), lightType) / 15f;
        lights[1] = _chunk.getParent().getLight((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f), lightType) / 15f;
        lights[2] = _chunk.getParent().getLight((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f), lightType) / 15f;
        lights[3] = _chunk.getParent().getLight((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f), lightType) / 15f;

        lights[4] = _chunk.getParent().getLight((int) (vertexPos.x + 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z + 0.5f), lightType) / 15f;
        lights[5] = _chunk.getParent().getLight((int) (vertexPos.x + 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z - 0.5f), lightType) / 15f;
        lights[6] = _chunk.getParent().getLight((int) (vertexPos.x - 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z - 0.5f), lightType) / 15f;
        lights[7] = _chunk.getParent().getLight((int) (vertexPos.x - 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z + 0.5f), lightType) / 15f;

        boolean[] blocks = new boolean[8];

        blocks[0] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f))).isBlockTypeTranslucent();
        blocks[1] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f))).isBlockTypeTranslucent();
        blocks[2] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f))).isBlockTypeTranslucent();
        blocks[3] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f))).isBlockTypeTranslucent();

        blocks[4] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z + 0.5f))).isBlockTypeTranslucent();
        blocks[5] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z - 0.5f))).isBlockTypeTranslucent();
        blocks[6] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z - 0.5f))).isBlockTypeTranslucent();
        blocks[7] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z + 0.5f))).isBlockTypeTranslucent();

        int counter = 0;
        for (int i = 0; i < 8; i++) {
            if (blocks[i]) {
                result += lights[i];
                counter++;
            }
        }

        return result / counter;
    }

    private float getOcclusionValue(Vector3f vertexPos, Chunk.LIGHT_TYPE lightType) {

        float result = 1.0f;
        boolean[] blocks = new boolean[8];

        blocks[0] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f))).isCastingShadows();
        blocks[1] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f))).isCastingShadows();
        blocks[2] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f))).isCastingShadows();
        blocks[3] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f))).isCastingShadows();

        blocks[4] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z + 0.5f))).isCastingShadows();
        blocks[5] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z - 0.5f))).isCastingShadows();
        blocks[6] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z - 0.5f))).isCastingShadows();
        blocks[7] = Block.getBlockForType(_chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y - 0.5f), (int) (vertexPos.z + 0.5f))).isCastingShadows();

        for (int i = 0; i < 8; i++) {
            if (blocks[i]) {
                result -= Configuration.OCCLUSION_AMOUNT;
            }
        }

        return result;
    }


    /**
     * Generates the billboard vertices for a given local block position.
     *
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     */
    private void generateBillboardVertices(ChunkMesh mesh, int x, int y, int z) {
        byte block = _chunk.getBlock(x, y, z);

        // Ignore normal blocks
        if (!Block.getBlockForType(block).isBlockBillboard()) {
            return;
        }

        /*
         * First side of the billboard
         */
        Vector4f colorBillboardOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);
        Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y, 0);

        Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);
        Vector3f p2 = VectorPool.getVector(0.5f, -0.5f, -0.5f);
        Vector3f p3 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
        Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);

        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p1));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p2));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p3));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p4));
        addBlockTextureData(mesh._vertexElements[2], texOffset, VectorPool.getVector(0, 0, 1));

        /*
        * Second side of the billboard
        */
        colorBillboardOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BACK);
        texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y, 0);

        p1 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);
        p2 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
        p3 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
        p4 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);

        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p1));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p2));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p3));
        addBlockVertexData(mesh._vertexElements[2], colorBillboardOffset, moveVectorToWorldSpace(x, y, z, p4));
        addBlockTextureData(mesh._vertexElements[2], texOffset, VectorPool.getVector(0, 0, 1));
    }

    private void generateBlockVertices(ChunkMesh mesh, int x, int y, int z) {
        byte block = _chunk.getBlock(x, y, z);

        /*
         * Determine the render process.
         */
        RENDER_TYPE renderType = RENDER_TYPE.TRANS;

        if (!Block.getBlockForType(block).isBlockTypeTranslucent()) {
            renderType = RENDER_TYPE.OPAQUE;
        }

        // Ignore invisible blocks and billboards
        if (Block.getBlockForType(block).isBlockInvisible() || Block.getBlockForType(block).isBlockBillboard()) {
            return;
        }

        boolean drawFront, drawBack, drawLeft, drawRight, drawTop, drawBottom;
        byte blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y + 1), _chunk.getBlockWorldPosZ(z));

        drawTop = isSideVisibleForBlockTypes(blockToCheck, block);
        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z - 1));
        drawFront = isSideVisibleForBlockTypes(blockToCheck, block);
        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z + 1));
        drawBack = isSideVisibleForBlockTypes(blockToCheck, block);
        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x - 1), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z));
        drawLeft = isSideVisibleForBlockTypes(blockToCheck, block);
        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + 1), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z));
        drawRight = isSideVisibleForBlockTypes(blockToCheck, block);
        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z));
        drawBottom = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawTop) {
            Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

            Vector3f p1 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(0, 1, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.TOP);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        if (drawFront) {
            Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

            Vector3f p1 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, -0.5f, -0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(0, 0, -1);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        if (drawBack) {
            Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

            Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);

            Vector3f norm = VectorPool.getVector(0, 0, 1);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BACK);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        if (drawLeft) {
            Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

            Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(-1, 0, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.LEFT);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        if (drawRight) {
            Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

            Vector3f p1 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(0.5f, -0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(1, 0, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.RIGHT);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        if (drawBottom) {
            Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

            Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, -0.5f, -0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);

            Vector3f norm = VectorPool.getVector(0, -1, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BOTTOM);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, renderType, blockForm);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param norm
     * @param colorOffset
     * @param texOffset
     * @param renderType
     */
    void generateVerticesForBlockSide(ChunkMesh mesh, int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm, Vector4f colorOffset, Vector3f texOffset, RENDER_TYPE renderType, Block.BLOCK_FORM blockForm) {
        ChunkMesh.VertexElements vertexElements = mesh._vertexElements[0];

        if (renderType == RENDER_TYPE.TRANS) {
            vertexElements = mesh._vertexElements[1];
        }

        switch (blockForm) {
            case CACTUS:
                generateCactusSide(p1, p2, p3, p4, norm);
                break;
        }

        addBlockTextureData(vertexElements, texOffset, norm);

        addBlockVertexData(vertexElements, colorOffset, moveVectorToWorldSpace(x, y, z, p1));
        addBlockVertexData(vertexElements, colorOffset, moveVectorToWorldSpace(x, y, z, p2));
        addBlockVertexData(vertexElements, colorOffset, moveVectorToWorldSpace(x, y, z, p3));
        addBlockVertexData(vertexElements, colorOffset, moveVectorToWorldSpace(x, y, z, p4));
    }

    private Vector3f moveVectorToWorldSpace(int cPosX, int cPosY, int cPosZ, Vector3f offset) {
        float offsetX = _chunk.getPosition().x * Configuration.CHUNK_DIMENSIONS.x;
        float offsetY = _chunk.getPosition().y * Configuration.CHUNK_DIMENSIONS.y;
        float offsetZ = _chunk.getPosition().z * Configuration.CHUNK_DIMENSIONS.z;

        offset.x += offsetX + cPosX;
        offset.y += offsetY + cPosY;
        offset.z += offsetZ + cPosZ;

        return offset;
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
     * Returns true if the block side is adjacent to a translucent block or an air
     * block.
     * <p/>
     * NOTE: Air and leafs have to be handled separately. Otherwise the water surface would not be displayed due to the tessellation process.
     *
     * @param blockToCheck
     * @param currentBlock
     * @return
     */
    private boolean isSideVisibleForBlockTypes(byte blockToCheck, byte currentBlock) {
        Block bCheck = Block.getBlockForType(blockToCheck);
        Block cBlock = Block.getBlockForType(currentBlock);

        return bCheck.getClass() == BlockAir.class || cBlock.doNotTessellate() || bCheck.isBlockBillboard() || (Block.getBlockForType(blockToCheck).isBlockTypeTranslucent() && !Block.getBlockForType(currentBlock).isBlockTypeTranslucent());
    }
}
