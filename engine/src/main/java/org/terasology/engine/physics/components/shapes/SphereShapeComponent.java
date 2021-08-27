// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components.shapes;

import org.terasology.gestalt.entitysystem.component.Component;

public class SphereShapeComponent implements Component<SphereShapeComponent> {
    public float radius = 0.5f;

    @Override
    public void copyFrom(SphereShapeComponent other) {
        this.radius = other.radius;
    }
}
