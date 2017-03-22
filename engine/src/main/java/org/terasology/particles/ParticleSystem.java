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

import com.google.common.collect.BiMap;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.components.ParticleSystemComponent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Holds the data of a particle system used to render the system.
 * Essentially a reflection of a particle system represented in the ECS.
 * A particle system's data is updated whenever the particle system is changed in it's ECS representation.
 */
public class ParticleSystem {

    /**
     * This particle system's entity (with ParticleSystemComponent).
     */
    public final EntityRef entityRef;

    /**
     * This particle system's particle pool.
     */
    public final ParticlePool particlePool;

    /**
     *
     */
    public int collisionUpdateIteration;

    public final ParticleSystemComponent particleSystemComponent;

    /**
     * Affector functions are called on each particle of a system during an update.
     */
    public final Map<Component, AffectorFunction> affectors;

    /**
     * This particle system's emitters.
     */
    public Collection<ParticleEmitter> emitters;

    public ParticleSystem(final EntityRef entityRef) {
        this.entityRef = entityRef;
        this.particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);
        this.particlePool = new ParticlePool(particleSystemComponent.particlePoolSize);
        this.collisionUpdateIteration = 0;
        this.affectors = new LinkedHashMap<>();
    }

    /**
     * Creates ParticleEmitter for each emitter to be used by the updater, called when emitter changes (add/remove/generator change)
     */
    public void update(final BiMap<Class<Component>, GeneratorFunction> generatorFunctions, final BiMap<Class<Component>, AffectorFunction> affectorFunctions) {
        if (emitters == null) {
            emitters = new ArrayList<>(particleSystemComponent.getEmitters().size());
        } else {
            emitters.clear();
        }

        // Create ParticleEmitter objects to represent each emitter.
        emitters.addAll(particleSystemComponent.getEmitters().stream()
                .filter(emitterEntity -> emitterEntity != null
                        && emitterEntity.hasComponent(ParticleEmitterComponent.class)
                        && emitterEntity.hasComponent(LocationComponent.class))
                .map(emitterEntity -> new ParticleEmitter(
                        emitterEntity.getComponent(ParticleEmitterComponent.class),
                        emitterEntity.getComponent(LocationComponent.class),
                        generatorFunctions
                )).collect(Collectors.toList()));

        fetchAffectorFunctions(affectorFunctions);
    }

    /**
     * Pairs affectors with their registered functions.
     */
    private void fetchAffectorFunctions(final BiMap<Class<Component>, AffectorFunction> affectorFunctions) {
        affectors.clear();

        for (EntityRef affector : particleSystemComponent.getAffectors()) {
            for (Component component : affector.iterateComponents()) {
                AffectorFunction function = affectorFunctions.get(component.getClass());

                if (function != null) {
                    affectors.put(component, function);
                }
            }
        }
    }

    /**
     * Stores all data needed to update an emitter.
     * A ParticleEmitter is maintained for each emitter in the system
     */
    public final class ParticleEmitter {
        public final ParticleEmitterComponent emitterComponent;
        public final LocationComponent locationComponent;

        /**
         * Generator functions are called on the new particle when this emitter emits
         */
        public final Map<Component, GeneratorFunction> generators;

        /**
         *
         */
        public float nextEmission;

        public ParticleEmitter(final ParticleEmitterComponent emitterComponent, final LocationComponent locationComponent, final BiMap<Class<Component>, GeneratorFunction> generatorFunctions) {
            this.emitterComponent = emitterComponent;
            this.locationComponent = locationComponent;
            this.generators = new LinkedHashMap<>();
            this.nextEmission = 0;

            fetchGeneratorFunctions(generatorFunctions);
        }

        /**
        * Pairs generators with their registered functions to be efficiently called later.
        */
        public void fetchGeneratorFunctions(final BiMap<Class<Component>, GeneratorFunction> generatorFunctions) {
            generators.clear();

            if (emitterComponent != null) {
                for (EntityRef generator : emitterComponent.getGenerators()) {
                    for (Component component : generator.iterateComponents()) {
                        GeneratorFunction function = generatorFunctions.get(component.getClass());

                        if (function != null) {
                            generators.put(component, function);
                        }
                    }
                }
            }
        }
    }
}
