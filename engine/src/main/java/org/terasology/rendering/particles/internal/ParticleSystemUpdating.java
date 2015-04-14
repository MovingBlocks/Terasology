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

import com.google.common.collect.BiMap;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.*;
import org.terasology.rendering.particles.components.affectors.EnergyColorAffectorComponent;
import org.terasology.rendering.particles.events.ParticleSystemUpdateEvent;
import org.terasology.rendering.particles.functions.affectors.AffectorFunction;
import org.terasology.rendering.particles.functions.affectors.EnergyColorAffectorFunction;
import org.terasology.rendering.particles.functions.generators.ColorRangeGeneratorFunction;
import org.terasology.rendering.particles.functions.generators.GeneratorFunction;
import org.terasology.utilities.random.FastRandom;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by Linus on 28-2-2015.
 */
public class ParticleSystemUpdating {
    private static final int PHYSICS_SKIP_NR = 100;

    private static float movingAvgDelta = 1.0f / 60.0f; //just a guess, doesn't matter much
    private static FastRandom random = new FastRandom();

    public static void checkCollision(final ParticlePool pool, final Physics physics, final int offset, float delta) {
        Vector3f vel = new Vector3f();
        Vector3f halfVelDir = new Vector3f();
        Vector3f curr = new Vector3f();

        for (int i = offset; i < pool.livingParticles(); i+= PHYSICS_SKIP_NR ) {
            int i3 = i * 3;
            curr.set(pool.position[i3 + 0], pool.position[i3+1], pool.position[i3+2]);
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

    public static void updateLifeRemaining(final ParticlePool pool, float delta) {
        for (int i = 0; i < pool.livingParticles(); i++) {
           pool.energy[i] -= delta;
        }

        for(int i = 0; i < pool.livingParticles(); i++) {
            while (pool.energy[i] < 0 && i < pool.livingParticles()) {
                pool.moveDeceasedParticle(i);
            }
        }
    }

    public static void updateParticles(ParticleSystemStateData partSys,
                                       final BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                                       float delta) {
        updateLifeRemaining(partSys.particlePool, delta);

        ParticleSystemComponent partComp = partSys.entityRef.getComponent(ParticleSystemComponent.class);

        partSys.affectors.forEach(
                (component, affector) -> affector.beforeUpdates(component, random, delta)
        );

        for(int i = 0; i < partSys.particlePool.livingParticles(); i++) {
            partSys.particlePool.loadTemporaryDataFrom(i, DataMask.ALL.rawMask);


            partSys.affectors.forEach(
                    (component, affector) -> affector.update(component, partSys.particlePool.temporaryParticleData, random, delta)
            );

            partSys.particlePool.storeTemporaryDataAt(i, DataMask.ALL.rawMask);
        }
    }

    //== emission ======================================================================================================

    public static void emitParticle(final ParticleSystemStateData particleSystem,
                                    final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions
    ) {
        int index = particleSystem.particlePool.reviveParticle();

        particleSystem.particlePool.loadTemporaryDataFrom(index, DataMask.ALL.rawMask);

        particleSystem.generators.forEach( (component, generator) ->
            generator.onEmission(component, particleSystem.particlePool.temporaryParticleData, random)
        );

        particleSystem.particlePool.temporaryParticleData.position.add(particleSystem.entityRef.getComponent(ParticleSystemComponent.class).emitter.getComponent(LocationComponent.class).getWorldPosition());

        particleSystem.particlePool.storeTemporaryDataAt(index, DataMask.ALL.rawMask);
    }

    public static void updateEmitter(final ParticleSystemStateData partSys,
                                     final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions,
                                     final float delta) {
        float deltaLeft = delta;
        while (deltaLeft > 0 && partSys.particlePool.deadParticles() > 0 ) {
            if (partSys.nextEmission < deltaLeft) {
                deltaLeft -= partSys.nextEmission;
                float freq1 = 1.0f / partSys.emitterComponent.spawnRateMax;
                float freq2 = 1.0f / partSys.emitterComponent.spawnRateMin;
                partSys.nextEmission = random.nextFloat(freq1 , freq2);

                emitParticle(partSys, registeredGeneratorFunctions);
            } else {
                partSys.nextEmission -= deltaLeft;
                deltaLeft = 0;
            }
        }
    }

    //== general =======================================================================================================

    public static void update(final ParticleSystemStateData partSys,
                              final Physics physics,
                              final BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions,
                              final BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions,
                              final float delta
    ) {
        movingAvgDelta = TeraMath.lerp(movingAvgDelta, delta, 0.05f);
        updateEmitter(partSys, registeredGeneratorFunctions, delta);
        updateParticles(partSys, registeredAffectorFunctions, delta);

        checkCollision(partSys.particlePool, physics, partSys.collisionUpdateIteration, delta);
        partSys.collisionUpdateIteration = (partSys.collisionUpdateIteration + 1) % PHYSICS_SKIP_NR;

        partSys.entityRef.getComponent(ParticleSystemComponent.class).maxLifeTime -= delta;
    }

    private ParticleSystemUpdating() {
        throw new UnsupportedOperationException();
    }
}
