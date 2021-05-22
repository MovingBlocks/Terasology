// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.functions.generators;

import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.components.generators.EnergyRangeGeneratorComponent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
@RegisterParticleSystemFunction()
public final class EnergyRangeGeneratorFunction extends GeneratorFunction<EnergyRangeGeneratorComponent> {

    public EnergyRangeGeneratorFunction() {
        super(ParticleDataMask.ENERGY);
    }

    @Override
    public void onEmission(EnergyRangeGeneratorComponent component, ParticleData particleData, Random random) {
        particleData.energy = random.nextFloat(component.minEnergy, component.maxEnergy);
    }
}
