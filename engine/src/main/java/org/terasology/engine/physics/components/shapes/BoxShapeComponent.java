// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components.shapes;

import org.joml.Vector3f;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public class BoxShapeComponent implements Component<BoxShapeComponent> {
    @Replicate
    public Vector3f extents = new Vector3f(1, 1, 1);

    @Override
    public void copy(BoxShapeComponent other) {
        this.extents = new Vector3f(other.extents);
    }
}
