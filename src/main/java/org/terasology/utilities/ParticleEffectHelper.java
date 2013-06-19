/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.utilities;

import org.terasology.asset.Assets;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Vector3f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ParticleEffectHelper {

    public static void spawnParticleEffect(Vector3f position, BlockParticleEffectComponent particleEffect) {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        EntityRef blockParticlesEntity = entityManager.create();
        blockParticlesEntity.addComponent(new LocationComponent(position));
        blockParticlesEntity.addComponent(particleEffect);
    }

    public static BlockParticleEffectComponent createSmokeExplosionParticleEffect() {
        BlockParticleEffectComponent particleEffect = new BlockParticleEffectComponent();
        particleEffect.spawnCount = 512;
        particleEffect.initialVelocityRange.set(6, 6, 6);
        particleEffect.spawnRange.set(2f, 2f, 2f);
        particleEffect.destroyEntityOnCompletion = true;
        particleEffect.minSize = 0.5f;
        particleEffect.maxSize = 1.5f;
        particleEffect.minLifespan = 1f;
        particleEffect.maxLifespan = 1.5f;
        particleEffect.targetVelocity.set(0, 2, 0);
        particleEffect.acceleration.set(2f, 2f, 2f);
        particleEffect.collideWithBlocks = true;
        particleEffect.texture = Assets.getTexture("engine:fx_smoke");
        particleEffect.blendMode = BlockParticleEffectComponent.ParticleBlendMode.ADD;
        particleEffect.color.w = 0.5f;
        particleEffect.color.x = 216.0f / 255.0f;
        particleEffect.color.y = 199.0f / 255.0f;
        particleEffect.color.z = 158.0f / 255.0f;

        return particleEffect;
    }

    public static BlockParticleEffectComponent createDefaultBlockParticleEffect(Block block) {
        BlockParticleEffectComponent blockParticleEffect = new BlockParticleEffectComponent();
        blockParticleEffect.spawnCount = 64;
        blockParticleEffect.blockType = block.getBlockFamily();
        blockParticleEffect.initialVelocityRange.set(4, 4, 4);
        blockParticleEffect.spawnRange.set(0.3f, 0.3f, 0.3f);
        blockParticleEffect.destroyEntityOnCompletion = true;
        blockParticleEffect.minSize = 0.05f;
        blockParticleEffect.maxSize = 0.1f;
        blockParticleEffect.minLifespan = 1f;
        blockParticleEffect.maxLifespan = 1.5f;
        blockParticleEffect.targetVelocity.set(0, -5, 0);
        blockParticleEffect.acceleration.set(2f, 2f, 2f);
        blockParticleEffect.collideWithBlocks = true;
        blockParticleEffect.randBlockTexDisplacement = true;

        return blockParticleEffect;
    }

    public static BlockParticleEffectComponent createDustParticleEffect() {
        BlockParticleEffectComponent particleEffect = new BlockParticleEffectComponent();
        particleEffect.spawnCount = 64;
        particleEffect.initialVelocityRange.set(4, 4, 4);
        particleEffect.spawnRange.set(0.3f, 0.3f, 0.3f);
        particleEffect.destroyEntityOnCompletion = true;
        particleEffect.minSize = 0.25f;
        particleEffect.maxSize = 1f;
        particleEffect.minLifespan = 1f;
        particleEffect.maxLifespan = 1.5f;
        particleEffect.targetVelocity.set(0, 2, 0);
        particleEffect.acceleration.set(2f, 2f, 2f);
        particleEffect.collideWithBlocks = true;
        particleEffect.texture = Assets.getTexture("engine:fx_smoke");
        particleEffect.blendMode = BlockParticleEffectComponent.ParticleBlendMode.ADD;
        particleEffect.color.w = 0.5f;
        particleEffect.color.x = 216.0f / 255.0f;
        particleEffect.color.y = 199.0f / 255.0f;
        particleEffect.color.z = 158.0f / 255.0f;

        return particleEffect;
    }
}
