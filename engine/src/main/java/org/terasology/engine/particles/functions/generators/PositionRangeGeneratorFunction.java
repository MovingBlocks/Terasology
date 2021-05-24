// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.generators;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.generators.PositionRangeGeneratorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
@RegisterParticleSystemFunction()
public final class PositionRangeGeneratorFunction extends GeneratorFunction<PositionRangeGeneratorComponent> {

    public PositionRangeGeneratorFunction() {
        super(ParticleDataMask.POSITION);
    }

    @Override
    public void onEmission(final PositionRangeGeneratorComponent component,
                           final ParticleData particleData,
                           final Random random
    ) {
        particleData.position.set(
                random.nextFloat(component.minPosition.x(), component.maxPosition.x()),
                random.nextFloat(component.minPosition.y(), component.maxPosition.y()),
                random.nextFloat(component.minPosition.z(), component.maxPosition.z())
        );
    }
}
