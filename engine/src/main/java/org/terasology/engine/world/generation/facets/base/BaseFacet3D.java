// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 */
public class BaseFacet3D implements WorldFacet3D {

    private BlockRegion worldRegion;
    private BlockRegion relativeRegion;

    public BaseFacet3D(BlockRegionc targetRegion, Border3D border) {
        worldRegion = border.expandTo3D(targetRegion);
        relativeRegion = border.expandTo3D(targetRegion.getSize(new Vector3i()));
    }

    @Override
    public final BlockRegion getWorldRegion() {
        return worldRegion;
    }

    @Override
    public final BlockRegion getRelativeRegion() {
        return relativeRegion;
    }

    protected final int getRelativeIndex(int x, int y, int z) {
        if (!relativeRegion.contains(x, y, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d, %d) for region %s", x, y, z, relativeRegion.toString()));
        }
        return x - relativeRegion.minX() + relativeRegion.getSizeX() * (y - relativeRegion.minY() + relativeRegion.getSizeY() * (z - relativeRegion.minZ()));
    }

    protected final int getWorldIndex(int x, int y, int z) {
        if (!worldRegion.contains(x, y, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d, %d) for region %s", x, y, z, worldRegion.toString()));
        }
        return x - worldRegion.minX() + worldRegion.getSizeX() * (y - worldRegion.minY() + worldRegion.getSizeY() * (z - worldRegion.minZ()));
    }
}
