/*
 * Copyright 2012
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

import javax.vecmath.Vector3f;
import java.util.Iterator;

/**
 * Describes an axis-aligned bounded space in 3D integer.
 * @author Immortius
 */
public class Region3i implements Iterable<Vector3i> {
    public static final Region3i EMPTY = new Region3i();

    private final Vector3i min = new Vector3i();
    private final Vector3i size = new Vector3i();

    public static Region3i createFromMinAndSize(Vector3i min, Vector3i size)
    {
        if (size.x <= 0 || size.y <= 0 || size.z <= 0)
        {
            return EMPTY;
        }
        return new Region3i(min, size);
    }

    public static Region3i createFromCenterExtents(Vector3f center, Vector3f extents)
    {
        Vector3f min = new Vector3f(center.x - extents.x, center.y - extents.y, center.z - extents.z);
        Vector3f max = new Vector3f(center.x + extents.x, center.y + extents.y, center.z + extents.z);
        max.x = max.x - Math.ulp(max.x);
        max.y = max.y - Math.ulp(max.y);
        max.z = max.z - Math.ulp(max.z);
        return createFromMinMax(new Vector3i(min),
                new Vector3i(max));
    }

    public static Region3i createFromCenterExtents(Vector3i center, Vector3i extents)
    {
        Vector3i min = new Vector3i(center.x - extents.x, center.y - extents.y, center.z - extents.z);
        Vector3i max = new Vector3i(center.x + extents.x, center.y + extents.y, center.z + extents.z);
        return createFromMinMax(min,
                max);
    }

    public static Region3i createFromCenterExtents(Vector3i center, int extent)
    {
        Vector3i min = new Vector3i(center.x - extent, center.y - extent, center.z - extent);
        Vector3i max = new Vector3i(center.x + extent, center.y + extent, center.z + extent);
        return createFromMinMax(min,
                max);
    }

    public static Region3i createBounded(Vector3i a, Vector3i b)
    {
        Vector3i min = new Vector3i(a);
        min.min(b);
        Vector3i max = new Vector3i(a);
        max.max(b);
        return createFromMinMax(min, max);
    }

    public static Region3i createFromMinMax(Vector3i min, Vector3i max)
    {
        Vector3i size = new Vector3i(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
        if (size.x <= 0 || size.y <= 0 || size.z <= 0)
        {
            return EMPTY;
        }
        return new Region3i(min, size);
    }

    public static Region3i createEncompassing(Region3i a, Region3i b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        Vector3i min = a.min();
        min.min(b.min());
        Vector3i max = a.max();
        max.max(b.max());
        return createFromMinMax(min, max);
    }

    /**
     * Constructs an empty Region with size (0,0,0).
     */
    public Region3i()
    {
    }

    private Region3i(Vector3i min, Vector3i size)
    {
        this.min.set(min);
        this.size.set(size);
    }

    public boolean isEmpty()
    {
        return size.x + size.y + size().z == 0;
    }

    /**
     * @return The smallest vector in the region
     */
    public Vector3i min()
    {
        return new Vector3i(min);
    }

    /**
     * @return The size of the region
     */
    public Vector3i size()
    {
        return new Vector3i(size);
    }

    /**
     * @return The largest vector in the region
     */
    public Vector3i max()
    {
        Vector3i max = new Vector3i(min);
        max.add(size);
        max.sub(1,1,1);
        return max;
    }

    /**
     * @param other
     * @return The region that is encompassed by both this and other. If they
     *         do not overlap then the empty region is returned
     */
    public Region3i intersect(Region3i other)
    {
        Vector3i min = min();
        min.max(other.min());
        Vector3i max = max();
        max.min(other.max());

        return createFromMinMax(min, max);
    }


    /**
     * Creates a new region that is the same as this region but expanded in all directions by the given amount
     * @param amount
     * @return A new region
     */
    public Region3i expand(int amount) {
        Vector3i min = min();
        min.sub(amount, amount, amount);
        Vector3i max = max();
        max.add(amount, amount, amount);
        return createFromMinMax(min, max);
    }

    public Region3i expand(Vector3i amount) {
        Vector3i min = min();
        min.sub(amount);
        Vector3i max = max();
        max.add(amount);
        return createFromMinMax(min, max);
    }

    public Region3i expandToContain(Vector3i adjPos) {
        Vector3i min = min();
        min.min(adjPos);
        Vector3i max = max();
        max.max(adjPos);
        return createFromMinMax(min, max);
    }

    /**
     *
     * @return The position at the center of the region
     */
    public Vector3f center()
    {
        Vector3f result = min.toVector3f();
        result.add(size.toVector3f());
        result.scale(0.5f);
        return result;
    }

    /**
     *
     * @param offset
     * @return A copy of the region offset by the given value
     */
    public Region3i move(Vector3i offset)
    {
        Vector3i newMin = min();
        newMin.add(offset);
        return Region3i.createFromMinAndSize(newMin, size);
    }

    /**
     *
     * @param pos
     * @return Whether this region includes pos
     */
    public boolean encompasses(Vector3i pos)
    {
        return (pos.x >= min.x) && (pos.y >= min.y) && (pos.z >= min.z) && (pos.x < min.x + size.x) && (pos.y < min.y + size.y) && (pos.z < min.z + size.z);
    }

    /**
     *
     * @param pos
     * @return The nearest position within the region to the given pos.
     */
    public Vector3i getNearestPointTo(Vector3i pos)
    {
        Vector3i result = new Vector3i(pos);
        result.min(max());
        result.max(min);
        return result;
    }

    @Override
    public Iterator<Vector3i> iterator()
    {
        return new Region3iIterator(this);
    }

    @Override
    public String toString() {
        return "(Min: " + min + ", Size: " + size + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Region3i)
        {
            Region3i other = (Region3i)obj;
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

    private class Region3iIterator implements Iterator<Vector3i>
    {
        Vector3i pos;

        public Region3iIterator(Region3i region)
        {
            this.pos = new Vector3i();
        }

        @Override
        public boolean hasNext()
        {
            return pos.x < size.x;
        }

        @Override
        public Vector3i next()
        {
            Vector3i result = new Vector3i(pos.x + min.x, pos.y + min.y, pos.z + min.z);
            pos.z++;
            if (pos.z >= size.z)
            {
                pos.z = 0;
                pos.y++;
                if (pos.y >= size.y)
                {
                    pos.y = 0;
                    pos.x++;
                }
            }
            return result;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
