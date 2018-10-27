/*
 * Copyright 2017 MovingBlocks
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

import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 2D Rectangle
 */
// TODO: Review and bring into line with Region3i's api
public class Rect2i extends BaseRect {
    public static final Rect2i EMPTY = new Rect2i();

    // position
    private int posX;
    private int posY;

    // size
    private int w;
    private int h;

    protected Rect2i() {
    }

    protected Rect2i(int x, int y, int w, int h) {
        this.posX = x;
        this.posY = y;

        this.w = w;
        this.h = h;
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

    public static Rect2i createFromMinAndSize(Vector2ic min, Vector2ic size) {
        return createFromMinAndSize(min.x(), min.y(), size.x(), size.y());
    }

    public static Rect2i createFromMinAndMax(Vector2ic min, Vector2ic max) {
        return createFromMinAndMax(min.x(), min.y(), max.x(), max.y());
    }

    public static Rect2i createEncompassing(Vector2ic a, Vector2ic b) {
        return createEncompassing(a.x(), a.y(), b.x(), b.y());
    }

    public static Rect2i createEncompassing(int ax, int ay, int bx, int by) {
        return createFromMinAndMax(Math.min(ax, bx), Math.min(ay, by), Math.max(ax, bx), Math.max(ay, by));
    }

    public boolean isEmpty() {
        return w == 0 || h == 0;
    }

    @Override
    public Rect2f getBounds() {
        return Rect2f.createFromMinAndSize(posX, posY, w, h);
    }

    /**
     * @return The smallest vector in the region
     */
    public Vector2i min() {
        return new Vector2i(posX, posY);
    }

    public Vector2i max() {
        return new Vector2i(maxX(), maxY());
    }

    /**
     * @return The size of the region
     */
    public Vector2i size() {
        return new Vector2i(w, h);
    }

    public int maxX() {
        return posX + w - 1;
    }

    public int minX() {
        return posX;
    }


    public int maxY() {
        return posY + h - 1;
    }

    public int minY() {
        return posY;
    }

    public int width() {
        return w;
    }

    public int height() {
        return h;
    }

    /**
     * @return The area of the Rect2i - width * height
     */
    public int area() {
        return w * h;
    }

    /**
     * @param other
     * @return The Rect2i that is encompassed by both this and other. If they
     * do not overlap then the Rect2i.EMPTY is returned
     */
    public Rect2i intersect(Rect2i other) {
        int minX = Math.max(posX, other.posX);
        int maxX = Math.min(maxX(), other.maxX());
        int minY = Math.max(posY, other.posY);
        int maxY = Math.min(maxY(), other.maxY());
        return createFromMinAndMax(minX, minY, maxX, maxY);
    }

    /**
     * Computes the distance to a given point
     * @param px the point x coordinate
     * @param py the point y coordinate
     * @return the squared distance between point and this rectangle
     */
    public float distance(int px, int py) {
        return (float) Math.sqrt(distanceSquared(px, py));
    }

    /**
     * Computes the squared distance to a given point
     * @param px the point x coordinate
     * @param py the point y coordinate
     * @return the squared distance between point and this rectangle
     */
    public int distanceSquared(int px, int py) {
        // In contrast to Rect2f.distanceSquared, we compute the distance to
        // maxX and maxY instead of x+width/y+height.
        // This is why we need to subtract 1 from width/height.
        int cx2 = posX * 2 + w - 1;
        int cy2 = posY * 2 + h - 1;
        int dx = Math.max(Math.abs(2 * px - cx2) - w + 1, 0) / 2;
        int dy = Math.max(Math.abs(2 * py - cy2) - h + 1, 0) / 2;
        return dx * dx + dy * dy;
    }

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true only if the left <= x < right and top <= y < bottom
     */
    @Override
    public boolean contains(float x, float y) {
        return !isEmpty()
            && (x >= posX)
            && (y >= posY)
            && (x < posX + w)
            && (y < posY + h);
    }

    public boolean contains(Rect2i other) {
        return !isEmpty()
            && other.posX >= posX
            && other.posY >= posY
            && other.posX + other.w <= posX + w
            && other.posY + other.h <= posY + h;
    }

    public boolean overlaps(Rect2i other) {
        if (!(isEmpty() || other.isEmpty())) {
            int minX = Math.max(posX, other.posX);
            int maxX = Math.min(posX + w - 1, other.posX + other.w - 1);
            if (minX > maxX) {
                return false;
            }
            int minY = Math.max(posY, other.posY);
            int maxY = Math.min(posY + h - 1, other.posY + other.h - 1);
            return minY <= maxY;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Rect2i) {
            Rect2i other = (Rect2i) obj;
            return other.posX == posX && other.posY == posY && other.w == w && other.h == h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + posX;
        result = prime * result + posY;
        result = prime * result + w;
        result = prime * result + h;
        return result;
    }

    @Override
    public String toString() {
        return String.format("(x=%d y=%d w=%d h=%d)", posX, posY, w, h);
    }

    /**
     * Returns the difference between a and b - that is all parts of a that are not contained by b.
     *
     * @param a
     * @param b
     * @return A collection of rectangles that convertToString the difference of a - b. May be empty if a is completely encompassed by b.
     */
    public static List<Rect2i> difference(Rect2i a, Rect2i b) {
        List<Rect2i> result = Lists.newArrayList();
        if (b.contains(a)) {
            return result;
        }
        if (!a.overlaps(b)) {
            result.add(a);
            return result;
        }

        if (a.posX < b.posX) {
            result.add(Rect2i.createFromMinAndMax(a.posX, a.posY, b.posX - 1, a.posY + a.h - 1));
        }
        if (a.posY < b.posY) {
            result.add(Rect2i.createFromMinAndMax(Math.max(a.posX, b.posX), a.posY, a.posX + a.w - 1, b.posY - 1));
        }
        if (a.maxX() > b.maxX()) {
            result.add(Rect2i.createFromMinAndMax(b.posX + b.w, Math.max(a.posY, b.posY), a.posX + a.w - 1, a.posY + a.h - 1));
        }
        if (a.maxY() > b.maxY()) {
            result.add(Rect2i.createFromMinAndMax(Math.max(a.posX, b.posX), b.posY + b.h, Math.min(a.posX + a.w, b.posY + b.w) - 1, a.posY + a.h - 1));
        }

        return result;
    }

    public Rect2i expand(Vector2ic amount) {
        return expand(amount.x(), amount.y());
    }

    public Rect2i expand(int dx, int dy) {
        int minX = minX() - dx;
        int minY = minY() - dy;
        int maxX = maxX() + dx;
        int maxY = maxY() + dy;
        return createFromMinAndMax(minX, minY, maxX, maxY);
    }

    public int sizeX() {
        return w;
    }

    public int sizeY() {
        return h;
    }

    @Override
    public int outcode(float x, float y) {
        int out = 0;
        if (this.w <= 0) {
            out |= OUT_LEFT | OUT_RIGHT;
        } else if (x < this.posX) {
            out |= OUT_LEFT;
        } else if (x >= this.posX + this.w) {
            out |= OUT_RIGHT;
        }
        if (this.h <= 0) {
            out |= OUT_TOP | OUT_BOTTOM;
        } else if (y < this.posY) {
            out |= OUT_TOP;
        } else if (y >= this.posY + this.h) {
            out |= OUT_BOTTOM;
        }
        return out;
    }

    /**
     * Provides a read-only iterator over the positions in the Rect2i. They are iterated
     * from min to max, x before y (so all values at minY, then minY + 1, etc)
     * <br/><br/>
     * Do <b>not</b> store the result vectors as they are reused!
     *
     * @return An iterator over all positions in the Rect2i.
     */
    public Iterable<Vector2ic> contents() {
        return new Iterable<Vector2ic>() {

            @Override
            public Iterator<Vector2ic> iterator() {
                return new Iterator<Vector2ic>() {

                    private Vector2i pos = new Vector2i(posX - 1, posY);

                    @Override
                    public boolean hasNext() {
                        return pos.x() < maxY() || pos.x() < maxX();
                    }

                    @Override
                    public Vector2ic next() {
                        pos.x++;
                        if (pos.x > maxX()) {
                            pos.x = posX;
                            pos.y++;
                            if (pos.y > maxY()) {
                                throw new NoSuchElementException();
                            }
                        }
                        return pos;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
