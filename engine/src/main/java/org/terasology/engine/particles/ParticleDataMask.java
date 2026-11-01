// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles;

import org.terasology.context.annotation.API;

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
