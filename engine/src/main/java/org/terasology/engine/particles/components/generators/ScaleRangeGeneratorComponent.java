// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;


@API
public class ScaleRangeGeneratorComponent implements Component {
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
}
