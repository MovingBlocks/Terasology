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

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.BaseVector3i;

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

    public Region3i(Region3i source) {
        this.minX = source.minX;
        this.minY = source.minY;
        this.minZ = source.minZ;

        this.maxX = source.maxX;
        this.maxY = source.maxY;
        this.maxZ = source.maxZ;
    }

    public Region3i(Vector3ic min, Vector3ic max){
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

    public boolean isValid() {
        return (minX < maxX) && (minY < maxY) && (minZ < maxZ);
    }

    public boolean isEmpty() {
        return (maxX - minX) == 0 && (maxY - minY) == 0 && (maxZ - minZ) == 0;
    }

    /**
     * An empty Region with size (0,0,0).
     * @return An empty Region3i
     */
    public static Region3i empty() {
        return new Region3i(0,0,0,0,0,0);
    }

    /**
     * @param min the min point of the region
     * @param size the size of the region
     * @return a new region base on the min point and region size, empty if the size is negative
     */
    public static Region3i createFromMinAndSize(BaseVector3i min, BaseVector3i size) {
        if (size.x() <= 0 || size.y() <= 0 || size.z() <= 0) {
            return empty();
        }
        return new Region3i(min, size);
    }

    /**
     * Create a region with center point and x,y,z coordinate extents size
     * @param center the center point of region
     * @param extents the extents size of each side of region
     * @return a new region base on the center point and extents size
     */
    public static Region3i createFromCenterExtents(BaseVector3f center, BaseVector3f extents) {
        org.terasology.math.geom.Vector3f min = new org.terasology.math.geom.Vector3f(center.x() - extents.x(), center.y() - extents.y(), center.z() - extents.z());
        org.terasology.math.geom.Vector3f max = new org.terasology.math.geom.Vector3f(center.x() + extents.x(), center.y() + extents.y(), center.z() + extents.z());
        max.x = max.x - Math.ulp(max.x);
        max.y = max.y - Math.ulp(max.y);
        max.z = max.z - Math.ulp(max.z);
        return createFromMinMax(new org.terasology.math.geom.Vector3i(min), new org.terasology.math.geom.Vector3i(max));
    }

    /**
     * Create a region with center point and x,y,z coordinate extents size
     * @param center the center point of region
     * @param extents the extents size of each side of region
     * @return a new region base on the center point and extents size
     */
    public static Region3i createFromCenterExtents(BaseVector3i center, BaseVector3i extents) {
        org.terasology.math.geom.Vector3i min = new org.terasology.math.geom.Vector3i(center.x() - extents.x(), center.y() - extents.y(), center.z() - extents.z());
        org.terasology.math.geom.Vector3i max = new org.terasology.math.geom.Vector3i(center.x() + extents.x(), center.y() + extents.y(), center.z() + extents.z());
        return createFromMinMax(min, max);
    }

    /**
     * Create a region with center point and extents size
     * @param center the center point of region
     * @param extent the extents size of region
     * @return a new region base on the center point and extents size
     */
    public static Region3i createFromCenterExtents(BaseVector3i center, int extent) {
        org.terasology.math.geom.Vector3i min = new org.terasology.math.geom.Vector3i(center.x() - extent, center.y() - extent, center.z() - extent);
        org.terasology.math.geom.Vector3i max = new org.terasology.math.geom.Vector3i(center.x() + extent, center.y() + extent, center.z() + extent);
        return createFromMinMax(min, max);
    }

    /**
     * Create a region by two point
     * @param a vertex a
     * @param b the diagonal vertex of a
     * @return a new region base on vertex a and b
     */
    public static Region3i createBounded(BaseVector3i a, BaseVector3i b) {
        org.terasology.math.geom.Vector3i min = new org.terasology.math.geom.Vector3i(a);
        min.min(b);
        org.terasology.math.geom.Vector3i max = new org.terasology.math.geom.Vector3i(a);
        max.max(b);
        return createFromMinMax(min, max);
    }

    /**
     * Create a region by two point
     * @param min the min point of the region
     * @param max the max point of the region
     * @return a new region base on min and max point
     */
    public static Region3i createFromMinMax(BaseVector3i min, BaseVector3i max) {
        org.terasology.math.geom.Vector3i size = new org.terasology.math.geom.Vector3i(max.x() - min.x() + 1, max.y() - min.y() + 1, max.z() - min.z() + 1);
        if (size.x <= 0 || size.y <= 0 || size.z <= 0) {
            return empty();
        }
        return new Region3i(min, size);
    }

    public static Region3i createEncompassing(Region3i a, Region3i b) {
        if (a.isValid()) {
            return b;
        }
        if (b.isValid()) {
            return a;
        }

        return new Region3i(a).union(b.minX, b.minY, b.minZ).union(b.maxX, b.maxY, b.maxZ);
//
//        if (a.isEmpty()) {
//            return b;
//        }
//        if (b.isEmpty()) {
//            return a;
//        }
//        Vector3i min = a.min();
//        min.min(b.min());
//        Vector3i max = a.max();
//        max.max(b.max());
//        return createFromMinMax(min, max);
    }


    /**
     * @return The smallest vector in the region
     */
    public org.terasology.math.geom.Vector3i min() {
        return new org.terasology.math.geom.Vector3i(minX, minY, minZ);
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

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

    public int sizeX() {
        return maxX - minX;
    }

    public int sizeY() {
        return maxY - minY;
    }

    public int sizeZ() {
        return maxZ - minZ;
    }

    /**
     * @return The largest vector in the region
     */
    public org.terasology.math.geom.Vector3i max() {
        return new org.terasology.math.geom.Vector3i(maxX(),maxY(),maxZ());
    }

    public int maxX() {
        return maxX - 1;
    }

    public int maxY() {
        return maxY - 1;
    }

    public int maxZ() {
        return maxZ - 1;
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



    public Region3i inflate(int x, int y, int z, Region3i dest) {
        dest.minX = (this.minX - x);
        dest.minY = (this.minY - y);
        dest.minZ = (this.minZ - z);
        dest.maxX = (this.minX + x);
        dest.maxY = (this.minY + y);
        dest.maxZ = (this.minZ + z);
        return this;
    }

    public Region3i inflate(Vector3ic pos, Region3i dest) {
        return inflate(pos.x(), pos.y(), pos.z(), dest);
    }

    public Region3i inflate(Vector3ic pos) {
        return inflate(pos.x(), pos.y(), pos.z(), this);
    }

    public Region3i inflate(int amount, Region3i dest) {
        return inflate(amount, amount, amount, dest);
    }

    public Region3i inflate(int amount) {
        return inflate(amount, amount, amount, this);
    }

    public Region3i union(Vector3ic pos, Region3i dest) {
        return union(pos.x(), pos.y(), pos.z(), dest);
    }

    public Region3i union(Vector3ic pos) {
        return union(pos.x(), pos.y(), pos.z(), this);
    }

    public Region3i union(int x, int y, int z, Region3i dest) {
        dest.minX = this.minX < x ? this.minX : x;
        dest.minY = this.minY < y ? this.minY : y;
        dest.minZ = this.minZ < z ? this.minZ : z;
        dest.maxX = this.maxX > x ? this.maxX : x;
        dest.maxY = this.maxY > y ? this.maxY : y;
        dest.maxZ = this.maxZ > z ? this.maxZ : z;
        return dest;
    }

    public Region3i union(int x, int y, int z) {
        return union(x, y, z, this);
    }


    public boolean testPoint(Vector3ic p) {
        return testPoint(p.x(), p.y(), p.z());
    }
    public boolean testPoint(int x, int y, int z){
        return (x >= minX) && (y >= minY) && (z >= minZ) && (x < maxX) && (y < maxY) && (z < maxZ);
    }


    /**
     * Creates a new region that is the same as this region but expanded in all directions by the given amount
     *
     * @param amount
     * @return A new region
     */
    public Region3i expand(int amount) {
        org.terasology.math.geom.Vector3i expandedMin = min();
        expandedMin.sub(amount, amount, amount);
        org.terasology.math.geom.Vector3i expandedMax = max();
        expandedMax.add(amount, amount, amount);
        return createFromMinMax(expandedMin, expandedMax);
    }

    public Region3i expand(BaseVector3i amount) {
        org.terasology.math.geom.Vector3i expandedMin = min();
        expandedMin.sub(amount);
        org.terasology.math.geom.Vector3i expandedMax = max();
        expandedMax.add(amount);
        return createFromMinMax(expandedMin, expandedMax);
    }

    public Region3i expandToContain(BaseVector3i adjPos) {
        org.terasology.math.geom.Vector3i expandedMin = min();
        expandedMin.min(adjPos);
        org.terasology.math.geom.Vector3i expandedMax = max();
        expandedMax.max(adjPos);
        return createFromMinMax(expandedMin, expandedMax);
    }

    /**
     * @return The position at the center of the region
     */
    public org.terasology.math.geom.Vector3f center() {
        org.terasology.math.geom.Vector3f result = new org.terasology.math.geom.Vector3f(minX, minY, minZ);
        org.terasology.math.geom.Vector3f halfSize = new org.terasology.math.geom.Vector3f(sizeX(), sizeY(), sizeZ());
        halfSize.scale(0.5f);
        result.add(halfSize);
        return result;
    }



    /**
     * @param offset
     * @return A copy of the region offset by the given value
     */
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

    public boolean encompasses(int x, int y, int z) {
        return (x >= minX) && (y >= minY) && (z >= minZ) && (x < maxX) && (y < maxY) && (z < maxZ);
//        return (x >= min.x) && (y >= min.y) && (z >= min.z) && (x < min.x + size.x) && (y < min.y + size.y) && (z < min.z + size.z);
    }

    /**
     * @param pos
     * @return The nearest position within the region to the given pos.
     */
    public org.terasology.math.geom.Vector3i getNearestPointTo(BaseVector3i pos) {
        org.terasology.math.geom.Vector3i result = new org.terasology.math.geom.Vector3i(pos);
        result.min(max());
        result.max(new org.terasology.math.geom.Vector3i(minX, minY, minZ));
        return result;
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        return new Iterator<Vector3ic>() {
            private final Vector3i pos = new Vector3i(minX,minY,minZ);
            private final int[] p = {minX,minY,minZ};
            @Override
            public boolean hasNext() {
                return p[0] < maxX;
            }

            @Override
            public Vector3ic next() {
                pos.set(p[0],p[1],p[2]);
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
        Iterator<Vector3ic> it =  new Iterator<Vector3ic>() {
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
                if(next != null) current.set(next);
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
