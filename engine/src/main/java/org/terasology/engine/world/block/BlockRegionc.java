// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.Intersectionf;
import org.terasology.joml.geom.LineSegmentf;
import org.terasology.joml.geom.Planef;
import org.terasology.joml.geom.Rayf;
import org.terasology.joml.geom.Spheref;
import org.terasology.engine.math.Side;

import java.util.Iterator;
import java.util.Optional;

/**
 * An immutable, bounded, axis-aligned volume in space denoting a collection of blocks contained within.
 */
public interface BlockRegionc extends Iterable<Vector3ic> {

    // -- ITERABLE ---------------------------------------------------------------------------------------------------//

    /**
     * Iterate over the blocks in the block region, where the same {@link Vector3ic} is reused for low memory
     * footprint.
     * <p>
     * Do not store the elements directly or use them outside the context of the iterator as they will change when the
     * iterator is advanced. You may create new vectors from the elements if necessary, e.g.:
     * <pre>
     *     for (Vector3ic p : region) {
     *         Vector3i pos = new Vector3i(p);
     *         // use 'pos' instead of 'p'
     *     }
     * </pre>
     */
    Iterator<Vector3ic> iterator();

    // -- min -------------------------------------------------------------------------------------------------------//

    /**
     * The x-coordinate of the minimum corner
     */
    int minX();

    /**
     * The y-coordinate of the minimum corner
     */
    int minY();

    /**
     * The z-coordinate of the minimum corner
     */
    int minZ();

    /**
     * Get the block coordinate of the minimum corner.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Vector3i getMin(Vector3i dest) {
        return dest.set(minX(), minY(), minZ());
    }

    /**
     * Set the minimum x-coordinate of the region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is greater than the maximum x coordinate
     */
    BlockRegion minX(int x, BlockRegion dest);

    /**
     * Set the minimum y-coordinate of the region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code y} is greater than the maximum y coordinate
     */
    BlockRegion minY(int y, BlockRegion dest);

    /**
     * Set the minimum z-coordinate of the region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code z} is greater than the maximum z coordinate
     */
    BlockRegion minZ(int z, BlockRegion dest);

    /**
     * Set the coordinates of the minimum corner for this region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the maximum
     *         corner
     */
    BlockRegion setMin(int x, int y, int z, BlockRegion dest);

    /**
     * Set the coordinates of the minimum corner for this region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the maximum
     *         corner
     */
    default BlockRegion setMin(Vector3ic min, BlockRegion dest) {
        return this.setMin(min.x(), min.y(), min.z(), dest);
    }

    /**
     * Translate the minimum corner of the region by adding given {@code (dx, dy, dz)}.
     *
     * @param dx the number of blocks to add to the minimum corner on the x axis
     * @param dy the number of blocks to add to the minimum corner on the y axis
     * @param dz the number of blocks to add to the minimum corner on the z axis
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting region would be {@link #isValid() invalid}.
     */
    default BlockRegion addToMin(int dx, int dy, int dz, BlockRegion dest) {
        return this.setMin(minX() + dx, minY() + dy, minZ() + dz, dest);
    }

    /**
     * Translate the minimum corner of the region by adding given {@code (dx, dy, dz)}.
     *
     * @param dmin the translation vector for the minimum corner
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting region would be {@link #isValid() invalid}.
     */
    default BlockRegion addToMin(Vector3ic dmin, BlockRegion dest) {
        return this.addToMin(dmin.x(), dmin.y(), dmin.z(), dest);
    }

    // -- max -------------------------------------------------------------------------------------------------------//

    /**
     * The x-coordinate of the maximum corner
     */
    int maxX();

    /**
     * The y-coordinate of the maximum corner
     */
    int maxY();

    /**
     * The z-coordinate of the maximum corner
     */
    int maxZ();

