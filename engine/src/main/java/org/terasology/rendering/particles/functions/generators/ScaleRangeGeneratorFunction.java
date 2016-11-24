/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.particles.functions.generators;

import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.generators.ScaleRangeGeneratorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
public final class ScaleRangeGeneratorFunction extends GeneratorFunction<ScaleRangeGeneratorComponent> {
    public ScaleRangeGeneratorFunction() {
        super(ScaleRangeGeneratorComponent.class, DataMask.SCALE);
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
