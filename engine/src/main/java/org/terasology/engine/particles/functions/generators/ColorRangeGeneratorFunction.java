// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.generators;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.generators.ColorRangeGeneratorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
@RegisterParticleSystemFunction
public final class ColorRangeGeneratorFunction extends GeneratorFunction<ColorRangeGeneratorComponent> {

    public ColorRangeGeneratorFunction() {
        super(ParticleDataMask.COLOR);
    }

    @Override
    public void onEmission(final ColorRangeGeneratorComponent component,
                           final ParticleData particleData,
                           final Random random
    ) {
        particleData.color.set(random.nextFloat(component.minColorComponents.x(), component.maxColorComponents.x()),
            random.nextFloat(component.minColorComponents.y(), component.maxColorComponents.y()),
            random.nextFloat(component.minColorComponents.z(), component.maxColorComponents.z()),
            random.nextFloat(component.minColorComponents.w(), component.maxColorComponents.w()));
    }
}
