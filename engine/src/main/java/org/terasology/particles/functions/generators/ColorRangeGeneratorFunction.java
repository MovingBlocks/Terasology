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
package org.terasology.particles.functions.generators;

import org.terasology.particles.ParticleData;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.components.generators.ColorRangeGeneratorComponent;
import org.terasology.particles.functions.RegisterParticleSystemFunction;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
@RegisterParticleSystemFunction()
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
