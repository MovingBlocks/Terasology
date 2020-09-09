// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation.facets.base;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 * A base class for sparse (map-based)
 * implementations of {@link WorldFacet3D}.
 *
 */
public abstract class SparseFacet3D implements WorldFacet3D {

    private final Region3i worldRegion;
    private final Region3i relativeRegion;

    public SparseFacet3D(Region3i targetRegion, Border3D border) {
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

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkWorldCoords(int x, int y, int z) {
        if (!worldRegion.encompasses(x, y, z)) {
            String text = "Out of bounds: (%d, %d, %d) for region %s";
            String msg = String.format(text, x, y, z, worldRegion.toString());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkRelativeCoords(int x, int y, int z) {
        if (!relativeRegion.encompasses(x, y, z)) {
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
        Vector3i worldMin = getWorldRegion().min();
        Vector3i relMin = getRelativeRegion().min();
        Vector3i size = getRelativeRegion().size();
        return String.format("SparseFacet3D [worldMin=%s, relativeMin=%s, size=%s]", worldMin, relMin, size);
    }
}
