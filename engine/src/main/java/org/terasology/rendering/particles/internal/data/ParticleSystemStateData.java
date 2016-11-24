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
package org.terasology.rendering.particles.internal.data;

import com.google.common.collect.BiMap;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.particles.components.ParticleEmitterComponent;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.rendering.particles.functions.affectors.AffectorFunction;
import org.terasology.rendering.particles.functions.generators.GeneratorFunction;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Linus on 4-3-2015.
 */
public class ParticleSystemStateData {

    public final EntityRef entityRef;
    public final ParticlePool particlePool;
    public float nextEmission;
    public int collisionUpdateIteration;

    // Data that is fetched before each update.
    public final FetchedData fetchedData;

    public ParticleSystemStateData(final EntityRef entityRef) {
        this.entityRef = entityRef;
        this.particlePool = new ParticlePool(entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles);
        this.collisionUpdateIteration = 0;
        this.nextEmission = 0;

        this.fetchedData = new FetchedData();
    }

    public class FetchedData {
        public final Map<Component, AffectorFunction> affectors;
        public final Map<Component, GeneratorFunction> generators;

        public ParticleSystemComponent particleSystemComponent;
        public ParticleEmitterComponent emitterComponent;
        public LocationComponent emitterLocationComponent;

        public FetchedData() {
            this.affectors = new LinkedHashMap<>();
            this.generators = new LinkedHashMap<>();
        }

        public void update(final BiMap<Class<Component>, GeneratorFunction> generatorFunctions,
                           final BiMap<Class<Component>, AffectorFunction> affectorFunctions
        ) {
            particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);

            emitterComponent =
                    particleSystemComponent.emitter != null
                    ? particleSystemComponent.emitter.getComponent(ParticleEmitterComponent.class)
                    : null;

            emitterLocationComponent =
                    particleSystemComponent.emitter != null
                    ? particleSystemComponent.emitter.getComponent(LocationComponent.class)
                    : null;

            fetchGeneratorFunctions(generatorFunctions);
            fetchAffectorFunctions(affectorFunctions);
        }

        private void fetchGeneratorFunctions(final BiMap<Class<Component>, GeneratorFunction> generatorFunctions) {
            generators.clear();

            for (EntityRef generator: emitterComponent.generators) {
                for (Component component: generator.iterateComponents()) {
                    GeneratorFunction<Component> function = generatorFunctions.get(component.getClass());

                    if (function != null) {
                        generators.put(component, function);
                    }
                }
            }
        }

        private void fetchAffectorFunctions(final BiMap<Class<Component>, AffectorFunction> affectorFunctions) {
            affectors.clear();

            for (EntityRef affector: entityRef.getComponent(ParticleSystemComponent.class).affectors) {
                for (Component component: affector.iterateComponents()) {
                    AffectorFunction<Component> function = affectorFunctions.get(component.getClass());

                    if (function != null) {
                        affectors.put(component, function);
                    }
                }
            }
        }
    }
}
