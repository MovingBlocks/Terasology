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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.*;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector4f;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.particles.components.ParticleEmitterComponent;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.rendering.particles.components.affectors.*;
import org.terasology.rendering.particles.components.generators.*;
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

    // Internal state of all particle systems
    private Map<EntityRef, ParticleSystemStateData> particleSystems = new HashMap<>();
    /*
    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onActivated(OnActivatedComponent event, EntityRef entity) {
        ParticleSystemComponent component = entity.getComponent(ParticleSystemComponent.class);
        /*
        particleSystems.put(entity, new ParticleSystemStateData(
                entity,
              //  registeredParticleSimulators.get(component.systemType),
                new ParticlePool(component.nrOfParticles)
        ));
        *

    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        particleSystems.remove(entity);
    }
    */
    public EntityRef spawnTestSystem(Vector3f location) {
        synchronized(particleSystems) {
            LocationComponent locationComponent = new LocationComponent(location);

            ParticleEmitterComponent emitterComponent = new ParticleEmitterComponent();

            EntityBuilder emitterBuilder = entityManager.newBuilder();
            emitterBuilder.setPersistent(false);
            emitterBuilder.addComponent(emitterComponent);
            emitterBuilder.addComponent(locationComponent);
            EntityRef emitter = emitterBuilder.build();

            EntityBuilder particleSystemBuilder = entityManager.newBuilder();

            ParticleSystemComponent particleSystemComponent = new ParticleSystemComponent();
            particleSystemComponent.emitter = emitter;


            particleSystemBuilder.setPersistent(false);
            particleSystemBuilder.addComponent(particleSystemComponent);
            EntityRef particleSystem = particleSystemBuilder.build();
            return particleSystem;
        }
    }

    @Override
    public void shutdown() {
        particleSystems.clear();
    }

    float cumDelta = 0.0f;

    public void update(float delta) {

        synchronized(particleSystems) {
            cumDelta += delta;
            for (ParticleSystemStateData system : particleSystems.values()) {
                ParticleSystemUpdating.update(system, physics, delta);
            }
            cumDelta += delta;

            Iterator<Map.Entry<EntityRef,ParticleSystemStateData>> iter = particleSystems.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<EntityRef,ParticleSystemStateData> entry = iter.next();
                if(entry.getKey().getComponent(ParticleSystemComponent.class).maxLifeTime < 0){
                    entry.getKey().destroy();
                    iter.remove();
                }
            }

            if (cumDelta > 1.0f) {
                cumDelta = 0;
                System.out.println(particleSystems.size() + " particle systems active.");
                if (particleSystems.size() > 0) {
                    System.out.println(particleSystems.values().toArray(new ParticleSystemStateData[0])[0].particlePool.livingParticles() + " parts");
                }
            }
        }
    }


    public void renderAlphaBlend() {
        ParticleSystemRendering.render(worldRenderer, particleSystems.values());
    }

    /**
     * @return
     */
    @Command(shortDescription = "Spawns a particle system in front of you.")
    public String spawnExplosion() {
        synchronized(particleSystems) {
            Camera camera = worldRenderer.getActiveCamera();
            Vector3f spawnPosition = new Vector3f(worldRenderer.getActiveCamera().getViewingDirection());
            HitResult hit = physics.rayTrace(camera.getPosition(), camera.getViewingDirection(), 25, StandardCollisionGroup.WORLD);

            if (hit.isHit()) {
                spawnPosition.set(hit.getHitPoint());
            } else {
                spawnPosition.scale(25);
                spawnPosition.add(worldRenderer.getActiveCamera().getPosition());
            }

            EntityRef entityRef = spawnTestSystem(spawnPosition);

            ParticleSystemStateData partsys = new ParticleSystemStateData(entityRef, new ParticlePool(entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles));
            partsys.emitter.spawnRateMax = 10000.0f;
            partsys.emitter.spawnRateMin = 10000.0f;

            partsys.affectors.add(new ForwardEulerAffector());
            partsys.affectors.add(new StaticForceAffector(new Vector3f(0, -1.0f, 0)));
            partsys.affectors.add(new DragAffector(0.35f));
            EnergyColorAffector energyColorAffector = new EnergyColorAffector();
            energyColorAffector.gradientMap.put(4.5f, new Vector4f(0.9f, 0.9f, 0.9f, 0.8f));
            energyColorAffector.gradientMap.put(4.2f, new Vector4f(0.0f, 0.0f, 0.9f, 1.0f));
            energyColorAffector.gradientMap.put(3.5f, new Vector4f(0.0f, 0.0f, 0.6f, 0.7f));
            energyColorAffector.gradientMap.put(0.0f, new Vector4f(0.0f, 0.0f, 0.6f, 0.0f));
            partsys.affectors.add(energyColorAffector);

            partsys.generators.add(new BoxPositionGenerator(new Vector3f(-0.2f, -0.2f, -0.2f), new Vector3f(0.2f, 0.2f, 0.2f)));
            partsys.generators.add(new PointVelocityGenerator(new Vector3f(0, 0, 0), 8));
            partsys.generators.add(new RandomEnergyGenerator(4.5f, 5f));

            this.particleSystems.put(entityRef, partsys);
            entityRef.getComponent(ParticleSystemComponent.class).maxLifeTime = 6.0f;

            return String.format("Sparkly: %s", spawnPosition);
        }
    }

    /**
     * @return
     */
    @Command(shortDescription = "Spawns a particle system in front of you.")
    public String spawnFire() {

        Camera camera = worldRenderer.getActiveCamera();
            Vector3f spawnPosition = new Vector3f(worldRenderer.getActiveCamera().getViewingDirection());
            HitResult hit = physics.rayTrace(camera.getPosition(), camera.getViewingDirection(), 25, StandardCollisionGroup.WORLD);

            if (hit.isHit()) {
                spawnPosition.set(hit.getHitPoint());
            } else {
                spawnPosition.scale(25);
                spawnPosition.add(worldRenderer.getActiveCamera().getPosition());
            }

            EntityRef entityRef = spawnTestSystem(spawnPosition);
            entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles = 5000;

            ParticleSystemStateData partsys = new ParticleSystemStateData(entityRef, new ParticlePool(entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles));
            partsys.emitter.spawnRateMax = 400.0f;
            partsys.emitter.spawnRateMin = 300.0f;

            partsys.affectors.add(new ForwardEulerAffector());
            partsys.affectors.add(new StaticForceAffector(new Vector3f(0, 0.25f, 0)));
            partsys.affectors.add(new DragAffector(0.6f));
            EnergyColorAffector energyColorAffector = new EnergyColorAffector();
            energyColorAffector.gradientMap.put(6.5f, new Vector4f(0.9f, 0.9f, 0.9f, 0.8f));
            energyColorAffector.gradientMap.put(6.4f, new Vector4f(1.0f, 1.0f, 0.3f, 1.0f));
            energyColorAffector.gradientMap.put(5.6f, new Vector4f(1.0f, 0.2f, 0.1f, 0.0f));
            energyColorAffector.gradientMap.put(5.0f, new Vector4f(1.0f, 0.2f, 0.1f, 0.7f));
            energyColorAffector.gradientMap.put(2.5f, new Vector4f(0.1f, 0.1f, 0.1f, 0.0f));
            energyColorAffector.gradientMap.put(1.0f, new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
            energyColorAffector.gradientMap.put(0.0f, new Vector4f(0.1f, 0.1f, 0.1f, 0.0f));
            partsys.affectors.add(energyColorAffector);


            EnergySizeAffector energySizeAffector = new EnergySizeAffector();
            energySizeAffector.gradientMap.put(6.5f, new Vector3f(0.01f, 0.01f, 0.01f));
            energySizeAffector.gradientMap.put(6.2f, new Vector3f(0.1f, 0.1f, 0.1f));
            energySizeAffector.gradientMap.put(5.0f, new Vector3f(0.0f, 0.0f, 0.0f));
            energySizeAffector.gradientMap.put(2.5f, new Vector3f(0.01f, 0.01f, 0.01f));
            energySizeAffector.gradientMap.put(0.0f, new Vector3f(0.7f, 0.7f, 0.7f));
            partsys.affectors.add(energySizeAffector);

            partsys.affectors.add(new TurbulenceAffector(2.5f));

            partsys.generators.add(new BoxPositionGenerator(new Vector3f(-0.2f, -0.2f, -0.2f), new Vector3f(0.2f, 0.2f, 0.2f)));
            partsys.generators.add(new BoxColorGenerator(new Vector4f(0.7f, 0.2f, 0.f, 1), new Vector4f(1, 0.4f, 0.3f, 1)));
            partsys.generators.add(new BoxVelocityGenerator(new Vector3f(-1f, 0.5f, -1f), new Vector3f(+1f, 2.0f, +1f)));
            partsys.generators.add(new RandomEnergyGenerator(6, 7));

            this.particleSystems.put(entityRef, partsys);

            return String.format("Sparkly: %s", spawnPosition );
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


}
