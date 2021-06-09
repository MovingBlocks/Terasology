// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.Color;

/**
 * A component that provides a color to describe an entity
 */
public class ColorComponent implements Component<ColorComponent> {

    @Replicate
    public Color color;

    @Override
    public void copy(ColorComponent other) {
        this.color = other.color;
    }
}
