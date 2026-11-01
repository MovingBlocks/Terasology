// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;


@API
public class PositionRangeGeneratorComponent implements Component<PositionRangeGeneratorComponent> {

    public Vector3f minPosition;
    public Vector3f maxPosition;

    public PositionRangeGeneratorComponent(final Vector3f minPosition, final Vector3f maxPosition) {
        this.minPosition = new Vector3f(minPosition);
        this.maxPosition = new Vector3f(maxPosition);
    }

    public PositionRangeGeneratorComponent() {
        minPosition = new Vector3f();
        maxPosition = new Vector3f();
    }

    @Override
    public void copyFrom(PositionRangeGeneratorComponent other) {
        this.minPosition = new Vector3f(other.minPosition);
        this.maxPosition = new Vector3f(other.maxPosition);
    }
}
