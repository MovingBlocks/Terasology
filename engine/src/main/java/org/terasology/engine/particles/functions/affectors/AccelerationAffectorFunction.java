/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.particles.functions.affectors;

import org.terasology.particles.ParticleData;
import org.terasology.particles.ParticleDataMask;
import org.terasology.particles.components.affectors.AccelerationAffectorComponent;
import org.terasology.particles.functions.RegisterParticleSystemFunction;
import org.terasology.utilities.random.Random;

/**
 *
 */
@RegisterParticleSystemFunction()
public final class AccelerationAffectorFunction extends AffectorFunction<AccelerationAffectorComponent> {
    public AccelerationAffectorFunction() {
        super(ParticleDataMask.VELOCITY);
    }

    @Override
    public void update(final AccelerationAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        particleData.velocity.add(
                component.acceleration.x() * delta,
                component.acceleration.y() * delta,
                component.acceleration.z() * delta
        );
    }
}
