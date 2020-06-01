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

import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.module.sandbox.API;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.events.ParticleSystemUpdateEvent;
import org.terasology.particles.rendering.ParticleRenderingData;
import org.terasology.particles.updating.ParticleUpdater;
import org.terasology.particles.updating.ParticleUpdaterImpl;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A particle system manager implementation using events.
 * <p>
 * See {@link ParticleSystemManager} for more information.
 */
@API
@Share(ParticleSystemManager.class)
@RegisterSystem(RegisterMode.CLIENT)
public class ParticleSystemManagerImpl extends BaseComponentSystem implements UpdateSubscriberSystem, ParticleSystemManager {

    @In
    private Physics physics;

    @In
    private ModuleManager moduleManager;

    private ParticleUpdater particleUpdater;


    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterActivated(OnActivatedComponent event, EntityRef entity, ParticleEmitterComponent particleEmitterComponent) {
        particleEmitterComponent.ownerEntity = entity;
        particleEmitterComponent.locationComponent = entity.getComponent(LocationComponent.class);
        if (particleEmitterComponent.particlePool == null) {
            particleEmitterComponent.particlePool = new ParticlePool(particleEmitterComponent.maxParticles);
            particleEmitterComponent.particlePool.initRendering();
        }
        particleUpdater.addEmitter(entity);
        particleUpdater.configureEmitter(particleEmitterComponent);
    }

    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterChanged(ParticleSystemUpdateEvent event, EntityRef entity, ParticleEmitterComponent emitter) {
        particleUpdater.configureEmitter(emitter);
    }

    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterDeactivated(BeforeDeactivateComponent event, EntityRef entity, ParticleEmitterComponent particleEmitterComponent) {
        particleUpdater.removeEmitter(entity);
    }

    /**
     * Creates and initializes a new {@link ParticleUpdater}.
     */
    public void initialise() {
        particleUpdater = new ParticleUpdaterImpl(physics, moduleManager);
        particleUpdater.initialize();
    }

    /**
     * De-registers all affector and generator functions and disposes the {@link ParticleUpdater}
     */
    @Override
    public void shutdown() {
        particleUpdater.dispose();
        particleUpdater = null;
    }

    /**
     * Updates all particle emitters, first spawning new particles and then applying affectors.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    public void update(float delta) {
        particleUpdater.update(delta);
    }

    @Override
    public Stream<ParticleRenderingData> getParticleEmittersByDataComponent(Class<? extends Component> particleDataComponent) {
        return particleUpdater.getParticleEmitters().stream()
                .filter(emitter -> emitter.ownerEntity.hasComponent(particleDataComponent))  // filter emitters, whose owning entity has a particleDataComponent
                .filter(distinctByKey(emitter -> emitter.particlePool))  // filter emitters referencing a unique particle pool
                .map(emitter -> new ParticleRenderingData<>(
                        emitter.ownerEntity.getComponent(particleDataComponent),
                        emitter.particlePool
                ));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return element -> seen.add(keyExtractor.apply(element));
    }
}
