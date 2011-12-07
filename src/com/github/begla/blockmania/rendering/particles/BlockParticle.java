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
package com.github.begla.blockmania.rendering.particles;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.rendering.manager.ShaderManager;
import com.github.begla.blockmania.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Particle used when blocks are destroyed.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockParticle extends Particle {

    private final float _texOffsetX;
    private final float _texOffsetY;
    private final float _lightOffset;
    private final byte _blockType;

    private static final int[] _displayLists = new int[BlockManager.getInstance().availableBlocksSize()];

    public BlockParticle(int lifeTime, Vector3f position, byte blockType, BlockParticleEmitter parent) {
        super(lifeTime, position, parent);

        _blockType = blockType;

        // Random values
        _size = (float) ((_rand.randomDouble() + 1.0) / 2.0) * 0.05f + 0.05f;

        _lightOffset = (float) ((_rand.randomDouble() + 1.0) / 2.0) * 0.05f + 0.95f;
        _texOffsetX = (float) (((_rand.randomDouble() + 1.0) / 2.0) * (0.0624 - 0.02));
        _texOffsetY = (float) (((_rand.randomDouble() + 1.0) / 2.0) * (0.0624 - 0.02));

        _position.x += _rand.randomDouble() * 0.3;
        _position.y += _rand.randomDouble() * 0.3;
        _position.z += _rand.randomDouble() * 0.3;

        _lifetime *= (_rand.randomDouble() + 1.0) / 2.0;
    }

    @Override
    public boolean canMoveVertically() {
        BlockParticleEmitter pE = (BlockParticleEmitter) getParent();
        // Very simple "collision detection" for particles.
        return pE.getParent().getWorldProvider().getBlockAtPosition(new Vector3f(_position.x, _position.y + 2 * ((_velocity.y >= 0) ? _size : -_size), _position.z)) == 0x0;
    }

    protected void renderParticle() {
        if (_displayLists[_blockType] == 0) {
            _displayLists[_blockType] = glGenLists(1);
            glNewList(_displayLists[_blockType], GL11.GL_COMPILE);
            drawParticle();
            glEndList();
        }

        BlockParticleEmitter pE = (BlockParticleEmitter) getParent();
        double lightValueSun = ((double) pE.getParent().getWorldProvider().getLightAtPosition(_position, Chunk.LIGHT_TYPE.SUN));
        lightValueSun = (lightValueSun / 15.0) * pE.getParent().getDaylight();
        double lightValueBlock = pE.getParent().getWorldProvider().getLightAtPosition(_position, Chunk.LIGHT_TYPE.BLOCK);
        lightValueBlock = lightValueBlock / 15.0;

        float lightValue = (float) Math.max(lightValueSun, lightValueBlock) * _lightOffset;

        int light = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "light");
        int texOffsetX = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "texOffsetX");
        int texOffsetY = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("particle"), "texOffsetY");
        GL20.glUniform1f(light, lightValue);
        GL20.glUniform1f(texOffsetX, _texOffsetX);
        GL20.glUniform1f(texOffsetY, _texOffsetY);

        glCallList(_displayLists[_blockType]);
    }

    private void drawParticle() {
        Block b = BlockManager.getInstance().getBlock(_blockType);

        glBegin(GL_QUADS);
        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x, b.calcTextureOffsetFor(Block.SIDE.FRONT).y);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x + 0.02f, b.calcTextureOffsetFor(Block.SIDE.FRONT).y);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x + 0.02f, b.calcTextureOffsetFor(Block.SIDE.FRONT).y + 0.02f);
        GL11.glVertex3f(0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Block.SIDE.FRONT).x, b.calcTextureOffsetFor(Block.SIDE.FRONT).y + 0.02f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.0f);
        glEnd();

    }
}
