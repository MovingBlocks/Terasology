/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.math;

import org.joml.Math;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.StaticSound;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3f;

import java.util.Iterator;

/**
 * Describes an axis-aligned bounded space in 3D integer.
 *
 */
public final class Region3i implements Iterable<Vector3ic> {

    /**
     * @deprecated As of 24 sep 2018, because it is error prone.
     * Everyone can change this instance and destroy the invariant properties.
     * Some methods used to return this instance silently on special cases.
     */
    @Deprecated
    public static final Region3i EMPTY = Region3i.empty();

    /**
     * The x coordinate of the minimum corner.
     */
    public int minX = Integer.MAX_VALUE;
    /**
     * The y coordinate of the minimum corner.
     */
    public int minY = Integer.MAX_VALUE;
    /**
     * The z coordinate of the minimum corner.
     */
    public int minZ = Integer.MAX_VALUE;
    /**
     * The x coordinate of the maximum corner.
     */
    public int maxX = Integer.MIN_VALUE;
    /**
     * The y coordinate of the maximum corner.
     */
    public int maxY = Integer.MIN_VALUE;
    /**
     * The z coordinate of the maximum corner.
     */
    public int maxZ = Integer.MIN_VALUE;


    @Deprecated
    private Region3i(BaseVector3i min, BaseVector3i size) {
        this.minX = min.x();
        this.minY = min.y();
        this.minZ = min.z();

        this.maxX = min.x() + size.x();
        this.maxY = min.y() + size.y();
        this.maxZ = min.z() + size.z();
    }

    public Region3i() {
    }

    public Region3i(Region3i source) {
        this.minX = source.minX;
        this.minY = source.minY;
        this.minZ = source.minZ;

        this.maxX = source.maxX;
        this.maxY = source.maxY;
        this.maxZ = source.maxZ;
    }

    public Region3i(Vector3ic min, Vector3ic max) {
        this.minX = min.x();
        this.minY = min.y();
        this.minZ = min.z();

        this.maxX = max.x();
        this.maxY = max.y();
        this.maxZ = max.z();
    }

    public Region3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * Check whether <code>this</code> rectangle represents a valid rectangle.
     *
     * @return <code>true</code> iff this rectangle is valid; <code>false</code> otherwise
     */
    public boolean isValid() {
        return (minX < maxX) && (minY < maxY) && (minZ < maxZ);
    }

    /**
     *
     * @return <code>true</code> iff this rectangle has zero width, height and length; <code>false</code> otherwise
     * @deprecated use {@link #isValid()} to check if the region is a valid shape.
     */
    @Deprecated
    public boolean isEmpty() {
        return (maxX - minX) == 0 && (maxY - minY) == 0 && (maxZ - minZ) == 0;
    }


    /**
     * Set the minimum corner.
     *
     * @param min the minimum coordinate
     * @return this
     */
    public Region3i setMin(Vector3ic min) {
        return this.setMin(min.x(), min.y(), min.z());
    }

    /**
     * set the minimum corner.
     *
     * @param x the x coordinate of the minimum corner
     * @param y the y coordinate of the minimum corner
     * @param z the z coordinate of the minimum corner
     * @return this
     */
    public Region3i setMin(int x, int y, int z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        return this;
    }

    /**
     * set the maximum corner.
     *
     * @param max the maximum coordinate
     * @return this
     */
    public Region3i setMax(Vector3ic max) {
        return this.setMax(max.x(), max.y(), max.z());
    }

    public Region3i setMax(int x, int y, int z) {
        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;
        return this;
    }

    /**
     * Ensure that the minimum coordinates are strictly less than or equal to the maximum coordinates by swapping
     * them if necessary.
     *
     * @return this
     */
    public Region3i correctBounds() {
        int tmp;
        if (this.minX > this.maxX) {
            tmp = this.minX;
            this.minX = this.maxX;
            this.maxX = tmp;
        }
        if (this.minY > this.maxY) {
            tmp = this.minY;
            this.minY = this.maxY;
            this.maxY = tmp;
        }
        if (this.minZ > this.maxZ) {
            tmp = this.minZ;
            this.minZ = this.maxZ;
            this.maxZ = tmp;
        }
        return this;
    }

