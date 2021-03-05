// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 * A base class for sparse (map-based) implementations of {@link WorldFacet3D}.
 */
public abstract class SparseFacet3D implements WorldFacet3D {

    private BlockRegion worldRegion;
    private BlockRegion relativeRegion;

    public SparseFacet3D(BlockRegionc targetRegion, Border3D border) {
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

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkWorldCoords(int x, int y, int z) {
        if (!worldRegion.contains(x, y, z)) {
            String text = "Out of bounds: (%d, %d, %d) for region %s";
            String msg = String.format(text, x, y, z, worldRegion.toString());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkRelativeCoords(int x, int y, int z) {
        if (!relativeRegion.contains(x, y, z)) {
            String text = "Out of bounds: (%d, %d, %d) for region %s";
            String msg = String.format(text, x, y, z, relativeRegion.toString());
            throw new IllegalArgumentException(msg);
        }
    }

    protected final Vector3i worldToRelative(int x, int y, int z) {

        return new Vector3i(
                x - worldRegion.minX() + relativeRegion.minX(),
                y - worldRegion.minY() + relativeRegion.minY(),
                z - worldRegion.minZ() + relativeRegion.minZ());
    }

    protected final Vector3i relativeToWorld(int x, int y, int z) {

        return new Vector3i(
                x - relativeRegion.minX() + worldRegion.minX(),
                y - relativeRegion.minY() + worldRegion.minY(),
                z - relativeRegion.minZ() + worldRegion.minZ());
    }

    @Override
    public String toString() {
        Vector3i worldMin = getWorldRegion().getMin(new Vector3i());
        Vector3i relMin = getRelativeRegion().getMin(new Vector3i());
        Vector3i size = getRelativeRegion().getSize(new Vector3i());
        return String.format("SparseFacet3D [worldMin=%s, relativeMin=%s, size=%s]", worldMin, relMin, size);
    }
}
