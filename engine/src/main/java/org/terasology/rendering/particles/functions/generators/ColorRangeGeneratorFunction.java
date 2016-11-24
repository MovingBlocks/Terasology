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
import org.terasology.rendering.particles.components.generators.ColorRangeGeneratorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
public final class ColorRangeGeneratorFunction extends GeneratorFunction<ColorRangeGeneratorComponent> {

    public ColorRangeGeneratorFunction() {
        super(ColorRangeGeneratorComponent.class, DataMask.COLOR);
    }

    @Override
    public void onEmission(final ColorRangeGeneratorComponent component,
                           final ParticleData particleData,
                           final Random random
    ) {
        particleData.color.setX(random.nextFloat(component.minColorComponents.x(), component.maxColorComponents.x()));
        particleData.color.setY(random.nextFloat(component.minColorComponents.y(), component.maxColorComponents.y()));
        particleData.color.setZ(random.nextFloat(component.minColorComponents.z(), component.maxColorComponents.z()));
        particleData.color.setW(random.nextFloat(component.minColorComponents.w(), component.maxColorComponents.w()));
    }
}
