/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block;

import org.joml.AABBf;
import org.joml.AABBi;
import org.joml.Intersectionf;
import org.joml.LineSegmentf;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Planef;
import org.joml.Rayf;
import org.joml.RoundingMode;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * is a bounded box describing blocks contained within.
 * A {@link BlockRegion} is described and backed by an {@link AABBi}
 */
public class BlockRegion {

    /**
     * AABB region that backs a BlockRegion
     */
    public final AABBi aabb = new AABBi();

    public BlockRegion() {
    }

    public BlockRegion(BlockRegion source) {
        aabb.set(source.aabb);
    }

    public BlockRegion(AABBi source) {
        aabb.set(source);
    }

    public BlockRegion(Vector3ic min, Vector3ic max) {
        this(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    public BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.setMin(minX, minY, minZ).setMax(maxX, maxY, maxZ);
    }


    /**
     * get the minimum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getMin(Vector3i dest) {
        return dest.set(aabb.minX, aabb.minY, aabb.minZ);
    }

    /**
     * get the maximum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getMax(Vector3i dest) {
        return dest.set(aabb.maxX - 1, aabb.maxY - 1, aabb.maxZ - 1);
    }
    /**
     * the maximum coordinate of the second block x
     * @return the minimum coordinate x
     */
    public int getMaxX() {
        return this.aabb.maxX - 1;
    }
    /**
     * the maximum coordinate of the second block y
     * @return the minimum coordinate y
     */
    public int getMaxY() {
        return this.aabb.maxY - 1;
    }
    /**
     * the maximum coordinate of the second block z
     * @return the minimum coordinate z
     */
    public int getMaxZ() {
        return this.aabb.maxZ - 1;
    }

    /**
     * the minimum coordinate of the first block x
     * @return the minimum coordinate x
     */
    public int getMinX() {
        return this.aabb.minX;
    }
    /**
     * the minimum coordinate of the first block y
     * @return the minimum coordinate y
     */
    public int getMinY() {
        return this.aabb.minY;
    }

    /**
     * the minimum coordinate of the first block z
     * @return the minimum coordinate z
     */
    public int getMinZ() {
        return this.aabb.minZ;
    }

    /**
     * Sets the minimum coordinate of the first block for <code>this</code> {@link BlockRegion}
     *
     * @param min the first coordinate of the first block
     * @return this
     */
    public BlockRegion setMin(Vector3ic min) {
        this.aabb.setMin(min);
        return this;
    }

    /**
     * Sets the maximum coordinate of the second block for <code>this</code> {@link BlockRegion}
     *
     * @param max the second coordinate of the second block
     * @return this
     */
    public BlockRegion setMax(Vector3ic max) {
        this.setMax(max.x(), max.y(), max.z());
        return this;
    }

    /**
     * sets the maximum block for this {@link BlockRegion}
     *
     * @param maxX the x coordinate of the first block
     * @param maxY the y coordinate of the first block
     * @param maxZ the z coordinate of the first block
     * @return this
     */
    public BlockRegion setMax(int maxX, int maxY, int maxZ) {
        this.aabb.setMax(maxX + 1, maxY + 1, maxZ + 1);
        return this;
    }

    /**
     * sets the minimum block for this {@link BlockRegion}
     *
     * @param minX the x coordinate of the first block
     * @param minY the y coordinate of the first block
     * @param minZ the z coordinate of the first block
     * @return this
     */
    public BlockRegion setMin(int minX, int minY, int minZ) {
        aabb.setMin(minX, minY, minZ);
        return this;
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and the given {@link EntityRef} associated with a block <code>p</code>.
     * @param blockRef entityRef that describes a block
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion union(EntityRef blockRef, BlockRegion dest) {
        BlockComponent component = blockRef.getComponent(BlockComponent.class);
        if (component != null) {
            return this.union(component.position.x(), component.position.y(), component.position.z(), dest);
        }
        return dest;
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and the given block <code>p</code>.
     *
     * @param p the position of the block
     * @return this
     */
    public BlockRegion union(Vector3ic p) {
        return union(p.x(), p.y(), p.z(), this);
    }

    /**
     * Compute the union of <code>this</code> and the given block <code>(x, y, z)</code> and stores the result in <code>dest</code>
     *
     * @param x    the x coordinate of the block
     * @param y    the y coordinate of the block
     * @param z    the z coordinate of the block
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion union(int x, int y, int z, BlockRegion dest) {
        // a block is (x,y,z) and (x + 1, y + 1, z + 1)
        dest.aabb.minX = Math.min(this.aabb.minX, x);
        dest.aabb.minY = Math.min(this.aabb.minY, y);
        dest.aabb.minZ = Math.min(this.aabb.minZ, z);
        dest.aabb.maxX = Math.max(this.aabb.maxX, (x + 1));
        dest.aabb.maxY = Math.max(this.aabb.maxY, (y + 1));
        dest.aabb.maxZ = Math.max(this.aabb.maxZ, (z + 1));
        return dest;
    }

    /**
     * Compute the union of <code>this</code> and the given block <code>(x, y, z)</code> and store the result in <code>dest</code>.
     * @param pos the position of the block
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion union(Vector3ic pos, BlockRegion dest) {
        return this.union(pos.x(), pos.y(), pos.z(), dest);
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other {@link BlockRegion}
     * @return this
     */
    public BlockRegion union(BlockRegion other) {
        return this.union(other.aabb);
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other {@link AABBi}
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion union(AABBi other, BlockRegion dest) {
        dest.union(other);
        return dest;
    }

    /**
     *  Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other the other {@link AABBi}
     * @return this
     */
    public BlockRegion union(AABBi other) {
        this.aabb.union(other);
        return this;
    }

    /**
     * Ensure that the minimum coordinates are strictly less than or equal to the maximum coordinates by swapping
     * them if necessary.
     *
     * @return this
     */
    public BlockRegion correctBounds() {
        this.aabb.correctBounds();
        return this;
    }

    /**
     * set the size of the block region from minimum.
     * @param x the x coordinate to set the size
     * @param y the y coordinate to set the size
     * @param z the z coordinate to set the size
     * @return this
     */
    public BlockRegion setSize(int x, int y, int z) {
        this.aabb.maxX = this.aabb.minX + x;
        this.aabb.maxY = this.aabb.minY + y;
        this.aabb.maxZ = this.aabb.minZ + z;
        return this;
    }

    /**
     * set the size of the block region from minimum.
     * @param size the size to set the {@link BlockRegion}
     * @return this
     */
    public BlockRegion setSize(Vector3ic size) {
        return setSize(size.x(), size.y(), size.z());
    }

    /**
     * the number of blocks for the +x, +y, +z from the minimum to the maximum
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getSize(Vector3i dest) {
        return dest.set(this.aabb.maxX - this.aabb.minX, this.aabb.maxY - this.aabb.minY, this.aabb.maxZ - this.aabb.minZ);
    }

    /**
     * The number of blocks on the X axis
     *
     * @return number of blocks in the X axis
     */
    public int getSizeX() {
        return this.aabb.maxX - this.aabb.minX;
    }

    /**
     * The number of blocks on the Y axis
     *
     * @return number of blocks in the Y axis
     */
    public int getSizeY() {
        return this.aabb.maxY - this.aabb.minY;
    }

    /**
     * The number of blocks on the Z axis
     *
     * @return number of blocks in the Z axis
     */
    public int getSizeZ() {
        return this.aabb.maxZ - this.aabb.minZ;
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     * @param x  the x coordinate to translate by
     * @param y  the y coordinate to translate by
     * @param z  the z coordinate to translate by
     * @return this
     */
    public BlockRegion translate(int x, int y, int z) {
        aabb.translate(x, y, z);
        return this;
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     * @param xyz  the vector to translate by
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion translate(Vector3ic xyz, BlockRegion dest) {
        aabb.translate(xyz, dest.aabb);
        return dest;
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param xyz
     *          the vector to translate by
     * @return this
     */
    public BlockRegion translate(Vector3ic xyz) {
        this.aabb.translate(xyz);
        return this;
    }

    /**
     * Apply the given {@link Matrix4fc#isAffine() affine} transformation to this {@link BlockRegion}.
     * <p>
     * The matrix in <code>m</code> <i>must</i> be {@link Matrix4fc#isAffine() affine}.
     *
     * @param m
     *          the affine transformation matrix
     * @return this
     */
    public BlockRegion transform(Matrix4fc m) {
        this.aabb.transform(m);
        return this;
    }

    /**
     * Apply the given {@link Matrix4fc#isAffine() affine} transformation to this {@link BlockRegion}.
     * <p>
     * The matrix in <code>m</code> <i>must</i> be {@link Matrix4fc#isAffine() affine}.
     *
     * @param m
     *          the affine transformation matrix
     * @return this
     */
    public BlockRegion transform(Matrix4fc m, BlockRegion dest) {
        this.aabb.transform(m, dest.aabb);
        return dest;
    }

    /**
     * Test whether the block <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param pos  the coordinates of the block
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
        return x >= aabb.minX && y >= aabb.minY && z >= aabb.minZ && x < aabb.maxX && y < aabb.maxY && z < aabb.maxZ;
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
        return this.aabb.containsPoint(x, y, z);
    }

    /**
     * Test whether the point <code>(x, y, z)</code> lies inside this AABB.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(int x, int y, int z) {
        return this.aabb.containsPoint(x, y, z);
    }

    /**
     * Test whether the given point lies inside this AABB.
     *
     * @param point the coordinates of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(Vector3ic point) {
        return this.aabb.containsPoint(point);
    }

    /**
     * Test whether the given point lies inside this AABB.
     *
     * @param point the coordinates of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(Vector3fc point) {
        return this.aabb.containsPoint(point);
    }

    /**
     * Test whether the plane given via its plane equation <code>a*x + b*y + c*z + d = 0</code> intersects this AABB.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     *
     * @param a the x factor in the plane equation
     * @param b the y factor in the plane equation
     * @param c the z factor in the plane equation
     * @param d the constant in the plane equation
     * @return <code>true</code> iff the plane intersects this AABB; <code>false</code> otherwise
     */
    public boolean intersectsPlane(float a, float b, float c, float d) {
        return this.aabb.intersectsPlane(a, b, c, d);
    }

    /**
     * Test whether the given plane intersects this AABB.
     * <p>
     * Reference: <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/">http://www.lighthouse3d.com</a> ("Geometric Approach - Testing Boxes II")
     *
     * @param plane the plane
     * @return <code>true</code> iff the plane intersects this AABB; <code>false</code> otherwise
     */
    public boolean intersectsPlane(Planef plane) {
        return this.aabb.intersectsPlane(plane);
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other BlockRegion
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsBlockRegion(BlockRegion other) {
        return this.aabb.intersectsAABB(other.aabb);
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other AABB
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsAABB(AABBi other) {
        return this.aabb.intersectsAABB(other);
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other AABB
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsAABB(AABBf other) {
        return this.aabb.intersectsAABB(other);
    }

    /**
     * Test whether this AABB intersects the given sphere with equation
     * <code>(x - centerX)^2 + (y - centerY)^2 + (z - centerZ)^2 - radiusSquared = 0</code>.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     * @param centerX the x coordinate of the center of the sphere
     * @param centerY the y coordinate of the center of the sphere
     * @param centerZ the z coordinate of the center of the sphere
     * @param radiusSquared the square radius of the sphere
     * @return <code>true</code> iff this AABB and the sphere intersect; <code>false</code> otherwise
     */
    public boolean intersectsSphere(float centerX, float centerY, float centerZ, float radiusSquared) {
        return Intersectionf.testAabSphere(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, centerX, centerY, centerZ, radiusSquared);
    }

    /**
     * Test whether this AABB intersects the given sphere.
     * <p>
     * Reference: <a href="http://stackoverflow.com/questions/4578967/cube-sphere-intersection-test#answer-4579069">http://stackoverflow.com</a>
     *
     * @param sphere the sphere
     * @return <code>true</code> iff this AABB and the sphere intersect; <code>false</code> otherwise
     */
    public boolean intersectsSphere(Spheref sphere) {
        return Intersectionf.testAabSphere(aabb, sphere);
    }

    /**
     * Test whether the given ray with the origin <code>(originX, originY, originZ)</code> and direction <code>(dirX, dirY, dirZ)</code>
     * intersects this AABB.
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
        return Intersectionf.testRayAab(originX, originY, originZ, dirX, dirY, dirZ, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
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
        return Intersectionf.testRayAab(ray, aabb);
    }

    /**
     * Determine whether the undirected line segment with the end points <code>(p0X, p0Y, p0Z)</code> and <code>(p1X, p1Y, p1Z)</code>
     * intersects this AABB, and return the values of the parameter <i>t</i> in the ray equation
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
     * @param result
     *              a vector which will hold the resulting values of the parameter
     *              <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     *              iff the line segment intersects this AABB
     * @return {@link Intersectionf#INSIDE} if the line segment lies completely inside of this AABB; or
     *         {@link Intersectionf#OUTSIDE} if the line segment lies completely outside of this AABB; or
     *         {@link Intersectionf#ONE_INTERSECTION} if one of the end points of the line segment lies inside of this AABB; or
     *         {@link Intersectionf#TWO_INTERSECTION} if the line segment intersects two sides of this AABB or lies on an edge or a side of this AABB
     */
    public int intersectLineSegment(float p0X, float p0Y, float p0Z, float p1X, float p1Y, float p1Z, Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(p0X, p0Y, p0Z, p1X, p1Y, p1Z, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, result);
    }

    /**
     * Determine whether the given undirected line segment intersects this AABB, and return the values of the parameter <i>t</i> in the ray equation
     * <i>p(t) = origin + p0 * (p1 - p0)</i> of the near and far point of intersection.
     * <p>
     * This method returns <code>true</code> for a line segment whose either end point lies inside this AABB.
     * <p>
     * Reference: <a href="https://dl.acm.org/citation.cfm?id=1198748">An Efficient and Robust Ray–Box Intersection</a>
     *
     * @param lineSegment the line segment
     * @param result
     *              a vector which will hold the resulting values of the parameter
     *              <i>t</i> in the ray equation <i>p(t) = p0 + t * (p1 - p0)</i> of the near and far point of intersection
     *              iff the line segment intersects this AABB
     * @return {@link Intersectionf#INSIDE} if the line segment lies completely inside of this AABB; or
     *         {@link Intersectionf#OUTSIDE} if the line segment lies completely outside of this AABB; or
     *         {@link Intersectionf#ONE_INTERSECTION} if one of the end points of the line segment lies inside of this AABB; or
     *         {@link Intersectionf#TWO_INTERSECTION} if the line segment intersects two sides of this AABB or lies on an edge or a side of this AABB
     */
    public int intersectLineSegment(LineSegmentf lineSegment, Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(lineSegment, aabb, result);
    }

    /**
     * Check whether <code>this</code> BlockRegion represents a valid BlockRegion.
     *
     * @return <code>true</code> iff this BlockRegion is valid; <code>false</code> otherwise
     */
    public boolean isValid() {
        return aabb.isValid();
    }

    /**
     * calculate the BlockRegion that is intersected between another region
     * @param other the other BlockRegion
     * @param dest holds the result
     * @return dest
     */
    public BlockRegion intersection(BlockRegion other, BlockRegion dest) {
        this.aabb.intersection(other.aabb, dest.aabb);
        return dest;
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extent extents to grow each face
     * @param dest holds the result
     * @return dest
     */
    public BlockRegion addExtents(int extent, BlockRegion dest) {
        return addExtents(extent, extent, extent, dest);
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param extentZ the z coordinate to grow the extents
     * @return this
     */
    public BlockRegion addExtents(int extentX, int extentY, int extentZ) {
        return addExtents(extentX, extentY, extentZ, this);
    }

    /**
     * Adds extend for each face of a BlockRegion
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param extentZ the z coordinate to grow the extents
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion addExtents(int extentX, int extentY, int extentZ, BlockRegion dest) {
        dest.aabb.minX = this.aabb.minX - extentX;
        dest.aabb.minY = this.aabb.minY - extentY;
        dest.aabb.minZ = this.aabb.minZ - extentZ;

        dest.aabb.maxX = this.aabb.maxX + extentX;
        dest.aabb.maxY = this.aabb.maxY + extentY;
        dest.aabb.maxZ = this.aabb.maxZ + extentZ;
        return dest;
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param extentZ the z coordinate to grow the extents
     * @param dest will hold the result
     * @return dest
     */
    public BlockRegion addExtents(float extentX, float extentY, float extentZ, BlockRegion dest) {
        dest.aabb.minX = Math.roundUsing(this.aabb.minX - extentX, RoundingMode.FLOOR);
        dest.aabb.minY = Math.roundUsing(this.aabb.minY - extentY, RoundingMode.FLOOR);
        dest.aabb.minZ = Math.roundUsing(this.aabb.minZ - extentZ, RoundingMode.FLOOR);

        dest.aabb.maxX = Math.roundUsing(this.aabb.maxX + extentX, RoundingMode.CEILING);
        dest.aabb.maxY = Math.roundUsing(this.aabb.maxY + extentY, RoundingMode.CEILING);
        dest.aabb.maxZ = Math.roundUsing(this.aabb.maxZ + extentZ, RoundingMode.CEILING);
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockRegion region = (BlockRegion) o;
        return aabb.equals(region.aabb);
    }

    @Override
    public int hashCode() {
        return aabb.hashCode();
    }

    @Override
    public String toString() {
        return "(" + this.aabb.minX + " " + this.aabb.minY + " " + this.aabb.minZ + ") < " +
            "(" + (this.aabb.maxX - 1) + " " + (this.aabb.maxY - 1) + " " + (this.aabb.maxZ - 1) + ")";
    }
}