    /**
     * An empty Region with size (0,0,0).
     *
     * @return An empty Region3i
     */
    @Deprecated
    public static Region3i empty() {
        return new Region3i(0, 0, 0, 0, 0, 0);
    }

    /**
     * @param min  the min point of the region
     * @param size the size of the region
     * @return a new region base on the min point and region size, empty if the size is negative
     */
    @Deprecated
    public static Region3i createFromMinAndSize(BaseVector3i min, BaseVector3i size) {
        if (size.x() <= 0 || size.y() <= 0 || size.z() <= 0) {
            return empty();
        }
        return new Region3i(min, size);
    }

    /**
     * Create a region with center point and x,y,z coordinate extents size
     *
     * @param center  the center point of region
     * @param extents the extents size of each side of region
     * @return a new region base on the center point and extents size
     */
    @Deprecated
    public static Region3i createFromCenterExtents(BaseVector3f center, BaseVector3f extents) {
        org.terasology.math.geom.Vector3f min = new org.terasology.math.geom.Vector3f(center.x() - extents.x(), center.y() - extents.y(), center.z() - extents.z());
        org.terasology.math.geom.Vector3f max = new org.terasology.math.geom.Vector3f(center.x() + extents.x(), center.y() + extents.y(), center.z() + extents.z());
        max.x = max.x - java.lang.Math.ulp(max.x);
        max.y = max.y - java.lang.Math.ulp(max.y);
        max.z = max.z - java.lang.Math.ulp(max.z);
        return createFromMinMax(new org.terasology.math.geom.Vector3i(min), new org.terasology.math.geom.Vector3i(max));
    }

    /**
     * Create a region with center point and x,y,z coordinate extents size
     *
     * @param center  the center point of region
     * @param extents the extents size of each side of region
     * @return a new region base on the center point and extents size
     */
    @Deprecated
    public static Region3i createFromCenterExtents(BaseVector3i center, BaseVector3i extents) {
        org.terasology.math.geom.Vector3i min = new org.terasology.math.geom.Vector3i(center.x() - extents.x(), center.y() - extents.y(), center.z() - extents.z());
        org.terasology.math.geom.Vector3i max = new org.terasology.math.geom.Vector3i(center.x() + extents.x(), center.y() + extents.y(), center.z() + extents.z());
        return createFromMinMax(min, max);
    }

    /**
     * Create a region with center point and extents size
     *
     * @param center the center point of region
     * @param extent the extents size of region
     * @return a new region base on the center point and extents size
     */
    @Deprecated
    public static Region3i createFromCenterExtents(BaseVector3i center, int extent) {
        org.terasology.math.geom.Vector3i min = new org.terasology.math.geom.Vector3i(center.x() - extent, center.y() - extent, center.z() - extent);
        org.terasology.math.geom.Vector3i max = new org.terasology.math.geom.Vector3i(center.x() + extent, center.y() + extent, center.z() + extent);
        return createFromMinMax(min, max);
    }

    /**
     * Create a region by two point
     *
     * @param a vertex a
     * @param b the diagonal vertex of a
     * @return a new region base on vertex a and b
     */
    @Deprecated
    public static Region3i createBounded(BaseVector3i a, BaseVector3i b) {
        org.terasology.math.geom.Vector3i min = new org.terasology.math.geom.Vector3i(a);
        min.min(b);
        org.terasology.math.geom.Vector3i max = new org.terasology.math.geom.Vector3i(a);
        max.max(b);
        return createFromMinMax(min, max);
    }

