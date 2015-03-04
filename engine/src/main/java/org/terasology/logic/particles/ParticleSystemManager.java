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
package org.terasology.logic.particles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.*;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.components.ParticleEmitterComponent;
import org.terasology.logic.particles.components.ParticleSystemComponent;
import org.terasology.logic.particles.internal.ParticleSystemStateData;
import org.terasology.math.Vector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.util.*;

/**
 * Component system responsible for all ParticleSystem components.
 *
 * @author Linus van Elswijk <linusvanelswijk@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ParticleSystemManager extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem {

    private static final Logger logger = LoggerFactory.getLogger(ParticleSystemManager.class);

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private Physics physics;

    private Map<String, ParticleSimulatorType> registeredParticleSimulators;

    // Internal state of all particle systems
    private Map<EntityRef, ParticleSystemStateData> particleSystems = new HashMap<>();

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onActivated(OnActivatedComponent event, EntityRef entity) {
        ParticleSystemComponent component = entity.getComponent(ParticleSystemComponent.class);

        particleSystems.put(entity, new ParticleSystemStateData(
                entity,
                registeredParticleSimulators.get(component.systemType),
                new ParticlePool(component.nrOfParticles)
        ));

    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        particleSystems.remove(entity);
    }
    /*
    private ParticleSimulatorType getParticleSystemSimulator(String name) {
        ParticleSimulatorType simulatorType = registeredParticleSimulators.get(name);
        if(simulatorType == null) {
            logger.error("\"" + name + "\" is not a registered particle system simulator. Reverting to default.");
            simulatorType = getParticleSystemSimulator("")
        }
    }
    */

    private ParticleSystemComponent createParticleSystemComponent(EntityRef emitter) {
        ParticleSystemComponent system = new ParticleSystemComponent(10000, false, false);

        return system;
    }

    private ParticleEmitterComponent createParticleEmitterComponent(LocationComponent location) {
        ParticleEmitterComponent particleEmitterComponent = new ParticleEmitterComponent();

        particleEmitterComponent.spawnRateMean = 0.2f;
        particleEmitterComponent.spawnRateStdDev = 0.0f;
        particleEmitterComponent.spawnPositionMin = new Vector3f(-10f, -0.10f, -10f);
        particleEmitterComponent.spawnPositionMax = new Vector3f(10f, 0.10f, 10f);
        particleEmitterComponent.color = new Vector4f(1, 1, 1, 1);
        particleEmitterComponent.colorRandomness = new Vector4f(0,0,0,0);
        particleEmitterComponent.minVelocity = 5.0f;
        particleEmitterComponent.maxVelocity = 7.5f;
        particleEmitterComponent.velocityDirection = Vector3i.down().toVector3f();
        particleEmitterComponent.velocityDirectionRandomness = new Vector3f(0,0,0);

        return particleEmitterComponent;
    }


    public void registerParticleSimulatorType(ParticleSimulatorType simulator) {
        if(registeredParticleSimulators.containsKey(simulator.getName())) {
            logger.error("\"" + simulator.getName() + "\" is already registered as particle system simulator.");
        } else {
            registeredParticleSimulators.put(simulator.getName(), simulator);
            logger.info("Registered \"" + simulator.getName() + "\" particle system simulator.");
        }
    }

    public void spawnTestSystem(Vector3f location) {

        LocationComponent locationComponent = new LocationComponent(location);
        ParticleEmitterComponent emitterComponent = createParticleEmitterComponent(locationComponent);
        EntityBuilder emitterBuilder = entityManager.newBuilder();
        emitterBuilder.setPersistent(false);
        emitterBuilder.addComponent(emitterComponent);
        emitterBuilder.addComponent(locationComponent);
        EntityRef emitter = emitterBuilder.build();

        EntityBuilder particleSystemBuilder = entityManager.newBuilder();
        ParticleSystemComponent particleSystemComponent = createParticleSystemComponent(emitter);


        particleSystemBuilder.setPersistent(false);
        particleSystemBuilder.addComponent(particleSystemComponent);
        EntityRef particleSystem = particleSystemBuilder.build();
        emitter.setOwner(particleSystem);
    }

    @Override
    public void shutdown() {
        particleSystems.clear();
    }

    float cumDelta = 0.0f;

    public void update(float delta) {
        cumDelta += delta;
        for(ParticleSystemStateData system: particleSystems.values()) {
            ParticleSystemUpdating.update(system, physics, delta);
        }
        cumDelta += delta;

        if(cumDelta > 1.0f) {
            cumDelta = 0;
            System.out.println(particleSystems.size() + " particle systems active.");
        }
    }

    public void renderAlphaBlend() {

    }

    /**
     * @return
     */
    @Command(shortDescription = "Spawns a particle system in front of you.")
    public String spawnParticleSystem() {

            Vector3f spawnPosition = new Vector3f(worldRenderer.getActiveCamera().getViewingDirection());
            spawnPosition.scale(25);
            spawnPosition.add(worldRenderer.getActiveCamera().getPosition());
            spawnTestSystem(spawnPosition);

            return String.format("Sparkly: %s", spawnPosition );
    }

    public void renderOpaque() {
        ParticleSystemRendering.render(worldRenderer, particleSystems.values());
    }

    public void renderOverlay() {

    }

    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }


}
