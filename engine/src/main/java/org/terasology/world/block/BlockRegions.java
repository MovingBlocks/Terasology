// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Arrays;
import java.util.Iterator;

public final class BlockRegions {
    private BlockRegions() {
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of {@code min} should be smaller or equal to the respective component in {@code max}. If
     * a dimension of {@code min} is greater than the respective dimension of {@code max} the resulting block region
     * will have a size of 0 along that dimension.
     * <p>
     * Consider using {@link #encompassing(Vector3ic...)} as an alternative.
     *
     * @return new block region
     */
    public static BlockRegion createFromMinAndSize(Vector3ic min, Vector3ic size) {
        return new BlockRegion().setMin(min).setMax(
            min.x() + size.x() - 1,
            min.y() + size.y() - 1,
            min.z() + size.z() - 1
        );
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of {@code min} should be smaller or equal to the respective component in {@code max}. If
     * a dimension of {@code min} is greater than the respective dimension of {@code max} the resulting block region
     * will have a size of 0 along that dimension.
     * <p>
     * Consider using {@link #encompassing(Vector3ic...)} as an alternative.
     *
     * @return new block region
     */
    public static BlockRegion createFromMinAndMax(Vector3ic min, Vector3ic max) {
        return new BlockRegion().setMin(min).setMax(max);
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of {@code min} should be smaller or equal to the respective component in {@code max}. If
     * a dimension of {@code min} is greater than the respective dimension of {@code max} the resulting block region
     * will have a size of 0 along that dimension.
     * <p>
     * Consider using {@link #encompassing(Vector3ic...)} as an alternative.
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
        return new BlockRegion().union(center).addExtents(extents);
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

        return new BlockRegion()
                .setMin(new Vector3i(min, RoundingMode.CEILING))
                .setMax(new Vector3i(max, RoundingMode.FLOOR));
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of {@code min} should be smaller or equal to the respective component in {@code max}. If
     * a dimension of {@code min} is greater than the respective dimension of {@code max} the resulting block region
     * will have a size of 0 along that dimension.
     * <p>
     * Consider using {@link #encompassing(Vector3ic...)} as an alternative.
     *
     * @return new block region
     */
    public static BlockRegion createFromMinAndSize(Vector3ic min, Vector3ic size) {
        return new BlockRegion().setMin(min).setMax(
            min.x() + size.x() - 1,
            min.y() + size.y() - 1,
            min.z() + size.z() - 1
        );
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing all the given positions.
     *
     * @param positions the positions that must be contained in the resulting block region
     * @return a new block region containing all given positions
     */
    public static BlockRegion encompassing(Vector3ic... positions) {
        return Arrays.stream(positions).reduce(new BlockRegion(), BlockRegion::union, BlockRegion::union);
    }

    /**
     * An iterable over the blocks in the block region, where each position is wrapped in a new {@link Vector3i}.
     * <p>
     * If you only need the elements of the block region in the local context of the iterator step without modifications
     * you may want to consider using {@link #iterableInPlace(BlockRegion)} instead.
     *
     * @param region the region to iterate over
     */
    public static Iterable<Vector3i> iterable(BlockRegion region) {
        return () -> {
            Iterator<Vector3ic> itr = iterableInPlace(region).iterator();
            return new Iterator<Vector3i>() {
                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public Vector3i next() {
                    return new Vector3i(itr.next());
                }
            };
        };
    }

    /**
     * An iterable over the blocks in the block region, where the same {@link Vector3ic} is reused to avoid GC.
     * <p>
     * Use this iterable for performance optimized iterations where the positions are only needed within the context of
     * the iterator sequence.
     * <p>
     * Do not store the elements directly or use them outside the context of the iterator as they will change when the
     * iterator is advanced. You may create new vectors from the elements if necessary, or use {@link
     * #iterable(BlockRegion)} instead.
     *
     * @param region the region to iterate over
     */
    public static Iterable<Vector3ic> iterableInPlace(BlockRegion region) {
        return new BlockRegionIterable(region);
    }
}
