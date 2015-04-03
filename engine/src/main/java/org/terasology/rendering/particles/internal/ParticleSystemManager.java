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
import org.terasology.config.Config;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.*;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector4f;
import org.terasology.module.sandbox.API;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.particles.ParticleManagerInterface;
import org.terasology.rendering.particles.components.ParticleEmitterComponent;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.rendering.particles.components.affectors.*;
import org.terasology.rendering.particles.components.generators.*;
import org.terasology.rendering.particles.functions.affectors.*;
import org.terasology.rendering.particles.functions.generators.*;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.util.*;

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
    private Config config;

    @In
    private Physics physics;

    private BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions = HashBiMap.create();
    private BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions = HashBiMap.create();

    // Internal state of all particle systems
    private Map<EntityRef, ParticleSystemStateData> particleSystems = new HashMap<>();

    /*
    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onActivated(OnActivatedComponent event, EntityRef entity) {
        ParticleSystemComponent component = entity.getComponent(ParticleSystemComponent.class);

        particleSystems.put(entity, new ParticleSystemStateData(
                entity,
                new ParticlePool(component.nrOfParticles)
        ));
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

    public void initialise() {
        registerGeneratorFunction(new ColorRangeGeneratorFunction());
        registerGeneratorFunction(new EnergyRangeGeneratorFunction());
        registerGeneratorFunction(new PositionRangeGeneratorFunction());
        registerGeneratorFunction(new SizeRangeGeneratorFunction());
        registerGeneratorFunction(new VelocityRangeGeneratorFunction());

        registerAffectorFunction(new DampingAffectorFunction());
        registerAffectorFunction(new EnergyColorAffectorFunction());
        registerAffectorFunction(new EnergySizeAffectorFunction());
        registerAffectorFunction(new ForceAffectorFunction());
        registerAffectorFunction(new ForwardEulerAffectorFunction());
        registerAffectorFunction(new TurbulenceAffectorFunction());
    }

    @Override
    public void shutdown() {
        particleSystems.clear();
        registeredAffectorFunctions.clear();
        registeredGeneratorFunctions.clear();
    }

    float cumDelta = 0.0f;

    public void update(float delta) {

        synchronized(particleSystems) {
            cumDelta += delta;
            for (ParticleSystemStateData system : particleSystems.values()) {
                ParticleSystemUpdating.update(system, physics, registeredGeneratorFunctions, registeredAffectorFunctions, delta);
            }
            cumDelta += delta;

            Iterator<Map.Entry<EntityRef,ParticleSystemStateData>> iter = particleSystems.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<EntityRef,ParticleSystemStateData> entry = iter.next();
                if (entry.getKey().getComponent(ParticleSystemComponent.class).maxLifeTime < 0){
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
            /*
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

            partsys.affectors.add(new ForwardEulerAffectorComponent());
            partsys.affectors.add(new ForceAffectorComponent(new Vector3f(0, -1.0f, 0)));
            partsys.affectors.add(new DampingAffectorComponent(0.35f));
            EnergyColorAffectorComponent energyColorAffector = new EnergyColorAffectorComponent();
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

            */
            Vector3f spawnPosition = new Vector3f();
            return String.format("Sparkly: %s", spawnPosition );
        }
    }

    private Vector3f getSpawnPosition() {
        Camera camera = worldRenderer.getActiveCamera();
        Vector3f spawnPosition = new Vector3f(worldRenderer.getActiveCamera().getViewingDirection());
        HitResult hit = physics.rayTrace(camera.getPosition(), camera.getViewingDirection(), 25, StandardCollisionGroup.WORLD);

        if (hit.isHit()) {
            spawnPosition.set(hit.getHitPoint());
        } else {
            spawnPosition.scale(25);
            spawnPosition.add(worldRenderer.getActiveCamera().getPosition());
        }

        return spawnPosition;
    }



    /**
     * @return
     */
    @Command(shortDescription = "Spawns a particle system in front of you.")
    public String spawnFire() {
        EntityRef entityRef = createParticleSystem();
        Vector3f spawnPosition = getSpawnPosition();

        ParticleSystemComponent particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);
        ParticleEmitterComponent emitterComponent = particleSystemComponent.emitter.getComponent(ParticleEmitterComponent.class);
        LocationComponent emitterLocationComponent = particleSystemComponent.emitter.getComponent(LocationComponent.class);

        if(emitterComponent == null) throw new RuntimeException();
        if(emitterComponent.generators == null) throw new RuntimeException();

        particleSystemComponent.nrOfParticles = 5000;

        emitterComponent.spawnRateMax = 400.0f;
        emitterComponent.spawnRateMin = 300.0f;

        emitterLocationComponent.setWorldPosition(spawnPosition);

        //==============================================================================================================
        // adding simple affectors
        //==============================================================================================================

        particleSystemComponent.affectors.add(entityManager.create(new ForwardEulerAffectorComponent()));
        particleSystemComponent.affectors.add(entityManager.create(new ForceAffectorComponent(new Vector3f(0, 0.25f, 0))));
        particleSystemComponent.affectors.add(entityManager.create(new DampingAffectorComponent(0.6f)));
        particleSystemComponent.affectors.add(entityManager.create(new TurbulenceAffectorComponent(0.25f)));


        //==============================================================================================================
        // adding gradient affectors
        //==============================================================================================================

        // Example of using named variables and map.put calls to define keyframes
        final float keyFireStart   = 6.5f;
        final float keyFireYellow  = 6.4f;
        final float keyFireRed     = 5.6f;
        final float keyFireEnd     = 5.0f;
        final float keySmokeStart  = 2.5f;
        final float keyFullOpacity = 1.0f;
        final float keySmokeEnd    = 0.0f;

        EnergySizeAffectorComponent energySizeAffector = new EnergySizeAffectorComponent();
            energySizeAffector.sizeMap.add(new EnergySizeAffectorComponent.EnergyAndSize(keyFireStart, new Vector3f(0.01f, 0.01f, 0.01f)));
            energySizeAffector.sizeMap.add(new EnergySizeAffectorComponent.EnergyAndSize(keyFireRed, new Vector3f(0.1f, 0.1f, 0.1f)));
            energySizeAffector.sizeMap.add(new EnergySizeAffectorComponent.EnergyAndSize(keyFireEnd, new Vector3f(0.00f, 0.00f, 0.00f)));
            energySizeAffector.sizeMap.add(new EnergySizeAffectorComponent.EnergyAndSize(keySmokeStart, new Vector3f(0.02f, 0.02f, 0.02f)));
            energySizeAffector.sizeMap.add(new EnergySizeAffectorComponent.EnergyAndSize(keySmokeEnd, new Vector3f(0.5f, 0.5f, 0.5f)));
        particleSystemComponent.affectors.add(entityManager.create(energySizeAffector));


        // Example of using arrays to define keyframes (
        float[] keyEnergies = {
            keyFireStart,
            keyFireYellow,
            keyFireRed,
            keyFireEnd,
            keySmokeStart,
            keyFullOpacity,
            keySmokeEnd
        };

        Vector4f[]  colors = {
            new Vector4f(0.9f, 0.9f, 0.9f, 0.8f),   // fire start
            new Vector4f(1.0f, 1.0f, 0.3f, 1.0f),   //   yellow
            new Vector4f(1.0f, 0.2f, 0.1f, 0.0f),   //   red
            new Vector4f(1.0f, 0.2f, 0.1f, 0.7f),   // fire end
            new Vector4f(0.1f, 0.1f, 0.1f, 0.0f),   // smoke start
            new Vector4f(0.1f, 0.1f, 0.1f, 1.0f),   //   smoke full opacity
            new Vector4f(0.1f, 0.1f, 0.1f, 0.0f),   // end
        };

        emitterComponent.generators.add(entityManager.create(
                new PositionRangeGeneratorComponent(new Vector3f(-0.2f, -0.2f, -0.2f), new Vector3f(0.2f, 0.2f, 0.2f))
        ));
        emitterComponent.generators.add(entityManager.create(
                new PositionRangeGeneratorComponent(new Vector3f(-0.2f, -0.2f, -0.2f), new Vector3f(0.2f, 0.2f, 0.2f))
        ));
        emitterComponent.generators.add(entityManager.create(
                new EnergyRangeGeneratorComponent(6, 7)
        ));

        particleSystems.put(entityRef, new ParticleSystemStateData(entityRef, new ParticlePool(particleSystemComponent.nrOfParticles)));

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


    //

    @Override
    public EntityRef createParticleSystem() {
        EntityBuilder psEntityBuilder = entityManager.newBuilder();
        psEntityBuilder.setPersistent(false);
        ParticleSystemComponent particleSystemComponent = new ParticleSystemComponent();
        psEntityBuilder.addComponent(particleSystemComponent);

        EntityRef particleSystem = psEntityBuilder.build();
        particleSystemComponent.emitter = createEmmiter(particleSystem);

        return particleSystem;
    }

    private EntityRef createEmmiter(EntityRef particleSystem) {
        EntityBuilder emmiterEntityBuilder = entityManager.newBuilder();
        emmiterEntityBuilder.setPersistent(particleSystem.isPersistent());

        emmiterEntityBuilder.addComponent(new ParticleEmitterComponent());
        emmiterEntityBuilder.addComponent(new LocationComponent());

        EntityRef emitter = emmiterEntityBuilder.build();
        emitter.setOwner(particleSystem);
        particleSystem.getComponent(ParticleSystemComponent.class).emitter = emitter;

        return emitter;
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
