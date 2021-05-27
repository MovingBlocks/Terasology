// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.generators;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.generators.VelocityRangeGeneratorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
@RegisterParticleSystemFunction
public final class VelocityRangeGeneratorFunction extends GeneratorFunction<VelocityRangeGeneratorComponent> {
    public VelocityRangeGeneratorFunction() {
        super(ParticleDataMask.VELOCITY);
    }

    @Override
    public void onEmission(final VelocityRangeGeneratorComponent component,
                           final ParticleData particleData,
                           final Random random) {
        particleData.velocity.set(
            random.nextFloat(component.minVelocity.x(), component.maxVelocity.x()),
            random.nextFloat(component.minVelocity.y(), component.maxVelocity.y()),
            random.nextFloat(component.minVelocity.z(), component.maxVelocity.z()));
    }
}
