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
import org.terasology.particles.ParticleSystemStateData;
import org.terasology.particles.components.ParticleSystemComponent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.particles.rendering.ParticleRenderer;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.utilities.random.FastRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * See ParticleUpdater for more information.
 */
class ParticleUpdaterImpl implements ParticleUpdater {

    private static final int PHYSICS_SKIP_NR = 100;

    /**
     * Maps ParticleSystemComponent entity to its ParticleSystemStateData
     */
    private final Map<EntityRef, ParticleSystemStateData> registeredParticleSystems = new HashMap<>();

    private final FastRandom random = new FastRandom();
    private final Physics physics;
    private float movingAvgDelta = 1.0f / 60.0f; //Starting guess average physics update delta

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

        registeredParticleSystems.put(entity, new ParticleSystemStateData(entity));
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
        for (ParticleSystemStateData particleSystem : registeredParticleSystems.values()) {
            update(particleSystem, delta); // Update particle system.
        }
    }

    @Override
    public Stream<ParticleSystemStateData> getStateDataForRenderer(Class<? extends ParticleRenderer> renderer) {
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
    private void updateParticles(final ParticleSystemStateData particleSystem, final float delta) {
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

    private void emitParticle(final ParticleSystemStateData particleSystem, final ParticleSystemStateData.EmitterData emitterData) {
        int index = particleSystem.particlePool.reviveParticle();

        particleSystem.particlePool.loadTemporaryDataFrom(index, ParticleDataMask.ALL.toInt());

        emitterData.generators.forEach(
                (component, generator) ->
                        generator.onEmission(component, particleSystem.particlePool.temporaryParticleData, random)
        );

        particleSystem.particlePool.temporaryParticleData.position.add(
                emitterData.locationComponent.getWorldPosition()
        );

        particleSystem.particlePool.storeTemporaryDataAt(index, ParticleDataMask.ALL.toInt());
    }

    /*
    * Emits particles from emitter
    * */
    private void updateEmitter(final ParticleSystemStateData partSys, final ParticleSystemStateData.EmitterData emitterData, final int particleReviveLimit, final float delta) {
        float deltaLeft = delta;

        while (deltaLeft > 0 && partSys.particlePool.deadParticles() > particleReviveLimit) {
            if (emitterData.nextEmission < deltaLeft) {
                deltaLeft -= emitterData.nextEmission;
                float freq1 = 1.0f / emitterData.emitterComponent.spawnRateMax;
                float freq2 = 1.0f / emitterData.emitterComponent.spawnRateMin;
                emitterData.nextEmission = random.nextFloat(freq1, freq2);

                emitParticle(partSys, emitterData);
            } else {
                emitterData.nextEmission -= deltaLeft;
                deltaLeft = 0;
            }
        }
    }

    //== general =======================================================================================================

    private void update(final ParticleSystemStateData partSys, final float delta) {
        int emitterCount = partSys.fetchedData.emitters.size();
        int deadParticles = partSys.particlePool.deadParticles();
        int poolPartition = partSys.particlePool.deadParticles() / (emitterCount != 0 ? emitterCount : 1);

        for (ParticleSystemStateData.EmitterData emitterData : partSys.fetchedData.emitters) {
            deadParticles -= poolPartition;
            updateEmitter(partSys, emitterData, deadParticles, delta / (float) emitterCount); // Emit particles
        }

        updateParticles(partSys, delta); // Update lifetime and Affectors

        checkCollision(partSys.particlePool, partSys.collisionUpdateIteration);
        partSys.collisionUpdateIteration = (partSys.collisionUpdateIteration + 1) % PHYSICS_SKIP_NR;

        partSys.entityRef.getComponent(ParticleSystemComponent.class).maxLifeTime -= delta;
    }
}