    /**
     * Create a region by two point
     *
     * @param min the min point of the region
     * @param max the max point of the region
     * @return a new region base on min and max point
     */
    @Deprecated
    public static Region3i createFromMinMax(BaseVector3i min, BaseVector3i max) {
        org.terasology.math.geom.Vector3i size = new org.terasology.math.geom.Vector3i(max.x() - min.x() + 1, max.y() - min.y() + 1, max.z() - min.z() + 1);
        if (size.x <= 0 || size.y <= 0 || size.z <= 0) {
            return empty();
        }
        return new Region3i(min, size);
    }

    @Deprecated
    public static Region3i createEncompassing(Region3i a, Region3i b) {
        if (a.isValid()) {
            return b;
        }
        if (b.isValid()) {
            return a;
        }
        return new Region3i(a).union(b.minX, b.minY, b.minZ).union(b.maxX, b.maxY, b.maxZ);
    }


    /**
     * @return The smallest vector in the region
     */
    public org.terasology.math.geom.Vector3i min() {
        return new org.terasology.math.geom.Vector3i(minX, minY, minZ);
    }


    @Deprecated
    public int minX() {
        return minX;
    }


    @Deprecated
    public int minY() {
        return minY;
    }


    @Deprecated
    public int minZ() {
        return minZ;
    }

    /**
     * @return The size of the region
     * @deprecated
     */
    @Deprecated
    public org.terasology.math.geom.Vector3i size() {
        return new org.terasology.math.geom.Vector3i(sizeX(), sizeY(), sizeZ());
    }

    @Deprecated
    public int sizeX() {
        return maxX - minX;
    }

    @Deprecated
    public int sizeY() {
        return maxY - minY;
    }

    @Deprecated
    public int sizeZ() {
        return maxZ - minZ;
    }

    /**
     * @return The largest vector in the region
     */
    @Deprecated
    public org.terasology.math.geom.Vector3i max() {
        return new org.terasology.math.geom.Vector3i(maxX(), maxY(), maxZ());
    }

    @Deprecated
    public int maxX() {
        return maxX - 1;
    }

    @Deprecated
    public int maxY() {
        return maxY - 1;
    }

    @Deprecated
    public int maxZ() {
        return maxZ - 1;
    }

    public int getMin(int component) throws IllegalArgumentException {
        switch (component) {
            case 0:
                return minX;
            case 1:
                return minY;
            case 2:
                return minZ;
            default:
                throw new IllegalArgumentException();
        }
    }


