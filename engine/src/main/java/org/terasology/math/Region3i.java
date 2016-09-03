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

import java.util.Collection;
import java.util.Iterator;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

/**
 * Describes an axis-aligned bounded space in 3D integer.
 */
public final class Region3i implements Iterable<Vector3i> {
    public static final Region3i EMPTY = new Region3i();

    private final Vector3i min = new Vector3i();
    private final Vector3i size = new Vector3i();

    /**
     * Constructs an empty Region with size (0,0,0).
     */
    private Region3i() {
    }

    private Region3i(BaseVector3i min, BaseVector3i size) {
        this.min.set(min);
        this.size.set(size);
    }

    public static Region3i createFromMinAndSize(BaseVector3i min, BaseVector3i size) {
        if (size.x() <= 0 || size.y() <= 0 || size.z() <= 0) {
            return EMPTY;
        }
        return new Region3i(min, size);
    }

    public static Region3i createFromCenterExtents(BaseVector3f center, BaseVector3f extents) {
        Vector3f min = new Vector3f(center.x() - extents.x(), center.y() - extents.y(), center.z() - extents.z());
        Vector3f max = new Vector3f(center.x() + extents.x(), center.y() + extents.y(), center.z() + extents.z());
        max.x = max.x - Math.ulp(max.x);
        max.y = max.y - Math.ulp(max.y);
        max.z = max.z - Math.ulp(max.z);
        return createFromMinMax(new Vector3i(min), new Vector3i(max));
    }

    public static Region3i createFromCenterExtents(BaseVector3i center, BaseVector3i extents) {
        Vector3i min = new Vector3i(center.x() - extents.x(), center.y() - extents.y(), center.z() - extents.z());
        Vector3i max = new Vector3i(center.x() + extents.x(), center.y() + extents.y(), center.z() + extents.z());
        return createFromMinMax(min, max);
    }

    public static Region3i createFromCenterExtents(BaseVector3i center, int extent) {
        Vector3i min = new Vector3i(center.x() - extent, center.y() - extent, center.z() - extent);
        Vector3i max = new Vector3i(center.x() + extent, center.y() + extent, center.z() + extent);
        return createFromMinMax(min, max);
    }

    public static Region3i createBounded(BaseVector3i a, BaseVector3i b) {
        Vector3i min = new Vector3i(a);
        min.min(b);
        Vector3i max = new Vector3i(a);
        max.max(b);
        return createFromMinMax(min, max);
    }

    public static Region3i createFromMinMax(BaseVector3i min, BaseVector3i max) {
        Vector3i size = new Vector3i(max.x() - min.x() + 1, max.y() - min.y() + 1, max.z() - min.z() + 1);
        if (size.x <= 0 || size.y <= 0 || size.z <= 0) {
            return EMPTY;
        }
        return new Region3i(min, size);
    }

    public static Region3i createEncompassing(Region3i a, Region3i b) {
        if (a.isEmpty()) {
            return b;
        }
        if (b.isEmpty()) {
            return a;
        }
        Vector3i min = a.min();
        min.min(b.min());
        Vector3i max = a.max();
        max.max(b.max());
        return createFromMinMax(min, max);
    }

    /**
     * Creates a {@link Region3i} that describes the bounding box for the given positions.
     * <p>
     * The bounding box of a set of positions is the smallest region such that the region encompasses all those points.
     * The bounding box for the empty set is the empty region.
     *
     * @param positions a (possibly empty) list of positions
     * @return the smallest region encompassing all given positions.
     */
    public static Region3i createBoundingBox(Collection<Vector3i> positions) {
        if (positions.isEmpty()) {
            return Region3i.EMPTY;
        }
        Vector3i min = Vector3i.one().mul(Integer.MAX_VALUE);
        Vector3i max = Vector3i.one().mul(Integer.MIN_VALUE);
        positions.forEach(pos -> {
            min.min(pos);
            max.max(pos);
        });
        return createFromMinMax(min, max);
    }

    public boolean isEmpty() {
        return size.x + size.y + size().z == 0;
    }

    /**
     * @return The smallest vector in the region
     */
    public Vector3i min() {
        return new Vector3i(min);
    }

    public int minX() {
        return min.x;
    }

    public int minY() {
        return min.y;
    }

    public int minZ() {
        return min.z;
    }

    /**
     * @return The size of the region
     */
    public Vector3i size() {
        return new Vector3i(size);
    }


    public int sizeX() {
        return size.x;
    }

    public int sizeY() {
        return size.y;
    }

    public int sizeZ() {
        return size.z;
    }

    /**
     * @return The largest vector in the region
     */
    public Vector3i max() {
        Vector3i max = new Vector3i(min);
        max.add(size);
        max.sub(1, 1, 1);
        return max;
    }

    public int maxX() {
        return min.x + size.x - 1;
    }

    public int maxY() {
        return min.y + size.y - 1;
    }

    public int maxZ() {
        return min.z + size.z - 1;
    }

