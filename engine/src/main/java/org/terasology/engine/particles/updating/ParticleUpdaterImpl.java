// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.updating;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.ParticlePool;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.particles.functions.ParticleSystemFunction;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.particles.functions.affectors.AffectorFunction;
import org.terasology.engine.particles.functions.generators.GeneratorFunction;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.utilities.ReflectionUtil;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.math.TeraMath;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * See ParticleUpdater for more information.
 */
public class ParticleUpdaterImpl implements ParticleUpdater {

    private static final Logger logger = LoggerFactory.getLogger(ParticleUpdaterImpl.class);

    /**
     * Number used in determining how many particles to skip in each collision update step, as updating all particles is
     * costly.
     */
    private static final int PHYSICS_SKIP_NR = 100;

    private ModuleManager moduleManager;

    /**
     * Map of Generators to the functions that process them.
     */
    private BiMap<Class<? extends Component>, GeneratorFunction> registeredGeneratorFunctions = HashBiMap.create();

    /**
     * Map of Affectors to the functions that process them.
     */
    private BiMap<Class<? extends Component>, AffectorFunction> registeredAffectorFunctions = HashBiMap.create();

    /**
     * Set of all particle emitters
     */
    private final Set<ParticleEmitterComponent> registeredParticleSystems = new HashSet<>();
    private final Set<ParticlePool> updatedParticlePools = new HashSet<>();

    private final FastRandom random = new FastRandom();
    private final Physics physics;
    private float movingAvgDelta = 1.0f / 60.0f; // Starting guess average physics updateParticleSystem delta

    public ParticleUpdaterImpl(final Physics physics, final ModuleManager moduleManager) {
        this.physics = physics;
        this.moduleManager = moduleManager;
    }

    @Override
    public void addEmitter(final EntityRef emitter) {
        Preconditions.checkArgument(emitter != null,
            "Argument can not be null"
        );

        ParticleEmitterComponent emitterComponent = emitter.getComponent(ParticleEmitterComponent.class);

        Preconditions.checkArgument(emitterComponent != null,
            "Entity %s does not have a ParticleEmitterComponent", emitter
        );

        registeredParticleSystems.add(emitterComponent);
    }

    @Override
    public void removeEmitter(final EntityRef emitter) {
        Preconditions.checkArgument(emitter != null,
            "Argument can not be null"
        );

        ParticleEmitterComponent emitterComponent = emitter.getComponent(ParticleEmitterComponent.class);

        Preconditions.checkState(registeredParticleSystems.contains(emitterComponent),
            "Entity %s is not a registered entity", emitter
        );

        registeredParticleSystems.remove(emitterComponent);
    }

    @Override
    public void update(final float delta) {
        movingAvgDelta = TeraMath.lerp(movingAvgDelta, delta, 0.05f);

        // It's important to update all emitters before the particle data inside the pools gets updated.
        // This ensures that all freshly revived particles are also being updated.
        Collection<ParticleEmitterComponent> particleEmitters = ImmutableList.copyOf(registeredParticleSystems);
        particleEmitters.forEach(x -> updateParticleEmitters(x, delta));
        particleEmitters.forEach(x -> updateParticleData(x, delta));
        updatedParticlePools.clear();
    }

    @Override
    public Set<ParticleEmitterComponent> getParticleEmitters() {
        return registeredParticleSystems;
    }

