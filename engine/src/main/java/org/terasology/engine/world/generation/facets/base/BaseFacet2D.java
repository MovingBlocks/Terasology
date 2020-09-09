// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.Rect2i;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 */
public class BaseFacet2D implements WorldFacet2D {

    private final Rect2i worldRegion;
    private final Rect2i relativeRegion;

    public BaseFacet2D(Region3i targetRegion, Border3D border) {
        worldRegion = border.expandTo2D(targetRegion);
        relativeRegion = border.expandTo2D(targetRegion.size());
    }

    @Override
    public final Rect2i getWorldRegion() {
        return worldRegion;
    }

    @Override
    public final Rect2i getRelativeRegion() {
        return relativeRegion;
    }

    protected final int getRelativeIndex(int x, int z) {
        if (!relativeRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, relativeRegion.toString()));
        }
        return x - relativeRegion.minX() + relativeRegion.sizeX() * (z - relativeRegion.minY());
    }

    protected final int getWorldIndex(int x, int z) {
        if (!worldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, worldRegion.toString()));
        }
        return x - worldRegion.minX() + worldRegion.sizeX() * (z - worldRegion.minY());
    }
}
