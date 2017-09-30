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
package org.terasology.particles.updating;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.ParticlePool;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.utilities.random.FastRandom;

import java.util.HashSet;
import java.util.Set;

/**
 * See ParticleUpdater for more information.
 */
class ParticleUpdaterImpl implements ParticleUpdater {

    private static final int PHYSICS_SKIP_NR = 100;

    /**
     * Set of all particle emitters
     */
    private final Set<ParticleEmitterComponent> registeredParticleSystems = new HashSet<>();

    /**
     * Map of ParticleDataComponent type to emitters of that type.
     */
    //private final Map<Class<? extends ParticleDataComponent>, ParticleEmitterComponent> particleSystemsLookup = new HashMap<>();

    private final FastRandom random = new FastRandom();
    private final Physics physics;
    private float movingAvgDelta = 1.0f / 60.0f; //Starting guess average physics updateParticleSystem delta

    //== public ========================================================================================================

    ParticleUpdaterImpl(final Physics physics) {
        this.physics = physics;
    }

    @Override
    public void register(final EntityRef emitter) {
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
    public void dispose(final EntityRef emitter) {
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

        for (ParticleEmitterComponent registeredParticleSystem : ImmutableList.copyOf(registeredParticleSystems)) {
            updateParticleSystem(registeredParticleSystem, delta);
        }
    }

    @Override
    public Set<ParticleEmitterComponent> getParticleEmitters() {
        return registeredParticleSystems;
    }

    @Override
    public void configureEmitter(final ParticleEmitterComponent emitter,
                                 final BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                                 final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions) {

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

    //== particles =====================================================================================================

    private void checkCollision(final ParticlePool pool, final int offset) {
        final Vector3f vel = new Vector3f();
        final Vector3f halfVelDir = new Vector3f();
        final Vector3f curr = new Vector3f();

        for (int i = offset; i < pool.livingParticles(); i += PHYSICS_SKIP_NR) {
            int i3 = i * 3;
            curr.set(pool.position[i3 + 0], pool.position[i3 + 1], pool.position[i3 + 2]);
            vel.set(pool.velocity[i3 + 0], pool.velocity[i3 + 1], pool.velocity[i3 + 2]);
            halfVelDir.scale(0).add(vel).normalize().scale(0.5f);
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

    //== emission ======================================================================================================

    private void emitParticle(final ParticleEmitterComponent particleEmitter) {
        int index = particleEmitter.particlePool.reviveParticle();

        particleEmitter.particlePool.loadTemporaryDataFrom(index, ParticleDataMask.ALL.toInt());

        particleEmitter.generatorFunctionMap.forEach(
                (component, generator) ->
                        generator.onEmission(component, particleEmitter.particlePool.temporaryParticleData, random)
        );

        particleEmitter.particlePool.temporaryParticleData.position.add(
                particleEmitter.locationComponent.getWorldPosition()
        );

        particleEmitter.particlePool.storeTemporaryDataAt(index, ParticleDataMask.ALL.toInt());
    }

    /*
    * Emits particles from emitter
    * */
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

    //== general =======================================================================================================

    private void updateParticleSystem(final ParticleEmitterComponent partSys, final float delta) {
        if (partSys.enabled && (partSys.particleSpawnsLeft == ParticleEmitterComponent.INFINITE_PARTICLE_SPAWNS || partSys.particleSpawnsLeft > 0)) {
            updateEmitter(partSys, 0, delta); // Emit particles
        }

        updateParticles(partSys, delta); // Update particle lifetime and Affectors

        if (partSys.particleCollision) {
            checkCollision(partSys.particlePool, partSys.collisionUpdateIteration);
            partSys.collisionUpdateIteration = (partSys.collisionUpdateIteration + 1) % PHYSICS_SKIP_NR;
        }

        // System ran out of lifetime -> stop emission -> dispose
        if (partSys.lifeTime != ParticleEmitterComponent.INDEFINITE_EMITTER_LIFETIME) {
            partSys.lifeTime = Math.max(0, partSys.lifeTime - delta);

            if (partSys.lifeTime == 0) {
                partSys.enabled = false;

                if (partSys.particlePool.deadParticles() == partSys.maxParticles) {
                    if (partSys.destroyEntityWhenDead) {
                        partSys.ownerEntity.destroy();
                    } else {
                        partSys.ownerEntity.removeComponent(ParticleEmitterComponent.class);
                    }
                }
            }
        }
    }
}
