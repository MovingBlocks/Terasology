// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.Component;
import org.terasology.nui.Color;

/**
 * A component that provides a color to describe an entity
 */
public class ColorComponent implements Component {

    @Replicate
    public Color color;
}