    @Override
    public void initialize() {
        ModuleEnvironment environment = moduleManager.getEnvironment();

        for (Class<?> type : environment.getTypesAnnotatedWith(RegisterParticleSystemFunction.class)) {
            if (!ParticleSystemFunction.class.isAssignableFrom(type)) {
                logger.atError().log("Cannot register particle system function {}, " +
                        "must be a subclass of ParticleSystemFunction", type.getSimpleName());
            } else {
                try {
                    ParticleSystemFunction function = (ParticleSystemFunction) type.newInstance();
                    if (function instanceof GeneratorFunction) {
                        Type componentClass = ReflectionUtil.getTypeParameterForSuper(type, GeneratorFunction.class, 0);
                        mapGeneratorFunction((GeneratorFunction) function, (Class<? extends Component>) componentClass);

                    } else if (function instanceof AffectorFunction) {
                        Type componentClass = ReflectionUtil.getTypeParameterForSuper(type, AffectorFunction.class, 0);
                        mapAffectorFunction((AffectorFunction) function, (Class<? extends Component>) componentClass);
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register particle system", e);
                }
            }
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void configureEmitter(final ParticleEmitterComponent emitter) {

        emitter.generatorFunctionMap.clear();
        emitter.affectorFunctionMap.clear();
        for (Component c : emitter.ownerEntity.iterateComponents()) {
            if (registeredGeneratorFunctions.containsKey(c.getClass())) {
                emitter.generatorFunctionMap.put(c, registeredGeneratorFunctions.get(c.getClass()));

            } else if (registeredAffectorFunctions.containsKey(c.getClass())) {
                emitter.affectorFunctionMap.put(c, registeredAffectorFunctions.get(c.getClass()));
            }
        }
    }

    /**
     * Maps a Generator function to the component it will be called on when new particles are emitted.
     *
     * @param generatorFunction The generator function to be used.
     * @param componentClass The component class this function is being mapped to.
     */
    private void mapGeneratorFunction(GeneratorFunction generatorFunction, Class<? extends Component> componentClass) {
        Preconditions.checkArgument(!registeredGeneratorFunctions.containsKey(componentClass),
            "Tried to register an GeneratorFunction for %s twice", generatorFunction
        );

        logger.info("Registering GeneratorFunction for Component class {}", componentClass);
        registeredGeneratorFunctions.put(componentClass, generatorFunction);
    }

    /**
     * Maps an Affector function to the component it will be called on when updating particles.
     *
     * @param affectorFunction The affector function to be used.
     * @param componentClass The component class this function is being mapped to.
     */
    private void mapAffectorFunction(AffectorFunction affectorFunction, Class<? extends Component> componentClass) {
        Preconditions.checkArgument(!registeredAffectorFunctions.containsKey(componentClass),
            "Tried to register an AffectorFunction for %s twice", affectorFunction
        );

        logger.info("Registering AffectorFunction for Component class {}", componentClass);
        registeredAffectorFunctions.put(componentClass, affectorFunction);
    }

    private void checkCollision(final ParticlePool pool, final int offset) {
        final Vector3f vel = new Vector3f();
        final Vector3f halfVelDir = new Vector3f();
        final Vector3f curr = new Vector3f();

        for (int i = offset; i < pool.livingParticles(); i += PHYSICS_SKIP_NR) {
            int i3 = i * 3;
            curr.set(pool.position[i3 + 0], pool.position[i3 + 1], pool.position[i3 + 2]);
            vel.set(pool.velocity[i3 + 0], pool.velocity[i3 + 1], pool.velocity[i3 + 2]);
            halfVelDir.set(0).add(vel).normalize().mul(0.5f);
            curr.sub(halfVelDir);
            float dist = (vel.length() + 0.5f) * movingAvgDelta * PHYSICS_SKIP_NR * 1.5f;
            vel.normalize();

            HitResult hitResult = physics.rayTrace(curr, vel, dist, StandardCollisionGroup.WORLD);
            if (hitResult.isHit()) {
                pool.energy[i] = 0;
            }
        }
    }

    private void updateLifeRemaining(final ParticlePool pool, final float delta) {
        for (int i = 0; i < pool.livingParticles(); i++) {
            pool.energy[i] -= delta;
        }

        for (int i = 0; i < pool.livingParticles(); i++) {
            while (pool.energy[i] < 0 && i < pool.livingParticles()) {
                pool.moveDeceasedParticle(i);
            }
        }
    }

    /*
     * Updates particle life and processes particle affectors
     * */
    private void updateParticles(final ParticleEmitterComponent particleSystem, final float delta) {
        updateLifeRemaining(particleSystem.particlePool, delta);

        particleSystem.affectorFunctionMap.forEach(
            (component, affector) -> affector.beforeUpdates(component, random, delta)
        );

        for (int i = 0; i < particleSystem.particlePool.livingParticles(); i++) {
            particleSystem.particlePool.loadTemporaryDataFrom(i, ParticleDataMask.ALL.toInt());

            particleSystem.affectorFunctionMap.forEach(
                (component, affector) ->
                    affector.update(component, particleSystem.particlePool.temporaryParticleData, random, delta)
            );

            particleSystem.particlePool.storeTemporaryDataAt(i, ParticleDataMask.ALL.toInt());
        }
    }

    private void emitParticle(final ParticleEmitterComponent particleEmitter) {
        int index = particleEmitter.particlePool.reviveParticle();

        particleEmitter.particlePool.loadTemporaryDataFrom(index, ParticleDataMask.ALL.toInt());

        particleEmitter.generatorFunctionMap.forEach(
            (component, generator) ->
                generator.onEmission(component, particleEmitter.particlePool.temporaryParticleData, random)
        );

        particleEmitter.particlePool.temporaryParticleData.position.add(
            particleEmitter.locationComponent.getWorldPosition(new Vector3f())
        );

        particleEmitter.particlePool.storeTemporaryDataAt(index, ParticleDataMask.ALL.toInt());
    }

    /**
     * Emits particles from emitter
     */
    private void updateEmitter(final ParticleEmitterComponent particleEmitter, final int particleReviveLimit, final float delta) {
        float deltaLeft = delta;

        while (deltaLeft > 0 && particleEmitter.particlePool.deadParticles() > particleReviveLimit) {
            if (particleEmitter.nextEmission < deltaLeft) {
                deltaLeft -= particleEmitter.nextEmission;
                float freq1 = 1.0f / particleEmitter.spawnRateMax;
                float freq2 = 1.0f / particleEmitter.spawnRateMin;
                particleEmitter.nextEmission = random.nextFloat(freq1, freq2);

                if (particleEmitter.particleSpawnsLeft != ParticleEmitterComponent.INFINITE_PARTICLE_SPAWNS) {
                    particleEmitter.particleSpawnsLeft--;
                }

                emitParticle(particleEmitter);
            } else {
                particleEmitter.nextEmission -= deltaLeft;
                deltaLeft = 0;
            }
        }
    }

    /**
     * Updates the specified particle emitter. Might cause the emitter to emit new particles or to be disabled once its
     * lifetime runs out.
     *
     * @param emitter the emitter to update
     * @param delta delta time
     */
    private void updateParticleEmitters(final ParticleEmitterComponent emitter, final float delta) {
        if (emitter.enabled && (emitter.particleSpawnsLeft == ParticleEmitterComponent.INFINITE_PARTICLE_SPAWNS || emitter.particleSpawnsLeft > 0)) {
            updateEmitter(emitter, 0, delta); // Emit particles
        }

        updateEmitterLifeTime(emitter, delta);
    }

    /**
     * Updates the particle data inside the particle pool, referenced by the specified particle emitter. During a single
     * update cycle, each pool is only updated once. In case multiple particle emitters are referencing it, it is only
     * updated the first time it's encountered. The update involves updating the trajectory and life time (optionally
     * dependent on collisions)
     *
     * @param particleSystem the particle system referencing the pool to update
     * @param delta delta time
     */
    private void updateParticleData(final ParticleEmitterComponent particleSystem, float delta) {
        if (!updatedParticlePools.contains(particleSystem.particlePool)) {
            updateParticles(particleSystem, delta); // Update particle lifetime and Affectors

            if (particleSystem.particleCollision) {
                checkCollision(particleSystem.particlePool, particleSystem.collisionUpdateIteration);
                particleSystem.collisionUpdateIteration = (particleSystem.collisionUpdateIteration + 1) % PHYSICS_SKIP_NR;
            }

            particleSystem.particlePool.prepareRendering();
            updatedParticlePools.add(particleSystem.particlePool);
        }
    }

    /**
     * Updates the particle emitters lifetime and disables or removes it potentially. In case the life time runs out,
     * the emitter is disabled. In case no more particles in the pool are alive, the emitter is destroyed.
     *
     * @param emitter emitter to update
     * @param delta delta time
     */
    private void updateEmitterLifeTime(ParticleEmitterComponent emitter, float delta) {
        if (emitter.lifeTime != ParticleEmitterComponent.INDEFINITE_EMITTER_LIFETIME) {
            emitter.lifeTime = Math.max(0, emitter.lifeTime - delta);

            if (emitter.lifeTime == 0) {
                emitter.enabled = false;

                if (emitter.particlePool.deadParticles() == emitter.maxParticles) {
                    if (emitter.destroyEntityWhenDead) {
                        emitter.ownerEntity.destroy();
                    } else {
                        emitter.ownerEntity.removeComponent(ParticleEmitterComponent.class);
                    }
                }
            }
        }
    }
}
