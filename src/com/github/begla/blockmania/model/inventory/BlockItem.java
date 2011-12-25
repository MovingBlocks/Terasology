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
package com.github.begla.blockmania.model.inventory;

import com.github.begla.blockmania.logic.manager.TextureManager;
import com.github.begla.blockmania.model.blocks.Block;
import com.github.begla.blockmania.model.blocks.BlockManager;
import org.lwjgl.opengl.GL11;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class BlockItem extends Item {

    private byte _blockId;

    public BlockItem(byte blockId) {
        _blockId = blockId;
        _toolId = (byte) 1;
    }

    public BlockItem(byte blockId, int amount) {
        this(blockId);
        setAmount(amount);
    }

    @Override
    public void renderIcon() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPushMatrix();
        GL11.glScalef(20f, 20f, 20f);
        GL11.glRotatef(170f, 1f, 0f, 0f);
        GL11.glRotatef(-16f, 0f, 1f, 0f);
        TextureManager.getInstance().bindTexture("terrain");

        Block block = BlockManager.getInstance().getBlock(_blockId);
        block.render();

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void render() {

    }

    public byte getBlockId() {
        return _blockId;
    }

    public boolean equals(Object o) {
        if (o != null) {
            if (o.getClass() == BlockItem.class) {
                BlockItem blockItem = (BlockItem) o;
                return blockItem.getBlockId() == getBlockId();
            }
        }

        return false;
    }
}
