// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.rendering;

import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.particles.ParticlePool;


@API
public final class ParticleRenderingData<E extends Component> {
    public final E particleData;
    public final ParticlePool particlePool;

    public ParticleRenderingData(E particleData, ParticlePool particlePool) {
        this.particleData = particleData;
        this.particlePool = particlePool;
    }
}
