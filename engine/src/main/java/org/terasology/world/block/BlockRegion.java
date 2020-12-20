// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import com.google.common.base.Preconditions;
import org.joml.AABBf;
import org.joml.Intersectionf;
import org.joml.LineSegmentf;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Planef;
import org.joml.Rayf;
import org.joml.RoundingMode;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Optional;

/**
 * A bounded, axis-aligned volume in space denoting a collection of blocks contained within.
 */
public class BlockRegion {

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
     * Consider using {@link BlockRegions#encompassing(Vector3ic, Vector3ic...)} as an alternative.
     *
     * @throws IllegalArgumentException if any min component is greater than the corresponding max component
     */
    public BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Preconditions.checkArgument(minX <= maxX);
        Preconditions.checkArgument(minY <= maxY);
        Preconditions.checkArgument(minZ <= maxZ);

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * Creates a new region spanning the smallest axis-aligned bounding box (AABB) containing both, min and max.
     * <p>
     * Note that each component of the minimum corner MUST be smaller or equal to the respective component of the
     * maximum corner. If a dimension of {@code min} is greater than the respective dimension of {@code max} an {@link
     * IllegalArgumentException} will be thrown.
     * <p>
     * Consider using {@link BlockRegions#encompassing(Vector3ic, Vector3ic...)} as an alternative.
     *
     * @throws IllegalArgumentException if any min component is greater than the corresponding max component
     */
    public BlockRegion(Vector3ic min, Vector3ic max) {
        this(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Creates a new region containing the single block given by the coordinates.
     */
    public BlockRegion(int x, int y, int z) {
        this(x, y, z, x, y, z);
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
     * @see #copy()
     */
    public BlockRegion(BlockRegion source) {
        this.set(source);
    }

    // -- GETTERS & SETTERS ------------------------------------------------------------------------------------------//

    /**
     * Reset this region to have the same minimum and maximum corner as the {@code source} region.
     *
     * @return this region (after modification)
     */
    public BlockRegion set(BlockRegion source) {
        this.minX = source.minX;
        this.minY = source.minY;
        this.minZ = source.minZ;

        this.maxX = source.maxX;
        this.maxY = source.maxY;
        this.maxZ = source.maxZ;
        return this;
    }

    /**
     * Create a fresh copy of this block region.
     *
     * @see BlockRegion#BlockRegion(BlockRegion)
     */
    public BlockRegion copy() {
        return new BlockRegion(this);
    }

    // -- min --------------------------------------------------------------------------------------------------------//

    /**
     * The x-coordinate of the minimum corner
     */
    public int minX() {
        return this.minX;
    }

    /**
     * the minimum coordinate of the first block x
     *
     * @return the minimum coordinate x
     * @deprecated use {@link #minX()}
     */
    @Deprecated
    public int getMinX() {
        return this.minX;
    }

    /**
     * Set the minimum x-coordinate of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if {@code x} is greater than the maximum x-coordinate
     */
    public BlockRegion minX(int x) {
        Preconditions.checkArgument(x <= this.maxX || this.maxX == Integer.MIN_VALUE);
        this.minX = x;
        return this;
    }

    /**
     * The y-coordinate of the minimum corner
     */
    public int minY() {
        return this.minY;
    }

    /**
     * the minimum coordinate of the first block y
     *
     * @return the minimum coordinate y
     * @deprecated use {@link #minY()}
     */
    @Deprecated
    public int getMinY() {
        return this.minY;
    }

    /**
     * Set the minimum y-coordinate of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if {@code y} is greater than the maximum y-coordinate
     */
    public BlockRegion minY(int y) {
        Preconditions.checkArgument(y <= this.maxY || this.maxY == Integer.MIN_VALUE);
        this.minY = y;
        return this;
    }

    /**
     * The z-coordinate of the minimum corner
     */
    public int minZ() {
        return this.minZ;
    }

    /**
     * the minimum coordinate of the first block z
     *
     * @return the minimum coordinate z
     * @deprecated use {@link #minZ()}
     */
    @Deprecated
    public int getMinZ() {
        return this.minZ;
    }

    /**
     * Set the minimum z-coordinate of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if {@code z} is greater than the maximum z-coordinate
     */
    public BlockRegion minZ(int z) {
        Preconditions.checkArgument(z <= this.maxZ || this.maxZ == Integer.MIN_VALUE);
        this.minZ = z;
        return this;
    }

    /**
     * Get the block coordinate of the minimum corner.
     *
     * @param dest will hold the result
     * @return {@code dest} after the result has been set
     */
    public Vector3i getMin(Vector3i dest) {
        return dest.set(minX, minY, minZ);
    }

    /**
     * Set the coordinates of the minimum corner for this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the max
     *         corner
     */
    public BlockRegion setMin(Vector3ic min) {
        return this.setMin(min.x(), min.y(), min.z());
    }

    /**
     * Set the coordinates of the minimum corner for this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the max
     *         corner
     */
    public BlockRegion setMin(int minX, int minY, int minZ) {
        Preconditions.checkArgument(minX <= this.maxX || this.maxX == Integer.MIN_VALUE);
        Preconditions.checkArgument(minY <= this.maxY || this.maxX == Integer.MIN_VALUE);
        Preconditions.checkArgument(minZ <= this.maxZ || this.maxX == Integer.MIN_VALUE);
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        return this;
    }

    // -- max --------------------------------------------------------------------------------------------------------//

    /**
     * The x-coordinate of the maximum corner
     */
    public int maxX() {
        return this.maxX;
    }

    /**
     * the maximum coordinate of the second block x
     *
     * @return the minimum coordinate x
     * @deprecated use {@link #maxX()}
     */
    @Deprecated
    public int getMaxX() {
        return this.maxX;
    }

    /**
     * Set the maximum x-coordinate of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    public BlockRegion maxX(int x) {
        Preconditions.checkArgument(x >= this.minX || this.minX == Integer.MAX_VALUE);
        this.maxX = x;
        return this;
    }

    /**
     * The y-coordinate of the maximum corner
     */
    public int maxY() {
        return this.maxY;
    }

    /**
     * the maximum coordinate of the second block y
     *
     * @return the minimum coordinate y
     * @deprecated use {@link #maxY()}
     */
    @Deprecated
    public int getMaxY() {
        return this.maxY;
    }

    /**
     * Set the maximum y-coordinate of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if {@code y} is smaller than the minimum y-coordinate
     */
    public BlockRegion maxY(int y) {
        Preconditions.checkArgument(y >= this.minY || this.minY == Integer.MAX_VALUE);
        this.maxY = y;
        return this;
    }

    /**
     * The z-coordinate of the maximum corner
     */
    public int maxZ() {
        return this.maxZ;
    }

    /**
     * the maximum coordinate of the second block z
     *
     * @return the minimum coordinate z
     * @deprecated use {@link #maxZ()}
     */
    @Deprecated
    public int getMaxZ() {
        return this.maxZ;
    }

    /**
     * Set the maximum z-coordinate of this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if {@code z} is smaller than the minimum z-coordinate
     */
    public BlockRegion maxZ(int z) {
        Preconditions.checkArgument(z >= this.minZ || this.minZ == Integer.MAX_VALUE);
        this.maxZ = z;
        return this;
    }

    /**
     * Get the block coordinate of the maximum corner.
     *
     * @param dest will hold the result
     * @return {@code dest} after the result has been set
     */
    public Vector3i getMax(Vector3i dest) {
        return dest.set(maxX, maxY, maxZ);
    }

    /**
     * Set the coordinates of the maximum corner for this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    public BlockRegion setMax(Vector3ic max) {
        return this.setMax(max.x(), max.y(), max.z());
    }

    /**
     * Set the coordinates of the maximum corner for this region.
     *
     * @return this region (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    public BlockRegion setMax(int maxX, int maxY, int maxZ) {
        Preconditions.checkArgument(maxX >= this.minX || this.minX == Integer.MAX_VALUE);
        Preconditions.checkArgument(maxY >= this.minY || this.minY == Integer.MAX_VALUE);
        Preconditions.checkArgument(maxZ >= this.minZ || this.minZ == Integer.MAX_VALUE);
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        return this;
    }

    // -- size -------------------------------------------------------------------------------------------------------//

    /**
     * Set the size of the block region from the minimum corner.
     *
     * @param x the x coordinate to set the size; must be > 0
     * @param y the y coordinate to set the size; must be > 0
     * @param z the z coordinate to set the size; must be > 0
     * @return this region (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    public BlockRegion setSize(int x, int y, int z) {
        Preconditions.checkArgument(x > 0);
        Preconditions.checkArgument(y > 0);
        Preconditions.checkArgument(z > 0);
        this.maxX = this.minX + x - 1;
        this.maxY = this.minY + y - 1;
        this.maxZ = this.minZ + z - 1;
        return this;
    }

    /**
     * Set the size of the block region from the minimum corner.
     *
     * @param size the size to set; all dimensions must be > 0
     * @return this region (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    public BlockRegion setSize(Vector3ic size) {
        return setSize(size.x(), size.y(), size.z());
    }

    /**
     * The number of blocks in this region along the +x, +y, +z  axis from the minimum to the maximum corner.
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getSize(Vector3i dest) {
        return dest.set(sizeX(), sizeY(), sizeZ());
    }

    /**
     * The number of blocks on the x axis.
     */
    public int sizeX() {
        return this.maxX - this.minX + 1;
    }

    /**
     * The number of blocks on the y axis.
     */
    public int sizeY() {
        return this.maxY - this.minY + 1;
    }

    /**
     * The number of blocks on the z axis.
     */
    public int sizeZ() {
        return this.maxZ - this.minZ + 1;
    }

    // -- world ------------------------------------------------------------------------------------------------------//

    /**
     * The bounding box in world coordinates.
     * <p>
     * The bounding box of a single block at {@code (x, y, z)} is centered at the integer coordinate {@code (x, y, z)}
     * and extents by {@code 0.5} in each dimension.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    //TODO: 1.9.26 has a constant interface for aabbf
    public AABBf getBounds(AABBf dest) {
        dest.minX = minX - .5f;
        dest.minY = minY - .5f;
        dest.minZ = minZ - .5f;

        dest.maxX = maxX + .5f;
        dest.maxY = maxY + .5f;
        dest.maxZ = maxZ + .5f;

        return dest;
    }

    /**
     * The center of the region in world coordinates if the region is valid; {@link Float#NaN} otherwise.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    public Vector3f center(Vector3f dest) {
        if (!this.isValid()) {
            return dest.set(Float.NaN);
        }
        return dest.set(
                (this.minX - .5f) + ((this.maxX - this.minX + 1.0f) / 2.0f),
                (this.minY - .5f) + ((this.maxY - this.minY + 1.0f) / 2.0f),
                (this.minZ - .5f) + ((this.maxZ - this.minZ + 1.0f) / 2.0f)
        );
    }

    // -- IN-PLACE MUTATION ------------------------------------------------------------------------------------------//

    /**
     * Compute the union of this region and the given block coordinate.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return this region (after modification)
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

    /**
     * Compute the union of this region and the given block coordinate.
     *
     * @param pos the position of the block
     * @return this region (after modification)
     */
    public BlockRegion union(Vector3ic pos) {
        return union(pos.x(), pos.y(), pos.z());
    }

    /**
     * Compute the union of this region and the other region.
     *
     * @param other {@link BlockRegion}
     * @return this region (after modification)
     */
    public BlockRegion union(BlockRegion other) {
        return this.union(other.minX, other.minY, other.minZ).union(other.maxX, other.maxY, other.maxZ);
    }

    /**
     * Compute the union of this region and the given block entity.
     * <p>
     * An entity is a block entity if it has the {@link BlockComponent}. This region will not be modified if the given
     * entity does not have a {@link BlockComponent}.
     *
     * @param blockRef entityRef that describes a block
     * @return this region (after modification)
     */
    public BlockRegion union(EntityRef blockRef) {
        BlockComponent component = blockRef.getComponent(BlockComponent.class);
        if (component != null) {
            return this.union(component.position.x(), component.position.y(), component.position.z());
        }
        return this;
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Compute the intersection of this region with the {@code other} region.
     * <p>
     * NOTE: If the regions don't intersect this region will become invalid!
     *
     * @param other the other region
     * @return this region (after modification) or {@link Optional#empty()} if the regions don't intersect
     */
    public Optional<BlockRegion> intersect(BlockRegion other) {
        this.minX = Math.max(minX, other.minX);
        this.minY = Math.max(minY, other.minY);
        this.minZ = Math.max(minZ, other.minZ);

        this.maxX = Math.min(maxX, other.maxX);
        this.maxY = Math.min(maxY, other.maxY);
        this.maxZ = Math.min(maxZ, other.maxZ);

        if (this.isValid()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param x the x coordinate to translate by
     * @param y the y coordinate to translate by
     * @param z the z coordinate to translate by
     */
    public BlockRegion translate(int x, int y, int z) {
        this.minX = this.minX + x;
        this.minY = this.minY + y;
        this.minZ = this.minZ + z;
        this.maxX = this.maxX + x;
        this.maxY = this.maxY + y;
        this.maxZ = this.maxZ + z;
        return this;
    }

    /**
     * Translate <code>this</code> by the given vector <code>vec</code>.
     *
     * @param vec the vector to translate by
     * @return this
     */
    public BlockRegion translate(Vector3ic vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Adds extend for each face of a BlockRegion
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param extentZ the z coordinate to grow the extents
     */
    public BlockRegion extend(int extentX, int extentY, int extentZ) {
        Preconditions.checkArgument(sizeX() + 2 * extentX > 0);
        Preconditions.checkArgument(sizeY() + 2 * extentY > 0);
        Preconditions.checkArgument(sizeZ() + 2 * extentZ > 0);
        this.minX = this.minX - extentX;
        this.minY = this.minY - extentY;
        this.minZ = this.minZ - extentZ;

        this.maxX = this.maxX + extentX;
        this.maxY = this.maxY + extentY;
        this.maxZ = this.maxZ + extentZ;

        return this;
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extents the coordinates to grow the extents
     * @return this
     */
    public BlockRegion extend(Vector3ic extents) {
        return extend(extents.x(), extents.y(), extents.z());
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param extentZ the z coordinate to grow the extents
     * @return dest
     */
    public BlockRegion extend(float extentX, float extentY, float extentZ) {
        return extend(
                Math.roundUsing(extentX, RoundingMode.FLOOR),
                Math.roundUsing(extentY, RoundingMode.FLOOR),
                Math.roundUsing(extentZ, RoundingMode.FLOOR));
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Apply the given {@link Matrix4fc#isAffine() affine} transformation to this {@link BlockRegion}.
     * <p>
     * The matrix in <code>m</code> <i>must</i> be {@link Matrix4fc#isAffine() affine}.
     *
     * @param m the affine transformation matrix
     * @return this
     */
    public BlockRegion transform(Matrix4fc m, BlockRegion dest) {
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

    /**
     * Apply the given {@link Matrix4fc#isAffine() affine} transformation to this {@link BlockRegion}.
     * <p>
     * The matrix in <code>m</code> <i>must</i> be {@link Matrix4fc#isAffine() affine}.
     *
     * @param m the affine transformation matrix
     * @return this
     */
    public BlockRegion transform(Matrix4fc m) {
        return transform(m, this);
    }

    // -- CHECKS -----------------------------------------------------------------------------------------------------//

    /**
     * Check whether <code>this</code> BlockRegion represents a valid BlockRegion.
     *
     * @return <code>true</code> iff this BlockRegion is valid; <code>false</code> otherwise
     */
    public boolean isValid() {
        return minX <= maxX && minY <= maxY && minZ <= maxZ;
    }


    // -- contains ---------------------------------------------------------------------------------------------------//

    /**
     * Test whether the block <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param pos the coordinates of the block
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsBlock(Vector3ic pos) {
        return containsBlock(pos.x(), pos.y(), pos.z());
    }

    /**
     * Test whether the block <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsBlock(int x, int y, int z) {
        return x >= minX && y >= minY && z >= minZ && x <= maxX && y <= maxY && z <= maxZ;
    }

    /**
     * Test whether the point <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return <code>true</code> iff the given point lies inside this BlockRegion; <code>false</code> otherwise
     */
    public boolean containsPoint(float x, float y, float z) {
        return x >= (this.minX - .5f)
                && y >= (this.minY - .5f)
                && z >= (this.minZ - .5f)
                && x <= (this.maxX + .5f)
                && y <= (this.maxY + .5f)
                && z <= (this.maxZ + .5f);
    }

    /**
     * Test whether the given point lies inside this AABB.
     *
     * @param point the coordinates of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(Vector3fc point) {
        return this.containsPoint(point.x(), point.y(), point.z());
    }

    // -- intersects -------------------------------------------------------------------------------------------------//

    /**
     * Test whether the plane given via its plane equation <code>a*x + b*y + c*z + d = 0</code> intersects this AABB.
     * <p>
     * Reference:
     * <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a>
     * ("Geometric Approach - Testing Boxes II")
     *
     * @param a the x factor in the plane equation
     * @param b the y factor in the plane equation
     * @param c the z factor in the plane equation
     * @param d the constant in the plane equation
     * @return <code>true</code> iff the plane intersects this AABB; <code>false</code> otherwise
     */
    public boolean intersectsPlane(float a, float b, float c, float d) {
        return Intersectionf.testAabPlane(
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f, a, b, c, d);
    }

    /**
     * Test whether the given plane intersects this AABB.
     * <p>
     * Reference:
     * <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a>
     * ("Geometric Approach - Testing Boxes II")
     *
     * @param plane the plane
     * @return <code>true</code> iff the plane intersects this AABB; <code>false</code> otherwise
     */
    public boolean intersectsPlane(Planef plane) {
        return Intersectionf.testAabPlane(
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f,
                plane.a,
                plane.b,
                plane.c,
                plane.d
        );
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other BlockRegion
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsBlockRegion(BlockRegion other) {
        return this.maxX >= other.minX && this.maxY >= other.minY && this.maxZ >= other.minZ &&
                this.minX <= other.maxX && this.minY <= other.maxY && this.minZ <= other.maxZ;
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other AABB
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsAABB(AABBf other) {
        return Intersectionf.testAabAab(
                this.minX - .5f, this.minY - .5f, this.minZ - .5f,
                this.maxX + .5f, this.maxY + .5f, this.maxZ + .5f,
                other.minX, other.minY, other.minZ,
                other.maxX, other.maxY, other.maxZ
        );
    }

    /**
     * Test whether this AABB intersects the given sphere with equation
     * <code>(x - centerX)^2 + (y - centerY)^2 + (z - centerZ)^2 - radiusSquared = 0</code>.
     * <p>
     * Reference:
     * <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     *
     * @param centerX the x coordinate of the center of the sphere
     * @param centerY the y coordinate of the center of the sphere
     * @param centerZ the z coordinate of the center of the sphere
     * @param radiusSquared the square radius of the sphere
     * @return <code>true</code> iff this AABB and the sphere intersect; <code>false</code> otherwise
     */
    public boolean intersectsSphere(float centerX, float centerY, float centerZ, float radiusSquared) {
        return Intersectionf.testAabSphere(
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f,
                centerX,
                centerY,
                centerZ,
                radiusSquared
        );
    }

    /**
     * Test whether this AABB intersects the given sphere.
     * <p>
     * Reference:
     * <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     *
     * @param sphere the sphere
     * @return <code>true</code> iff this AABB and the sphere intersect; <code>false</code> otherwise
     */
    public boolean intersectsSphere(Spheref sphere) {
        return Intersectionf.testAabSphere(
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f,
                sphere.x,
                sphere.y,
                sphere.z,
                sphere.r * sphere.r
        );
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX,
     * dirY, dirZ)</code> intersects this AABB.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside this AABB.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param originX the x coordinate of the ray's origin
     * @param originY the y coordinate of the ray's origin
     * @param originZ the z coordinate of the ray's origin
     * @param dirX the x coordinate of the ray's direction
     * @param dirY the y coordinate of the ray's direction
     * @param dirZ the z coordinate of the ray's direction
     * @return <code>true</code> if this AABB and the ray intersect; <code>false</code> otherwise
     */
    public boolean intersectsRay(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
        return Intersectionf.testRayAab(
                originX, originY, originZ, dirX, dirY, dirZ,
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f
        );
    }

    /**
     * Test whether the given ray intersects this AABB.
     * <p>
     * This method returns <code>true</code> for a ray whose origin lies inside this AABB.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param ray the ray
     * @return <code>true</code> if this AABB and the ray intersect; <code>false</code> otherwise
     */
    public boolean intersectsRay(Rayf ray) {
        return Intersectionf.testRayAab(
                ray.oX, ray.oY, ray.oZ, ray.dX, ray.dY, ray.dZ,
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f
        );
    }

    /**
     * Determine whether the undirected line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X,
     * p1Y, p1Z)</code> intersects this AABB, and return the values of the parameter <i>t</i> in the ray equation
     * <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns <code>true</code> for a line segment whose either end point lies inside this AABB.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param p0X the x coordinate of the line segment's first end point
     * @param p0Y the y coordinate of the line segment's first end point
     * @param p0Z the z coordinate of the line segment's first end point
     * @param p1X the x coordinate of the line segment's second end point
     * @param p1Y the y coordinate of the line segment's second end point
     * @param p1Z the z coordinate of the line segment's second end point
     * @param result a vector which will hold the resulting values of the parameter
     *         <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     *         iff the line segment intersects this AABB
     * @return {@link Intersectionf#INSIDE} if the line segment lies completely inside of this AABB; or {@link
     *         Intersectionf#OUTSIDE} if the line segment lies completely outside of this AABB; or {@link
     *         Intersectionf#ONE_INTERSECTION} if one of the end points of the line segment lies inside of this AABB; or
     *         {@link Intersectionf#TWO_INTERSECTION} if the line segment intersects two sides of this AABB or lies on
     *         an edge or a side of this AABB
     */
    public int intersectLineSegment(float p0X, float p0Y, float p0Z, float p1X, float p1Y, float p1Z, Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(p0X, p0Y, p0Z, p1X, p1Y, p1Z,
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f, result);

    }

    /**
     * Determine whether the given undirected line segment intersects this AABB, and return the values of the parameter
     * <i>t</i> in the ray equation
     * <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns <code>true</code> for a line segment whose either end point lies inside this AABB.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param lineSegment the line segment
     * @param result a vector which will hold the resulting values of the parameter
     *         <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     *         iff the line segment intersects this AABB
     * @return {@link Intersectionf#INSIDE} if the line segment lies completely inside of this AABB; or {@link
     *         Intersectionf#OUTSIDE} if the line segment lies completely outside of this AABB; or {@link
     *         Intersectionf#ONE_INTERSECTION} if one of the end points of the line segment lies inside of this AABB; or
     *         {@link Intersectionf#TWO_INTERSECTION} if the line segment intersects two sides of this AABB or lies on
     *         an edge or a side of this AABB
     */
    public int intersectLineSegment(LineSegmentf lineSegment, Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(
                lineSegment.aX, lineSegment.aY, lineSegment.aZ, lineSegment.bX, lineSegment.bY, lineSegment.bZ,
                minX - .5f,
                minY - .5f,
                minZ - .5f,
                maxX + .5f,
                maxY + .5f,
                maxZ + .5f, result);
    }

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
        return "(" + this.minX + " " + this.minY + " " + this.minZ + ") < " +
                "(" + (this.maxX - 1) + " " + (this.maxY - 1) + " " + (this.maxZ - 1) + ")";
    }
}
