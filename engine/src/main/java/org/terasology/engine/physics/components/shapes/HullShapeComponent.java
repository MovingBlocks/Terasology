// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components.shapes;

import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.gestalt.entitysystem.component.Component;

public class HullShapeComponent implements Component<HullShapeComponent> {
    public Mesh sourceMesh;

    @Override
    public void copyFrom(HullShapeComponent other) {
        this.sourceMesh = other.sourceMesh;
    }
}
