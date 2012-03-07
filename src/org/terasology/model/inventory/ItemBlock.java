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
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.Side;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.rendering.shader.ShaderProgram;

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

    @Override
    public void renderFirstPersonView(Player player) {
        Block activeBlock = _blockGroup.getArchetypeBlock();

        // Adjust the brightness of the block according to the current position of the player
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();

        // Apply biome and overall color offset
        // TODO: Should get temperature, etc from world provider
        Vector4f color = activeBlock.calcColorOffsetFor(Side.FRONT, Terasology.getInstance().getActiveWorldRenderer().getActiveTemperature(player.getPosition()), Terasology.getInstance().getActiveWorldRenderer().getActiveTemperature(player.getPosition()));
        shader.setFloat3("colorOffset", color.x, color.y, color.z);

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

        activeBlock.renderWithLightValue(Terasology.getInstance().getActiveWorldRenderer().getRenderingLightValue());

        glPopMatrix();

        if (activeBlock.isTranslucent()) {
            glDisable(GL11.GL_ALPHA_TEST);
        }
        glDisable(GL11.GL_BLEND);
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
