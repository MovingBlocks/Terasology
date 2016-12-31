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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Linus on 4-3-2015.
 */
public class ParticleSystemStateData {

    public final EntityRef entityRef;
    public final ParticlePool particlePool;
    public int collisionUpdateIteration;

    // Data used by the updater implementation, fetched every update
    public final FetchedData fetchedData;

    public ParticleSystemStateData(final EntityRef entityRef) {
        this.entityRef = entityRef;
        this.particlePool = new ParticlePool(entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles);
        this.collisionUpdateIteration = 0;

        this.fetchedData = new FetchedData();
    }

    public class FetchedData {

        // Affector functions called on each particle during update
        public final Map<Component, AffectorFunction> affectors;

        public ParticleSystemComponent particleSystemComponent;
        public List<EmitterData> emitters;

        public FetchedData() {
            this.affectors = new LinkedHashMap<>();
            this.particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);
        }

        /*
        * Creates EmitterData for each emitter to be used by updater, called when emitter changes (add/remove/generator change)
        * */
        public void update(final BiMap<Class<Component>, GeneratorFunction> generatorFunctions,
                           final BiMap<Class<Component>, AffectorFunction> affectorFunctions
        ) {

            if (emitters == null) {
                emitters = new ArrayList<>(particleSystemComponent.getEmitters().size());
            } else {
                emitters.clear();
            }

            // Thanks IntelliJ ;)
            emitters.addAll(particleSystemComponent.getEmitters().stream().map(emitter -> new EmitterData(
                    emitter != null ? emitter.getComponent(ParticleEmitterComponent.class) : null,
                    emitter != null ? emitter.getComponent(LocationComponent.class) : null,
                    generatorFunctions
            )).collect(Collectors.toList()));

            fetchAffectorFunctions(affectorFunctions);
        }

        /*
        * Pairs affectors with their registered functions.
        * */
        private void fetchAffectorFunctions(final BiMap<Class<Component>, AffectorFunction> affectorFunctions) {
            affectors.clear();

            for (EntityRef affector: particleSystemComponent.getAffectors()) {
                for (Component component: affector.iterateComponents()) {
                    AffectorFunction<Component> function = affectorFunctions.get(component.getClass());

                    if (function != null) {
                        affectors.put(component, function);
                    }
                }
            }
        }
    }

    // Stores all data needed to update an emitter, EmitterData maintained for each emitter in the system
    public final class EmitterData {
        public final ParticleEmitterComponent emitterComponent;
        public final LocationComponent locationComponent;

        // Generator functions called on the new particle when this emitter emits
        public final Map<Component, GeneratorFunction> generators;

        public float nextEmission;

        public EmitterData (final ParticleEmitterComponent emitterComponent, final LocationComponent locationComponent, final BiMap<Class<Component>, GeneratorFunction> generatorFunctions) {
            this.emitterComponent = emitterComponent;
            this.locationComponent = locationComponent;
            this.generators = new LinkedHashMap<>();
            this.nextEmission = 0;

            fetchGeneratorFunctions(generatorFunctions);
        }

        /*
        * Pairs generators with their registered functions.
        * */
        public void fetchGeneratorFunctions (final BiMap<Class<Component>, GeneratorFunction> generatorFunctions) {
            generators.clear();

            for (EntityRef generator: emitterComponent.getGenerators()) {
                for (Component component: generator.iterateComponents()) {
                    GeneratorFunction<Component> function = generatorFunctions.get(component.getClass());

                    if (function != null) {
                        generators.put(component, function);
                    }
                }
            }
        }
    }
}