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

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * 2D Rectangle
 */
// TODO: Review and bring into line with Region3i's api
public final class Rect2i {
    public static final Rect2i EMPTY = new Rect2i();

    // position
    private int posX;
    private int posY;

    // size
    private int w;
    private int h;

    private Rect2i() {
    }

    private Rect2i(int x, int y, int w, int h) {
        this.posX = x;
        this.posY = y;

        this.w = w;
        this.h = h;
    }

    public static Rect2i createFromMinAndSize(int x, int y, int width, int height) {
        return new Rect2i(x, y, width, height);
    }

    public static Rect2i createFromMinAndMax(int minX, int minY, int maxX, int maxY) {
        if (maxX < minX || maxY < minY) {
            return EMPTY;
        }
        return new Rect2i(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public static Rect2i createEncompassing(Vector2i a, Vector2i b) {
        return createEncompassing(a.x, a.y, b.x, b.y);
    }

    public static Rect2i createEncompassing(int ax, int ay, int bx, int by) {
        return createFromMinAndMax(Math.min(ax, bx), Math.min(ay, by), Math.max(ax, bx), Math.max(ay, by));
    }

    public boolean isEmpty() {
        return w == 0 || h == 0;
    }

    /**
     * @return The smallest vector in the region
     */
    public Vector2i min() {
        return new Vector2i(posX, posY);
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
     *         do not overlap then the Rect2i.EMPTY is returned
     */
    public Rect2i intersect(Rect2i other) {
        int minX = Math.max(posX, other.posX);
        int maxX = Math.min(maxX(), other.maxX());
        int minY = Math.max(posY, other.posY);
        int maxY = Math.min(maxY(), other.maxY());
        if (maxX < minX || maxY < minY) {
            return EMPTY;
        }
        return createFromMinAndMax(minX, minY, maxX, maxY);
    }

    /**
     * @param pos
     * @return Whether this Rect2i includes pos
     */
    public boolean contains(Vector2i pos) {
        return contains(pos.x, pos.y);
    }

    public boolean contains(int x, int y) {
        return !isEmpty() && (x >= posX) && (y >= posY) && (x < posX + w) && (y <= posY + h);
    }

    public boolean encompasses(Rect2i other) {
        return !isEmpty() && other.posX >= posX && other.posY >= posY && other.posX + other.w <= posX + w && other.posY + w <= posY + w;
    }

    public boolean overlaps(Rect2i other) {
        return !(isEmpty() || other.isEmpty()) && other.posX < posX + w && other.posX + other.w - 1 > posX && other.posY < posY + h && other.posY + other.h - 1 > posY;
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
        return Objects.hash(posX, posY, w, h);
    }

    @Override
    public String toString() {
        return String.format("(x=%d y=%d w=%d h=%d)", posX, posY, w, h);
    }

    ////////

    // a - b
    // @pre a and b have the same size
    // @return list of disjunct rects building the subtraction result (eg. L-shape)

    /**
     * Returns the difference between a and b - that is all parts of a that are not contained by b.
     *
     * @param a
     * @param b
     * @return A collection of rectangles that compose the difference of a - b. May be empty if a is completely encompassed by b.
     */
    public static List<Rect2i> difference(Rect2i a, Rect2i b) {
        List<Rect2i> result = Lists.newArrayList();
        if (b.encompasses(a)) {
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


}
