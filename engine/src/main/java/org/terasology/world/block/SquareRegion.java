// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.AABBi;
import org.joml.Math;
import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.joml.RoundingMode;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3ic;

/**
 * is a bounded rectangle describing squares contained within.
 * A {@link SquareRegion} is described and backed by an {@link org.joml.Rectanglei}
 */
public class SquareRegion {
    /**
     * rectangle region that backs a SquareRegion
     */
    public final Rectanglei rectangle = new Rectanglei();

    public SquareRegion() {
    }

    public SquareRegion(SquareRegion source) {
        RectangleUtility.set(rectangle, source.rectangle);
    }

    public SquareRegion(Rectanglei source) {
        RectangleUtility.set(rectangle, source);
    }

    public SquareRegion(Vector2ic min, Vector2ic max) {
        this(min.x(), min.y(), max.x(), max.y());
    }

    public SquareRegion(int minX, int minY, int maxX, int maxY) {
        this.setMin(minX, minY).setMax(maxX, maxY);
    }


    /**
     * get the minimum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getMin(Vector2i dest) {
        return dest.set(rectangle.minX, rectangle.minY);
    }

    /**
     * get the maximum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getMax(Vector2i dest) {
        return dest.set(rectangle.maxX - 1, rectangle.maxY - 1);
    }

    /**
     * the maximum coordinate of the second block x
     *
     * @return the minimum coordinate x
     */
    public int getMaxX() {
        return this.rectangle.maxX - 1;
    }

    /**
     * the maximum coordinate of the second block y
     *
     * @return the minimum coordinate y
     */
    public int getMaxY() {
        return this.rectangle.maxY - 1;
    }

    /**
     * the minimum coordinate of the first block x
     *
     * @return the minimum coordinate x
     */
    public int getMinX() {
        return this.rectangle.minX;
    }

    /**
     * the minimum coordinate of the first block y
     *
     * @return the minimum coordinate y
     */
    public int getMinY() {
        return this.rectangle.minY;
    }

    /**
     * Sets the minimum coordinate of the first block for <code>this</code> {@link BlockRegion}
     *
     * @param min the first coordinate of the first block
     * @return this
     */
    public SquareRegion setMin(Vector2ic min) {
        RectangleUtility.setMin(this.rectangle, min);
        return this;
    }

    /**
     * Sets the maximum coordinate of the second block for <code>this</code> {@link BlockRegion}
     *
     * @param max the second coordinate of the second block
     * @return this
     */
    public SquareRegion setMax(Vector2ic max) {
        this.setMax(max.x(), max.y());
        return this;
    }

    /**
     * sets the maximum block for this {@link BlockRegion}
     *
     * @param maxX the x coordinate of the first block
     * @param maxY the y coordinate of the first block
     * @return this
     */
    public SquareRegion setMax(int maxX, int maxY) {
        RectangleUtility.setMax(this.rectangle, maxX + 1, maxY + 1);
        return this;
    }

