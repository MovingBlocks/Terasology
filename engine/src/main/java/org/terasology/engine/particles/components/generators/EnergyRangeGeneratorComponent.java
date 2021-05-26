// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;


@API
public class EnergyRangeGeneratorComponent implements Component {
    public float minEnergy = 100.0f;
    public float maxEnergy = 100.0f;

    public EnergyRangeGeneratorComponent(final float minEnergy, final float maxEnergy) {
        this.minEnergy = minEnergy;
        this.maxEnergy = maxEnergy;
    }

    public EnergyRangeGeneratorComponent() {
    }
}
