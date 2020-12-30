// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import com.google.common.base.Preconditions;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.RoundingMode;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.Side;

import java.util.Iterator;
import java.util.Optional;

/**
 * A mutable, bounded, axis-aligned volume in space denoting a collection of blocks contained within.
 */
public class BlockRegion implements BlockRegionc {

    public static final BlockRegionc INVALID = new BlockRegion();

    /**
     * The x coordinate of the minimum corner.
     */
    private int minX = Integer.MAX_VALUE;
    /**
     * The y coordinate of the minimum corner.
     */
    private int minY = Integer.MAX_VALUE;
    /**
     * The z coordinate of the minimum corner.
     */
    private int minZ = Integer.MAX_VALUE;
    /**
     * The x coordinate of the maximum corner.
     */
    private int maxX = Integer.MIN_VALUE;
    /**
     * The y coordinate of the maximum corner.
     */
    private int maxY = Integer.MIN_VALUE;
    /**
     * The z coordinate of the maximum corner.
     */
    private int maxZ = Integer.MIN_VALUE;

    // -- CONSTRUCTORS -----------------------------------------------------------------------------------------------//

    /**
     * INTERNAL: Creates an empty block region with invalid minimum/maximum corners.
     * <p>
     * {@link #isValid()} will return {@code false} for an empty block region created via this constructor.
     */
    BlockRegion() {
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of the minimum corner MUST be smaller or equal to the respective component of the
     * maximum corner. If a dimension of {@code min} is greater than the respective dimension of {@code max} an {@link
     * IllegalArgumentException} will be thrown.
     * <p>
     * Consider using {@link #union(Vector3ic)} as an alternative.
     *
     * @throws IllegalArgumentException if any min component is greater than the corresponding max component
     */
    public BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of the minimum corner MUST be smaller or equal to the respective component of the
     * maximum corner. If a dimension of {@code min} is greater than the respective dimension of {@code max} an {@link
     * IllegalArgumentException} will be thrown.
     * <p>
     * Consider using {@link #union(Vector3ic)} as an alternative.
     *
     * @throws IllegalArgumentException if any min component is greater than the corresponding max component
     */
    public BlockRegion(Vector3ic min, Vector3ic max) {
        this.set(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Creates a new region containing the single block given by the coordinates.
     */
    public BlockRegion(int x, int y, int z) {
        this.set(x, y, z, x, y, z);
    }

    /**
     * Creates a new region containing the single block given by the coordinates.
     */
    public BlockRegion(Vector3ic block) {
        this(block.x(), block.y(), block.z());
    }

    /**
     * Create a new copy of the given block region {@code source}.
     *
     * @param source the block region to copy.
     */
    public BlockRegion(BlockRegionc source) {
        this.set(source);
    }

    // -- ITERABLE ---------------------------------------------------------------------------------------------------//

    @Override
    public Iterator<Vector3ic> iterator() {
        return new Iterator<Vector3ic>() {
            private Vector3i current = null;
            private final Vector3i next = getMin(new Vector3i());

            public boolean findNext() {
                if (current.equals(next)) {
                    next.z++;
                    if (next.z > maxZ) {
                        next.z = minZ;
                        next.y++;
                        if (next.y > maxY) {
                            next.y = minY;
                            next.x++;
                        }
                    }
                    return contains(next);
                }
                return true;
            }

            @Override
            public boolean hasNext() {
                if (!isValid()) {
                    return false;
                }
                if (current == null) {
                    return true;
                }

                if (current.equals(next)) {
                    return findNext();
                }
                return contains(next);
            }

            @Override
            public Vector3ic next() {
                if (current == null) {
                    current = new Vector3i(next);
                    return next;
                }

                if (current.equals(next)) {
                    if (findNext()) {
                        return next;
                    }
                    return null;
                }
                current.set(next);
                return next;
            }
        };
    }

    // -- GETTERS & SETTERS ------------------------------------------------------------------------------------------//

    /**
     * Reset both the minimum and maximum corner of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if the given coordinates for min and max are {@link #isValid()
     *         invalid}.
     */
    public BlockRegion set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Preconditions.checkArgument(minX <= maxX || (minX == INVALID.minX() && maxX == INVALID.maxX()));
        Preconditions.checkArgument(minY <= maxY || (minY == INVALID.minY() && maxY == INVALID.maxY()));
        Preconditions.checkArgument(minZ <= maxZ || (minZ == INVALID.minZ() && maxZ == INVALID.maxZ()));
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        return this;
    }

    /**
     * Reset both the minimum and maximum corner of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if the given coordinates for min and max are {@link #isValid()
     *         invalid}.
     */
    public BlockRegion set(Vector3ic min, Vector3ic max) {
        return this.set(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Reset this region to have the same minimum and maximum corner as the {@code source} region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if the given coordinates for min and max are {@link #isValid()
     *         invalid}.
     */
    public BlockRegion set(BlockRegionc source) {
        return this.set(source.minX(), source.minY(), source.minZ(), source.maxX(), source.maxY(), source.maxZ());
    }

    // -- min --------------------------------------------------------------------------------------------------------//

    @Override
    public int minX() {
        return this.minX;
    }

    @Override
    public int minY() {
        return this.minY;
    }

    @Override
    public int minZ() {
        return this.minZ;
    }

    @Override
    public BlockRegion minX(int x, BlockRegion dest) {
        return dest.set(x, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BlockRegion minX(int x) {
        return this.minX(x, this);
    }

    @Override
    public BlockRegion minY(int y, BlockRegion dest) {
        return dest.set(this.minX, y, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BlockRegion minY(int y) {
        return this.minY(y, this);
    }

    @Override
    public BlockRegion minZ(int z, BlockRegion dest) {
        return dest.set(this.minX, this.minY, z, this.maxX, this.maxY, this.maxZ);
    }

    public BlockRegion minZ(int z) {
        return this.minZ(z, this);
    }

    @Override
    public BlockRegion setMin(int x, int y, int z, BlockRegion dest) {
        return dest.set(x, y, z, this.maxX, this.maxY, this.maxZ);
    }

    public BlockRegion setMin(int x, int y, int z) {
        return this.setMin(x, y, z, this);
    }

    public BlockRegion setMin(Vector3ic min) {
        return this.setMin(min, this);
    }

    public BlockRegion addToMin(int dx, int dy, int dz) {
        return this.addToMin(dx, dy, dz, this);
    }

    public BlockRegion addToMin(Vector3ic dmin) {
        return this.addToMin(dmin, this);
    }


    // -- max --------------------------------------------------------------------------------------------------------//

    @Override
    public int maxX() {
        return this.maxX;
    }

    @Override
    public int maxY() {
        return this.maxY;
    }

    @Override
    public int maxZ() {
        return this.maxZ;
    }

    @Override
    public BlockRegion maxX(int x, BlockRegion dest) {
        return dest.set(this.minX, this.minY, this.minZ, x, this.maxY, this.maxZ);
    }

    public BlockRegion maxX(int x) {
        return this.maxX(x, this);
    }

    @Override
    public BlockRegion maxY(int y, BlockRegion dest) {
        return dest.set(this.minX, this.minY, this.minZ, this.maxX, y, this.maxZ);
    }

    public BlockRegion maxY(int y) {
        return this.maxY(y, this);
    }

    @Override
    public BlockRegion maxZ(int z, BlockRegion dest) {
        return dest.set(this.minX, this.minY, this.minZ, this.maxX, this.maxY, z);
    }

    public BlockRegion maxZ(int z) {
        return this.maxZ(z, this);
    }

    @Override
    public BlockRegion setMax(int x, int y, int z, BlockRegion dest) {
        return dest.set(this.minX, this.minY, this.minZ, x, y, z);
    }

    public BlockRegion setMax(int x, int y, int z) {
        return this.setMax(x, y, z, this);
    }

    public BlockRegion setMax(Vector3ic max) {
        return this.setMax(max, this);
    }

    public BlockRegion addToMax(int dx, int dy, int dz) {
        return this.addToMax(dx, dy, dz, this);
    }

    public BlockRegion addToMax(Vector3ic dmax) {
        return this.addToMax(dmax, this);
    }

    // -- size -------------------------------------------------------------------------------------------------------//

    @Override
    public BlockRegion setSize(int x, int y, int z, BlockRegion dest) {
        return dest.set(this.minX, this.minY, this.minZ, this.minX + x - 1, this.minY + y - 1, this.minZ + z - 1);
    }

    public BlockRegion setSize(int x, int y, int z) {
        return this.setSize(x, y, z, this);
    }

    public BlockRegion setSize(Vector3ic size) {
        return setSize(size.x(), size.y(), size.z());
    }

    // -- IN-PLACE MUTATION ------------------------------------------------------------------------------------------//
    @Override
    public BlockRegion union(int x, int y, int z, BlockRegion dest) {
        dest.minX = Math.min(this.minX, x);
        dest.minY = Math.min(this.minY, y);
        dest.minZ = Math.min(this.minZ, z);

        dest.maxX = Math.max(this.maxX, x);
        dest.maxY = Math.max(this.maxY, y);
        dest.maxZ = Math.max(this.maxZ, z);
        return dest;
    }

    public BlockRegion union(int x, int y, int z) {
        return this.union(x, y, z, this);
    }

    public BlockRegion union(Vector3ic pos) {
        return union(pos.x(), pos.y(), pos.z(), this);
    }

    public BlockRegion union(BlockRegionc other) {
        return this.union(other, this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public Optional<BlockRegion> intersect(BlockRegionc other, BlockRegion dest) {
        dest.minX = Math.max(minX, other.minX());
        dest.minY = Math.max(minY, other.minY());
        dest.minZ = Math.max(minZ, other.minZ());

        dest.maxX = Math.min(maxX, other.maxX());
        dest.maxY = Math.min(maxY, other.maxY());
        dest.maxZ = Math.min(maxZ, other.maxZ());

        if (dest.isValid()) {
            return Optional.of(dest);
        } else {
            return Optional.empty();
        }
    }

    public Optional<BlockRegion> intersect(BlockRegionc other) {
        return this.intersect(other, this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public BlockRegion translate(int x, int y, int z, BlockRegion dest) {
        dest.minX = this.minX + x;
        dest.minY = this.minY + y;
        dest.minZ = this.minZ + z;
        dest.maxX = this.maxX + x;
        dest.maxY = this.maxY + y;
        dest.maxZ = this.maxZ + z;
        return dest;
    }

    public BlockRegion translate(int x, int y, int z) {
        return this.translate(x, y, z, this);
    }

    public BlockRegion translate(Vector3ic vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    public BlockRegion setPosition(int x, int y, int z) {
        return this.setPosition(x, y, z, this);
    }

    public BlockRegion setPosition(Vector3ic pos) {
        return this.setPosition(pos.x(), pos.y(), pos.z(), this);
    }

    @Override
    public BlockRegion expand(int dx, int dy, int dz, BlockRegion dest) {
        return dest.set(this.minX - dx, this.minY - dy, this.minZ - dz,
                this.maxX + dx, this.maxY + dy, this.maxZ + dz);
    }

    public BlockRegion expand(int dx, int dy, int dz) {
        return this.expand(dx, dy, dz, this);
    }

    public BlockRegion expand(Vector3ic extents) {
        return expand(extents.x(), extents.y(), extents.z());
    }

    // -- transform --------------------------------------------------------------------------------------------------//

    @Override
    public BlockRegion transform(Matrix4fc m, BlockRegion dest) {
        Preconditions.checkArgument(m.isAffine());
        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;
        float minx = Float.POSITIVE_INFINITY;
        float miny = Float.POSITIVE_INFINITY;
        float minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY;
        float maxy = Float.NEGATIVE_INFINITY;
        float maxz = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            float x = minX + (i & 1) * dx;
            float y = minY + (i >> 1 & 1) * dy;
            float z = minZ + (i >> 2 & 1) * dz;
            float tx = m.m00() * x + m.m10() * y + m.m20() * z + m.m30();
            float ty = m.m01() * x + m.m11() * y + m.m21() * z + m.m31();
            float tz = m.m02() * x + m.m12() * y + m.m22() * z + m.m32();
            minx = Math.min(tx, minx);
            miny = Math.min(ty, miny);
            minz = Math.min(tz, minz);
            maxx = Math.max(tx, maxx);
            maxy = Math.max(ty, maxy);
            maxz = Math.max(tz, maxz);
        }
        dest.minX = Math.roundUsing(minx, RoundingMode.FLOOR);
        dest.minY = Math.roundUsing(miny, RoundingMode.FLOOR);
        dest.minZ = Math.roundUsing(minz, RoundingMode.FLOOR);
        dest.maxX = Math.roundUsing(maxx, RoundingMode.CEILING);
        dest.maxY = Math.roundUsing(maxy, RoundingMode.CEILING);
        dest.maxZ = Math.roundUsing(maxz, RoundingMode.CEILING);

        return dest;
    }

    public BlockRegion transform(Matrix4fc m) {
        return transform(m, this);
    }

    /**
     * Restrict this region to a 1-width region that borders the provided {@link Side} of a region.
     * <p>
     * The resulting region is a subset of this region, i.e., the intersection of the face region with the source region
     * is exactly the face region.
     *
     * @param side the side of the region
     * @return this region (after modification)
     */
    public BlockRegion face(Side side) {
        return face(side, this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Compare this region to another object for equality.
     * <p>
     * Two regions are equal iff their minimum and maximum corner are the equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BlockRegion region = (BlockRegion) obj;
        return minX == region.minX
                && minY == region.minY
                && minZ == region.minZ
                && maxX == region.maxX
                && maxY == region.maxY
                && maxZ == region.maxZ;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + minX;
        result = prime * result + minY;
        result = prime * result + minZ;
        result = prime * result + maxX;
        result = prime * result + maxY;
        result = prime * result + maxZ;
        return result;
    }

    @Override
    public String toString() {
        return "BlockRegion[(" + this.minX + " " + this.minY + " " + this.minZ + ")..." +
                "(" + (this.maxX) + " " + (this.maxY) + " " + (this.maxZ) + ")]";
    }
}
