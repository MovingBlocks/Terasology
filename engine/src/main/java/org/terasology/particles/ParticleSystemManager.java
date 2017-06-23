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
package org.terasology.particles;

import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.particles.rendering.ParticleRenderingData;

import java.util.stream.Stream;

/**
 * Component system responsible for keeping track of all ParticleSystem components and updating them.
 * Also maintains a registry of generator and affector functions to be used when processing generators
 * and affectors during a particle system update.
 */

@API
public interface ParticleSystemManager {

    void registerAffectorFunction(AffectorFunction affectorFunction);

    void registerGeneratorFunction(GeneratorFunction generatorFunction);

    Stream<ParticleRenderingData> getParticleEmittersByDataComponent(Class<? extends Component> particleDataComponent);
}
