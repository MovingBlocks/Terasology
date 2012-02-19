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
package org.terasology.model.inventory;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.TextureManager;
import org.terasology.math.Side;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;

import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class ItemBlock extends Item {

    private BlockGroup _blockGroup;

    public ItemBlock(BlockGroup blockGroup) {
        _blockGroup = blockGroup;
        _toolId = (byte) 1;
    }

    public ItemBlock(BlockGroup blockGroup, int amount) {
        this(blockGroup);
        setAmount(amount);
    }

    @Override
    public boolean renderFirstPersonView(Player player) {
        Block activeBlock = _blockGroup.getArchetypeBlock();

        TextureManager.getInstance().bindTexture("terrain");
        ShaderManager.getInstance().enableShader("block");

        // Adjust the brightness of the block according to the current position of the player
        int light = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("block"), "light");
        GL20.glUniform1f(light, Terasology.getInstance().getActiveWorldRenderer().getRenderingLightValue());

        // Apply biome and overall color offset
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(3);
        Vector4f color = activeBlock.calcColorOffsetFor(Side.FRONT, Terasology.getInstance().getActiveWorldRenderer().getActiveTemperature(), Terasology.getInstance().getActiveWorldRenderer().getActiveTemperature());
        colorBuffer.put(color.x);
        colorBuffer.put(color.y);
        colorBuffer.put(color.z);

        colorBuffer.flip();
        int colorOffset = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("block"), "colorOffset");
        GL20.glUniform3(colorOffset, colorBuffer);

        glEnable(GL11.GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glAlphaFunc(GL_GREATER, 0.1f);
        if (activeBlock.isTranslucent()) {
            glEnable(GL11.GL_ALPHA_TEST);
        }

        glPushMatrix();

        glTranslatef(1.0f, -1.3f + (float) player.calcBobbingOffset((float) Math.PI / 8f, 0.05f, 2.5f) - player.getHandMovementAnimationOffset() * 0.5f, -1.5f - player.getHandMovementAnimationOffset() * 0.5f);
        glRotatef(-25f - player.getHandMovementAnimationOffset() * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(35f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0f, 0.25f, 0f);

        activeBlock.render();

        glPopMatrix();

        if (activeBlock.isTranslucent()) {
            glDisable(GL11.GL_ALPHA_TEST);
        }
        glDisable(GL11.GL_BLEND);

        ShaderManager.getInstance().enableShader(null);

        return true;
    }

    public BlockGroup getBlockGroup() {
        return _blockGroup;
    }

    public boolean equals(Object o) {
        if (o != null) {
            if (o.getClass() == ItemBlock.class) {
                ItemBlock itemBlock = (ItemBlock) o;
                return itemBlock.getBlockGroup() == getBlockGroup();
            }
        }

        return false;
    }
}
