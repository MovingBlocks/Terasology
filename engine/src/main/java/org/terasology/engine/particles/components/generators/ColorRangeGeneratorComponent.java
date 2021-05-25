// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector4f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;


@API
public class ColorRangeGeneratorComponent implements Component {

    public Vector4f minColorComponents;
    public Vector4f maxColorComponents;

    public ColorRangeGeneratorComponent(final Vector4f minColorComponents, final Vector4f maxColorComponents) {
        this.minColorComponents = new Vector4f(minColorComponents);
        this.maxColorComponents = new Vector4f(maxColorComponents);
    }

    public ColorRangeGeneratorComponent() {
        minColorComponents = new Vector4f();
        maxColorComponents = new Vector4f();
    }
}
