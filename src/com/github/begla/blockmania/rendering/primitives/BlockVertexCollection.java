package com.github.begla.blockmania.rendering.primitives;


import com.github.begla.blockmania.model.blocks.Block;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class BlockVertexCollection {
    public static void addBlockMesh(Vector4f color, float size, float light1, float light2, float posX, float posY, float posZ) {
        Vector2f defaultSize = new Vector2f(1.0f, 1.0f);
        Vector2f defaultOffset = new Vector2f(0.0f, 0.0f);
        addBlockMesh(color, defaultOffset, defaultSize, size, light1, light2, posX, posY, posZ);
    }

    public static void addBlockMesh(Vector4f color, Vector2f texOffset, Vector2f texSize, float size, float light1, float light2, float posX, float posY, float posZ) {
        Vector2f[] sizes = new Vector2f[6];
        Vector2f[] offsets = new Vector2f[6];

        for (int i = 0; i < 6; i++) {
            sizes[i] = texSize;
            offsets[i] = texOffset;
        }

        addBlockMesh(color, offsets, sizes, size, light1, light2, posX, posY, posZ);
    }

    public static void addBlockMesh(Vector4f color, Vector2f[] texOffsets, Vector2f[] texSizes, float size, float light1, float light2, float posX, float posY, float posZ) {
        final float sizeHalf = size / 2;

        BlockTessellator.getInstance().resetParams();
        BlockTessellator.getInstance().setColor(new Vector4f(light1 * color.x, light1 * color.y, light1 * color.z, color.w));

        Vector3f t1 = new Vector3f(-sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ);
        Vector3f t2 = new Vector3f(sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ);
        Vector3f t3 = new Vector3f(sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ);
        Vector3f t4 = new Vector3f(-sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[0].x, texOffsets[0].y));
        BlockTessellator.getInstance().addVertex(t1);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[0].x + texSizes[0].x, texOffsets[0].y));
        BlockTessellator.getInstance().addVertex(t2);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[0].x + texSizes[0].x, texOffsets[0].y + texSizes[0].y));
        BlockTessellator.getInstance().addVertex(t3);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[0].x, texOffsets[0].y + texSizes[0].y));
        BlockTessellator.getInstance().addVertex(t4);

        Vector3f l1 = new Vector3f(-sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ);
        Vector3f l2 = new Vector3f(-sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ);
        Vector3f l3 = new Vector3f(-sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ);
        Vector3f l4 = new Vector3f(-sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[1].x, texOffsets[1].y + texSizes[1].y));
        BlockTessellator.getInstance().addVertex(l1);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[1].x + texSizes[1].x, texOffsets[1].y + texSizes[1].y));
        BlockTessellator.getInstance().addVertex(l2);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[1].x + texSizes[1].x, texOffsets[1].y));
        BlockTessellator.getInstance().addVertex(l3);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[1].x, texOffsets[1].y));
        BlockTessellator.getInstance().addVertex(l4);

        Vector3f r1 = new Vector3f(sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ);
        Vector3f r2 = new Vector3f(sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ);
        Vector3f r3 = new Vector3f(sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ);
        Vector3f r4 = new Vector3f(sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[2].x, texOffsets[2].y));
        BlockTessellator.getInstance().addVertex(r1);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[2].x + texSizes[2].x, texOffsets[2].y));
        BlockTessellator.getInstance().addVertex(r2);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[2].x + texSizes[2].x, texOffsets[2].y + texSizes[2].y));
        BlockTessellator.getInstance().addVertex(r3);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[2].x, texOffsets[2].y + texSizes[2].y));
        BlockTessellator.getInstance().addVertex(r4);

        BlockTessellator.getInstance().setColor(new Vector4f(light2 * color.x, light2 * color.y, light2 * color.z, color.w));

        Vector3f f1 = new Vector3f(-sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ);
        Vector3f f2 = new Vector3f(sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ);
        Vector3f f3 = new Vector3f(sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ);
        Vector3f f4 = new Vector3f(-sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[3].x, texOffsets[3].y));
        BlockTessellator.getInstance().addVertex(f1);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[3].x + texSizes[3].x, texOffsets[3].y));
        BlockTessellator.getInstance().addVertex(f2);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[3].x + texSizes[3].x, texOffsets[3].y + texSizes[3].y));
        BlockTessellator.getInstance().addVertex(f3);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[3].x, texOffsets[3].y + texSizes[3].y));
        BlockTessellator.getInstance().addVertex(f4);

        Vector3f b1 = new Vector3f(-sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ);
        Vector3f b2 = new Vector3f(sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ);
        Vector3f b3 = new Vector3f(sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ);
        Vector3f b4 = new Vector3f(-sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[4].x, texOffsets[4].y + texSizes[4].y));
        BlockTessellator.getInstance().addVertex(b1);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[4].x + texSizes[4].x, texOffsets[4].y + texSizes[4].y));
        BlockTessellator.getInstance().addVertex(b2);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[4].x + texSizes[4].x, texOffsets[4].y));
        BlockTessellator.getInstance().addVertex(b3);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[4].x, texOffsets[4].y));
        BlockTessellator.getInstance().addVertex(b4);

        Vector3f bo1 = new Vector3f(-sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ);
        Vector3f bo2 = new Vector3f(sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ);
        Vector3f bo3 = new Vector3f(sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ);
        Vector3f bo4 = new Vector3f(-sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[5].x, texOffsets[5].y));
        BlockTessellator.getInstance().addVertex(bo1);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[5].x + texSizes[5].x, texOffsets[5].y));
        BlockTessellator.getInstance().addVertex(bo2);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[5].x + texSizes[5].x, texOffsets[5].y + texSizes[5].y));
        BlockTessellator.getInstance().addVertex(bo3);
        BlockTessellator.getInstance().setTex(new Vector2f(texOffsets[5].x, texOffsets[5].y + texSizes[5].y));
        BlockTessellator.getInstance().addVertex(bo4);
    }

    public static void addTexturedBlock(Block block, float size, float light1, float light2) {
        Vector2f[] sizes = new Vector2f[6];
        Vector2f[] offsets = new Vector2f[6];

        for (int i = 0; i < 6; i++) {
            sizes[i] = new Vector2f(Block.TEXTURE_OFFSET_WIDTH, Block.TEXTURE_OFFSET_WIDTH);
            offsets[i] = new Vector2f(block.calcTextureOffsetFor(Block.SIDE.values()[i]));
        }

        addBlockMesh(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), offsets, sizes, size, light1, light2, 0, 0, 0);
    }

    public static void addTexturedBillboard(Block block, float size, float light1) {
        final float sizeHalf = size / 2;

        BlockTessellator.getInstance().resetParams();
        BlockTessellator.getInstance().setColor(new Vector4f(light1, light1, light1, 1.0f));

        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.LEFT).x, block.calcTextureOffsetFor(Block.SIDE.LEFT).y + Block.TEXTURE_OFFSET_WIDTH));
        BlockTessellator.getInstance().addVertex(new Vector3f(0f, -sizeHalf, -sizeHalf));
        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.LEFT).x + Block.TEXTURE_OFFSET_WIDTH, block.calcTextureOffsetFor(Block.SIDE.LEFT).y + Block.TEXTURE_OFFSET_WIDTH));
        BlockTessellator.getInstance().addVertex(new Vector3f(0f, -sizeHalf, sizeHalf));
        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.LEFT).x + Block.TEXTURE_OFFSET_WIDTH, block.calcTextureOffsetFor(Block.SIDE.LEFT).y));
        BlockTessellator.getInstance().addVertex(new Vector3f(0f, sizeHalf, sizeHalf));
        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.LEFT).x, block.calcTextureOffsetFor(Block.SIDE.LEFT).y));
        BlockTessellator.getInstance().addVertex(new Vector3f(0f, sizeHalf, -sizeHalf));

        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.BACK).x, block.calcTextureOffsetFor(Block.SIDE.BACK).y + Block.TEXTURE_OFFSET_WIDTH));
        BlockTessellator.getInstance().addVertex(new Vector3f(-sizeHalf, -sizeHalf, 0f));
        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.BACK).x + Block.TEXTURE_OFFSET_WIDTH, block.calcTextureOffsetFor(Block.SIDE.BACK).y + Block.TEXTURE_OFFSET_WIDTH));
        BlockTessellator.getInstance().addVertex(new Vector3f(sizeHalf, -sizeHalf, 0f));
        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.BACK).x + Block.TEXTURE_OFFSET_WIDTH, block.calcTextureOffsetFor(Block.SIDE.BACK).y));
        BlockTessellator.getInstance().addVertex(new Vector3f(sizeHalf, sizeHalf, 0f));
        BlockTessellator.getInstance().setTex(new Vector2f(block.calcTextureOffsetFor(Block.SIDE.BACK).x, block.calcTextureOffsetFor(Block.SIDE.BACK).y));
        BlockTessellator.getInstance().addVertex(new Vector3f(-sizeHalf, sizeHalf, 0f));
    }
}
