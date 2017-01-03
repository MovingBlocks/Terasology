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

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.module.sandbox.API;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.components.ParticleSystemComponent;
import org.terasology.particles.events.ParticleSystemUpdateEvent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.particles.rendering.ParticleRenderer;
import org.terasology.particles.updating.ParticleUpdater;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;

import java.util.stream.Stream;

/**
 * See ParticleSystemManager for more information.
 */
@API
@Share(ParticleSystemManager.class)
@RegisterSystem(RegisterMode.CLIENT)
public class ParticleSystemManagerImpl extends BaseComponentSystem implements UpdateSubscriberSystem, ParticleSystemManager {

    private static final Logger logger = LoggerFactory.getLogger(ParticleSystemManagerImpl.class);

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private Physics physics;

    private ParticleUpdater particleUpdater;

    private BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions = HashBiMap.create();
    private BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions = HashBiMap.create();


    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onSystemActivated(OnActivatedComponent event, EntityRef entity, ParticleSystemComponent particleSystemComponent) {
        particleSystemComponent.ownerEntity = entity;

        particleUpdater.register(entity);

        particleUpdater.updateStateData(entity, registeredAffectorFunctions, registeredGeneratorFunctions);
    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onSystemDeactivated(BeforeDeactivateComponent event, EntityRef entity, ParticleSystemComponent particleSystemComponent) {
        particleUpdater.dispose(entity);
    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onSystemChanged(ParticleSystemUpdateEvent event, EntityRef entity, ParticleSystemComponent particleSystemComponent) {
        particleUpdater.updateStateData(entity, registeredAffectorFunctions, registeredGeneratorFunctions);
    }

    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterActivated(OnActivatedComponent event, EntityRef entity, ParticleEmitterComponent particleEmitterComponent) {
        particleEmitterComponent.ownerEntity = entity;
    }

    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterChanged(ParticleSystemUpdateEvent event, EntityRef entity) {
        particleUpdater.updateStateData(entity.getOwner(), registeredAffectorFunctions, registeredGeneratorFunctions);
    }


    public void initialise() {
        particleUpdater = ParticleUpdater.create(physics);
    }

    @Override
    public void shutdown() {
        registeredAffectorFunctions.clear();
        registeredGeneratorFunctions.clear();

        particleUpdater = null;
    }

    public void update(float delta) {
        particleUpdater.update(delta);
    }

    @Override
    public void registerGeneratorFunction(GeneratorFunction generatorFunction) {
        Preconditions.checkArgument(!registeredGeneratorFunctions.containsKey(generatorFunction.getComponentClass()),
                "Tried to register an GeneratorFunction for %s twice", generatorFunction
        );

        logger.info("Registering GeneratorFunction for Component class {}", generatorFunction.getComponentClass());
        registeredGeneratorFunctions.put(generatorFunction.getComponentClass(), generatorFunction);
    }

    @Override
    public Stream<ParticleSystemStateData> getStateDataForRenderer(Class<? extends ParticleRenderer> renderer) {
        return particleUpdater.getStateDataForRenderer(renderer);
    }

    @Override
    public void registerAffectorFunction(AffectorFunction affectorFunction) {
        Preconditions.checkArgument(!registeredAffectorFunctions.containsKey(affectorFunction.getComponentClass()),
                "Tried to register an AffectorFunction for %s twice", affectorFunction
        );

        logger.info("Registering AffectorFunction for Component class {}", affectorFunction.getComponentClass());
        registeredAffectorFunctions.put(affectorFunction.getComponentClass(), affectorFunction);
    }
}