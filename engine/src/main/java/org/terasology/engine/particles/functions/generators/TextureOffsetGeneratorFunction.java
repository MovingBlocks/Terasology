// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.generators;

import org.joml.Vector2f;
import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.generators.TextureOffsetGeneratorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

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
