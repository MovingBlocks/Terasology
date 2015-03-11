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

import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.*;
import org.terasology.utilities.random.FastRandom;

/**
 * Created by Linus on 28-2-2015.
 */
public class ParticleSystemUpdating {
    private static final int PHYSICS_SKIP_NR = 10;

    private static FastRandom random = new FastRandom();
    /*
    public static void updatePositions(final ParticlePool pool, float delta) {
        for (int i = 0; i < pool.livingParticles() * 3; i++) {
            pool.position[i] += pool.velocity[i] * delta;
        }

        for(int i = 0; i < pool.livingParticles(); i++) {
            pool.velocity[i*3+1] -= 9.81 * delta;
            pool.velocity[i*3+1] *= 0.98;
        }
    }
*/
    public static void checkCollision(final ParticlePool pool, final Physics physics, final int offset, float delta) {
        Vector3f vel = new Vector3f();
        Vector3f curr = new Vector3f();

        for (int i = offset; i < pool.livingParticles(); i+= PHYSICS_SKIP_NR ) {
            int i3 = i * 3;
            curr.set(pool.position[i3 + 0], pool.position[i3+1], pool.position[i3+2]);
            vel.set(pool.velocity[i3 + 0], pool.velocity[i3+1], pool.velocity[i3+2]);
            float dist = vel.length() * delta * PHYSICS_SKIP_NR * 1.5f;
            vel.normalize();

            HitResult hitResult = physics.rayTrace(curr, vel, dist, StandardCollisionGroup.WORLD);
            if(hitResult.isHit()) {
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

    public static void updateParticles(ParticleSystemStateData partSys, float delta) {
        updateLifeRemaining(partSys.particlePool, delta);

        for(int i = 0; i < partSys.particlePool.livingParticles(); i++) {
            partSys.particlePool.loadTemporaryDataFrom(i, DataMask.ALL.rawMask);
           /* for(Affector affector: partSys.affectors) {
                affector.onUpdate(partSys.particlePool.temporaryParticleData, random, delta);
            }
            partSys.particlePool.storeTemporaryDataAt(i, DataMask.ALL.rawMask);
            */
        }
    }

    //== emission ======================================================================================================

    public static void emitParticle(final ParticleSystemStateData particleSystem) {
        /*
        int index = particleSystem.particlePool.reviveParticle();

        particleSystem.particlePool.loadTemporaryDataFrom(index, DataMask.ALL.rawMask);

        for(Generator generator: particleSystem.generators ) {
            generator.onEmission(particleSystem.particlePool.temporaryParticleData, random);
        }

        particleSystem.particlePool.temporaryParticleData.position.add(particleSystem.entityRef.getComponent(ParticleSystemComponent.class).emitter.getComponent(LocationComponent.class).getWorldPosition());
        particleSystem.particlePool.storeTemporaryDataAt(index, DataMask.ALL.rawMask);
        */
    }

    public static void updateEmitter(final ParticleSystemStateData partSys, final float delta) {
        /*float deltaLeft = delta;
        while (deltaLeft > 0 && partSys.particlePool.deadParticles() > 0 ) {
            if (partSys.nextEmission < deltaLeft) {
                deltaLeft -= partSys.nextEmission;
                float freq1 = 1 / partSys.emitter.spawnRateMax;
                float freq2 = 1 / partSys.emitter.spawnRateMin;
                partSys.nextEmission = random.nextFloat(freq1 , freq2);

                if(partSys.entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles > 0) {
                    emitParticle(partSys);
                    if(partSys.entityRef.getComponent(ParticleSystemComponent.class).maxLifeTime < Float.POSITIVE_INFINITY) {
                        partSys.entityRef.getComponent(ParticleSystemComponent.class).nrOfParticles--;
                    }
                }
            }
            else {
                partSys.nextEmission -= deltaLeft;
                deltaLeft = 0;
            }
        }
        */
    }

    //== general =======================================================================================================

    public static void update(final ParticleSystemStateData partSys, final Physics physics, final float delta) {
        updateEmitter(partSys, delta);
        checkCollision(partSys.particlePool, physics, partSys.collisionUpdateIteration, delta);
        partSys.collisionUpdateIteration = (partSys.collisionUpdateIteration + 1) % PHYSICS_SKIP_NR;
        updateParticles(partSys, delta);

        partSys.entityRef.getComponent(ParticleSystemComponent.class).maxLifeTime -= delta;
    }

    private ParticleSystemUpdating() {
        throw new UnsupportedOperationException();
    }
}