    /**
     * Get the block coordinate of the maximum corner.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Vector3i getMax(Vector3i dest) {
        return dest.set(maxX(), maxY(), maxZ());
    }

    /**
     * Set the maximum x-coordinate of the region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    BlockRegion maxX(int x, BlockRegion dest);

    /**
     * Set the maximum y-coordinate of the region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    BlockRegion maxY(int y, BlockRegion dest);

    /**
     * Set the maximum z-coordinate of the region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    BlockRegion maxZ(int z, BlockRegion dest);

    /**
     * Set the coordinates of the maximum corner for this region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    BlockRegion setMax(int x, int y, int z, BlockRegion dest);

    /**
     * Set the coordinates of the maximum corner for this region.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    default BlockRegion setMax(Vector3ic max, BlockRegion dest) {
        return this.setMax(max.x(), max.y(), max.z(), dest);
    }

    /**
     * Translate the maximum corner of the region by adding given {@code (dx, dy, dz)}.
     *
     * @param dx the number of blocks to add to the maximum corner on the x axis
     * @param dy the number of blocks to add to the maximum corner on the y axis
     * @param dz the number of blocks to add to the maximum corner on the z axis
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting region would be {@link #isValid() invalid}.
     */
    default BlockRegion addToMax(int dx, int dy, int dz, BlockRegion dest) {
        return this.setMax(maxX() + dx, maxY() + dy, maxZ() + dz, dest);
    }

    /**
     * Translate the maximum corner of the region by adding given {@code (dx, dy, dz)}.
     *
     * @param dmax the translation vector for the maximum corner
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting region would be {@link #isValid() invalid}.
     */
    default BlockRegion addToMax(Vector3ic dmax, BlockRegion dest) {
        return this.addToMax(dmax.x(), dmax.y(), dmax.z(), dest);
    }

    // -- size ------------------------------------------------------------------------------------------------------//

    /**
     * The number of blocks on the x axis.
     */
    default int getSizeX() {
        return maxX() - minX() + 1;
    }

    /**
     * The number of blocks on the y axis.
     */
    default int getSizeY() {
        return maxY() - minY() + 1;
    }

    /**
     * The number of blocks on the z axis.
     */
    default int getSizeZ() {
        return maxZ() - minZ() + 1;
    }

    /**
     * The number of blocks in this region along the +x, +y, +z  axis from the minimum to the maximum corner.
     *
     * @param dest will hold the result
     * @return dest
     */
    default Vector3i getSize(Vector3i dest) {
        return dest.set(getSizeX(), getSizeY(), getSizeZ());
    }

    /**
     * Set the size of the block region from the minimum corner.
     *
     * @param x the x coordinate to set the size; must be greater than 0
     * @param y the y coordinate to set the size; must be greater than 0
     * @param z the z coordinate to set the size; must be greater than 0
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    BlockRegion setSize(int x, int y, int z, BlockRegion dest);

    /**
     * Set the size of the block region from the minimum corner.
     *
     * @param size the size to set; all dimensions must be greater than 0
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    default BlockRegion setSize(Vector3ic size, BlockRegion dest) {
        return this.setSize(size.x(), size.y(), size.z(), dest);
    }

    /**
     * The volume of this region in blocks, i.e., the number of blocks contained in this region.
     * <p>
     * The volume is computed by
     * <pre>
     *     volume = sizeX * sizeY * sizeZ;
     * </pre>
     *
     * @return the volume in blocks
     */
    default int volume() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    // -- world -----------------------------------------------------------------------------------------------------//

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
    default AABBf getBounds(AABBf dest) {
        dest.minX = minX() - .5f;
        dest.minY = minY() - .5f;
        dest.minZ = minZ() - .5f;

        dest.maxX = maxX() + .5f;
        dest.maxY = maxY() + .5f;
        dest.maxZ = maxZ() + .5f;

        return dest;
    }

    /**
     * The center of the region in world coordinates if the region is valid; {@link Float#NaN} otherwise.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Vector3f center(Vector3f dest) {
        if (!this.isValid()) {
            return dest.set(Float.NaN);
        }
        return dest.set(
                (this.minX() - .5f) + ((this.maxX() - this.minX() + 1.0f) / 2.0f),
                (this.minY() - .5f) + ((this.maxY() - this.minY() + 1.0f) / 2.0f),
                (this.minZ() - .5f) + ((this.maxZ() - this.minZ() + 1.0f) / 2.0f)
        );
    }

    // -- IN-PLACE MUTATION -----------------------------------------------------------------------------------------//
    // -- union -----------------------------------------------------------------------------------------------------//

    /**
     * Compute the union of this region and the given block coordinate.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    BlockRegion union(int x, int y, int z, BlockRegion dest);

    /**
     * Compute the union of this region and the given block coordinate.
     *
     * @param pos the position of the block
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockRegion union(Vector3ic pos, BlockRegion dest) {
        return this.union(pos.x(), pos.y(), pos.z(), dest);
    }

    /**
     * Compute the union of this region and the other region.
     *
     * @param other {@link BlockRegion}
     * @param dest destination; will hold the result
     * @return dest (after modification)
     */
    default BlockRegion union(BlockRegionc other, BlockRegion dest) {
        return this.union(other.minX(), other.minY(), other.minZ(), dest)
                .union(other.maxX(), other.maxY(), other.maxZ(), dest);
    }

