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
package org.terasology.rendering.particles.internal.data;

import com.google.common.base.Preconditions;
import org.terasology.rendering.particles.DataMask;
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
    public final float[] energy;

    // Per particle 2d vectors
    public final float[] textureOffset;

    // Per particle 3d vectors
    public final float[] position;
    public final float[] previousPosition;
    public final float[] velocity;

    public final float[] scale;

    // Per particle 4d vectors
    public final float[] color;

    //== private attributes =============================

    public final ParticleData temporaryParticleData = new ParticleData();

    private int firstDeadParticleIndex;
    private final int rawSize;

    //== Constructors ===================================

    public ParticlePool(final int size) {
        Preconditions.checkArgument(size > 0, "Size must be >0, but was %s", size);

        this.rawSize = size;
        this.firstDeadParticleIndex = 0;

        // Per particle scalars
        this.energy = new float[size];

        // Per particle 2d vectors
        this.textureOffset = new float[size * 2];

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

    public int reviveParticle() {
        resetParticleData(firstDeadParticleIndex);
        firstDeadParticleIndex++;

        return firstDeadParticleIndex - 1;
    }

    public void moveDeceasedParticle(final int index) {
        firstDeadParticleIndex--;
        // First dead particle now points to the last living particle and there is a dead particle in the living pool.
        // Moving the last living particle to the location of the deceased particle fixes both issues.
        loadTemporaryDataFrom(firstDeadParticleIndex, DataMask.toInt(DataMask.ALL));
        storeTemporaryDataAt(index, DataMask.toInt(DataMask.ALL));
    }

    //== moving particle data ===========================

    public void loadTemporaryDataFrom(final int index, int rawMask) {
        final int index2 = 2 * index;
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars
        if (DataMask.ENERGY.isEnabled(rawMask)) {
            temporaryParticleData.energy = energy[index];
        }

        // 2d vectors
        if (DataMask.TEXTURE_OFFSET.isEnabled(rawMask)) {
            temporaryParticleData.textureOffset.set(
                    textureOffset[index2 + X_OFFSET],
                    textureOffset[index2 + Y_OFFSET]
            );
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

    public void storeTemporaryDataAt(final int index, final int rawMask) {
        final int index2 = 2 * index;
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars
        if (DataMask.ENERGY.isEnabled(rawMask)) {
            energy[index] = temporaryParticleData.energy;
        }

        // 2d vectors
        if (DataMask.TEXTURE_OFFSET.isEnabled(rawMask)) {
            textureOffset[index2 + X_OFFSET] = temporaryParticleData.textureOffset.x();
            textureOffset[index2 + Y_OFFSET] = temporaryParticleData.textureOffset.y();
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

    private void resetParticleData(final int i) {
        final int i2 = i * 2;
        final int i3 = i * 3;
        final int i4 = i * 4;

        // scalars
        energy[i] = 1.0f;

        // 2D vectors
        textureOffset[i2 + X_OFFSET] = 0.0f;

        // 3D vectors
        position[i3 + X_OFFSET] = position[i3 + Y_OFFSET] = position[i3 + Z_OFFSET] = 0.0f;
        previousPosition[i3 + X_OFFSET] = previousPosition[i3 + Y_OFFSET] = previousPosition[i3 + Z_OFFSET] = 0.0f;
        velocity[i3 + X_OFFSET] = velocity[i3 + Y_OFFSET] = velocity[i3 + Z_OFFSET] = 0.0f;
        scale[i3 + X_OFFSET] = scale[i3 + Y_OFFSET] = scale[i3 + Z_OFFSET] = 1.0f;

        // 4D vectors
        color[i4 + X_OFFSET] = color[i4 + Y_OFFSET] = color[i4 + Z_OFFSET] = color[i4 + W_OFFSET ] = 1.0f;
    }
}
