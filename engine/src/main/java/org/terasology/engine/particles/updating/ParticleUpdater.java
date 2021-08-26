// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.updating;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.particles.ParticleSystemManager;
import org.terasology.engine.particles.components.ParticleEmitterComponent;

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
