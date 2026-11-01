// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles;

import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.particles.events.ParticleSystemUpdateEvent;
import org.terasology.engine.particles.rendering.ParticleRenderingData;
import org.terasology.engine.particles.updating.ParticleUpdater;
import org.terasology.engine.particles.updating.ParticleUpdaterImpl;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.context.annotation.API;

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


    @ReceiveEvent(components = ParticleEmitterComponent.class)
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

    @ReceiveEvent(components = ParticleEmitterComponent.class)
    public void onEmitterChanged(ParticleSystemUpdateEvent event, EntityRef entity, ParticleEmitterComponent emitter) {
        particleUpdater.configureEmitter(emitter);
    }

    @ReceiveEvent(components = ParticleEmitterComponent.class)
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
                // filter emitters, whose owning entity has a particleDataComponent
                .filter(emitter -> emitter.ownerEntity.hasComponent(particleDataComponent))
                // filter emitters referencing a unique particle pool
                .filter(distinctByKey(emitter -> emitter.particlePool))
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
