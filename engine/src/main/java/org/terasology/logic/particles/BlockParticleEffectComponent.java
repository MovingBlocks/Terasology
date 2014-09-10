/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.particles;

import com.google.common.collect.Lists;
import org.terasology.reflection.MappedContainer;
import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockParticleEffectComponent implements Component {

    public enum ParticleBlendMode {
        OPAQUE,
        ADD
    }

    // Can be null for non-block particles
    public BlockFamily blockType;
    // If no texture is specified, the default block texture atlas is used
    public Texture texture;

    public int spawnCount = 16;
    public boolean destroyEntityOnCompletion;
    public Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    public ParticleBlendMode blendMode = ParticleBlendMode.OPAQUE;

    // Initial conditions
    public Vector3f spawnRange = new Vector3f();
    public Vector3f initialVelocityRange = new Vector3f();
    public float minSize = 0.1f;
    public float maxSize = 1.0f;
    public float minLifespan;
    public float maxLifespan = 1.0f;

    public boolean randBlockTexDisplacement;
    public Vector2f randBlockTexDisplacementScale = new Vector2f(0.25f, 0.25f);

    // Lifetime conditions
    public Vector3f targetVelocity = new Vector3f();
    public Vector3f acceleration = new Vector3f();
    public boolean collideWithBlocks;

    public List<Particle> particles = Lists.newArrayList();

    @MappedContainer
    public static class Particle {
        public Vector3f velocity = new Vector3f();
        public Vector3f position = new Vector3f();
        public float size = 1.0f;
        public float lifeRemaining = 1.0f;
        public Vector2f texOffset = new Vector2f(0, 0);
        public Vector2f texSize = new Vector2f(1, 1);
        public Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
