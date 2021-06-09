// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.joml.Vector3i;
import org.terasology.nui.Color;

/**
 * Entities with this component will cause a outline be drawn about the specified region in block coordinates.
 */
public class RegionOutlineComponent implements VisualComponent<RegionOutlineComponent> {
    public Vector3i corner1;
    public Vector3i corner2;
    public Color color = new Color(Color.white);

    @Override
    public void copy(RegionOutlineComponent other) {
        this.corner1 = new Vector3i(other.corner1);
        this.corner2 = new Vector3i(other.corner2);
        this.color = other.color;
    }
}
