/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.particles;

import org.terasology.gestalt.module.sandbox.API;

/**
 * Data mask used internally by the particle system.
 */
@API
public enum ParticleDataMask {

    ENERGY(0b0000001),
    TEXTURE_OFFSET(0b0000010),
    POSITION(0b0000100),
    PREVIOUS_POSITION(0b0001000),
    VELOCITY(0b0010000),
    SCALE(0b0100000),
    COLOR(0b1000000),
    ALL(0b1111111);

    private final int rawMask;

    ParticleDataMask(final int rawMask) {
        this.rawMask = rawMask;
    }

    public boolean isEnabled(final int mask) {
        return (this.rawMask & mask) != 0;
    }

    public int toInt() {
        return ParticleDataMask.toInt(this);
    }

    public static int toInt(ParticleDataMask dataMask, ParticleDataMask... dataMasks) {
        int combinedMask = dataMask.rawMask;

        for (ParticleDataMask dataMaskI : dataMasks) {
            combinedMask |= dataMaskI.rawMask;
        }

        return combinedMask;
    }
}
