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
package org.terasology.logic.particles.internal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.particles.ParticlePool;
import org.terasology.logic.particles.ParticleSimulatorType;
import org.terasology.logic.particles.SimpleParticleSimulator;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Linus on 4-3-2015.
 */
public class ParticleSystemStateData {
    public ParticleSystemStateData(EntityRef entityRef, ParticleSimulatorType simulator, ParticlePool particlePool) {
        emissionQueue = new LinkedList<>();
        this.simulator = simulator;
        this.particlePool = particlePool;
        this.collisionUpdateIteration = 0;
        this.entityRef = entityRef;
    }


    public final Queue<Float> emissionQueue;

    public final ParticleSimulatorType simulator;
    public final ParticlePool particlePool;
    public int collisionUpdateIteration;
    public final EntityRef entityRef;
}
