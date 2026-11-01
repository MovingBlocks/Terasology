// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;


@API
public class EnergyRangeGeneratorComponent implements Component<EnergyRangeGeneratorComponent> {
    public float minEnergy = 100.0f;
    public float maxEnergy = 100.0f;

    public EnergyRangeGeneratorComponent(final float minEnergy, final float maxEnergy) {
        this.minEnergy = minEnergy;
        this.maxEnergy = maxEnergy;
    }

    public EnergyRangeGeneratorComponent() {
    }

    @Override
    public void copyFrom(EnergyRangeGeneratorComponent other) {
        this.minEnergy = other.minEnergy;
        this.maxEnergy = other.maxEnergy;
    }
}
