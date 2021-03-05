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
