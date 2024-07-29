// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.context.annotation.API;

/**
 * This is a {@link LayerThickness} for a layer that has a constant, predetermined thickness at all paints.
 */
@API
public class ConstantLayerThickness implements LayerThickness {

    private final int thickness;

    /**
     * @param thickness the desired thickness of this layer
     */
    public ConstantLayerThickness(int thickness) {
        this.thickness = thickness;
    }

    @Override
    public int get(int x, int z) {
        return thickness;
    }
}
