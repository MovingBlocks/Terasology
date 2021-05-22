// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;

/**
 *
 */
@API
public class PositionRangeGeneratorComponent implements Component {

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
}
