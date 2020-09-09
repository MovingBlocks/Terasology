// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 */
public class BaseFacet3D implements WorldFacet3D {

    private final Region3i worldRegion;
    private final Region3i relativeRegion;

    public BaseFacet3D(Region3i targetRegion, Border3D border) {
        worldRegion = border.expandTo3D(targetRegion);
        relativeRegion = border.expandTo3D(targetRegion.size());
    }

    @Override
    public final Region3i getWorldRegion() {
        return worldRegion;
    }

    @Override
    public final Region3i getRelativeRegion() {
        return relativeRegion;
    }

    protected final int getRelativeIndex(int x, int y, int z) {
        if (!relativeRegion.encompasses(x, y, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d, %d) for region %s", x, y, z, relativeRegion.toString()));
        }
        return x - relativeRegion.minX() + relativeRegion.sizeX() * (y - relativeRegion.minY() + relativeRegion.sizeY() * (z - relativeRegion.minZ()));
    }

    protected final int getWorldIndex(int x, int y, int z) {
        if (!worldRegion.encompasses(x, y, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d, %d) for region %s", x, y, z, worldRegion.toString()));
        }
        return x - worldRegion.minX() + worldRegion.sizeX() * (y - worldRegion.minY() + worldRegion.sizeY() * (z - worldRegion.minZ()));
    }
}
