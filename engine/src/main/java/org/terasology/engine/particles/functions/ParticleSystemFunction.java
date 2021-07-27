// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions;

import org.terasology.engine.particles.ParticleDataMask;

/**
 * Base class for GeneratorFunction and AffectorFunction. A particle system function is called on a particle to update its fields.
 */
public abstract class ParticleSystemFunction<T> {
    private final int rawDataMask;

    public ParticleSystemFunction(ParticleDataMask dataMask, ParticleDataMask... dataMasks) {
        this.rawDataMask = ParticleDataMask.toInt(dataMask, dataMasks);
    }

    public final int getDataMask() {
        return rawDataMask;
    }
}
