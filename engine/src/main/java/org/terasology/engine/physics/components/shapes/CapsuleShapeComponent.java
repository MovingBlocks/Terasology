// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components.shapes;

import org.terasology.gestalt.entitysystem.component.Component;

public class CapsuleShapeComponent implements Component<CapsuleShapeComponent> {
    public float radius = 0.5f;
    public float height = 1.0f;

    @Override
    public void copy(CapsuleShapeComponent other) {
        this.radius = other.radius;
        this.height = other.height;
    }
}
