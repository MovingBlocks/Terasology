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

import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.generators.TextureOffsetGeneratorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 13-4-2015.
 */
public class TextureOffsetGeneratorFunction extends GeneratorFunction<TextureOffsetGeneratorComponent> {

    public TextureOffsetGeneratorFunction() {
        super(TextureOffsetGeneratorComponent.class, DataMask.TEXTURE_OFFSET);
    }

    @Override
    public void onEmission(TextureOffsetGeneratorComponent component, ParticleData particleData, Random random) {
        final int randomOffsetIndex = random.nextInt(component.validOffsets.size());
        final Vector2f randomOffset = component.validOffsets.get(randomOffsetIndex);
        particleData.textureOffset.set(randomOffset.getX(), randomOffset.getY());
    }
}
