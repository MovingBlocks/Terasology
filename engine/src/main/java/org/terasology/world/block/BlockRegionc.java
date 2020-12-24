// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import org.joml.AABBf;
import org.joml.Intersectionf;
import org.joml.LineSegmentf;
import org.joml.Planef;
import org.joml.Rayf;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Iterator;

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

    // -- CHECKS -----------------------------------------------------------------------------------------------------//

    /**
     * Check whether this region is valid. A region is valid if the minimum corner is componen-wise smaller or equal to
     * the maximum corner.
     *
     * @return {@code true} iff this region is valid; {@code false} otherwise
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
