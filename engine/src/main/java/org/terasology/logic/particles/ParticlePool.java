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

import com.google.common.base.Preconditions;

/**
 * Object to keep track of the state of the living particles in a particle system and
 * also maintains a pool of dead particles that can be recycled.
 *
 * @author Linus van Elswijk <linusvanelswijk@gmail.com>
 */
public final class ParticlePool {

    //== package private attributes =====================

    // Per particle scalars
    final float[] size;
    final float[] energy;

    // Per particle 3d vectors
    final float[] position;
    final float[] previousPosition;
    final float[] velocity;

    // Per particle 4d vectors
    final float[] color;

    //== private attributes =============================

    private final ParticleData temporaryParticleData = new ParticleData(this);

    private int firstDeadParticleIndex;
    private final int rawSize;

    //== public methods =================================

    public int size() {
        return rawSize;
    }

    public int livingParticles() {
        return firstDeadParticleIndex;
    }

    public int deadParticles() {
        return rawSize - firstDeadParticleIndex;
    }

    //== package private methods ========================

    int reviveParticle() {
        firstDeadParticleIndex++;

        return firstDeadParticleIndex - 1;
    }

    void moveDeceasedParticle(final int index) {
        firstDeadParticleIndex--;
        // First dead particle now points to the last living particle and there is a dead particle in the living pool.
        // Moving the last living particle to the location of the deceased particle fixes both issues.
        temporaryParticleData.loadFrom(firstDeadParticleIndex);
        temporaryParticleData.storeAt(index);
    }

    ParticlePool(final int size) {
        Preconditions.checkArgument(size > 0, "Size must be >0, but was %s", size);

        this.rawSize = size;

        // Per particle scalars
        this.size = new float[size];
        this.energy = new float[size];

        // Per particle 3d vectors
        position = new float[size * 3];
        previousPosition =  new float[size * 3];
        velocity = new float[size * 3];

        // Per particle 4d vectors
        color =  new float[size * 4];
    }

    //== private methods ================================

    private ParticlePool() {
        throw new UnsupportedOperationException();
    }
}
