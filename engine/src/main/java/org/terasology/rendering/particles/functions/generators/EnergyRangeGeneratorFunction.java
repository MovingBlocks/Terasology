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
import org.terasology.rendering.particles.components.generators.EnergyRangeGeneratorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 11-3-2015.
 */
public final class EnergyRangeGeneratorFunction extends GeneratorFunction<EnergyRangeGeneratorComponent> {

    public EnergyRangeGeneratorFunction() {
        super(EnergyRangeGeneratorComponent.class, DataMask.ENERGY);
    }

    @Override
    public void onEmission(EnergyRangeGeneratorComponent component, ParticleData particleData, Random random) {
        particleData.energy = random.nextFloat(component.minEnergy, component.maxEnergy);
    }
}
