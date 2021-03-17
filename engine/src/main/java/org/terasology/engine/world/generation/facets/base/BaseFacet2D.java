// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 */
public class BaseFacet2D implements WorldFacet2D {

    private BlockArea worldRegion;
    private BlockArea relativeRegion;

    public BaseFacet2D(BlockRegionc targetRegion, Border3D border) {
        worldRegion = border.expandTo2D(targetRegion);
        relativeRegion = border.expandTo2D(targetRegion.getSize(new Vector3i()));
    }

    @Override
    public final BlockAreac getWorldArea() {
        return worldRegion;
    }

    @Override
    public final BlockAreac getRelativeArea() {
        return relativeRegion;
    }

    protected final int getRelativeIndex(int x, int z) {
        if (!relativeRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, relativeRegion.toString()));
        }
        return x - relativeRegion.minX() + relativeRegion.getSizeX() * (z - relativeRegion.minY());
    }

    protected final int getWorldIndex(int x, int z) {
        if (!worldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, worldRegion.toString()));
        }
        return x - worldRegion.minX() + worldRegion.getSizeX() * (z - worldRegion.minY());
    }
}
