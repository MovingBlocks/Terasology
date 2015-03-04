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

/**
 * Created by Linus on 4-3-2015.
 */
public class SimpleParticleSimulator extends ParticleSimulatorType {
    SimpleParticleSimulator() {
        super("Simple");
    }

    @Override
    public void onCreation() {

    }

    @Override
    public void onUpdate(final ParticleData particleData, final float delta) {
        particleData.previousPosition.set(particleData.position);
        forwardEulerPhysics(particleData, delta);
    }

    @Override
    public void onCollision(final ParticleData particleData, HitResult hit) {
        particleData.energy = 0;
    }

    @Override
    public void onEmission(final ParticleData particleData) {
        
    }

    @Override
    public void onDeath(final ParticleData particleData) {

    }
}