    // -- intersect -------------------------------------------------------------------------------------------------//

    /**
     * Compute the intersection of this region with the {@code other} region.
     * <p>
     * NOTE: If the regions don't intersect the destination region will become invalid!
     *
     * @param other the other region
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification) or {@link Optional#empty()} if the regions don't intersect
     */
    Optional<BlockRegion> intersect(BlockRegionc other, BlockRegion dest);

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Translate this region by the given vector {@code (x, y, z))}.
     *
     * @param dx the x coordinate to translate by
     * @param dy the y coordinate to translate by
     * @param dz the z coordinate to translate by
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    BlockRegion translate(int dx, int dy, int dz, BlockRegion dest);

    /**
     * Translate this region by the given vector {@code vec}.
     *
     * @param vec the vector to translate by
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockRegion translate(Vector3ic vec, BlockRegion dest) {
        return this.translate(vec.x(), vec.y(), vec.z(), dest);
    }

    /**
     * Move this region to the given position {@code (x, y, z)}. The position is defined by the minimum corner.
     *
     * @param x the new x coordinate of the minimum corner
     * @param y the new y coordinate of the minimum corner
     * @param z the new z coordinate of the minimum corner
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockRegion setPosition(int x, int y, int z, BlockRegion dest) {
        return dest.translate(x - this.minX(), y - this.minY(), z - this.minZ());
    }

    /**
     * Move this region to the given position {@code (x, y, z)}. The position is defined by the minimum corner.
     *
     * @param pos the new coordinates of the minimum corner
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockRegion setPosition(Vector3ic pos, BlockRegion dest) {
        return setPosition(pos.x(), pos.y(), pos.z(), dest);
    }

    // -- expand -----------------------------------------------------------------------------------------------------//

    /**
     * Expand this region by adding the given {@code extents} for each face of the region.
     *
     * @param dx the amount of blocks to extend this region by along the x axis in both directions
     * @param dy the amount of blocks to extend this region by along the y axis in both directions
     * @param dz the amount of blocks to extend this region by along the z axis in both directions
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if extending this region would result in any non-positive dimension
     */
    BlockRegion expand(int dx, int dy, int dz, BlockRegion dest);

    /**
     * Expand this region by adding the given {@code extents} for each face of a region.
     *
     * @param vec the amount of blocks to expand this region by
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if extending this region would result in any non-positive dimension
     */
    default BlockRegion expand(Vector3ic vec, BlockRegion dest) {
        return this.expand(vec.x(), vec.y(), vec.z(), dest);
    }

    // -- transform --------------------------------------------------------------------------------------------------//

    /**
     * Apply the given {@link Matrix4fc#isAffine() affine} transformation to this {@link BlockRegion}.
     * <p>
     * The matrix in {@code m} <i>must</i> be {@link Matrix4fc#isAffine() affine}.
     *
     * @param m the affine transformation matrix
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the matrix {@code m} is not {@link Matrix4fc#isAffine() affine}
     */
    BlockRegion transform(Matrix4fc m, BlockRegion dest);

    // -- face -------------------------------------------------------------------------------------------------------//

    /**
     * Calculates a 1 width region that borders the provided {@link Side} of a region.
     * <p>
     * The resulting region is a subset of this region, i.e., the intersection of the face region with the source region
     * is exactly the face region.
     *
     * @param side the side of the region
     * @param dest will hold the result
     * @return dest
     */
    default BlockRegion face(Side side, BlockRegion dest) {
        switch (side) {
            case TOP:
                return dest.set(this.minX(), this.maxY(), this.minZ(), this.maxX(), this.maxY(), this.maxZ());
            case BOTTOM:
                return dest.set(this.minX(), this.minY(), this.minZ(), this.maxX(), this.minY(), this.maxZ());
            case LEFT:
                return dest.set(this.minX(), this.minY(), this.minZ(), this.minX(), this.maxY(), this.maxZ());
            case RIGHT:
                return dest.set(this.maxX(), this.minY(), this.minZ(), this.maxX(), this.maxY(), this.maxZ());
            case FRONT:
                return dest.set(this.minX(), this.minY(), this.minZ(), this.maxX(), this.maxY(), this.minZ());
            case BACK:
                return dest.set(this.minX(), this.minY(), this.maxZ(), this.maxX(), this.maxY(), this.maxZ());
            default:
                return dest.set(this);
        }
    }

    // -- CHECKS -----------------------------------------------------------------------------------------------------//

    /**
     * Check whether this region BlockRegion represents a valid BlockRegion.
     *
     * @return {@code true} iff this BlockRegion is valid; {@code false} otherwise
     */
    default boolean isValid() {
        return minX() <= maxX() && minY() <= maxY() && minZ() <= maxZ();
    }

    // -- contains ---------------------------------------------------------------------------------------------------//

    /**
     * Test whether the block {@code (x, y, z)} lies inside this region.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return {@code true} iff the given point lies inside this region; {@code false} otherwise
     */
    default boolean contains(int x, int y, int z) {
        return x >= minX() && y >= minY() && z >= minZ() && x <= maxX() && y <= maxY() && z <= maxZ();
    }

    /**
     * Test whether the block at position {@code pos} lies inside this region.
     *
     * @param pos the coordinates of the block
     * @return {@code true} iff the given point lies inside this region; {@code false} otherwise
     */
    default boolean contains(Vector3ic pos) {
        return this.contains(pos.x(), pos.y(), pos.z());
    }

    /**
     * Test whether the point {@code (x, y, z)} lies inside this region.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return {@code true} iff the given point lies inside this region; {@code false} otherwise
     * @see #getBounds(AABBf)
     */
    default boolean contains(float x, float y, float z) {
        return x >= (this.minX() - .5f)
                && y >= (this.minY() - .5f)
                && z >= (this.minZ() - .5f)
                && x <= (this.maxX() + .5f)
                && y <= (this.maxY() + .5f)
                && z <= (this.maxZ() + .5f);
    }

    /**
     * Test whether the {@code point} lies inside this region.
     *
     * @param point the coordinates of the point
     * @return {@code true} iff the given point lies inside this region; {@code false} otherwise
     */
    default boolean contains(Vector3fc point) {
        return this.contains(point.x(), point.y(), point.z());
    }

    /**
     * Test whether the given region {@code other} is fully enclosed by this region.
     *
     * @param other the other region
     * @return {@code true} iff the given region is fully enclosed by this region; {@code false} otherwise
     */
    default boolean contains(BlockRegionc other) {
        return this.contains(other.minX(), other.minY(), other.minZ())
                && this.contains(other.maxX(), other.maxY(), other.maxZ());
    }

    // -- intersects -------------------------------------------------------------------------------------------------//

    /**
     * Test whether this region and the AABB {@code other} intersect.
     *
     * @param other the other AABB
     * @return {@code true} iff both AABBs intersect; {@code false} otherwise
     */
    default boolean intersectsAABB(AABBf other) {
        return Intersectionf.testAabAab(
                this.minX() - .5f, this.minY() - .5f, this.minZ() - .5f,
                this.maxX() + .5f, this.maxY() + .5f, this.maxZ() + .5f,
                other.minX, other.minY, other.minZ,
                other.maxX, other.maxY, other.maxZ
        );
    }

    /**
     * Test whether this region and {@code other} intersect.
     *
     * @param other the other BlockRegion
     * @return {@code true} iff both regions intersect; {@code false} otherwise
     */
    default boolean intersectsBlockRegion(BlockRegionc other) {
        return this.maxX() >= other.minX() && this.maxY() >= other.minY() && this.maxZ() >= other.minZ()
                && this.minX() <= other.maxX() && this.minY() <= other.maxY() && this.minZ() <= other.maxZ();
    }

    /**
     * Test whether the plane given via its plane equation <code>a*x + b*y + c*z + d = 0</code> intersects this region.
     * <p>
     * Reference:
     * <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a>
     * ("Geometric Approach - Testing Boxes II")
     *
     * @param a the x factor in the plane equation
     * @param b the y factor in the plane equation
     * @param c the z factor in the plane equation
     * @param d the constant in the plane equation
     * @return {@code true} iff the plane intersects this region; {@code false} otherwise
     */
    default boolean intersectsPlane(float a, float b, float c, float d) {
        return Intersectionf.testAabPlane(
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f, a, b, c, d);
    }

    /**
     * Test whether the given plane intersects this region.
     * <p>
     * Reference:
     * <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a>
     * ("Geometric Approach - Testing Boxes II")
     *
     * @param plane the plane
     * @return {@code true} iff the plane intersects this region; {@code false} otherwise
     */
    default boolean intersectsPlane(Planef plane) {
        return this.intersectsPlane(plane.a, plane.b, plane.c, plane.d);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX,
     * dirY, dirZ)</code> intersects this AABB.
     * <p>
     * This method returns {@code true} for a ray whose origin lies inside this region.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param originX the x coordinate of the ray's origin
     * @param originY the y coordinate of the ray's origin
     * @param originZ the z coordinate of the ray's origin
     * @param dirX the x coordinate of the ray's direction
     * @param dirY the y coordinate of the ray's direction
     * @param dirZ the z coordinate of the ray's direction
     * @return {@code true} if this region and the ray intersect; {@code false} otherwise
     */
    default boolean intersectsRay(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
        return Intersectionf.testRayAab(
                originX, originY, originZ, dirX, dirY, dirZ,
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f
        );
    }

    /**
     * Test whether the given ray intersects this region.
     * <p>
     * This method returns {@code true} for a ray whose origin lies inside this region.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param ray the ray
     * @return {@code true} if this AABB and the ray intersect; {@code false} otherwise
     */
    default boolean intersectsRay(Rayf ray) {
        return this.intersectsRay(ray.oX, ray.oY, ray.oZ, ray.dX, ray.dY, ray.dZ);
    }

    /**
     * Test whether this region intersects the given sphere with equation
     * <code>(x - centerX)^2 + (y - centerY)^2 + (z - centerZ)^2 - radiusSquared = 0</code>.
     * <p>
     * Reference:
     * <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     *
     * @param centerX the x coordinate of the center of the sphere
     * @param centerY the y coordinate of the center of the sphere
     * @param centerZ the z coordinate of the center of the sphere
     * @param radiusSquared the square radius of the sphere
     * @return {@code true} iff this region and the sphere intersect; {@code false} otherwise
     */
    default boolean intersectsSphere(float centerX, float centerY, float centerZ, float radiusSquared) {
        return Intersectionf.testAabSphere(
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f,
                centerX,
                centerY,
                centerZ,
                radiusSquared
        );
    }

    /**
     * Test whether this region intersects the given sphere.
     * <p>
     * Reference:
     * <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     *
     * @param sphere the sphere
     * @return {@code true} iff this region and the sphere intersect; {@code false} otherwise
     */
    default boolean intersectsSphere(Spheref sphere) {
        return this.intersectsSphere(sphere.x, sphere.y, sphere.z, sphere.r * sphere.r);
    }

    /**
     * Determine whether the undirected line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X,
     * p1Y, p1Z)</code> intersects this region, and return the values of the parameter <i>t</i> in the ray equation
     * <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns {@code true} for a line segment whose either end point lies inside this AABB.
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
    default int intersectLineSegment(float p0X, float p0Y, float p0Z, float p1X, float p1Y, float p1Z,
                                     Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(p0X, p0Y, p0Z, p1X, p1Y, p1Z,
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f, result);
    }

    /**
     * Determine whether the given undirected line segment intersects this region, and return the values of the
     * parameter
     * <i>t</i> in the ray equation
     * <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns {@code true} for a line segment whose either end point lies inside this regiono.
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
    default int intersectLineSegment(LineSegmentf lineSegment, Vector2f result) {
        return this.intersectLineSegment(lineSegment.aX, lineSegment.aY, lineSegment.aZ,
                lineSegment.bX, lineSegment.bY, lineSegment.bZ,
                result);
    }

    // ---------------------------------------------------------------------------------------------------------------//
    boolean equals(Object obj);

    int hashCode();

    String toString();
}
