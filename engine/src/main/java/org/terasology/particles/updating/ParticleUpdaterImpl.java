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
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.ParticlePool;
import org.terasology.particles.ParticleSystem;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.components.ParticleSystemComponent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.particles.rendering.ParticleRenderer;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.utilities.random.FastRandom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * See ParticleUpdater for more information.
 */
class ParticleUpdaterImpl implements ParticleUpdater {

    private static final int PHYSICS_SKIP_NR = 100;

    /**
     * Maps ParticleSystemComponent entity to its ParticleSystem
     */
    private final Map<EntityRef, ParticleSystem> registeredParticleSystems = new HashMap<>();

    private final FastRandom random = new FastRandom();
    private final Physics physics;
    private float movingAvgDelta = 1.0f / 60.0f; //Starting guess average physics updateParticleSystem delta

    //== public ========================================================================================================

    public ParticleUpdaterImpl(final Physics physics) {
        this.physics = physics;
    }

    @Override
    public void register(final EntityRef entity) {
        Preconditions.checkArgument(entity != null,
                "Argument can not be null"
        );

        Preconditions.checkState(!registeredParticleSystems.containsKey(entity),
                "Entity %s was already registered", entity
        );

        Preconditions.checkArgument(entity.getComponent(ParticleSystemComponent.class) != null,
                "Entity %s does not have a ParticleSystemComponent", entity
        );

        registeredParticleSystems.put(entity, new ParticleSystem(entity));
    }

    @Override
    public void dispose(final EntityRef entity) {
        Preconditions.checkArgument(entity != null,
                "Argument can not be null"
        );

        Preconditions.checkState(registeredParticleSystems.containsKey(entity),
                "Entity %s is not a registered entity", entity
        );

        registeredParticleSystems.remove(entity);
    }

    @Override
    public void update(final float delta) {
        movingAvgDelta = TeraMath.lerp(movingAvgDelta, delta, 0.05f);
        Collection<ParticleSystem> systemIterator = new ArrayList<>(registeredParticleSystems.size());
        systemIterator.addAll(registeredParticleSystems.values());
        for (ParticleSystem particleSystem : systemIterator) {
            updateParticleSystem(particleSystem, delta); // Update particle system.
        }
    }

    @Override
    public Stream<ParticleSystem> getStateDataForRenderer(Class<? extends ParticleRenderer> renderer) {
        return registeredParticleSystems.values().stream()
                .filter(p -> p.fetchedData.particleSystemComponent.getRenderer().getName().equals(renderer.getName()));
    }

    @Override
    public void updateStateData(final EntityRef entityRef,
                                final BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                                final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions) {

        // Update particle system data
        if (registeredParticleSystems.containsKey(entityRef)) {
            registeredParticleSystems.get(entityRef).fetchedData.update(registeredGeneratorFunctions, registeredAffectorFunctions);
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
    private void updateParticles(final ParticleSystem particleSystem, final float delta) {
        updateLifeRemaining(particleSystem.particlePool, delta);

        particleSystem.fetchedData.affectors.forEach(
                (component, affector) -> affector.beforeUpdates(component, random, delta)
        );

        for (int i = 0; i < particleSystem.particlePool.livingParticles(); i++) {
            particleSystem.particlePool.loadTemporaryDataFrom(i, ParticleDataMask.ALL.toInt());

            particleSystem.fetchedData.affectors.forEach(
                    (component, affector) ->
                            affector.update(component, particleSystem.particlePool.temporaryParticleData, random, delta)
            );

            particleSystem.particlePool.storeTemporaryDataAt(i, ParticleDataMask.ALL.toInt());
        }
    }

    //== emission ======================================================================================================

    private void emitParticle(final ParticleSystem particleSystem, final ParticleSystem.ParticleEmitter particleEmitter) {
        int index = particleSystem.particlePool.reviveParticle();

        particleSystem.particlePool.loadTemporaryDataFrom(index, ParticleDataMask.ALL.toInt());

        particleEmitter.generators.forEach(
                (component, generator) ->
                        generator.onEmission(component, particleSystem.particlePool.temporaryParticleData, random)
        );

        particleSystem.particlePool.temporaryParticleData.position.add(
                particleEmitter.locationComponent.getWorldPosition()
        );

        particleSystem.particlePool.storeTemporaryDataAt(index, ParticleDataMask.ALL.toInt());
    }

    /*
    * Emits particles from emitter
    * */
    private void updateEmitter(final ParticleSystem partSys, final ParticleSystem.ParticleEmitter particleEmitter, final int particleReviveLimit, final float delta) {
        float deltaLeft = delta;

        while (deltaLeft > 0 && partSys.particlePool.deadParticles() > particleReviveLimit) {
            if (particleEmitter.nextEmission < deltaLeft) {
                deltaLeft -= particleEmitter.nextEmission;
                float freq1 = 1.0f / particleEmitter.emitterComponent.spawnRateMax;
                float freq2 = 1.0f / particleEmitter.emitterComponent.spawnRateMin;
                particleEmitter.nextEmission = random.nextFloat(freq1, freq2);

                emitParticle(partSys, particleEmitter);
            } else {
                particleEmitter.nextEmission -= deltaLeft;
                deltaLeft = 0;
            }
        }
    }

    //== general =======================================================================================================

    private void updateParticleSystem(final ParticleSystem partSys, final float delta) {
        ParticleSystemComponent particleSystem = partSys.entityRef.getComponent(ParticleSystemComponent.class);

        int emitterCount = partSys.fetchedData.emitters.size();
        int deadParticles = partSys.particlePool.deadParticles();
        int poolPartition = partSys.particlePool.deadParticles() / (emitterCount != 0 ? emitterCount : 1);

        if (particleSystem.enabled) {
            for (ParticleSystem.ParticleEmitter particleEmitter : partSys.fetchedData.emitters) {
                deadParticles -= poolPartition;
                updateEmitter(partSys, particleEmitter, deadParticles, delta / (float) emitterCount); // Emit particles
            }
        }

        updateParticles(partSys, delta); // Update particle lifetime and Affectors

        if (particleSystem.particleCollision) {
            checkCollision(partSys.particlePool, partSys.collisionUpdateIteration);
            partSys.collisionUpdateIteration = (partSys.collisionUpdateIteration + 1) % PHYSICS_SKIP_NR;
        }

        // System ran out of lifetime -> stop emission -> dispose
        if (particleSystem.lifeTime != ParticleSystemComponent.INDEFINITE_SYSTEM_LIFETIME) {
            particleSystem.lifeTime = Math.max(0, particleSystem.lifeTime - delta);

            if (particleSystem.lifeTime == 0) {
                particleSystem.enabled = false;

                if (partSys.particlePool.deadParticles() == particleSystem.particlePoolSize) {
                    if (particleSystem.destroyEmittersWhenDead) {
                        for (ParticleSystem.ParticleEmitter particleEmitter : partSys.fetchedData.emitters) {
                            if (particleEmitter.emitterComponent.destroyEntityWhenDead && !particleEmitter.emitterComponent.ownerEntity.equals(partSys.entityRef)) {
                                particleEmitter.emitterComponent.ownerEntity.destroy();
                            } else {
                                particleEmitter.emitterComponent.ownerEntity.removeComponent(ParticleEmitterComponent.class);
                            }
                        }
                    }

                    registeredParticleSystems.remove(partSys.entityRef);
                    if (particleSystem.destroyEntityWhenDead) {
                        partSys.entityRef.destroy();
                    } else {
                        partSys.entityRef.removeComponent(ParticleSystemComponent.class);
                    }
                }
            }
        }
    }
}
