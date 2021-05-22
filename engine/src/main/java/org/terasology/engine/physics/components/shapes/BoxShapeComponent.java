// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components.shapes;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

public class BoxShapeComponent implements Component {
    @Replicate
    public Vector3f extents = new Vector3f(1, 1, 1);
}