    /**
     * sets the minimum block for this {@link BlockRegion}
     *
     * @param minX the x coordinate of the first block
     * @param minY the y coordinate of the first block
     * @return this
     */
    public SquareRegion setMin(int minX, int minY) {
        RectangleUtility.setMin(this.rectangle, minX, minY);
        return this;
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and the given block <code>p</code>.
     *
     * @param p the position of the block
     * @return this
     */
    public SquareRegion union(Vector2ic p) {
        return union(p.x(), p.y(), this);
    }

    /**
     * Compute the union of <code>this</code> and the given block <code>(x, y, z)</code> and stores the result in
     * <code>dest</code>
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param dest will hold the result
     * @return dest
     */
    public SquareRegion union(int x, int y, SquareRegion dest) {
        // a block is (x,y,z) and (x + 1, y + 1, z + 1)
        dest.rectangle.minX = Math.min(this.rectangle.minX, x);
        dest.rectangle.minY = Math.min(this.rectangle.minY, y);
        dest.rectangle.maxX = Math.max(this.rectangle.maxX, (x + 1));
        dest.rectangle.maxY = Math.max(this.rectangle.maxY, (y + 1));
        return dest;
    }

    /**
     * Compute the union of <code>this</code> and the given block <code>(x, y, z)</code> and store the result in
     * <code>dest</code>.
     *
     * @param pos the position of the block
     * @param dest will hold the result
     * @return dest
     */
    public SquareRegion union(Vector3ic pos, SquareRegion dest) {
        return this.union(pos.x(), pos.y(), dest);
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other {@link BlockRegion}
     * @return this
     */
    public SquareRegion union(SquareRegion other) {
        return this.union(other.rectangle);
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other {@link AABBi}
     * @param dest will hold the result
     * @return dest
     */
    public SquareRegion union(Rectanglei other, SquareRegion dest) {
        dest.union(other);
        return dest;
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other the other {@link AABBi}
     * @return this
     */
    public SquareRegion union(Rectanglei other) {
        RectangleUtility.union(this.rectangle, other);
        return this;
    }

    /**
     * Ensure that the minimum coordinates are strictly less than or equal to the maximum coordinates by swapping them
     * if necessary.
     *
     * @return this
     */
    public SquareRegion correctBounds() {
        int tmp;
        if (this.rectangle.minX > this.rectangle.maxX) {
            tmp = this.rectangle.minX;
            this.rectangle.minX = this.rectangle.maxX;
            this.rectangle.maxX = tmp;
        }
        if (this.rectangle.minY > this.rectangle.maxY) {
            tmp = this.rectangle.minY;
            this.rectangle.minY = this.rectangle.maxY;
            this.rectangle.maxY = tmp;
        }
        return this;
    }

    /**
     * set the size of the block region from minimum.
     *
     * @param x the x coordinate to set the size
     * @param y the y coordinate to set the size
     * @return this
     */
    public SquareRegion setSize(int x, int y) {
        this.rectangle.maxX = this.rectangle.minX + x;
        this.rectangle.maxY = this.rectangle.minY + y;
        return this;
    }

    /**
     * set the size of the block region from minimum.
     *
     * @param size the size to set the {@link BlockRegion}
     * @return this
     */
    public SquareRegion setSize(Vector2ic size) {
        return setSize(size.x(), size.y());
    }

    /**
     * the number of blocks for the +x, +y, +z from the minimum to the maximum
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getSize(Vector2i dest) {
        return dest.set(this.rectangle.maxX - this.rectangle.minX, this.rectangle.maxY - this.rectangle.minY);
    }

    /**
     * The number of blocks on the X axis
     *
     * @return number of blocks in the X axis
     */
    public int getSizeX() {
        return this.rectangle.maxX - this.rectangle.minX;
    }

    /**
     * The number of blocks on the Y axis
     *
     * @return number of blocks in the Y axis
     */
    public int getSizeY() {
        return this.rectangle.maxY - this.rectangle.minY;
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param x the x coordinate to translate by
     * @param y the y coordinate to translate by
     * @return this
     */
    public SquareRegion translate(int x, int y) {
        rectangle.translate(x, y);
        return this;
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param xy the vector to translate by
     * @param dest will hold the result
     * @return dest
     */
    public SquareRegion translate(Vector2ic xy, SquareRegion dest) {
        rectangle.translate(xy, dest.rectangle);
        return dest;
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param xy the vector to translate by
     * @return this
     */
    public SquareRegion translate(Vector2ic xy) {
        this.rectangle.translate(xy);
        return this;
    }


    /**
     * Test whether the block <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param pos the coordinates of the block
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
        return x >= rectangle.minX && y >= rectangle.minY && x < rectangle.maxX && y < rectangle.maxY;
    }

    /**
     * Test whether the point <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return <code>true</code> iff the given point lies inside this BlockRegion; <code>false</code> otherwise
     */
    public boolean containsPoint(float x, float y) {
        return x >= rectangle.minX && y >= rectangle.minY && x <= rectangle.maxX && y <= rectangle.maxY;
    }

    /**
     * Test whether the point <code>(x, y, z)</code> lies inside this AABB.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(int x, int y) {
        return this.rectangle.containsPoint(x, y);
    }

    /**
     * Test whether the given point lies inside this AABB.
     *
     * @param point the coordinates of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(Vector2ic point) {
        return this.rectangle.containsPoint(point);
    }

    /**
     * Test whether the given point lies inside this AABB.
     *
     * @param point the coordinates of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(Vector2fc point) {
        return this.containsPoint(point.x(), point.y());
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other BlockRegion
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsBlockGrid(SquareRegion other) {
        return this.rectangle.intersectsRectangle(other.rectangle);
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other AABB
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsRectangle(Rectanglei other) {
        return this.rectangle.intersectsRectangle(other);
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other AABB
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsRectangle(Rectanglef other) {
        return this.rectangle.intersectsRectangle(other);
    }

    /**
     * Check whether <code>this</code> BlockRegion represents a valid BlockRegion.
     *
     * @return <code>true</code> iff this BlockRegion is valid; <code>false</code> otherwise
     */
    public boolean isValid() {
        return rectangle.isValid();
    }

    /**
     * calculate the BlockRegion that is intersected between another region
     *
     * @param other the other BlockRegion
     * @param dest holds the result
     * @return dest
     */
    public SquareRegion intersection(SquareRegion other, SquareRegion dest) {
        this.rectangle.intersection(other.rectangle, dest.rectangle);
        return dest;
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extent extents to grow each face
     * @param dest holds the result
     * @return dest
     */
    public SquareRegion addExtents(int extent, SquareRegion dest) {
        return addExtents(extent, extent, dest);
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @return this
     */
    public SquareRegion addExtents(int extentX, int extentY) {
        return addExtents(extentX, extentY, this);
    }

    /**
     * Adds extend for each face of a BlockRegion
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param dest will hold the result
     * @return dest
     */
    public SquareRegion addExtents(int extentX, int extentY, SquareRegion dest) {
        dest.rectangle.minX = this.rectangle.minX - extentX;
        dest.rectangle.minY = this.rectangle.minY - extentY;

        dest.rectangle.maxX = this.rectangle.maxX + extentX;
        dest.rectangle.maxY = this.rectangle.maxY + extentY;
        return dest;
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @param dest will hold the result
     * @return dest
     */
    public SquareRegion addExtents(float extentX, float extentY, SquareRegion dest) {
        dest.rectangle.minX = Math.roundUsing(this.rectangle.minX - extentX, RoundingMode.FLOOR);
        dest.rectangle.minY = Math.roundUsing(this.rectangle.minY - extentY, RoundingMode.FLOOR);

        dest.rectangle.maxX = Math.roundUsing(this.rectangle.maxX + extentX, RoundingMode.CEILING);
        dest.rectangle.maxY = Math.roundUsing(this.rectangle.maxY + extentY, RoundingMode.CEILING);
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (rectangle.getClass() == o.getClass()) {
            return rectangle.equals(o);
        }

        if (getClass() != o.getClass()) {
            return false;
        }
        SquareRegion region = (SquareRegion) o;
        return rectangle.equals(region.rectangle);
    }

    @Override
    public int hashCode() {
        return rectangle.hashCode();
    }

    @Override
    public String toString() {
        return "(" + this.rectangle.minX + " " + this.rectangle.minY + ") < " +
            "(" + (this.rectangle.maxX - 1) + " " + (this.rectangle.maxY - 1) + ")";
    }

    /**
     * Temporary Utility
     * <p>
     * https://github.com/JOML-CI/JOML/pull/244 https://github.com/JOML-CI/JOML/pull/245
     */
    public static class RectangleUtility {

        public static Rectanglei set(Rectanglei current, Rectanglei source) {
            current.minX = source.minX;
            current.minY = source.minY;
            current.maxX = source.maxX;
            current.maxY = source.maxY;
            return current;
        }

        /**
         * Set the minimum corner coordinates.
         *
         * @param minX the x coordinate of the minimum corner
         * @param minY the y coordinate of the minimum corner
         * @return this
         */
        public static Rectanglei setMin(Rectanglei current, int minX, int minY) {
            current.minX = minX;
            current.minY = minY;
            return current;
        }

        /**
         * Set the minimum corner coordinates.
         *
         * @param min the minimum coordinates
         * @return this
         */
        public static Rectanglei setMin(Rectanglei current, Vector2ic min) {
            current.minX = min.x();
            current.minY = min.y();
            return current;
        }


        /**
         * Set the maximum corner coordinates.
         *
         * @param maxX the x coordinate of the maximum corner
         * @param maxY the y coordinate of the maximum corner
         * @return this
         */
        public static Rectanglei setMax(Rectanglei current, int maxX, int maxY) {
            current.maxX = maxX;
            current.maxY = maxY;
            return current;
        }

        /**
         * Set the maximum corner coordinates.
         *
         * @param max the maximum coordinates
         * @return this
         */
        public static Rectanglei setMax(Rectanglei current, Vector2ic max) {
            current.maxX = max.x();
            current.maxY = max.y();
            return current;
        }

        /**
         * Set <code>this</code> to the union of <code>this</code> and the given point <code>p</code>.
         *
         * @param x the x coordinate of the point
         * @param y the y coordinate of the point
         * @return this
         */
        public static Rectanglei union(Rectanglei current, int x, int y) {
            return union(current, x, y, current);
        }

        /**
         * Set <code>this</code> to the union of <code>this</code> and the given point <code>p</code>.
         *
         * @param p the point
         * @return this
         */
        public static Rectanglei union(Rectanglei current, Vector2ic p) {
            return union(current, p.x(), p.y(), current);
        }

        /**
         * Compute the union of <code>this</code> and the given point <code>(x, y, z)</code> and store the result in
         * <code>dest</code>.
         *
         * @param x the x coordinate of the point
         * @param y the y coordinate of the point
         * @param dest will hold the result
         * @return dest
         */
        public static Rectanglei union(Rectanglei current, int x, int y, Rectanglei dest) {
            dest.minX = current.minX < x ? current.minX : x;
            dest.minY = current.minY < y ? current.minY : y;
            dest.maxX = current.maxX > x ? current.maxX : x;
            dest.maxY = current.maxY > y ? current.maxY : y;
            return dest;
        }

        /**
         * Compute the union of <code>this</code> and the given point <code>p</code> and store the result in
         * <code>dest</code>.
         *
         * @param p the point
         * @param dest will hold the result
         * @return dest
         */
        public static Rectanglei union(Rectanglei current, Vector2ic p, Rectanglei dest) {
            return union(current, p.x(), p.y(), dest);
        }

        /**
         * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
         *
         * @param other the other {@link Rectanglei}
         * @return this
         */
        public static Rectanglei union(Rectanglei current, Rectanglei other) {
            return union(current, other, current);
        }

        /**
         * Compute the union of <code>this</code> and <code>other</code> and store the result in <code>dest</code>.
         *
         * @param other the other {@link Rectanglei}
         * @param dest will hold the result
         * @return dest
         */
        public static Rectanglei union(Rectanglei current, Rectanglei other, Rectanglei dest) {
            dest.minX = current.minX < other.minX ? current.minX : other.minX;
            dest.minY = current.minY < other.minY ? current.minY : other.minY;
            dest.maxX = current.maxX > other.maxX ? current.maxX : other.maxX;
            dest.maxY = current.maxY > other.maxY ? current.maxY : other.maxY;
            return dest;
        }
    }
}
