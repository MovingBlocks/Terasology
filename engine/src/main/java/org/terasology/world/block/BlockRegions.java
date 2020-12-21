// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import com.google.common.base.Preconditions;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class BlockRegions {
    /**
     * An invalid block region.
     * <p>
     * CAUTION: Behavior for this region may be undeterministic for some operations. Avoid extensive use and encode an
     * <i>empty region</i> by other means, e.g., by using {@code null} or {@link java.util.Optional}.
     * <p>
     * This region may be used as initial value for reductions on block regions. For instance, to compute the union (the
     * region that encompasses all regions in some collection {@code regions}) the following Stream-API snippet can be
     * used:
     * <pre>
     *     BlockRegion union = regions.stream().reduce(BlockRegions.INVALID, BlockRegion::union, BlockRegion::union);
     * </pre>
     */
    public static final BlockRegion INVALID = new BlockRegion();

    private BlockRegions() {
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of {@code min} MUST be smaller or equal to the respective component in {@code max}. If a
     * dimension of {@code min} is greater than the respective dimension of {@code max} an {@link
     * IllegalArgumentException} will be thrown.
     * <p>
     * Consider using {@link #encompassing(Vector3ic, Vector3ic...)} as an alternative.
     */
    public static BlockRegion fromMinAndMax(Vector3ic min, Vector3ic max) {
        return new BlockRegion(min, max);
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of {@code min} MUST be smaller or equal to the respective component in {@code max}. If a
     * dimension of {@code min} is greater than the respective dimension of {@code max} an {@link
     * IllegalArgumentException} will be thrown.
     * <p>
     * Consider using {@link #encompassing(Vector3ic, Vector3ic...)} as an alternative.
     *
     * @return new block region
     */
    public static BlockRegion fromMinAndMax(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new BlockRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Creates a new region centered around {@code center} extending each side by {@code extents}.
     * <p>
     * The resulting axis-aligned bounding box (AABB) will have a size of {@code 2 * extents}
     * <p>
     * The {@code extents} MUST be non-negative in every dimension, otherwise an {@link IllegalArgumentException} will
     * be thrown.
     */
    public static BlockRegion fromCenterAndExtents(int centerX, int centerY, int centerZ,
                                                   int extentsX, int extentsY, int extentsZ) {
        return new BlockRegion(centerX, centerY, centerZ).extend(extentsX, extentsY, extentsZ);
    }

    /**
     * Creates a new region centered around {@code center} extending each side by {@code extents}.
     * <p>
     * The resulting axis-aligned bounding box (AABB) will have a size of {@code 2 * extents}
     * <p>
     * The {@code extents} MUST be non-negative in every dimension, otherwise an {@link IllegalArgumentException} will
     * be thrown.
     */
    public static BlockRegion fromCenterAndExtents(Vector3ic center, Vector3ic extents) {
        return fromCenterAndExtents(center.x(), center.y(), center.z(), extents.x(), extents.y(), extents.z());
    }

    /**
     * Creates a new region centered around {@code center} extending each side by {@code extents}.
     * <p>
     * The computed min is rounded up (ceil), the computed max is rounded down (floor). Thus, the resulting axis-aligned
     * bounding box (AABB) will only include integer points that are within the floating point area and have a size of
     * {@code <= 2 * extents}.
     */
    public static BlockRegion fromCenterAndExtents(Vector3fc center, Vector3fc extents) {
        Vector3f min = center.sub(extents, new Vector3f());
        Vector3f max = center.add(extents, new Vector3f());

        return new BlockRegion(new Vector3i(min, RoundingMode.CEILING), new Vector3i(max, RoundingMode.FLOOR));
    }

    /**
     * Creates a new region spanning from the minimum corner {@code min} with given {@code size}.
     * <p>
     * The {@code size} MUST be positive (> 0) in all dimensions, otherwise an {@link IllegalArgumentException} will be
     * thrown.
     */
    public static BlockRegion createFromMinAndSize(Vector3ic min, Vector3ic size) {
        return new BlockRegion(min).setSize(size);
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing all the given positions.
     *
     * @param positions the positions that should be encompassed by the block region
     * @return a block region containing all given positions
     * @throws IllegalArgumentException if the stream contains no positions
     */
    public static BlockRegion encompassing(Stream<? extends Vector3ic> positions) {
        BlockRegion result = positions.reduce(new BlockRegion(), BlockRegion::union, BlockRegion::union);
        if (!result.isValid()) {
            throw new IllegalArgumentException("A BlockRegion must encompass at least one block.");
        }
        return result;
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing all the given positions.
     *
     * @param first the first position (to ensure that the region is not empty)
     * @param positions the other positions that must be contained in the resulting block region
     * @return a new block region containing all given positions
     */
    public static BlockRegion encompassing(Vector3ic first, Vector3ic... positions) {
        return encompassing(Stream.concat(Stream.of(first), Arrays.stream(positions)));
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing all the given positions.
     *
     * @param positions the positions that should be encompassed by the block region
     * @return a block region containing all given positions
     * @throws IllegalArgumentException if the iterable contains no positions
     */
    public static BlockRegion encompassing(Iterable<? extends Vector3ic> positions) {
        return encompassing(StreamSupport.stream(positions.spliterator(), false));
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing all the given positions.
     *
     * @param positions the positions that should be encompassed by the block region
     * @return a block region containing all given positions
     * @throws IllegalArgumentException if the collection is empty
     */
    public static BlockRegion encompassing(Collection<? extends Vector3ic> positions) {
        Preconditions.checkArgument(!positions.isEmpty(), "A BlockRegion must encompass at least one block.");
        return encompassing(positions.stream());
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
