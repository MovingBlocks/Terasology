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
import org.joml.Rectanglef;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 2D Rectangle
 */
// TODO: Review and bring into line with Region3i's api
public class Rectanglei implements Rectangleic {

    public static Rectangleic EMPTY = new Rectanglei(0,0,0,0);

    // position
    private int minX, maxX;
    private int minY, maxY;

    protected Rectanglei() {
    }

    public Rectanglei(Rectangleic source) {
        this.minX = source.minX();
        this.minY = source.minY();
        this.maxX = source.maxX();
        this.maxY = source.maxY();
    }

    protected Rectanglei(int minX, int minY, int maxX, int maxY) {
       this.minX = minX;
       this.minY = minY;
       this.maxX = maxX;
       this.maxY = maxY;
    }

    private Rectanglei thisOrNew() {
        return this;
    }


    public static Rectanglei createFromMinAndSize(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return new Rectanglei(EMPTY);
        }
        return new Rectanglei(x, y, width, height);
    }

    public static Rectanglei createFromMinAndMax(int minX, int minY, int maxX, int maxY) {
        if (maxX < minX || maxY < minY) {
            return new Rectanglei(0,0,0,0);
        }
        return new Rectanglei(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public static Rectanglei createFromMinAndSize(Vector2ic min, Vector2ic size) {
        return createFromMinAndSize(min.x(), min.y(), size.x(), size.y());
    }

    public static Rectanglei createFromMinAndMax(Vector2ic min, Vector2ic max) {
        return createFromMinAndMax(min.x(), min.y(), max.x(), max.y());
    }

    public static Rectanglei createEncompassing(Vector2ic a, Vector2ic b) {
        return createEncompassing(a.x(), a.y(), b.x(), b.y());
    }

    public static Rectanglei createEncompassing(int ax, int ay, int bx, int by) {
        return createFromMinAndMax(Math.min(ax, bx), Math.min(ay, by), Math.max(ax, bx), Math.max(ay, by));
    }

    public boolean isEmpty() {
        return (minX - maxX == 0) || (minY - maxY == 0);
    }

    /**
     * @return The smallest vector in the region
     */
    public Vector2i min(Vector2i dest) {
        return dest.set(minX,minY);
    }

    public Vector2i min(){
        return min(new Vector2i());
    }

    public Vector2i max(Vector2i dest) {
        return dest.set(maxX(),maxY());
    }

    public Vector2i max(){
        return max(new Vector2i());
    }

    /**
     * @return The size of the region
     */
    public Vector2i size(Vector2i dest) {
        return dest.set(maxX - minX, maxY - minY);
    }

    public Vector2i size(){
        return size(new Vector2i());
    }
    @Override
    public int maxX() {
        return maxX;
    }

    @Override
    public int minX() {
        return minX;
    }

    @Override
    public int maxY() {
        return maxY;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int width() {
        return maxX - minX;
    }

    @Override
    public int height() {
        return maxY - minY;
    }


    /**
     * @return The area of the Rect2i - width * height
     */
    public int area() {
        return (maxX - minX) * (maxY - minY);
    }

    /**
     * @param other
     * @return The Rect2i that is encompassed by both this and other. If they
     * do not overlap then the Rect2i.EMPTY is returned
     */
    public Rectanglei intersect(Rectangleic other,Rectanglei dest) {
        dest.minX = Math.max(minX(), other.minX());
        dest.maxX = Math.min(maxX(), other.maxX());
        dest.minY = Math.max(minY(), other.minY());
        dest.maxY = Math.min(maxY(), other.maxY());
        return dest;
    }

    public Rectanglei intersect(Rectangleic other){
        return intersect(other,thisOrNew());
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
        int cx2 = minX * 2 + (maxX-minX) - 1;
        int cy2 = minY * 2 + (maxY-minY) - 1;
        int dx = Math.max(Math.abs(2 * px - cx2) - (maxX - minX) + 1, 0) / 2;
        int dy = Math.max(Math.abs(2 * py - cy2) - (maxY - minY) + 1, 0) / 2;
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
                && (x >= this.minX)
                && (y >= this.minY)
                && (x < this.maxX)
                && (y < this.maxY);
    }

    @Override
    public boolean contains(Rectangleic other) {
        return !isEmpty()
                && other.minX() >= minX()
                && other.minY() >= minY()
                && other.maxX() <= maxX()
                && other.maxY() <= maxY();
    }

    @Override
    public boolean overlaps(Rectangleic other) {
        if (!(isEmpty() || other.isEmpty())) {
            int minX = Math.max(minX(), other.minX());
            int maxX = Math.min(maxX() - 1, maxX() - 1);
            if (minX > maxX) {
                return false;
            }
            int minY = Math.max(minY(), other.minY());
            int maxY = Math.min(maxY() - 1, other.maxY() - 1);
            return minY <= maxY;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rectanglef other = (Rectanglef) obj;
        if (Float.floatToIntBits(maxX) != Float.floatToIntBits(other.maxX))
            return false;
        if (Float.floatToIntBits(maxY) != Float.floatToIntBits(other.maxY))
            return false;
        if (Float.floatToIntBits(minX) != Float.floatToIntBits(other.minX))
            return false;
        if (Float.floatToIntBits(minY) != Float.floatToIntBits(other.minY))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + minX;
        result = prime * result + minY;
        result = prime * result + maxX;
        result = prime * result + maxY;
        return result;
    }

    @Override
    public String toString() {
        return String.format("(minX=%d minY=%d maxX=%d maxY=%d)", minX, minY, maxX, maxY);
    }

    /**
     * Returns the difference between a and b - that is all parts of a that are not contained by b.
     *
     * @param a
     * @param b
     * @return A collection of rectangles that convertToString the difference of a - b. May be empty if a is completely encompassed by b.
     */
    public static List<Rectangleic> difference(Rectangleic a, Rectangleic b) {
        List<Rectangleic> result = Lists.newArrayList();
        if (b.contains(a)) {
            return result;
        }
        if (!a.overlaps(b)) {
            result.add(a);
            return result;
        }

        if (a.minX() < b.minX()) {
            result.add(new Rectanglei(a.minX(),a.minY(),b.minX()-1,a.maxY() - 1));
            //Rect2i.createFromMinAndMax(a.posX, a.posY, b.posX - 1, a.posY + a.h - 1));
        }
        if (a.minY() < b.minY()) {
            result.add(new Rectanglei(Math.max(a.minX(),b.minX()),a.minY(),a.maxX()-1,b.minY()));
            //result.add(Rect2i.createFromMinAndMax(Math.max(a.posX, b.posX), a.posY, a.posX + a.w - 1, b.posY - 1));
        }
        if (a.maxX() > b.maxX()) {
            result.add(new Rectanglei(b.maxX(), Math.max(a.minY(), b.minY()), a.maxX() - 1, a.maxY() - 1));
            //    result.add(Rect2i.createFromMinAndMax(b.posX + b.w, Math.max(a.posY, b.posY), a.posX + a.w - 1, a.posY + a.h - 1));
        }
        if (a.maxY() > b.maxY()) {
            result.add(new Rectanglei(Math.max(a.minX(), b.minX()), b.maxY(), Math.min(a.maxX(), b.maxX()) - 1, a.maxY()- 1));

//            result.add(Rect2i.createFromMinAndMax(Math.max(a.posX, b.posX), b.posY + b.h, Math.min(a.posX + a.w, b.posY + b.w) - 1, a.posY + a.h - 1));
        }

        return result;
    }

    public Rectanglei expand(Vector2ic amount) {
        return expand(amount.x(), amount.y());
    }

    public Rectanglei expand(int dx, int dy) {
        int minX = minX() - dx;
        int minY = minY() - dy;
        int maxX = maxX() + dx;
        int maxY = maxY() + dy;
        return createFromMinAndMax(minX, minY, maxX, maxY);
    }

    @Override
    public int outcode(float x, float y) {
        int out = 0;
        if (this.width() <= 0) {
            out |= OUT_LEFT | OUT_RIGHT;
        } else if (x < this.minX) {
            out |= OUT_LEFT;
        } else if (x >= this.maxX) {
            out |= OUT_RIGHT;
        }
        if (this.height() <= 0) {
            out |= OUT_TOP | OUT_BOTTOM;
        } else if (y < this.minY) {
            out |= OUT_TOP;
        } else if (y >= this.maxY) {
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

                    private Vector2i pos = new Vector2i(minX, minY);

                    @Override
                    public boolean hasNext() {
                        return pos.y() < maxY() || pos.x() < maxX();
                    }

                    @Override
                    public Vector2ic next() {
                        pos.x++;
                        if (pos.x > maxX()) {
                            pos.x = minX;
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
