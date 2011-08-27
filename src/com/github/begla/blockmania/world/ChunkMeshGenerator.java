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
import com.github.begla.blockmania.utilities.VectorPool;
import gnu.trove.list.array.TFloatArrayList;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkMeshGenerator {

    private Chunk _chunk;

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

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    generateBlockVertices(mesh, x, y, z);
                    generateBillboardVertices(mesh, x, y, z);
                }
            }
        }

        return mesh;
    }

    private void addLightTexCoordFor(ChunkMesh mesh, int x, int y, int z, int dirX, int dirY, int dirZ, RENDER_TYPE r, float dimming) {
        TFloatArrayList l;

        if (r == RENDER_TYPE.BILLBOARD) {
            l = mesh.texLightBillboard;
        } else if (r == RENDER_TYPE.TRANS) {
            l = mesh.texLightTranslucent;
        } else {
            l = mesh.texLightOpaque;
        }

        float sunlight = (float) _chunk.getParent().getLight(_chunk.getBlockWorldPosX(x) + dirX, _chunk.getBlockWorldPosY(y) + dirY, _chunk.getBlockWorldPosZ(z) + dirZ, Chunk.LIGHT_TYPE.SUN) / 15f;
        float blocklight = (float) _chunk.getParent().getLight(_chunk.getBlockWorldPosX(x) + dirX, _chunk.getBlockWorldPosY(y) + dirY, _chunk.getBlockWorldPosZ(z) + dirZ, Chunk.LIGHT_TYPE.BLOCK) / 15f;

        l.add(sunlight * dimming);
        l.add(blocklight * dimming);
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
        RENDER_TYPE renderType = RENDER_TYPE.BILLBOARD;

        // Ignore normal blocks
        if (!Block.getBlockForType(block).isBlockBillboard()) {
            return;
        }

        float offsetX = _chunk.getPosition().x * Configuration.CHUNK_DIMENSIONS.x;
        float offsetY = _chunk.getPosition().y * Configuration.CHUNK_DIMENSIONS.y;
        float offsetZ = _chunk.getPosition().z * Configuration.CHUNK_DIMENSIONS.z;

        /*
         * First side of the billboard
         */
        Vector4f _colorBillboardOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);
        float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
        float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX);
        mesh.texBillboard.add(texOffsetY + 0.0624f);
        mesh.quadsBillboard.add(-0.5f + x + offsetX);
        mesh.quadsBillboard.add(-0.5f + y + offsetY);
        mesh.quadsBillboard.add(z + offsetZ);

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX + 0.0624f);
        mesh.texBillboard.add(texOffsetY + 0.0624f);
        mesh.quadsBillboard.add(0.5f + x + offsetX);
        mesh.quadsBillboard.add(-0.5f + y + offsetY);
        mesh.quadsBillboard.add(z + offsetZ);

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX + 0.0624f);
        mesh.texBillboard.add(texOffsetY);
        mesh.quadsBillboard.add(0.5f + x + offsetX);
        mesh.quadsBillboard.add(0.5f + y + offsetY);
        mesh.quadsBillboard.add(z + offsetZ);

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX);
        mesh.texBillboard.add(texOffsetY);
        mesh.quadsBillboard.add(-0.5f + x + offsetX);
        mesh.quadsBillboard.add(0.5f + y + offsetY);
        mesh.quadsBillboard.add(z + offsetZ);


        /*
         * Second side of the billboard
         */
        _colorBillboardOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BACK);
        texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x;
        texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y;

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX);
        mesh.texBillboard.add(texOffsetY + 0.0624f);
        mesh.quadsBillboard.add(x + offsetX);
        mesh.quadsBillboard.add(-0.5f + y + offsetY);
        mesh.quadsBillboard.add(-0.5f + z + offsetZ);

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX + 0.0624f);
        mesh.texBillboard.add(texOffsetY + 0.0624f);
        mesh.quadsBillboard.add(x + offsetX);
        mesh.quadsBillboard.add(-0.5f + y + offsetY);
        mesh.quadsBillboard.add(0.5f + z + offsetZ);

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX + 0.0624f);
        mesh.texBillboard.add(texOffsetY);
        mesh.quadsBillboard.add(x + offsetX);
        mesh.quadsBillboard.add(0.5f + y + offsetY);
        mesh.quadsBillboard.add(0.5f + z + offsetZ);

        addLightTexCoordFor(mesh, x, y, z, 0, 0, 0, renderType, 1f);
        mesh.colorBillboard.add(_colorBillboardOffset.x);
        mesh.colorBillboard.add(_colorBillboardOffset.y);
        mesh.colorBillboard.add(_colorBillboardOffset.z);
        mesh.colorBillboard.add(_colorBillboardOffset.w);
        mesh.texBillboard.add(texOffsetX);
        mesh.texBillboard.add(texOffsetY);
        mesh.quadsBillboard.add(x + offsetX);
        mesh.quadsBillboard.add(0.5f + y + offsetY);
        mesh.quadsBillboard.add(-0.5f + z + offsetZ);
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

        // Draw the chunk if the height map produced a too large number
        if (y == Configuration.CHUNK_DIMENSIONS.y - 1) {
            drawTop = true;
        }

        if (drawTop) {
            Vector3f p1 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(0, 1, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.TOP);
            float shadowIntens = simpleOcclusionAmount(x, y, z, 0, 1, 0);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, shadowIntens, renderType);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z - 1));
        drawFront = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawFront) {
            Vector3f p1 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, -0.5f, -0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(0, 0, -1);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);
            float shadowIntens = simpleOcclusionAmount(x, y, z, 0, 0, -1);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, shadowIntens, renderType);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z + 1));
        drawBack = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawBack) {
            Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);


            Vector3f norm = VectorPool.getVector(0, 0, 1);


            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BACK);
            float shadowIntens = simpleOcclusionAmount(x, y, z, 0, 0, 1);

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, shadowIntens, renderType);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x - 1), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z));
        drawLeft = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawLeft) {
            Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(-0.5f, 0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, 0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(-1, 0, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.LEFT);
            float shadowIntens = Configuration.BLOCK_SIDE_DIMMING;

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, shadowIntens, renderType);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x + 1), _chunk.getBlockWorldPosY(y), _chunk.getBlockWorldPosZ(z));
        drawRight = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawRight) {
            Vector3f p1 = VectorPool.getVector(0.5f, 0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, 0.5f, 0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(0.5f, -0.5f, -0.5f);

            Vector3f norm = VectorPool.getVector(1, 0, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.RIGHT);
            float shadowIntens = Configuration.BLOCK_SIDE_DIMMING;

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, shadowIntens, renderType);

            VectorPool.putVector(p1);
            VectorPool.putVector(p2);
            VectorPool.putVector(p3);
            VectorPool.putVector(p4);
            VectorPool.putVector(norm);
        }

        blockToCheck = _chunk.getParent().getBlock(_chunk.getBlockWorldPosX(x), _chunk.getBlockWorldPosY(y - 1), _chunk.getBlockWorldPosZ(z));
        drawBottom = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawBottom) {
            Vector3f p1 = VectorPool.getVector(-0.5f, -0.5f, -0.5f);
            Vector3f p2 = VectorPool.getVector(0.5f, -0.5f, -0.5f);
            Vector3f p3 = VectorPool.getVector(0.5f, -0.5f, 0.5f);
            Vector3f p4 = VectorPool.getVector(-0.5f, -0.5f, 0.5f);

            Vector3f norm = VectorPool.getVector(0, -1, 0);

            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BOTTOM);
            float shadowIntens = 1f;

            Vector3f texOffset = VectorPool.getVector(Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).x, Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).y, 0f);
            generateVerticesForBlockSide(mesh, x, y, z, p1, p2, p3, p4, norm, colorOffset, texOffset, shadowIntens, renderType);

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
     * @param shadowIntensity
     * @param renderType
     */
    void generateVerticesForBlockSide(ChunkMesh mesh, int x, int y, int z, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f norm, Vector4f colorOffset, Vector3f texOffset, float shadowIntensity, RENDER_TYPE renderType) {
        float offsetX = _chunk.getPosition().x * Configuration.CHUNK_DIMENSIONS.x;
        float offsetY = _chunk.getPosition().y * Configuration.CHUNK_DIMENSIONS.y;
        float offsetZ = _chunk.getPosition().z * Configuration.CHUNK_DIMENSIONS.z;

        TFloatArrayList color = mesh.colorOpaque;
        TFloatArrayList normals = mesh.normalsOpaque;
        TFloatArrayList tex = mesh.texOpaque;
        TFloatArrayList quads = mesh.quadsOpaque;

        if (renderType == RENDER_TYPE.TRANS) {
            color = mesh.colorTranslucent;
            normals = mesh.normalsTranslucent;
            tex = mesh.texTranslucent;
            quads = mesh.quadsTranslucent;
        }

        /*
         * Rotate the texture coordinates according to the
         * orientation of the plane.
         */
        if (norm.z == 1 || norm.x == -1) {
            tex.add(texOffset.x);
            tex.add(texOffset.y + 0.0624f);

            tex.add(texOffset.x + 0.0624f);
            tex.add(texOffset.y + 0.0624f);

            tex.add(texOffset.x + 0.0624f);
            tex.add(texOffset.y);

            tex.add(texOffset.x);
            tex.add(texOffset.y);
        } else {
            tex.add(texOffset.x);
            tex.add(texOffset.y);

            tex.add(texOffset.x + 0.0624f);
            tex.add(texOffset.y);

            tex.add(texOffset.x + 0.0624f);
            tex.add(texOffset.y + 0.0624f);

            tex.add(texOffset.x);
            tex.add(texOffset.y + 0.0624f);
        }

        color.add(colorOffset.x);
        color.add(colorOffset.y);
        color.add(colorOffset.z);
        color.add(colorOffset.w);
        normals.add(norm.x);
        normals.add(norm.y);
        normals.add(norm.z);
        addLightTexCoordFor(mesh, x, y, z, (int) norm.x, (int) norm.y, (int) norm.z, renderType, shadowIntensity);
        quads.add(p1.x + x + offsetX);
        quads.add(p1.y + y + offsetY);
        quads.add(p1.z + z + offsetZ);

        color.add(colorOffset.x);
        color.add(colorOffset.y);
        color.add(colorOffset.z);
        color.add(colorOffset.w);
        normals.add(norm.x);
        normals.add(norm.y);
        normals.add(norm.z);
        addLightTexCoordFor(mesh, x, y, z, (int) norm.x, (int) norm.y, (int) norm.z, renderType, shadowIntensity);
        quads.add(p2.x + x + offsetX);
        quads.add(p2.y + y + offsetY);
        quads.add(p2.z + z + offsetZ);

        color.add(colorOffset.x);
        color.add(colorOffset.y);
        color.add(colorOffset.z);
        color.add(colorOffset.w);
        normals.add(norm.x);
        normals.add(norm.y);
        normals.add(norm.z);
        addLightTexCoordFor(mesh, x, y, z, (int) norm.x, (int) norm.y, (int) norm.z, renderType, shadowIntensity);
        quads.add(p3.x + x + offsetX);
        quads.add(p3.y + y + offsetY);
        quads.add(p3.z + z + offsetZ);

        color.add(colorOffset.x);
        color.add(colorOffset.y);
        color.add(colorOffset.z);
        color.add(colorOffset.w);
        normals.add(norm.x);
        normals.add(norm.y);
        normals.add(norm.z);
        addLightTexCoordFor(mesh, x, y, z, (int) norm.x, (int) norm.y, (int) norm.z, renderType, shadowIntensity);
        quads.add(p4.x + x + offsetX);
        quads.add(p4.y + y + offsetY);
        quads.add(p4.z + z + offsetZ);
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

    /**
     * Calculates a simple occlusion value based on the amount of blocks
     * surrounding the given block position.
     *
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
     * @param dirX
     * @param dirY
     * @param dirZ
     * @return Occlusion amount
     */
    float simpleOcclusionAmount(int x, int y, int z, int dirX, int dirY, int dirZ) {
        if (x < 0 || z < 0 || y < 0) {
            return 0;
        }

        int intens = 0;

        ArrayList<Vector3f> positions = new ArrayList<Vector3f>();
        positions.add(VectorPool.getVector(x + dirX + 1, y + dirY, z + dirZ));
        positions.add(VectorPool.getVector(x + dirX - 1, y + dirY, z + dirZ));
        positions.add(VectorPool.getVector(x + dirX, y + dirY, z + dirZ + 1));
        positions.add(VectorPool.getVector(x + dirX, y + dirY, z + dirZ - 1));

        for (Vector3f p : positions) {
            if (Block.getBlockForType(_chunk.getParent().getBlock(_chunk.getBlockWorldPosX((int) p.x), _chunk.getBlockWorldPosY((int) p.y), _chunk.getBlockWorldPosZ((int) p.z))).isCastingShadows()) {
                intens++;
            }

            VectorPool.putVector(p);
        }

        return (float) Math.pow(0.9, intens);
    }
}
