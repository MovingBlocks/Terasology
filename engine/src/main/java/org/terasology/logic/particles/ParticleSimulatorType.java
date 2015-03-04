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

import org.terasology.physics.HitResult;

import java.util.Queue;

/**
 * Created by Linus on 4-3-2015.
 */
public abstract class ParticleSimulatorType {

    private final String rawName;

    protected static void forwardEulerPhysics(final ParticleData particleData, final float delta) {
        particleData.position.addX(particleData.velocity.x() * delta);
        particleData.position.addY(particleData.velocity.y() * delta);
        particleData.position.addZ(particleData.velocity.z() * delta);
    }

    public ParticleSimulatorType(String name) {
        rawName = name;
    }

    public abstract void onCreation();

    public abstract void onUpdate(final ParticleData particleData, final float delta);

    public abstract void onCollision(final ParticleData particleData, HitResult hit);

    public abstract void onEmission(final ParticleData particleData);

    public abstract void onDeath(final ParticleData particleData);

    public final String getName() {
        return rawName;
    }
}
