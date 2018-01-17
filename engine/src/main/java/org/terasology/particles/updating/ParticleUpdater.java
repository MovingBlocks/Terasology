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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.particles.ParticleSystemManager;
import org.terasology.particles.components.ParticleEmitterComponent;

import java.util.Collection;

/**
 * Updates the all registered particle systems every game update. Updates a particle system's state data when it has been changed.
 */
public interface ParticleUpdater {

    /**
     * Registers a particle emitter entity to be updated each particle update.
     *
     * @param entity The entity with the {@link ParticleEmitterComponent} being registered.
     */
    void addEmitter(EntityRef entity);

    /**
     * De-registers a particle emitter, stopping it from being updated.
     *
     * @param entity The entity with the particle system being disposed of.
     */
    void removeEmitter(EntityRef entity);

    /**
     * Prepares an emitter to be efficiently handled by the particle updater.
     * Should be called on newly-added emitters and after each configuration change to an existing emitter.
     *
     * @param emitter The particle emitter that is being updated.
     */
    void configureEmitter(ParticleEmitterComponent emitter);

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
     * Invoked when the updater is first created by the {@link ParticleSystemManager}
     */
    void initialize();

    /**
     * Invoked when the particle engine is shutting down (when the game is unloading).
     * De-registers all particle emitters.
     */
    void dispose();
}
