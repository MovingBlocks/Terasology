// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import com.google.common.base.Preconditions;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.RoundingMode;
import org.joml.Vector3i;
import org.joml.Vector3ic;

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

    /**
     * Set the minimum x-coordinate of the region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion minX(int x) {
        Preconditions.checkArgument(x <= maxX);
        this.minX = x;
        return this;
    }

    /**
     * Set the minimum y-coordinate of the region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion minY(int y) {
        Preconditions.checkArgument(y <= maxY);
        this.minY = y;
        return this;
    }

    /**
     * Set the minimum z-coordinate of the region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion minZ(int z) {
        Preconditions.checkArgument(z <= maxZ);
        this.minZ = z;
        return this;
    }

    /**
     * Set the coordinates of the minimum corner for this region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion setMin(int minX, int minY, int minZ) {
        return this.set(minX, minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BlockRegion setMin(Vector3ic min) {
        return this.setMin(min.x(), min.y(), min.z());
    }

    /**
     * Translate the minimum corner of the region by adding given {@code (dx, dy, dz)}.
     *
     * @param dx the number of blocks to add to the minimum corner on the x axis
     * @param dy the number of blocks to add to the minimum corner on the y axis
     * @param dz the number of blocks to add to the minimum corner on the z axis
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion addToMin(int dx, int dy, int dz) {
        return this.setMin(minX() + dx, minY() + dy, minZ() + dz);
    }

    public BlockRegion addToMin(Vector3ic dmin) {
        return this.addToMin(dmin.x(), dmin.y(), dmin.z());
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

    /**
     * Set the maximum x-coordinate of the region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion maxX(int x) {
        Preconditions.checkArgument(x >= minX);
        this.maxX = x;
        return this;
    }

    /**
     * Set the maximum y-coordinate of the region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion maxY(int y) {
        Preconditions.checkArgument(y >= minY);
        this.maxY = y;
        return this;
    }

    /**
     * Set the maximum z-coordinate of the region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion maxZ(int z) {
        Preconditions.checkArgument(z >= minZ);
        this.maxZ = z;
        return this;
    }

    /**
     * Set the coordinates of the maximum corner for this region.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion setMax(int x, int y, int z) {
        return this.set(this.minX, this.minY, this.minZ, x, y, z);
    }

    public BlockRegion setMax(Vector3ic max) {
        return this.setMax(max.x(), max.y(), max.z());
    }

    /**
     * Translate the maximum corner of the region by adding given {@code (dx, dy, dz)}.
     *
     * @param dx the number of blocks to add to the maximum corner on the x axis
     * @param dy the number of blocks to add to the maximum corner on the y axis
     * @param dz the number of blocks to add to the maximum corner on the z axis
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion addToMax(int dx, int dy, int dz) {
        return this.setMax(this.maxX + dx, this.maxY + dy, this.maxZ + dz);
    }

    public BlockRegion addToMax(Vector3ic dmax) {
        return this.addToMax(dmax.x(), dmax.y(), dmax.z());
    }

    // -- size -------------------------------------------------------------------------------------------------------//

    /**
     * Set the size of the block region from the minimum corner.
     *
     * @param x the x coordinate to set the size; must be > 0
     * @param y the y coordinate to set the size; must be > 0
     * @param z the z coordinate to set the size; must be > 0
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    public BlockRegion setSize(int x, int y, int z) {
        return this.setMax(this.minX + x, this.minY + y, this.minZ);
    }

    public BlockRegion setSize(Vector3ic size) {
        return setSize(size.x(), size.y(), size.z());
    }

    // -- IN-PLACE MUTATION ------------------------------------------------------------------------------------------//

    /**
     * Compute the union of this region and the given block coordinate.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return {@code this} (after modification)
     */
    public BlockRegion union(int x, int y, int z) {
        this.minX = Math.min(this.minX, x);
        this.minY = Math.min(this.minY, y);
        this.minZ = Math.min(this.minZ, z);

        this.maxX = Math.max(this.maxX, x);
        this.maxY = Math.max(this.maxY, y);
        this.maxZ = Math.max(this.maxZ, z);
        return this;
    }

    public BlockRegion union(Vector3ic pos) {
        return union(pos.x(), pos.y(), pos.z());
    }

    /**
     * Compute the union of this region and the other region.
     *
     * @param other {@link BlockRegion}
     * @return @code this} (after modification)
     */
    public BlockRegion union(BlockRegionc other) {
        return this.union(other.minX(), other.minY(), other.minZ()).union(other.maxX(), other.maxY(), other.maxZ());
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Compute the intersection of this region with the {@code other} region.
     * <p>
     * NOTE: If the regions don't intersect this region will become invalid!
     *
     * @param other the other region
     * @return {@code this} (after modification) or {@link Optional#empty()} if the regions don't intersect
     */
    public Optional<BlockRegion> intersect(BlockRegionc other) {
        this.minX = Math.max(minX, other.minX());
        this.minY = Math.max(minY, other.minY());
        this.minZ = Math.max(minZ, other.minZ());

        this.maxX = Math.min(maxX, other.maxX());
        this.maxY = Math.min(maxY, other.maxY());
        this.maxZ = Math.min(maxZ, other.maxZ());

        if (this.isValid()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Translate this region by the given vector {@code (dx, dy, dz))}.
     *
     * @param dx the x coordinate to translate by
     * @param dy the y coordinate to translate by
     * @param dz the z coordinate to translate by
     * @return {@code this} (after modification)
     */
    public BlockRegion translate(int dx, int dy, int dz) {
        this.minX = this.minX + dx;
        this.minY = this.minY + dy;
        this.minZ = this.minZ + dz;
        this.maxX = this.maxX + dx;
        this.maxY = this.maxY + dy;
        this.maxZ = this.maxZ + dz;
        return this;
    }

    public BlockRegion translate(Vector3ic vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    /**
     * Move this region to the given position {@code (x, y, z)). The position is defined by the minimum corner.
     *
     * @param x the new x coordinate of the minimum corner
     * @param y the new y coordinate of the minimum corner
     * @param z the new z coordinate of the minimum corner
     * @return {@code this} (after modification)
     */
    public BlockRegion setPosition(int x, int y, int z) {
        return this.translate(x - this.minX, y - this.minY, z - this.minZ);
    }

    public BlockRegion setPosition(Vector3ic pos) {
        return this.setPosition(pos.x(), pos.y(), pos.z());
    }

    // -- expand -----------------------------------------------------------------------------------------------------//

    /**
     * Expand this region by adding the given {@code extents} for each face of the region.
     *
     * @param dx the amount of blocks to extend this region by along the x axis in both directions
     * @param dy the amount of blocks to extend this region by along the y axis in both directions
     * @param dz the amount of blocks to extend this region by along the z axis in both directions
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting region would become {@link #isValid() invalid}.
     */
    public BlockRegion expand(int dx, int dy, int dz) {
        return this.set(this.minX - dx, this.minY - dy, this.minZ - dz,
                this.maxX + dx, this.maxY + dy, this.maxZ + dz);
    }

    public BlockRegion expand(Vector3ic extents) {
        return expand(extents.x(), extents.y(), extents.z());
    }

    // -- transform --------------------------------------------------------------------------------------------------//

    /**
     * Apply the given {@link Matrix4fc#isAffine() affine} transformation to this {@link BlockRegion}.
     * <p>
     * The matrix in {@code m} <i>must</i> be {@link Matrix4fc#isAffine() affine}.
     *
     * @param m the affine transformation matrix
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the matrix {@code m} is not {@link Matrix4fc#isAffine() affine}
     */
    public BlockRegion transform(Matrix4fc m) {
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
        this.minX = Math.roundUsing(minx, RoundingMode.FLOOR);
        this.minY = Math.roundUsing(miny, RoundingMode.FLOOR);
        this.minZ = Math.roundUsing(minz, RoundingMode.FLOOR);
        this.maxX = Math.roundUsing(maxx, RoundingMode.CEILING);
        this.maxY = Math.roundUsing(maxy, RoundingMode.CEILING);
        this.maxZ = Math.roundUsing(maxz, RoundingMode.CEILING);

        return this;
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
