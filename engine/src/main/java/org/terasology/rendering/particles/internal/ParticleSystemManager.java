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
package org.terasology.rendering.particles.internal;

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
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.module.sandbox.API;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.rendering.particles.ParticleManagerInterface;
import org.terasology.rendering.particles.components.OverheadParticleSystem;
import org.terasology.rendering.particles.components.ParticleEmitterComponent;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.rendering.particles.events.ParticleSystemUpdateEvent;
import org.terasology.rendering.particles.functions.affectors.AffectorFunction;
import org.terasology.rendering.particles.functions.affectors.DampingAffectorFunction;
import org.terasology.rendering.particles.functions.affectors.EnergyColorAffectorFunction;
import org.terasology.rendering.particles.functions.affectors.EnergyScaleAffectorFunction;
import org.terasology.rendering.particles.functions.affectors.ForceAffectorFunction;
import org.terasology.rendering.particles.functions.affectors.ForwardEulerAffectorFunction;
import org.terasology.rendering.particles.functions.affectors.PointForceAffectorFunction;
import org.terasology.rendering.particles.functions.affectors.TurbulenceAffectorFunction;
import org.terasology.rendering.particles.functions.generators.ColorRangeGeneratorFunction;
import org.terasology.rendering.particles.functions.generators.EnergyRangeGeneratorFunction;
import org.terasology.rendering.particles.functions.generators.GeneratorFunction;
import org.terasology.rendering.particles.functions.generators.PositionRangeGeneratorFunction;
import org.terasology.rendering.particles.functions.generators.ScaleRangeGeneratorFunction;
import org.terasology.rendering.particles.functions.generators.TextureOffsetGeneratorFunction;
import org.terasology.rendering.particles.functions.generators.VelocityRangeGeneratorFunction;
import org.terasology.rendering.particles.internal.rendering.ParticleRenderer;
import org.terasology.rendering.particles.internal.updating.ParticleUpdater;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

/**
 * Component system responsible for all ParticleSystem components.
 *
 * @author Linus van Elswijk <linusvanelswijk@gmail.com>
 */
@API
@RegisterSystem(RegisterMode.CLIENT)
public class ParticleSystemManager extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem, ParticleManagerInterface {

    private static final Logger logger = LoggerFactory.getLogger(ParticleSystemManager.class);

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Physics physics;

    private ParticleRenderer particleRenderer;
    private ParticleUpdater particleUpdater;

    private BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions = HashBiMap.create();
    private BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions = HashBiMap.create();


    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onSystemActivated(OnActivatedComponent event, EntityRef entity, ParticleSystemComponent particleSystemComponent) {
        particleSystemComponent.entityRef = entity;

        particleUpdater.register(entity);
        particleUpdater.updateStateData(entity, registeredAffectorFunctions, registeredGeneratorFunctions);
    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onSystemDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        particleUpdater.dispose(entity);
    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onSystemChanged(ParticleSystemUpdateEvent event, EntityRef entity) {
        particleUpdater.updateStateData(entity, registeredAffectorFunctions, registeredGeneratorFunctions);
    }

    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterActivated(OnActivatedComponent event, EntityRef entity, ParticleEmitterComponent particleEmitterComponent) {
        particleEmitterComponent.entityRef = entity;
    }

    @ReceiveEvent(components = {ParticleEmitterComponent.class})
    public void onEmitterChanged(ParticleSystemUpdateEvent event, EntityRef entity) {
        particleUpdater.updateStateData(entity.getOwner(), registeredAffectorFunctions, registeredGeneratorFunctions);
    }


    public void initialise() {
        particleRenderer = ParticleRenderer.create(logger);
        particleUpdater = ParticleUpdater.create(physics, logger);

        registerGeneratorFunction(new ColorRangeGeneratorFunction());
        registerGeneratorFunction(new EnergyRangeGeneratorFunction());
        registerGeneratorFunction(new PositionRangeGeneratorFunction());
        registerGeneratorFunction(new ScaleRangeGeneratorFunction());
        registerGeneratorFunction(new VelocityRangeGeneratorFunction());
        registerGeneratorFunction(new TextureOffsetGeneratorFunction());

        registerAffectorFunction(new DampingAffectorFunction());
        registerAffectorFunction(new EnergyColorAffectorFunction());
        registerAffectorFunction(new EnergyScaleAffectorFunction());
        registerAffectorFunction(new ForceAffectorFunction());
        registerAffectorFunction(new PointForceAffectorFunction());
        registerAffectorFunction(new ForwardEulerAffectorFunction());
        registerAffectorFunction(new TurbulenceAffectorFunction());
    }

    @Override
    public void shutdown() {
        registeredAffectorFunctions.clear();
        registeredGeneratorFunctions.clear();

        particleRenderer.dispose();
        particleRenderer = null;

        particleUpdater = null;
    }

    public void update(float delta) {

        // Keep weather particle emitters 10.0f above the camera TODO: Keep track of these LocationComponents separately, not search each update
        entityManager.getEntitiesWith(OverheadParticleSystem.class).forEach(
                (e) -> {
                    ParticleSystemComponent component = e.getComponent(ParticleSystemComponent.class);
                    Vector3f vector3f = new Vector3f(worldRenderer.getActiveCamera().getPosition());

                    for (EntityRef emitter : component.getEmitters()) {
                        emitter.getComponent(LocationComponent.class).setWorldPosition(
                                vector3f.addY(10.0f)
                        );
                    }
                }
        );

        particleUpdater.update(delta);
    }

    public void renderAlphaBlend() {
        if (particleRenderer != null) {
            particleRenderer.render(worldRenderer, particleUpdater.getStateData());
        }
    }

    public void renderOpaque() {

    }

    public void renderOverlay() {

    }

    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
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
    public void registerAffectorFunction(AffectorFunction affectorFunction) {
        Preconditions.checkArgument(!registeredAffectorFunctions.containsKey(affectorFunction.getComponentClass()),
                "Tried to register an AffectorFunction for %s twice", affectorFunction
        );

        logger.info("Registering AffectorFunction for Component class {}", affectorFunction.getComponentClass());
        registeredAffectorFunctions.put(affectorFunction.getComponentClass(), affectorFunction);
    }
}
