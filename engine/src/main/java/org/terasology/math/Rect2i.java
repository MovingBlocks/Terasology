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

import java.util.Iterator;

/**
 * 2D Rectangle
 * @deprecated Use {@link org.terasology.math.geom.Rect2i} instead.
 */
// TODO: Review and bring into line with Region3i's api
@Deprecated
public final class Rect2i extends org.terasology.math.geom.Rect2i implements Iterable<Vector2i> {
    public static final Rect2i EMPTY = new Rect2i();

    private Rect2i() {
        super(0,0,0,0);
    }

    private Rect2i(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public static Rect2i createFromMinAndSize(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return EMPTY;
        }
        return new Rect2i(x, y, width, height);
    }

    public static Rect2i createFromMinAndMax(int minX, int minY, int maxX, int maxY) {
        if (maxX < minX || maxY < minY) {
            return EMPTY;
        }
        return new Rect2i(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public static Rect2i createFromMinAndSize(org.terasology.math.geom.Vector2i min, org.terasology.math.geom.Vector2i size) {
        return createFromMinAndSize(min.x, min.y, size.x, size.y);
    }

    public static Rect2i createFromMinAndMax(org.terasology.math.geom.Vector2i min, org.terasology.math.geom.Vector2i max) {
        return createFromMinAndMax(min.x, min.y, max.x, max.y);
    }

    public static Rect2i createEncompassing(org.terasology.math.geom.Vector2i a, org.terasology.math.geom.Vector2i b) {
        return createEncompassing(a.x, a.y, b.x, b.y);
    }

    public static Rect2i createEncompassing(int ax, int ay, int bx, int by) {
        return createFromMinAndMax(Math.min(ax, bx), Math.min(ay, by), Math.max(ax, bx), Math.max(ay, by));
    }

    /**
     * @return The smallest vector in the region
     */
    @Override
    public Vector2i min() {
        return new Vector2i(minX(), minY());
    }

    @Override
    public Vector2i max() {
        return new Vector2i(maxX(), maxY());
    }

    /**
     * @return The size of the region
     */
    @Override
    public Vector2i size() {
        return new Vector2i(width(), height());
    }

    /**
     * @param other
     * @return The Rect2i that is encompassed by both this and other. If they
     * do not overlap then the Rect2i.EMPTY is returned
     */
    @Override
    public Rect2i intersect(org.terasology.math.geom.Rect2i other) {
        int minX = Math.max(minX(), other.minX());
        int maxX = Math.min(maxX(), other.maxX());
        int minY = Math.max(minY(), other.minY());
        int maxY = Math.min(maxY(), other.maxY());
        return createFromMinAndMax(minX, minY, maxX, maxY);
    }

    public boolean encompasses(org.terasology.math.geom.Rect2i other) {
        return !isEmpty() && other.minX() >= minX() && other.minY() >= minY() && other.minX() + other.width() <= minX() + width() && other.minY() + other.height() <= minY() + height();
    }

    @Override
    public Rect2i expand(org.terasology.math.geom.Vector2i amount) {
        Vector2i expandedMin = min();
        expandedMin.sub(amount);
        Vector2i expandedMax = max();
        expandedMax.add(amount);
        return createFromMinAndMax(expandedMin, expandedMax);
    }

    /**
     * Provides an iterator over the positions in the Rect2i. They are iterated from min to max, x before y (so all values at minY, then minY + 1, etc)
     *
     * @return An iterator over all positions in the Rect2i.
     */
    @Override
    public Iterator<Vector2i> iterator() {
        return new Iterator<Vector2i>() {

            private Vector2i pos = new Vector2i(minX() - 1, minY());

            @Override
            public boolean hasNext() {
                return pos.getY() < maxY() || pos.getX() < maxX();
            }

            @Override
            public Vector2i next() {
                pos.x++;
                if (pos.x > maxX()) {
                    pos.x = minX();
                    pos.y++;
                }
                return pos;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
