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
import org.terasology.asset.Assets;
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
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector4f;
import org.terasology.module.sandbox.API;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
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
    private Config config;

    @In
    private Physics physics;

    private ParticleRenderer particleRenderer;
    private ParticleUpdater particleUpdater;

    private BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions = HashBiMap.create();
    private BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions = HashBiMap.create();


    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onActivated(OnActivatedComponent event, EntityRef entity) {
        particleUpdater.register(entity);
    }

    @ReceiveEvent(components = {ParticleSystemComponent.class})
    public void onDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        particleUpdater.dispose(entity);
    }

    public EntityRef spawnTestSystem(Vector3f location) {
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

        entityManager.getEntitiesWith(RainPS.class).forEach(
                (e) -> {
                    ParticleSystemComponent component = e.getComponent(ParticleSystemComponent.class);
                    LocationComponent location = component.emitter.getComponent(LocationComponent.class);
                    Vector3f vector3f = new Vector3f(worldRenderer.getActiveCamera().getPosition());
                    location.setWorldPosition(
                            vector3f.addY(10.0f)
                    );
                }
        );
        particleUpdater.update(registeredAffectorFunctions, registeredGeneratorFunctions, delta);
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
    @Command(shortDescription = "Spawns a particle system in above you.")
    public String spawnRain() {
        EntityRef entityRef = createParticleSystem();
        entityRef.addComponent(new RainPS());
        Vector3f spawnPosition = getSpawnPosition();

        ParticleSystemComponent particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);
        ParticleEmitterComponent emitterComponent = particleSystemComponent.emitter.getComponent(ParticleEmitterComponent.class);
        LocationComponent emitterLocationComponent = particleSystemComponent.emitter.getComponent(LocationComponent.class);

        particleSystemComponent.nrOfParticles = 10000;

        emitterComponent.spawnRateMax = 4000.0f;
        emitterComponent.spawnRateMin = 3000.0f;

        emitterLocationComponent.setWorldPosition(spawnPosition);


        particleSystemComponent.texture = null;

        //==============================================================================================================
        // adding simple affectors
        //==============================================================================================================

        particleSystemComponent.affectors.add(entityManager.create(new ForwardEulerAffectorComponent()));
        particleSystemComponent.affectors.add(entityManager.create(new ForceAffectorComponent(new Vector3f(0, -9.81f, 0))));


        emitterComponent.generators.add(entityManager.create(
                new PositionRangeGeneratorComponent(new Vector3f(-15f, -15f, -15f), new Vector3f(15f, 15f, 15f))
        ));

        emitterComponent.generators.add(entityManager.create(
            new VelocityRangeGeneratorComponent(new Vector3f(0.2f, -10.0f, 0.2f), new Vector3f(-0.2f, -12.0f, -0.2f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new ScaleRangeGeneratorComponent(new Vector3f(0.01f, 0.8f, 0.01f), new Vector3f(0.02f, 1.1f, 0.02f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new ColorRangeGeneratorComponent(new Vector4f(0.5f, 0.6f, 0.85f, 1.0f), new Vector4f(0.6f, 0.8f, 0.93f, 1.0f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new EnergyRangeGeneratorComponent(2, 3)
        ));

        return String.format("Sparkly: %s", spawnPosition );
    }

    public static class RainPS implements Component {

    }

    public static enum FireStyle {
        SQUARE,
        DEFAULT,
        SMOKEY,
        TOON1,
        TOON2
    }

    @Command(shortDescription = "Spawns a particle system in front of you.")
    public void spawnFireSquare() {
        spawnFire(FireStyle.SQUARE);
    }

    @Command(shortDescription = "Spawns a particle system in front of you.")
    public void spawnFireDefualt() {
        spawnFire(FireStyle.DEFAULT);

    }

    @Command(shortDescription = "Spawns a particle system in front of you.")
    public void spawnFireSmokey() {

        spawnFire(FireStyle.SMOKEY);
    }


    @Command(shortDescription = "Spawns a particle system in front of you.")
    public void spawnFireToon1() {
        spawnFire(FireStyle.TOON1);

    }

    @Command(shortDescription = "Spawns a particle system in front of you.")
    public void spawnFireToon2() {
        spawnFire(FireStyle.TOON2);
    }

    private String spawnFire(FireStyle style) {
        EntityRef entityRef = createParticleSystem();
        Vector3f spawnPosition = getSpawnPosition();

        ParticleSystemComponent particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);
        ParticleEmitterComponent emitterComponent = particleSystemComponent.emitter.getComponent(ParticleEmitterComponent.class);
        LocationComponent emitterLocationComponent = particleSystemComponent.emitter.getComponent(LocationComponent.class);

        particleSystemComponent.nrOfParticles = 250;

        emitterComponent.spawnRateMax = 50.0f;
        emitterComponent.spawnRateMin = 20.0f;

        emitterLocationComponent.setWorldPosition(spawnPosition);

        switch (style) {
            case SQUARE:
                break;
            case DEFAULT:
                particleSystemComponent.texture = Assets.getTexture("engine:simpleParticle");
                particleSystemComponent.textureSize.setX(1.0f);
                particleSystemComponent.textureSize.setY(1.0f);
                break;
            case SMOKEY:
                particleSystemComponent.texture = Assets.getTexture("engine:smokeParticle");
                particleSystemComponent.textureSize.setX(1.0f);
                particleSystemComponent.textureSize.setY(1.0f);
                break;
            case TOON1:
                particleSystemComponent.texture = Assets.getTexture("engine:toonSmokeParticle");
                particleSystemComponent.textureSize.setX(1.0f);
                particleSystemComponent.textureSize.setY(1.0f);
                break;
            case TOON2:
                particleSystemComponent.texture = Assets.getTexture("engine:smokeAtlas");
                particleSystemComponent.textureSize.setX(0.5f);
                particleSystemComponent.textureSize.setY(1.0f);
                break;
        }

        //==============================================================================================================
        // adding simple affectors
        //==============================================================================================================

        particleSystemComponent.affectors.add(entityManager.create(new ForwardEulerAffectorComponent()));
        particleSystemComponent.affectors.add(entityManager.create(new ForceAffectorComponent(new Vector3f(0, 0.12f, 0))));
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

        EnergyScaleAffectorComponent energySizeAffector = new EnergyScaleAffectorComponent();
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keyFireStart, new Vector3f(0.02f, 0.02f, 0.02f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keyFireRed, new Vector3f(0.2f, 0.2f, 0.2f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keyFireEnd, new Vector3f(0.00f, 0.00f, 0.00f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keySmokeStart, new Vector3f(0.02f, 0.02f, 0.02f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keySmokeEnd, new Vector3f(0.5f, 0.5f, 0.5f)));
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

        EnergyColorAffectorComponent energyColorAffector = new EnergyColorAffectorComponent(keyEnergies, colors);
        particleSystemComponent.affectors.add(entityManager.create(energyColorAffector));

        emitterComponent.generators.add(entityManager.create(
                new PositionRangeGeneratorComponent(new Vector3f(-0.01f, -0.01f, -0.01f), new Vector3f(0.01f, 0.01f, 0.01f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new ScaleRangeGeneratorComponent(new Vector3f(0.1f, 0.1f, 0.1f), new Vector3f(0.2f, 0.2f, 0.2f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new ColorRangeGeneratorComponent(new Vector4f(0.2f, 0.2f, 0.2f, 1.0f), new Vector4f(0.8f, 0.8f, 0.8f, 1.0f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new EnergyRangeGeneratorComponent(6, 7)
        ));

        if(style == FireStyle.TOON2) {
            emitterComponent.generators.add(entityManager.create(
                    new TextureOffsetGeneratorComponent(
                            new Vector2f[]{
                                    new Vector2f(0, 0),
                                    new Vector2f(0.5f, 0)
                            }
                    )
            ));
        }

        return String.format("Sparkly: %s", spawnPosition );
    }


    @Command(shortDescription = "Spawns a particle system in front of you.")
    public String spawnExplosion() {
        EntityRef entityRef = createParticleSystem();
        Vector3f spawnPosition = getSpawnPosition();

        ParticleSystemComponent particleSystemComponent = entityRef.getComponent(ParticleSystemComponent.class);
        ParticleEmitterComponent emitterComponent = particleSystemComponent.emitter.getComponent(ParticleEmitterComponent.class);
        LocationComponent emitterLocationComponent = particleSystemComponent.emitter.getComponent(LocationComponent.class);

        particleSystemComponent.nrOfParticles = 500;

        emitterComponent.spawnRateMax = 1e5f;
        emitterComponent.spawnRateMin = 1e5f;

        emitterLocationComponent.setWorldPosition(spawnPosition);

        particleSystemComponent.texture = Assets.getTexture("engine:smokeAtlas");
        particleSystemComponent.textureSize.setX(0.5f);
        particleSystemComponent.textureSize.setY(1.0f);

        //==============================================================================================================
        // adding simple affectors
        //==============================================================================================================

        particleSystemComponent.affectors.add(entityManager.create(new ForwardEulerAffectorComponent()));
        particleSystemComponent.affectors.add(entityManager.create(new PointForceAffectorComponent(spawnPosition, 25.0f, 0.5f)));
        particleSystemComponent.affectors.add(entityManager.create(new ForceAffectorComponent(new Vector3f(0.0f, 0.25f, 0.0f))));
        particleSystemComponent.affectors.add(entityManager.create(new DampingAffectorComponent(0.8f)));

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

        EnergyScaleAffectorComponent energySizeAffector = new EnergyScaleAffectorComponent();
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keyFireStart, new Vector3f(0.1f, 0.1f, 0.1f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keyFireYellow, new Vector3f(1.4f, 1.4f, 1.4f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keyFireEnd, new Vector3f(0.4f, 0.4f, 0.4f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keySmokeStart, new Vector3f(0.2f, 0.2f, 0.2f)));
        energySizeAffector.sizeMap.add(new EnergyScaleAffectorComponent.EnergyAndScale(keySmokeEnd, new Vector3f(0.2f, 0.2f, 0.2f)));
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

        EnergyColorAffectorComponent energyColorAffector = new EnergyColorAffectorComponent(keyEnergies, colors);
        particleSystemComponent.affectors.add(entityManager.create(energyColorAffector));

        emitterComponent.generators.add(entityManager.create(
                new PositionRangeGeneratorComponent(new Vector3f(-0.01f, -0.01f, -0.01f), new Vector3f(0.01f, 0.01f, 0.01f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new ColorRangeGeneratorComponent(new Vector4f(0.2f, 0.2f, 0.2f, 1.0f), new Vector4f(0.8f, 0.8f, 0.8f, 1.0f))
        ));

        emitterComponent.generators.add(entityManager.create(
                new EnergyRangeGeneratorComponent(6.0f, 6.5f)
        ));

        emitterComponent.generators.add(entityManager.create(
                new TextureOffsetGeneratorComponent(
                        new Vector2f[]{
                                new Vector2f(0, 0),
                                new Vector2f(0.5f, 0)
                        }
                )
        ));

        return String.format("Sparkly: %s", spawnPosition );
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
