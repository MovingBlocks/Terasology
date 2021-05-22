// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.affectors;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.affectors.VelocityAffectorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;


@RegisterParticleSystemFunction()
public final class VelocityAffectorFunction extends AffectorFunction<VelocityAffectorComponent> {
    public VelocityAffectorFunction() {
        super(ParticleDataMask.POSITION, ParticleDataMask.VELOCITY);
    }

    @Override
    public void update(final VelocityAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        particleData.position.add(
                particleData.velocity.x() * delta,
                particleData.velocity.y() * delta,
                particleData.velocity.z() * delta
        );
    }
}
