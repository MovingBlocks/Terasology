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
import com.github.begla.blockmania.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Particle used when blocks are destroyed.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockParticle extends Particle {

    private float _texOffset, _lightOffset;
    private float _size;
    private byte _blockType;

    public BlockParticle(int lifeTime, Vector3f position, byte blockType, BlockParticleEmitter parent) {
        super(lifeTime, position, parent);
        _blockType = blockType;
        _size = (float) ((_rand.randomDouble() + 1.0) / 2.0) * 0.08f;
        _lightOffset = (float) ((_rand.randomDouble() + 1.0) / 2.0) * 0.2f + 0.8f;
        _texOffset = (float) (((_rand.randomDouble() + 1.0) / 2.0) * (0.0624 - 0.02));

        _position.x += _rand.randomDouble() * 0.4;
        _position.y += _rand.randomDouble() * 0.4;
        _position.z += _rand.randomDouble() * 0.4;

        _lifetime *= (_rand.randomDouble() + 1.0) / 2.0;
    }

    @Override
    public boolean canMove() {
        BlockParticleEmitter pE = (BlockParticleEmitter) getParent();

        // Very simple "collision detection" for particles.
        if (pE.getParent().getBlockAtPosition(new Vector3f(_position.x + 2 * ((_velocity.x >= 0) ? _size : -_size), _position.y + 2 * ((_velocity.y >= 0) ? _size : -_size), _position.z + 2 * ((_velocity.z >= 0) ? _size : -_size))) != 0x0) {
            return false;
        }

        return true;
    }

    @Override
    protected void renderParticle() {
        glDisable(GL11.GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        BlockParticleEmitter pE = (BlockParticleEmitter) getParent();
        double lightValueSun = pE.getParent().getDaylight() * ((double) pE.getParent().getLightAtPosition(_position, Chunk.LIGHT_TYPE.SUN));
        lightValueSun = Math.pow(0.82, 15.0 - lightValueSun);
        double lightValueBlock = pE.getParent().getLightAtPosition(_position, Chunk.LIGHT_TYPE.BLOCK);
        lightValueBlock = lightValueBlock / 15.0;
        float lightValue = (float) Math.max(lightValueSun, lightValueBlock) * _lightOffset;

        Block b = Block.getBlockForType(_blockType);

        glBegin(GL_QUADS);
        GL11.glColor3f(lightValue, lightValue, lightValue);
        GL11.glTexCoord2f(b.getTextureOffsetFor(Block.SIDE.FRONT).x + _texOffset, b.getTextureOffsetFor(Block.SIDE.FRONT).y + _texOffset);
        GL11.glVertex3f(-_size, _size, -_size);
        GL11.glTexCoord2f(b.getTextureOffsetFor(Block.SIDE.FRONT).x + 0.02f + _texOffset, b.getTextureOffsetFor(Block.SIDE.FRONT).y + _texOffset);
        GL11.glVertex3f(_size, _size, -_size);
        GL11.glTexCoord2f(b.getTextureOffsetFor(Block.SIDE.FRONT).x + 0.02f + _texOffset, b.getTextureOffsetFor(Block.SIDE.FRONT).y + 0.02f + _texOffset);
        GL11.glVertex3f(_size, -_size, -_size);
        GL11.glTexCoord2f(b.getTextureOffsetFor(Block.SIDE.FRONT).x + _texOffset, b.getTextureOffsetFor(Block.SIDE.FRONT).y + 0.02f + _texOffset);
        GL11.glVertex3f(-_size, -_size, -_size);
        glEnd();

        glDisable(GL_BLEND);
        glDisable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_CULL_FACE);
    }
}
