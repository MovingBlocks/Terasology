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

import org.joml.Vector2f;
import org.terasology.particles.ParticleData;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.components.generators.TextureOffsetGeneratorComponent;
import org.terasology.particles.functions.RegisterParticleSystemFunction;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 13-4-2015.
 */
@RegisterParticleSystemFunction()
public class TextureOffsetGeneratorFunction extends GeneratorFunction<TextureOffsetGeneratorComponent> {

    public TextureOffsetGeneratorFunction() {
        super(ParticleDataMask.TEXTURE_OFFSET);
    }

    @Override
    public void onEmission(TextureOffsetGeneratorComponent component, ParticleData particleData, Random random) {
        if (component.validOffsets.size() == 0) {
            return;
        }

        final int randomOffsetIndex = random.nextInt(component.validOffsets.size());
        final Vector2f randomOffset = component.validOffsets.get(randomOffsetIndex);
        particleData.textureOffset.set(randomOffset);
    }
}
