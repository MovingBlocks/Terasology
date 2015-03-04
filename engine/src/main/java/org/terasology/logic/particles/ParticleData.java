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

import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.nui.Color;

/**
 * Data object to store the data of a single particle.
 * Used internally for swapping operations.
 */
final class ParticleData {
    // scalars
    public float size;
    public float energy;

    // 3d vectors
    public final Vector3f position = new Vector3f();
    public final Vector3f previousPosition = new Vector3f();
    public final Vector3f velocity = new Vector3f();

    // 4d vectors
    public final Vector4f color = new Vector4f();

    // Package private stuff

    ParticleData(ParticlePool particlePool) {
        this.particlePool = particlePool;
    }

    ParticleData() { throw new UnsupportedOperationException(); }

    void loadFrom(final int index) {
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars
        size = particlePool.size[index];
        energy = particlePool.energy[index];


        // 3d vectors
        position.set(
                particlePool.position[index3 + 0],
                particlePool.position[index3 + 1],
                particlePool.position[index3 + 2]
        );

        previousPosition.set(
                particlePool.previousPosition[index3 + 0],
                particlePool.previousPosition[index3 + 1],
                particlePool.previousPosition[index3 + 2]
        );

        velocity.set(
                particlePool.velocity[index3 + 0],
                particlePool.velocity[index3 + 1],
                particlePool.velocity[index3 + 2]
        );


        // 4d vectors
        color.set(
                particlePool.color[index3 + 0],
                particlePool.color[index3 + 1],
                particlePool.color[index3 + 2],
                particlePool.color[index3 + 3]
        );
    }

    void storeAt(final int index) {
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars
        particlePool.size[index] = size;
        particlePool.energy[index] = energy;

        // 3d vectors
        particlePool.position[index3 + 0] = position.x();
        particlePool.position[index3 + 1] = position.y();
        particlePool.position[index3 + 2] = position.z();

        particlePool.previousPosition[index3 + 0] = previousPosition.x();
        particlePool.previousPosition[index3 + 1] = previousPosition.y();
        particlePool.previousPosition[index3 + 2] = previousPosition.z();

        particlePool.velocity[index3 + 0] = velocity.x();
        particlePool.velocity[index3 + 1] = velocity.y();
        particlePool.velocity[index3 + 2] = velocity.z();

        // 4d vectors
        particlePool.color[index4 + 0] = color.x();
        particlePool.color[index4 + 1] = color.y();
        particlePool.color[index4 + 2] = color.z();
        particlePool.color[index4 + 3] = color.w();
    }

    private ParticlePool particlePool;
}
