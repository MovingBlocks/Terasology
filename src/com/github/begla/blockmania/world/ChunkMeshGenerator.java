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
import com.github.begla.blockmania.rendering.VectorPool;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkMeshGenerator {

    private final Chunk _chunk;

    public enum RENDER_TYPE {
        TRANS,
        OPAQUE,
    }

    public ChunkMeshGenerator(Chunk chunk) {
        _chunk = chunk;
    }

    public ChunkMesh generateMesh() {
        ChunkMesh mesh = new ChunkMesh();

        for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
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
                    cIndex += 4;
                }

                Vector3f vertexPos = VectorPool.getVector(mesh._vertexElements[j].quads.get(i), mesh._vertexElements[j].quads.get(i + 1), mesh._vertexElements[j].quads.get(i + 2));

                mesh._vertexElements[j].vertices.put(vertexPos.x);
                mesh._vertexElements[j].vertices.put(vertexPos.y);
                mesh._vertexElements[j].vertices.put(vertexPos.z);

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].tex.get(cTex + 1));

                float occlusionValue = getOcclusionValue(vertexPos);
                mesh._vertexElements[j].vertices.put(getLightForVertexPos(vertexPos, Chunk.LIGHT_TYPE.SUN) * occlusionValue);
                mesh._vertexElements[j].vertices.put(getLightForVertexPos(vertexPos, Chunk.LIGHT_TYPE.BLOCK) * occlusionValue);

                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 1));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 2));
                mesh._vertexElements[j].vertices.put(mesh._vertexElements[j].color.get(cColor + 3));

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

        int counter = 0;
        for (int i = 0; i < 8; i++) {
            if (lights[i] > 0) {
                result += lights[i];
                counter++;
            }
        }

        if (counter == 0)
            return 0;

        return result / counter;
    }

    private float getOcclusionValue(Vector3f vertexPos) {

        float result = 1.0f;
        byte[] blocks = new byte[8];

        blocks[0] = _chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f));
        blocks[1] = _chunk.getParent().getBlock((int) (vertexPos.x + 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f));
        blocks[2] = _chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z - 0.5f));
        blocks[3] = _chunk.getParent().getBlock((int) (vertexPos.x - 0.5f), (int) (vertexPos.y + 0.5f), (int) (vertexPos.z + 0.5f));

        for (int i = 0; i < 4; i++) {
            Block b = Block.getBlockForType(blocks[i]);
            if (b.isCastingShadows() && !b.isBlockBillboard()) {
                result -= Configuration.OCCLUSION_AMOUNT;
            } else if (b.isCastingShadows() && b.isBlockBillboard()) {
                result -= Configuration.OCCLUSION_AMOUNT / 2;
            }
        }

        return result;
    }

    /**
     * Generates the billboard vertices for a given local block position.
     *
     * @param mesh The active mesh
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
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

        // Don't draw anything "below" the world
        if (y > 0) {
            blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z));
            drawBottom = isSideVisibleForBlockTypes(blockToCheck, block);
        } else {
            drawBottom = false;
        }

        Block.BLOCK_FORM blockForm = Block.getBlockForType(block).getBlockForm();

        // If the block is lowered, some more faces have to be drawn
        if (blockForm == Block.BLOCK_FORM.LOWERED_BOCK) {
            // Draw the top if a non-lowered block is above the lowered block
            blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y + 1), _chunk.getBlockWorldPosZ(z));
            if (Block.getBlockForType(blockToCheck).getBlockForm() != Block.BLOCK_FORM.LOWERED_BOCK) {
                drawTop = true;
            }

            blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z - 1));
            drawFront = isSideVisibleForBlockTypes(blockToCheck, block) || drawFront;
            blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z + 1));
            drawBack = isSideVisibleForBlockTypes(blockToCheck, block) || drawBack;
            blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x - 1), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z));
            drawLeft = isSideVisibleForBlockTypes(blockToCheck, block) || drawLeft;
            blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + 1), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z));
            drawRight = isSideVisibleForBlockTypes(blockToCheck, block) || drawRight;
        }

        if (drawTop) {
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

    void generateVerticesForBlockSide(ChunkMesh mesh, int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm, Vector4f colorOffset, Vector3f texOffset, RENDER_TYPE renderType, Block.BLOCK_FORM blockForm) {
        ChunkMesh.VertexElements vertexElements = mesh._vertexElements[0];

        if (renderType == RENDER_TYPE.TRANS) {
            vertexElements = mesh._vertexElements[1];
        }

        switch (blockForm) {
            case CACTUS:
                generateCactusSide(p1, p2, p3, p4, norm);
                break;
            case LOWERED_BOCK:
                generateLoweredBlock(x, y, z, p1, p2, p3, p4, norm);
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

    private void generateLoweredBlock(int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm) {
        byte bottomBlock = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z));
        boolean lowerBottom = Block.getBlockForType(bottomBlock).getBlockForm() == Block.BLOCK_FORM.LOWERED_BOCK || bottomBlock == 0x0;

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
     * Returns true if the block side is adjacent to a translucent block or an air
     * block.
     *
     * @param blockToCheck The block to check
     * @param currentBlock The current block
     * @return True if the side is visible for the given block types
     */
    private boolean isSideVisibleForBlockTypes(byte blockToCheck, byte currentBlock) {
        Block bCheck = Block.getBlockForType(blockToCheck);
        Block cBlock = Block.getBlockForType(currentBlock);

        return bCheck.getClass() == BlockAir.class || cBlock.doNotTessellate() || bCheck.isBlockBillboard() || (Block.getBlockForType(blockToCheck).isBlockTypeTranslucent() && !Block.getBlockForType(currentBlock).isBlockTypeTranslucent());
    }
}
