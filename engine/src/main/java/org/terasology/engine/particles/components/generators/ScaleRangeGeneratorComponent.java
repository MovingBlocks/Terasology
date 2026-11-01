// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;


@API
public class ScaleRangeGeneratorComponent implements Component<ScaleRangeGeneratorComponent> {
    public Vector3f minScale;
    public Vector3f maxScale;

    public ScaleRangeGeneratorComponent(final Vector3f min, final Vector3f max) {
        minScale = new Vector3f(min);
        maxScale = new Vector3f(max);
    }

    public ScaleRangeGeneratorComponent() {
        minScale = new Vector3f();
        maxScale = new Vector3f();
    }

    @Override
    public void copyFrom(ScaleRangeGeneratorComponent other) {
        this.minScale = new Vector3f(other.minScale);
        this.maxScale = new Vector3f(other.maxScale);
    }
}
