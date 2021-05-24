// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.generators;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.generators.ScaleRangeGeneratorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
@RegisterParticleSystemFunction()
public final class ScaleRangeGeneratorFunction extends GeneratorFunction<ScaleRangeGeneratorComponent> {
    public ScaleRangeGeneratorFunction() {
        super(ParticleDataMask.SCALE);
    }

    @Override
    public void onEmission(final ScaleRangeGeneratorComponent component,
                           final ParticleData particleData,
                           final Random random
    ) {
        particleData.scale.set(
                random.nextFloat(component.minScale.x(), component.maxScale.x()),
                random.nextFloat(component.minScale.y(), component.maxScale.y()),
                random.nextFloat(component.minScale.z(), component.maxScale.z())
        );
    }
}
