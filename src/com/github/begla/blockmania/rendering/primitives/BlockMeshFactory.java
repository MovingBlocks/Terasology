package com.github.begla.blockmania.rendering.primitives;


import com.github.begla.blockmania.logic.manager.TextureManager;

import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;

public class BlockMeshFactory {

    /* SINGLETON */
    private static BlockMeshFactory _instance;

    public static BlockMeshFactory getInstance() {
        if (_instance == null)
            _instance = new BlockMeshFactory();

        return _instance;
    }

    private BlockMeshFactory() {

    }

    public BlockTessellator.BlockMesh generateItemMesh(int posX, int posY) {
        TextureManager.Texture tex = TextureManager.getInstance().getTexture("items");
        ByteBuffer buffer = tex.data;

        posX *= 16;
        posY *= 16;

        int stride = tex.width * 4;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int r = buffer.get((posY + y) * stride + (posX + x) * 4) & 255;
                int g = buffer.get((posY + y) * stride + (posX + x) * 4 + 1) & 255;
                int b = buffer.get((posY + y) * stride + (posX + x) * 4 + 2) & 255;
                int a = buffer.get((posY + y) * stride + (posX + x) * 4 + 3) & 255;

                if (a != 0) {
                    BlockVertexCollection.addBlockMesh(new Vector4f(r / 255f, g / 255f, b / 255f, 1.0f), 2f * 0.0625f, 1.0f, 0.5f, 2f * 0.0625f * x - 1f / 2f, 2f * 0.0625f * (16 - y) - 1f, 0f);
                }
            }
        }

        BlockTessellator.BlockMesh result = BlockTessellator.getInstance().generateBlockMesh();
        BlockTessellator.getInstance().resetAll();

        return result;
    }

}
