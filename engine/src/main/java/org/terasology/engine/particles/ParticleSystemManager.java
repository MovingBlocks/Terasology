// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles;

import org.terasology.engine.particles.rendering.ParticleRenderingData;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

import java.util.stream.Stream;

/**
 * Component system responsible for keeping track of all {@link org.terasology.engine.particles.components.ParticleEmitterComponent}
 * components and updating them.
 * Also maintains a registry of generator and affector functions to be used when processing generators
 * and affectors during a particle system update.
 */

@API
public interface ParticleSystemManager {

    /**
     * Gets all current emitters that have a given particle data component and returns a stream of all particle pools
     * and their associated data for rendering.
     * A particle data component stores information used to define how the particles of the emitter it is attached to are rendered.
     *
     * @param particleDataComponent The particle data component to select emitters by.
     *
     * @return A stream of {@link ParticleRenderingData} to be used by particle renderers.
     */
    Stream<ParticleRenderingData> getParticleEmittersByDataComponent(Class<? extends Component> particleDataComponent);
}
