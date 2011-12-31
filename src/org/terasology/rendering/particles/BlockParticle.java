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
package org.terasology.rendering.particles;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockManager;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Particle used when blocks are destroyed.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockParticle extends Particle {

    private static final float TEX_SIZE = Block.TEXTURE_OFFSET / 4f;

    private final float _texOffsetX;
    private final float _texOffsetY;
    private final float _lightOffset;
    private final byte _blockType;

    private static final int[] _displayLists = new int[BlockManager.getInstance().availableBlocksSize()];

    public BlockParticle(int lifeTime, Vector3d position, byte blockType, BlockParticleEmitter parent) {
        super(lifeTime, position, parent);

        _blockType = blockType;

        // Random values
        _size = (float) ((_rand.randomDouble() + 1.0) / 2.0) * 0.05f + 0.05f;

        _lightOffset = (float) ((_rand.randomDouble() + 1.0) / 2.0) * 0.05f + 0.95f;
        _texOffsetX = (float) (((_rand.randomDouble() + 1.0) / 2.0) * (Block.TEXTURE_OFFSET - TEX_SIZE));
        _texOffsetY = (float) (((_rand.randomDouble() + 1.0) / 2.0) * (Block.TEXTURE_OFFSET - TEX_SIZE));

        _position.x += _rand.randomDouble() * 0.3;
        _position.y += _rand.randomDouble() * 0.3;
        _position.z += _rand.randomDouble() * 0.3;

        _lifetime *= (_rand.randomDouble() + 1.0) / 2.0;
    }

    @Override
    public boolean canMoveVertically() {
        BlockParticleEmitter pE = (BlockParticleEmitter) getParent();
        // Very simple "collision detection" for particles.
        return pE.getParent().getWorldProvider().getBlockAtPosition(new Vector3d(_position.x, _position.y + 2 * ((_velocity.y >= 0) ? _size : -_size), _position.z)) == 0x0;
    }

    protected void renderParticle() {
        if (_displayLists[_blockType] == 0) {
            _displayLists[_blockType] = glGenLists(1);
            glNewList(_displayLists[_blockType], GL11.GL_COMPILE);
            drawParticle();
            glEndList();
        }

        float lightValue = _parent.getParent().getRenderingLightValueAt(_position);

        // Apply biome and overall color offset
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(3);
        Vector4f color = BlockManager.getInstance().getBlock(_blockType).calcColorOffsetFor(Block.SIDE.FRONT, _parent.getParent().getActiveTemperature(), _parent.getParent().getActiveHumidity());
        colorBuffer.put(color.x);
        colorBuffer.put(color.y);
        colorBuffer.put(color.z);
        colorBuffer.flip();

        int colorOffset = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "colorOffset");
        int light = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "light");
        int texOffsetX = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "texOffsetX");
        int texOffsetY = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "texOffsetY");
        GL20.glUniform1f(light, lightValue * _lightOffset);
        GL20.glUniform1f(texOffsetX, _texOffsetX);
        GL20.glUniform1f(texOffsetY, _texOffsetY);
        GL20.glUniform3(colorOffset, colorBuffer);

        glCallList(_displayLists[_blockType]);
    }

    private void drawParticle() {
        Block b = BlockManager.getInstance().getBlock(_blockType);

        glBegin(GL_QUADS);
        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x, b.calcTextureOffsetFor(Block.SIDE.FRONT).y);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x + TEX_SIZE, b.calcTextureOffsetFor(Block.SIDE.FRONT).y);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x + TEX_SIZE, b.calcTextureOffsetFor(Block.SIDE.FRONT).y + TEX_SIZE);
        GL11.glVertex3f(0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x, b.calcTextureOffsetFor(Block.SIDE.FRONT).y + TEX_SIZE);
        GL11.glVertex3f(-0.5f, 0.5f, 0.0f);
        glEnd();

    }
}