    /**
     * Creates a new {@link Region3i} for the region encompassed by both this and other.
     * If the two regions do not overlap the empty region is returned.
     *
     * @param other the region to intersect
     * @return the region that is encompassed by both this and other, the empty region if they do not overlap
     */
    public Region3i intersect(Region3i other) {
        Vector3i intersectMin = min();
        intersectMin.max(other.min());
        Vector3i intersectMax = max();
        intersectMax.min(other.max());

        return createFromMinMax(intersectMin, intersectMax);
    }

    /**
     * Returns an {@link Iterator} over the positions of this region that are not encompassed by other.
     *
     * @param other the region to subtract
     * @return an iterator over the positions in this region that aren't in other
     */
    public Iterator<Vector3i> subtract(Region3i other) {
        return new SubtractiveIterator(other);
    }

    /**
     * Creates a new region that is the same as this region but expanded in all directions by the given amount
     *
     * @param amount amount to expand in all directions
     * @return the original region expanded in all directions by amount
     */
    public Region3i expand(int amount) {
        Vector3i expandedMin = min();
        expandedMin.sub(amount, amount, amount);
        Vector3i expandedMax = max();
        expandedMax.add(amount, amount, amount);
        return createFromMinMax(expandedMin, expandedMax);
    }

    public Region3i expand(BaseVector3i amount) {
        Vector3i expandedMin = min();
        expandedMin.sub(amount);
        Vector3i expandedMax = max();
        expandedMax.add(amount);
        return createFromMinMax(expandedMin, expandedMax);
    }

    public Region3i expandToContain(BaseVector3i adjPos) {
        Vector3i expandedMin = min();
        expandedMin.min(adjPos);
        Vector3i expandedMax = max();
        expandedMax.max(adjPos);
        return createFromMinMax(expandedMin, expandedMax);
    }

    /**
     * @return The position at the center of the region
     */
    public Vector3f center() {
        Vector3f result = min.toVector3f();
        Vector3f halfSize = size.toVector3f();
        halfSize.scale(0.5f);
        result.add(halfSize);
        return result;
    }

    /**
     * Creates a new region from this moved by the given offset.
     *
     * @param offset the offset to move this region
     * @return a copy of this region that is offset by the given value
     */
    public Region3i move(BaseVector3i offset) {
        Vector3i newMin = min();
        newMin.add(offset);
        return Region3i.createFromMinAndSize(newMin, size);
    }

    /**
     * Checks whether this region encompasses the given position.
     *
     * @param pos the position to check
     * @return whether this region includes pos
     */
    public boolean encompasses(BaseVector3i pos) {
        return encompasses(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Checks whether this region encompasses the position given by (x, y, z).
     *
     * @param x position on the x-axis
     * @param y position on the y-axis
     * @param z position on the z-axis
     * @return whether this region includes position (x, y, z)
     */
    public boolean encompasses(int x, int y, int z) {
        return (x >= min.x) && (y >= min.y) && (z >= min.z) && (x < min.x + size.x) && (y < min.y + size.y) && (z < min.z + size.z);
    }

    /**
     * Returns the point of this region that is closest to the given position.
     *
     * @param pos the position to check
     * @return the nearest position within the region to the given pos
     */
    public Vector3i getNearestPointTo(BaseVector3i pos) {
        Vector3i result = new Vector3i(pos);
        result.min(max());
        result.max(min);
        return result;
    }

    @Override
    public Iterator<Vector3i> iterator() {
        return new Region3iIterator();
    }

    @Override
    public String toString() {
        return "(Min: " + min + ", Size: " + size + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Region3i) {
            Region3i other = (Region3i) obj;
            return min.equals(other.min) && size.equals(other.size);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + min.hashCode();
        hash += 37 * hash + size.hashCode();
        return hash;
    }

    private class Region3iIterator implements Iterator<Vector3i> {
        Vector3i pos;

        public Region3iIterator() {
            this.pos = new Vector3i();
        }

        @Override
        public boolean hasNext() {
            return pos.x < size.x;
        }

        @Override
        public Vector3i next() {
            Vector3i result = new Vector3i(pos.x + min.x, pos.y + min.y, pos.z + min.z);
            pos.z++;
            if (pos.z >= size.z) {
                pos.z = 0;
                pos.y++;
                if (pos.y >= size.y) {
                    pos.y = 0;
                    pos.x++;
                }
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private class SubtractiveIterator implements Iterator<Vector3i> {
        private Iterator<Vector3i> innerIterator;
        private Vector3i next;
        private Region3i other;

        public SubtractiveIterator(Region3i other) {
            this.other = other;
            innerIterator = iterator();
            updateNext();
        }

        private void updateNext() {
            while (innerIterator.hasNext()) {
                next = innerIterator.next();
                if (!other.encompasses(next)) {
                    return;
                }
            }
            next = null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Vector3i next() {
            Vector3i result = new Vector3i(next);
            updateNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
