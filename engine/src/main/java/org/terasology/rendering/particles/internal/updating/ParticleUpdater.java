/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.particles.internal.updating;

import com.google.common.collect.BiMap;
import org.slf4j.Logger;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.physics.Physics;
import org.terasology.rendering.particles.functions.affectors.AffectorFunction;
import org.terasology.rendering.particles.functions.generators.GeneratorFunction;
import org.terasology.rendering.particles.internal.data.ParticleSystemStateData;

import java.util.Collection;

/**
 * Created by Linus on 16-4-2015.
 */
public interface ParticleUpdater {

    void register(EntityRef entity);
    void dispose(EntityRef entity);

    void update(final BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions,
                float delta);

    Collection<ParticleSystemStateData> getStateData();

    public static ParticleUpdater create(Physics physics, Logger logger) {
        return new ParticleUpdaterImplementation(physics);
    }
}
