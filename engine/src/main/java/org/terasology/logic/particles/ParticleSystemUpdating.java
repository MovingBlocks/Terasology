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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.components.ParticleEmitterComponent;
import org.terasology.logic.particles.components.ParticleSystemComponent;
import org.terasology.logic.particles.internal.ParticleSystemStateData;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.*;
import org.terasology.utilities.random.FastRandom;

/**
 * Created by Linus on 28-2-2015.
 */
public class ParticleSystemUpdating {
    private static final float PHYSICS_SKIP_NR = 10;

    private static FastRandom random = new FastRandom();

    public static void updatePositions(final ParticlePool pool, float delta) {
        for (int i = 0; i < pool.livingParticles() * 3; i++) {
            pool.position[i] += pool.velocity[i] * delta;
        }

        for(int i = 0; i < pool.livingParticles(); i++) {
            pool.velocity[i*3+1] -= 9.81 * delta;
            pool.velocity[i*3+1] *= 0.98;
        }
    }

    public static void checkCollision(final ParticlePool pool, final Physics physics, final int offset, float delta) {
        Vector3f vel = new Vector3f();
        Vector3f curr = new Vector3f();

        for (int i = 0; i < pool.livingParticles(); i+= PHYSICS_SKIP_NR ) {
            int i3 = i * 3;
            curr.set(pool.position[i3 + 0], pool.position[i3+1], pool.position[i3+2]);
            vel.set(pool.velocity[i3 + 0], pool.velocity[i3+1], pool.velocity[i3+2]);
            float dist = vel.length() * delta * PHYSICS_SKIP_NR;
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
            while (pool.energy[i] < 0) {
                pool.moveDeceasedParticle(i);
            }
        }
    }

    public static void update(ParticleSystemStateData partSys, float delta) {
        updateLifeRemaining(partSys.particlePool, delta);
        updatePositions(partSys.particlePool, delta);
    }

    public static void emit(ParticlePool particlePool, ParticleEmitterComponent emitter, Vector3f loc) {
        int index = particlePool.reviveParticle();

        int index3 = index * 3;
        int index4 = index * 4;

        particlePool.size[index] = 1.0f;
        particlePool.energy[index] = 10.0f;// + random.nextFloat(0.0f, 40.0f);

        particlePool.position[index3 + 0] = random.nextFloat(emitter.spawnPositionMin.x(), emitter.spawnPositionMax.getX()) + loc.x();
        particlePool.position[index3 + 1] = random.nextFloat(emitter.spawnPositionMin.y(), emitter.spawnPositionMax.getY()) + loc.y();
        particlePool.position[index3 + 2] = random.nextFloat(emitter.spawnPositionMin.z(), emitter.spawnPositionMax.getZ()) + loc.z();

        Vector3f velo = (new Vector3f(emitter.velocityDirection));//.scale(random.nextFloat(emitter.minVelocity, emitter.maxVelocity));

        particlePool.velocity[index3 + 0] = velo.x();
        particlePool.velocity[index3 + 1] = velo.y();
        particlePool.velocity[index3 + 2] = velo.z();

        particlePool.color[index4 + 0] = random.nextFloat(0.2f, 1.0f);
        particlePool.color[index4 + 1] = random.nextFloat(0.2f, 1.0f);
        particlePool.color[index4 + 2] = random.nextFloat(0.2f, 1.0f);
        particlePool.color[index4 + 3] = 1.0f;
    }

    public static void update(final ParticleSystemStateData partSys, final Physics physics, final float delta) {

        ParticleSystemComponent partSysComp = partSys.entityRef.getComponent(ParticleSystemComponent.class);

        int i = 0;
        while(partSys.particlePool.deadParticles() > 0 && i < 10) {
            ParticleEmitterComponent emmiterComp = partSysComp.emitters.get(0).getComponent(ParticleEmitterComponent.class);
            Vector3f loc = partSysComp.emitters.get(0).getComponent(LocationComponent.class).getWorldPosition();
            emit(partSys.particlePool, partSysComp.emitters.get(0).getComponent(ParticleEmitterComponent.class), loc);
            i++;
        }

        checkCollision(partSys.particlePool, physics, partSys.collisionUpdateIteration, delta);
        update(partSys, delta);

        partSys.collisionUpdateIteration = (partSys.collisionUpdateIteration + 1) % 50;
    }

    private ParticleSystemUpdating() {
        throw new UnsupportedOperationException();
    }
}
