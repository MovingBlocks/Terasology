// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.affectors;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.affectors.AccelerationAffectorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;


@RegisterParticleSystemFunction
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
