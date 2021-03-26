/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.engine.particles.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.module.sandbox.API;
import org.terasology.engine.particles.ParticlePool;
import org.terasology.engine.particles.functions.affectors.AffectorFunction;
import org.terasology.engine.particles.functions.generators.GeneratorFunction;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a particle emitter. A particle emitter will emit particles when enabled.
 *
 * Generator components can be attached to the emitter entity to configure the properties of newly emitted particles.
 *
 * Affector components can be attached to the emitter entity to update the fields of particles each frame, Ex.) Forces, Size change.
 *
 * It is up to the content-creator to attach a Particle Data component to the emitter entity and
 * then provide a renderer that renders that particle data.
 * See ParticleDataSpriteComponent and SpriteParticleRenderer for an example.
 */
@API
public class ParticleEmitterComponent implements Component {
    public static final int INFINITE_PARTICLE_SPAWNS = -1;
    public static final int INDEFINITE_EMITTER_LIFETIME = -1;

    /**
     * The amount of particles in this system's pool. Emitters under this system will share the particles.
     */
    public int maxParticles = 250;

    /**
     * Toggles if this system's particles should collide with blocks.
     */
    public boolean particleCollision = true;

    /**
     * The maximum spawn rate of this emitter in particles / second
     */
    public float spawnRateMax = 11.0f;

    /**
     * The minimum spawn rate of this emitter in particles / second
     */
    public float spawnRateMin = 9.0f;

    /**
     * Toggles if this particle emitter should emit new particles.
     */
    public boolean enabled = true;

    /**
     * The remaining life time of this emitter in seconds, the emitter will auto-remove upon reaching 0 TODO: Implement emitter lifetime
     */
    public float lifeTime = INDEFINITE_EMITTER_LIFETIME;

    /**
     * The maximum amount of particle this emitter can emit before auto-removing, the emitter will auto-remove upon reaching 0 TODO: Implement emitter max spawns
     */
    public int particleSpawnsLeft = INFINITE_PARTICLE_SPAWNS;

    /**
     * Toggles whether when this emitter is destroyed only the component should be destroyed or the whole entity.
     */
    public boolean destroyEntityWhenDead;

    // --------------
    // Runtime Fields
    // --------------

    /**
     * Reference to the entity this component is attached to
     */
    public EntityRef ownerEntity;

    /**
     * This emitter's particle pool.
     */
    public transient ParticlePool particlePool;

    /**
     * Maps Generator component -> Function that processes that Generator
     */
    public final Map<Component, GeneratorFunction> generatorFunctionMap = new LinkedHashMap<>();

    /**
     * Maps Affector component -> Function that processes that Affector
     */
    public final Map<Component, AffectorFunction> affectorFunctionMap = new LinkedHashMap<>();

    /**
     * This emitter's location component, for efficient getting during particle emission.
     */
    public LocationComponent locationComponent;

    /**
     * Seconds remaining until next emission
     */
    public float nextEmission;

    /**
     * Individual particle offset to start calculating particle collisions from.
     * Allows checking only some particles each update since it's a heavy operation.
     */
    public int collisionUpdateIteration;
}
