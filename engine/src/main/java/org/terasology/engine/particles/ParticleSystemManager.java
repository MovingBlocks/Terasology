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
package org.terasology.engine.particles;

import org.terasology.engine.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.engine.particles.rendering.ParticleRenderingData;

import java.util.stream.Stream;

/**
 * Component system responsible for keeping track of all {@link org.terasology.engine.particles.components.ParticleEmitterComponent} components and updating them.
 * Also maintains a registry of generator and affector functions to be used when processing generators
 * and affectors during a particle system update.
 */

@API
public interface ParticleSystemManager {

    /**
     * Gets all current emitters that have a given particle data component and returns a stream of all particle pools and their associated data for rendering.
     * A particle data component stores information used to define how the particles of the emitter it is attached to are rendered.
     *
     * @param particleDataComponent The particle data component to select emitters by.
     *
     * @return A stream of {@link ParticleRenderingData} to be used by particle renderers.
     */
    Stream<ParticleRenderingData> getParticleEmittersByDataComponent(Class<? extends Component> particleDataComponent);
}
