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

import com.google.common.base.Preconditions;
import org.terasology.rendering.particles.ParticleData;

/**
 * Object to keep track of the state of the living particles in a particle system and
 * also maintains a pool of dead particles that can be recycled.
 *
 * @author Linus van Elswijk <linusvanelswijk@gmail.com>
 */
public final class ParticlePool {

    //== package private attributes =====================

    // Static constants
    private static final int X_OFFSET = 0;
    private static final int Y_OFFSET = 1;
    private static final int Z_OFFSET = 2;
    private static final int W_OFFSET = 3;

    // Per particle scalars
    final float[] size;
    final float[] energy;

    // Per particle 3d vectors
    final float[] position;
    final float[] previousPosition;
    final float[] velocity;

    final float[] scale;

    // Per particle 4d vectors
    final float[] color;

    //== private attributes =============================

    final ParticleData temporaryParticleData = new ParticleData();

    private int firstDeadParticleIndex;
    private final int rawSize;

    //== Constructors ===================================

    ParticlePool(final int size) {
        Preconditions.checkArgument(size > 0, "Size must be >0, but was %s", size);

        this.rawSize = size;
        this.firstDeadParticleIndex = 0;

        // Per particle scalars
        this.size = new float[size];
        this.energy = new float[size];

        // Per particle 3d vectors
        this.position = new float[size * 3];
        this.previousPosition =  new float[size * 3];
        this.velocity = new float[size * 3];
        this.scale = new  float[size * 3];

        // Per particle 4d vectors
        this.color =  new float[size * 4];
    }

    private ParticlePool() {
        throw new UnsupportedOperationException();
    }

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
        loadTemporaryDataFrom(firstDeadParticleIndex, DataMask.ALL.rawMask);
        storeTemporaryDataAt(index, DataMask.ALL.rawMask);
    }

    //== moving particle data ===========================

    void loadTemporaryDataFrom(final int index, int rawMask) {
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars

        if (DataMask.SIZE.isEnabled(rawMask)) {
            temporaryParticleData.size = size[index];
        }

        if (DataMask.ENERGY.isEnabled(rawMask)) {
            temporaryParticleData.energy = energy[index];
        }

        // 3d vectors
        if (DataMask.POSITION.isEnabled(rawMask)) {
            temporaryParticleData.position.set(
                    position[index3 + X_OFFSET],
                    position[index3 + Y_OFFSET],
                    position[index3 + Z_OFFSET]
            );
        }

        if (DataMask.PREVIOUS_POSITION.isEnabled(rawMask)) {
            temporaryParticleData.previousPosition.set(
                    previousPosition[index3 + X_OFFSET],
                    previousPosition[index3 + Y_OFFSET],
                    previousPosition[index3 + Z_OFFSET]
            );
        }

        if (DataMask.VELOCITY.isEnabled(rawMask)) {
            temporaryParticleData.velocity.set(
                    velocity[index3 + X_OFFSET],
                    velocity[index3 + Y_OFFSET],
                    velocity[index3 + Z_OFFSET]
            );
        }

        if (DataMask.SCALE.isEnabled(rawMask)) {
            temporaryParticleData.scale.set(
                    scale[index3 + X_OFFSET],
                    scale[index3 + Y_OFFSET],
                    scale[index3 + Z_OFFSET]
            );
        }

        // 4d vectors
        if (DataMask.COLOR.isEnabled(rawMask)) {
            temporaryParticleData.color.set(
                    color[index4 + X_OFFSET],
                    color[index4 + Y_OFFSET],
                    color[index4 + Z_OFFSET],
                    color[index4 + W_OFFSET]
            );
        }
    }

    void storeTemporaryDataAt(final int index, final int rawMask) {
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        if (DataMask.SIZE.isEnabled(rawMask)) {
            size[index] = temporaryParticleData.size;
        }

        if (DataMask.ENERGY.isEnabled(rawMask)) {
            energy[index] = temporaryParticleData.energy;
        }

        // 3d vectors
        if (DataMask.POSITION.isEnabled(rawMask)) {
            position[index3 + X_OFFSET] = temporaryParticleData.position.x();
            position[index3 + Y_OFFSET] = temporaryParticleData.position.y();
            position[index3 + Z_OFFSET] = temporaryParticleData.position.z();
        }

        if (DataMask.PREVIOUS_POSITION.isEnabled(rawMask)) {
            previousPosition[index3 + X_OFFSET] = temporaryParticleData.previousPosition.x();
            previousPosition[index3 + Y_OFFSET] = temporaryParticleData.previousPosition.y();
            previousPosition[index3 + Z_OFFSET] = temporaryParticleData.previousPosition.z();
        }

        if (DataMask.VELOCITY.isEnabled(rawMask)) {
            velocity[index3 + X_OFFSET] = temporaryParticleData.velocity.x();
            velocity[index3 + Y_OFFSET] = temporaryParticleData.velocity.y();
            velocity[index3 + Z_OFFSET] = temporaryParticleData.velocity.z();
        }

        if (DataMask.SCALE.isEnabled(rawMask)) {
            scale[index3 + X_OFFSET] = temporaryParticleData.scale.x();
            scale[index3 + Y_OFFSET] = temporaryParticleData.scale.y();
            scale[index3 + Z_OFFSET] = temporaryParticleData.scale.z();
        }

        // 4d vectors
        if (DataMask.COLOR.isEnabled(rawMask)) {
            color[index4 + X_OFFSET] = temporaryParticleData.color.x();
            color[index4 + Y_OFFSET] = temporaryParticleData.color.y();
            color[index4 + Z_OFFSET] = temporaryParticleData.color.z();
            color[index4 + W_OFFSET] = temporaryParticleData.color.w();
        }
    }
}
