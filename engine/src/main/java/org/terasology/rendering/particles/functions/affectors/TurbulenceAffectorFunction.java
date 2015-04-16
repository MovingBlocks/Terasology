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
package org.terasology.rendering.particles.functions.affectors;

import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.affectors.TurbulenceAffectorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
public final class TurbulenceAffectorFunction extends AffectorFunction<TurbulenceAffectorComponent>{

    public TurbulenceAffectorFunction() {
        super(TurbulenceAffectorComponent.class, DataMask.VELOCITY);
    }

    @Override
    public void update(final TurbulenceAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        final float deltaMagnitude = delta * component.magnitude;

        particleData.velocity.add(
                random.nextFloat(-deltaMagnitude, +deltaMagnitude),
                random.nextFloat(-deltaMagnitude, +deltaMagnitude),
                random.nextFloat(-deltaMagnitude, +deltaMagnitude)
        );
    }
}