    /**
     * get component for maximum coordinate
     * @param component
     * @return
     * @throws IllegalArgumentException
     */
    public int getMax(int component) throws IllegalArgumentException {
        switch (component) {
            case 0:
                return maxX;
            case 1:
                return maxY;
            case 2:
                return maxZ;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * @param other
     * @return The region that is encompassed by both this and other. If they
     * do not overlap then the empty region is returned
     */
    public Region3i intersect(Region3i other) {
        org.terasology.math.geom.Vector3i intersectMin = min();
        intersectMin.max(other.min());
        org.terasology.math.geom.Vector3i intersectMax = max();
        intersectMax.min(other.max());

        return createFromMinMax(intersectMin, intersectMax);
    }

    /**
     * Compute the rectangle of intersection between <code>this</code> and the given rectangle and
     * store the result in <code>dest</code>.
     * <p>
     * If the two rectangles do not intersect, then {@link Float#NaN} is stored in each component
     * of <code>dest</code>.
     *
     * @param other region to intersect by
     * @param dest  will hold the result
     * @return dest
     */
    public Region3i intersection(Region3i other, Region3i dest) {
        dest.minX = Math.max(minX, other.minX);
        dest.minY = Math.max(minY, other.minY);
        dest.minZ = Math.max(minZ, other.minZ);

        dest.maxX = Math.min(maxX, other.maxX);
        dest.maxY = Math.min(maxY, other.maxY);
        dest.maxZ = Math.min(maxZ, other.maxZ);
        return dest;
    }


    private Region3i validate() {
        if (!isValid()) {
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            minZ = Integer.MAX_VALUE;

            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;
            maxZ = Integer.MIN_VALUE;

        }
        return this;
    }


    /**
     * inflate region from each surface by (+x, +y, +z)
     *
     * @param x    inflate by +x
     * @param y    inflate by +y
     * @param z    inflate by +z
     * @param dest will hold the result
     * @return dest
     */
    public Region3i inflate(int x, int y, int z, Region3i dest) {
        dest.minX = (this.minX - x);
        dest.minY = (this.minY - y);
        dest.minZ = (this.minZ - z);
        dest.maxX = (this.minX + x);
        dest.maxY = (this.minY + y);
        dest.maxZ = (this.minZ + z);
        return this;
    }

    /**
     * inflate region from each surface by (+x, +y, +z) from pos
     *
     * @param pos  components used for inflate
     * @param dest will hold the result
     * @return dest
     */
    public Region3i inflate(Vector3ic pos, Region3i dest) {
        return inflate(pos.x(), pos.y(), pos.z(), dest);
    }

    public Region3i inflate(Vector3ic pos) {
        return inflate(pos.x(), pos.y(), pos.z(), this);
    }

    /**
     * inflate region by the amount and write to dest
     *
     * @param amount amount to inflate region
     * @param dest
     * @return
     */
    public Region3i inflate(int amount, Region3i dest) {
        return inflate(amount, amount, amount, dest);
    }

    public Region3i inflate(int amount) {
        return inflate(amount, amount, amount, this);
    }

    public Region3i union(Vector3ic pos, Region3i dest) {
        return union(pos.x(), pos.y(), pos.z(), dest);
    }

    /**
     * increase <code>this</code> to contain <code>p</code>
     *
     * @param p point to union region
     * @return dest
     */
    public Region3i union(Vector3ic p) {
        return union(p.x(), p.y(), p.z(), this);
    }

    /**
     * increase <code>this</code> to contain (x, y, z)
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @param dest region to write to
     * @return dest
     */
    public Region3i union(int x, int y, int z, Region3i dest) {
        dest.minX = this.minX < x ? this.minX : x;
        dest.minY = this.minY < y ? this.minY : y;
        dest.minZ = this.minZ < z ? this.minZ : z;
        dest.maxX = this.maxX > x ? this.maxX : x;
        dest.maxY = this.maxY > y ? this.maxY : y;
        dest.maxZ = this.maxZ > z ? this.maxZ : z;
        return dest;
    }

    /**
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return this
     */
    public Region3i union(int x, int y, int z) {
        return union(x, y, z, this);
    }

    /**
     * test point for Region3i with a default offset of [.5,.5,.5] from p.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return this
     */
    public boolean testPoint(int x, int y, int z) {
        return testPoint(x, y, z, .5f, .5f, .5f);
    }

    /**
     * test point for Region3i with a default offset of [.5,.5,.5] from p.
     *
     * @param p point to test
     * @return <code>true</code> iff the given point lies inside this Region; <code>false</code> otherwise
     */
    public boolean testPoint(Vector3ic p) {
        return testPoint(p.x(), p.y(), p.z(), .5f, .5f, .5f);
    }

    /**
     * Test a point with a floating point offset
     *
     * @param p point to test {@link Region3i}
     * @param o offset for point <code>p</code>
     * @return <code>true</code> iff the given point lies inside this Region; <code>false</code> otherwise
     */
    public boolean testPoint(Vector3ic p, Vector3fc o) {
        return testPoint(p.x(), p.y(), p.z(), o.x(), o.y(), o.z());
    }


    /**
     * Test a point with a floating point offset
     *
     * @param x the x coordinate of the point, add <code>offsetX</code>
     * @param y the y coordinate of the point, add <code>offsetY</code>
     * @param z the z coordinate of the point, add <code>offsetZ</code>
     * @param offsetX offset x coordinate
     * @param offsetY offset y coordinate
     * @param offsetZ offset z coordinate
     * @return <code>true</code> iff the given point lies inside this Region; <code>false</code> otherwise
     */
    public boolean testPoint(int x, int y, int z, float offsetX, float offsetY, float offsetZ) {
        return testPoint(x + offsetX, y + offsetY, z + offsetZ);
    }

    /**
     * Test whether the point <code>(x,y,z)</code> lies inside this Region3i p >= min and p <= max
     *
     * @param x the x coordinate to test the region
     * @param y the y coordinate to test the region
     * @param z the z coordinate to test the region
     * @return if the point is contained in the region
     */
    public boolean testPoint(float x, float y, float z) {
        return (x >= minX) && (y >= minY) && (z >= minZ) && (x <= maxX) && (y <= maxY) && (z <= maxZ);
    }


    /**
     * Creates a new region that is the same as this region but expanded in all directions by the given amount
     *
     * @param amount
     * @return A new region
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #inflate(int)} or {@link #inflate(int, Region3i)}
     *             Note: {@link #inflate(int)} will modify <code>this</code>
     */
    @Deprecated
    public Region3i expand(int amount) {
        org.terasology.math.geom.Vector3i expandedMin = min();
        expandedMin.sub(amount, amount, amount);
        org.terasology.math.geom.Vector3i expandedMax = max();
        expandedMax.add(amount, amount, amount);
        return createFromMinMax(expandedMin, expandedMax);
    }

    /**
     *
     * @param amount
     * @return A new region
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #inflate(int, Region3i)}
     */
    @Deprecated
    public Region3i expand(BaseVector3i amount) {
        org.terasology.math.geom.Vector3i expandedMin = min();
        expandedMin.sub(amount);
        org.terasology.math.geom.Vector3i expandedMax = max();
        expandedMax.add(amount);
        return createFromMinMax(expandedMin, expandedMax);
    }

    /**
     * @param adjPos
     * @return
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #inflate(int, int, int, Region3i)}
     */
    @Deprecated
    public Region3i expandToContain(BaseVector3i adjPos) {
        org.terasology.math.geom.Vector3i expandedMin = min();
        expandedMin.min(adjPos);
        org.terasology.math.geom.Vector3i expandedMax = max();
        expandedMax.max(adjPos);
        return createFromMinMax(expandedMin, expandedMax);
    }

    /**
     * @return The position at the center of the region
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getCenter(Vector3f)}.
     */
    @Deprecated
    public org.terasology.math.geom.Vector3f center() {
        org.terasology.math.geom.Vector3f result = new org.terasology.math.geom.Vector3f(minX, minY, minZ);
        org.terasology.math.geom.Vector3f halfSize = new org.terasology.math.geom.Vector3f(sizeX(), sizeY(), sizeZ());
        halfSize.scale(0.5f);
        result.add(halfSize);
        return result;
    }

    /**
     * get center of {@link Region3i} and write to <code>dest</code>
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3f getCenter(Vector3f dest) {
        dest.x = (maxX - minX) * .5f;
        dest.y = (maxY - minY) * .5f;
        dest.z = (maxZ - minZ) * .5f;
        return dest;
    }


    /**
     * Translate <code>this</code> by the vector <code>(x, y, z)</code> and store the result in <code>dest</code>.
     *
     * @param x    the x coordinate to translate by
     * @param y    the y coordinate to translate by
     * @param dest will hold the result
     * @return dest
     */
    public Region3i translate(int x, int y, int z, Region3i dest) {
        dest.minX = minX + x;
        dest.minY = minY + y;
        dest.minZ = minZ + z;
        dest.maxX = maxX + x;
        dest.maxY = maxY + y;
        dest.maxZ = maxZ + z;
        return dest;
    }

    /**
     * Translate <code>this</code> by the vector <code>(x, y, z)</code> and store the result in <code>dest</code>.
     *
     * @param x the x coordinate to translate by
     * @param y the y coordinate to translate by
     * @param z the z coordinate to translate by
     * @return this
     */
    public Region3i translate(int x, int y, int z) {
        return this.translate(x, y, z, this);
    }


    /**
     * @param offset
     * @return A copy of the region offset by the given value
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #translate(int, int, int)}.
     */
    @Deprecated
    public Region3i move(BaseVector3i offset) {
        org.terasology.math.geom.Vector3i newMin = min();
        newMin.add(offset);
        return Region3i.createFromMinAndSize(newMin,
            new org.terasology.math.geom.Vector3i(sizeX(), sizeY(), sizeZ()));
    }

    /**
     * @param pos
     * @return Whether this region includes pos
     */
    public boolean encompasses(BaseVector3i pos) {
        return encompasses(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     * @deprecated This method is scheduled for removal in an upcoming version.
     *              Use the JOML implementation instead: {@link #testPoint(int, int, int)}.
     */
    @Deprecated
    public boolean encompasses(int x, int y, int z) {
        return (x >= minX) && (y >= minY) && (z >= minZ) && (x < maxX) && (y < maxY) && (z < maxZ);
    }

    /**
     * @param pos
     * @return The nearest position within the region to the given pos.
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getNearestPointToCorner(Vector3ic, Vector3i)}.
     */
    @Deprecated
    public org.terasology.math.geom.Vector3i getNearestPointTo(BaseVector3i pos) {
        org.terasology.math.geom.Vector3i result = new org.terasology.math.geom.Vector3i(pos);
        result.min(max());
        result.max(new org.terasology.math.geom.Vector3i(minX, minY, minZ));
        return result;
    }

    /**
     * @param p point to test
     * @param dest will hold the result
     * @return will hold the result
     */
    public Vector3i getNearestPointToCorner(Vector3ic p, Vector3i dest) {
        dest.x = Math.max(Math.min(maxX, p.x()), minX);
        dest.y = Math.max(Math.min(maxY, p.y()), minY);
        dest.z = Math.max(Math.min(maxZ, p.z()), minZ);
        return dest;
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        return new Iterator<Vector3ic>() {
            private final Vector3i pos = new Vector3i(minX, minY, minZ);
            private final int[] p = {minX, minY, minZ};

            @Override
            public boolean hasNext() {
                return p[0] < maxX;
            }

            @Override
            public Vector3ic next() {
                pos.set(p[0], p[1], p[2]);
                p[2]++;
                if (p[2] >= maxZ) {
                    p[2] = minZ;
                    p[1]++;
                    if (p[1] >= maxY) {
                        p[1] = minY;
                        p[0]++;
                    }
                }
                return pos;
            }
        };
    }

    /**
     * @param other
     * @return An iterator over the positions in this region that aren't in other
     */
    public Iterator<Vector3ic> subtract(Region3i other) {
        Iterator<Vector3ic> it = new Iterator<Vector3ic>() {
            private final Region3i region = new Region3i(other);
            private Vector3i current = new Vector3i();
            private Vector3ic next = null;
            private Iterator<Vector3ic> innerIterator = iterator();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Vector3ic next() {
                if (next != null) current.set(next);
                while (innerIterator.hasNext()) {
                    next = innerIterator.next();
                    if (!region.testPoint(next)) {
                        return current;
                    }
                }
                next = null;
                return current;
            }
        };
        it.next();
        return it;
    }

    @Override
    public String toString() {
        return "(Min: [" + minX + "," + minY + "," + minZ + "], Max: [" + maxX + "," + maxY + "," + maxZ + "])";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Region3i) {
            Region3i other = (Region3i) obj;
            return other.minX == this.minX && other.minY == this.minY && other.minZ == this.minZ
                && other.maxX == this.maxX && other.maxY == this.maxY && other.maxZ == this.maxZ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + minX;
        hash += 37 * hash + minY;
        hash += 37 * hash + minZ;
        hash += 37 * hash + maxX;
        hash += 37 * hash + maxY;
        hash += 37 * hash + maxZ;
        return hash;
    }
}
