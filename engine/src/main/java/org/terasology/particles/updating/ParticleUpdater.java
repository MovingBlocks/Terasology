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
package org.terasology.particles.updating;

import com.google.common.collect.BiMap;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.physics.Physics;

import java.util.Collection;

/**
 * Updates the all registered particle systems every game update. Updates a particle system's state data when it has been changed.
 */
public interface ParticleUpdater {

    /**
     * Registers a newly-added particle emitter entity to be updated each particle update.
     *
     * @param entity The entity with the {@link ParticleEmitterComponent} being registered.
     */
    void register(EntityRef entity);

    /**
     * @param entity The entity with the particle system being disposed of.
     */
    void dispose(EntityRef entity);

    /**
     * Prepares an emitter to be efficiently handled by the particle updater.
     * Should be called on newly-added emitters and after each configuration change to an existing emitter.
     *
     * @param emitter The particle emitter that is being updated.
     * @param registeredAffectorFunctions The list of affector functions to use when processing the given system's affectors.
     * @param registeredGeneratorFunctions The list of generator functions to use when processing the given system's generators.
     */
    void configureEmitter(ParticleEmitterComponent emitter,
                          BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                          BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions);

    /**
     * Updates all particle emitters, first spawning new particles and then applying affectors.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    void update(float delta);

    /**
     *
     * @return All current particle emitters.
     */
    Collection<ParticleEmitterComponent> getParticleEmitters();

    /**
     * Initializes a new particle updater.
     *
     * @param physics The physics system to be used when simulating particle physics (collisions).
     *
     * @return A newly configured particle updater.
     */
    static ParticleUpdater create(Physics physics) {
        return new ParticleUpdaterImpl(physics);
    }
}
