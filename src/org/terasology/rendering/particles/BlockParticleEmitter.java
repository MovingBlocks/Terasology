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

import org.terasology.logic.manager.ShaderManager;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Emits block particles.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockParticleEmitter extends ParticleEmitter {

    private byte _currentBlockType = BlockManager.getInstance().getBlock("Dirt").getId();

    public BlockParticleEmitter(WorldRenderer parent) {
        super(parent);
    }

    public void emitParticles(int amount, byte blockType) {
        _currentBlockType = blockType;
        super.emitParticles(amount);
    }

    public void render() {
        ShaderManager.getInstance().enableShader("particle");
        super.render();
    }

    @Override
    protected Particle createParticle() {
        return new BlockParticle(256, _origin, _currentBlockType, this);
    }
}
