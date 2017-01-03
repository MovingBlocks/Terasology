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
import org.terasology.particles.ParticleSystemStateData;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.particles.rendering.ParticleRenderer;
import org.terasology.physics.Physics;

import java.util.stream.Stream;

/**
 * Updates the all registered particle systems every game update. Updates a particle system's state data when it has been changed.
 */
public interface ParticleUpdater {

    /**
     * @param entity The entity with the particle system being registered.
     */
    void register(EntityRef entity);

    /**
     * @param entity The entity with the particle system being disposed of.
     */
    void dispose(EntityRef entity);

    /**
     * @param entityRef                    The entity with the particle system whose state is being updated.
     * @param registeredAffectorFunctions  The list of affector functions to use when processing the given system's affectors.
     * @param registeredGeneratorFunctions The list of generator functions to use when processing the given system's generators.
     */
    void updateStateData(final EntityRef entityRef,
                         final BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                         final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions);

    void update(float delta);

    Stream<ParticleSystemStateData> getStateDataForRenderer(Class<? extends ParticleRenderer> renderer);

    static ParticleUpdater create(Physics physics) {
        return new ParticleUpdaterImpl(physics);
    }
}
