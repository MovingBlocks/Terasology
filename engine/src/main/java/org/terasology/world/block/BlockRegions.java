// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class BlockRegions {
    private BlockRegions() {
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max
     *
     * @return new block region
     */
    public static BlockRegion createFromMinAndMax(Vector3ic min, Vector3ic max) {
        return new BlockRegion().setMin(min).setMax(max);
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max
     *
     * @return new block region
     */
    public static BlockRegion createFromMinAndMax(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new BlockRegion().setMin(minX, minY, minZ).setMax(maxX, maxY, maxZ);
    }

    /**
     * Creates a new region centered around {@code center} extending each side by {@code extents}. The resulting
     * axis-aligned bounding box (AABB) will have a size of {@code 2 * extents}
     *
     * @return new block region
     */
    public static BlockRegion createFromCenterAndExtents(Vector3ic center, Vector3ic extents) {
        return new BlockRegion().setMin(center).setMax(center).addExtents(extents);
    }

    /**
     * Creates a new region centered around {@code center} extending each side by {@code extents}. The computed min is
     * rounded up (ceil), the computed max is rounded down (floor). Thus, the resulting axis-aligned bounding box (AABB)
     * will only include integer points that are within the floating point area and have a size of {@code <= 2 *
     * extents}
     *
     * @return new block region
     */
    public static BlockRegion createFromCenterAndExtents(Vector3fc center, Vector3fc extents) {
        Vector3f min = center.sub(extents, new Vector3f());
        Vector3f max = center.add(extents, new Vector3f());

        return new BlockRegion().setMin(new Vector3i(min, RoundingMode.CEILING)).setMax(new Vector3i(max,
                RoundingMode.FLOOR));
    }
}
