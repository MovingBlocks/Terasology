// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import com.google.common.base.Preconditions;
import org.joml.AABBf;
import org.joml.AABBi;
import org.joml.Math;
import org.joml.Rectangled;
import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.joml.RoundingMode;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3ic;

/**
 * A bounded axis-aligned rectangle of blocks.
 * <p>
 * A block area is described and backed by an {@link org.joml.Rectanglei}
 *
 * @see BlockAreas
 */
public class BlockArea {
    private final Vector2i min = new Vector2i(Integer.MAX_VALUE);
    private final Vector2i max = new Vector2i(Integer.MIN_VALUE);

    private boolean dirty = true;
    private Rectanglef rectangle = new Rectanglef();

    public BlockArea() {
    }

    BlockArea(BlockArea source) {
        this.min.set(source.min);
        this.max.set(source.max);
    }

    /**
     * @return a copy of this block area
     */
    public BlockArea copy() {
        return new BlockArea(this);
    }

    /**
     * get the minimum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getMin(Vector2i dest) {
        return dest.set(min);
    }

    /**
     * get the maximum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getMax(Vector2i dest) {
        return dest.set(max);
    }

    /**
     * the maximum coordinate of the second block x
     *
     * @return the minimum coordinate x
     */
    public int getMaxX() {
        return max.x;
    }

    /**
     * the maximum coordinate of the second block y
     *
     * @return the minimum coordinate y
     */
    public int getMaxY() {
        return max.y;
    }

    /**
     * the minimum coordinate of the first block x
     *
     * @return the minimum coordinate x
     */
    public int getMinX() {
        return min.x;
    }

    /**
     * the minimum coordinate of the first block y
     *
     * @return the minimum coordinate y
     */
    public int getMinY() {
        return min.y;
    }

    /**
     * Sets the minimum coordinate of the first block for <code>this</code> {@link BlockRegion}
     *
     * @param min the first coordinate of the first block
     * @return this
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public BlockArea setMin(Vector2ic min) {
        return this.setMin(min.x(), min.y());
    }

    /**
     * sets the minimum block for this {@link BlockRegion}
     *
     * @param minX the x coordinate of the first block
     * @param minY the y coordinate of the first block
     * @return this
     */
    public BlockArea setMin(int minX, int minY) {
        Preconditions.checkArgument(minX <= this.max.x() ^ this.max.x() == Integer.MIN_VALUE);
        Preconditions.checkArgument(minY <= this.max.y() ^ this.max.y() == Integer.MIN_VALUE);
        this.min.set(minX, minY);
        return this;
    }

    /**
     * Sets the maximum coordinate of the second block for <code>this</code> {@link BlockRegion}
     *
     * @param max the second coordinate of the second block
     * @return this
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public BlockArea setMax(Vector2ic max) {
        return this.setMax(max.x(), max.y());
    }

    /**
     * sets the maximum block for this {@link BlockRegion}
     *
     * @param maxX the x coordinate of the first block
     * @param maxY the y coordinate of the first block
     * @return this
     */
    public BlockArea setMax(int maxX, int maxY) {
        Preconditions.checkArgument(maxX >= this.max.x() ^ this.max.x() == Integer.MAX_VALUE);
        Preconditions.checkArgument(maxY >= this.max.y() ^ this.max.y() == Integer.MAX_VALUE);
        this.max.set(maxX, maxY);
        return this;
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other {@link BlockRegion}
     * @return this
     */
    public BlockArea union(BlockArea other) {
        return this.union(other.min).union(other.max);
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and the given block <code>p</code>.
     *
     * @param p the position of the block
     * @return this
     */
    public BlockArea union(Vector2ic p) {
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
    public BlockArea union(int x, int y, BlockArea dest) {
        dest.min.set(Math.min(dest.min.x, x), Math.min(dest.min.y, y));
        dest.max.set(Math.max(dest.max.x, x), Math.max(dest.max.y, y));
        return dest;
    }

    /**
     * the number of blocks for the +x, +y, +z from the minimum to the maximum
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getSize(Vector2i dest) {
        return this.max.sub(this.min, dest);
    }

    /**
     * The number of blocks on the X axis
     *
     * @return number of blocks in the X axis
     */
    public int getSizeX() {
        return this.max.x() - this.min.x();
    }

    /**
     * The number of blocks on the Y axis
     *
     * @return number of blocks in the Y axis
     */
    public int getSizeY() {
        return this.max.y() - this.min.y();
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param x the x coordinate to translate by
     * @param y the y coordinate to translate by
     * @return this
     */
    public BlockArea translate(int x, int y) {
        return translate(x, y, this);
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param xy the vector to translate by
     * @return this
     */
    public BlockArea translate(Vector2ic xy) {
        return translate(xy, this);
    }

    /**
     * Translate <code>this</code> by the given vector <code>xyz</code>.
     *
     * @param xy the vector to translate by
     * @param dest will hold the result
     * @return dest
     */
    public BlockArea translate(Vector2ic xy, BlockArea dest) {
        return translate(xy.x(), xy.y(), dest);
    }

    /**
     * Translate <code>this</code> by the given vector <code>xy</code>.
     *
     * @param x the length to translate by along x direction
     * @param y the length to translate by along y direction
     * @param dest will hold the result
     * @return dest
     */
    public BlockArea translate(int x, int y, BlockArea dest) {
        dest.min.add(x, y);
        dest.max.add(x, y);
        return dest;
    }

    /**
     * Test whether the block <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param pos the coordinates of the block
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsBlock(Vector2ic pos) {
        return containsBlock(pos.x(), pos.y());
    }

    /**
     * Test whether the block <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsBlock(int x, int y) {
        return x >= min.x() && y >= min.y() && x <= max.x() && y <= max.y();
    }

    /**
     * WORLD-COORDINATES
     * Test whether the point <code>(x, y, z)</code> lies inside this BlockRegion.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return <code>true</code> iff the given point lies inside this BlockRegion; <code>false</code> otherwise
     */
    public boolean containsPoint(float x, float y) {
        updateRectangle();
        return x >= rectangle.minX && y >= rectangle.minY && x <= rectangle.maxX && y <= rectangle.maxY;
    }
    
    /**
     * @see #containsPoint(float, float)
     */
    public boolean containsPoint(Vector2fc point) {
        return this.containsPoint(point.x(), point.y());
    }

    /**
     * Test whether the point <code>(x, y, z)</code> lies inside this AABB.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(int x, int y) {
        return containsBlock(x, y);
    }

    /**
     * Test whether the given point lies inside this AABB.
     *
     * @param point the coordinates of the point
     * @return <code>true</code> iff the given point lies inside this AABB; <code>false</code> otherwise
     */
    public boolean containsPoint(Vector2ic point) {
        return containsPoint(point.x(), point.y());
    }

    /**
     * Test whether the given {@link BlockArea}  lies inside the {@link BlockArea}
     *
     * @param region the region to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsRectangularRegion(BlockArea region) {
        return this.rectangle.containsRectangle(region.rectangle);
    }

    /**
     * Test whether the given {@link Rectanglei}  lies inside the {@link BlockArea}
     *
     * @param rect the rectangle to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsRectangle(Rectanglei rect) {
        return this.rectangle.containsRectangle(rect);
    }

    /**
     * Test whether the given {@link Rectanglef}  lies inside the {@link BlockArea}
     *
     * @param rect the rectangle to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsRectangle(Rectanglef rect) {
        return this.rectangle.containsRectangle(rect);
    }

    /**
     * Test whether the given {@link Rectangled}  lies inside the {@link BlockArea}
     *
     * @param rect the rectangle to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsRectangle(Rectangled rect) {
        return this.rectangle.containsRectangle(rect);
    }


    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other BlockRegion
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsBlockGrid(BlockArea other) {
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
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other AABB
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsRegion(BlockArea other) {
        return this.rectangle.intersectsRectangle(other.rectangle);
    }

    /**
     * calculate the BlockRegion that is intersected between another region
     *
     * @param other the other BlockRegion
     * @param dest holds the result
     * @return dest
     */
    public BlockArea intersection(BlockArea other, BlockArea dest) {
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
    public BlockArea addExtents(int extent, BlockArea dest) {
        return addExtents(extent, extent, dest);
    }

    /**
     * Adds extend for each face of a BlockRegion.
     *
     * @param extentX the x coordinate to grow the extents
     * @param extentY the y coordinate to grow the extents
     * @return this
     */
    public BlockArea addExtents(int extentX, int extentY) {
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
    public BlockArea addExtents(int extentX, int extentY, BlockArea dest) {
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
    public BlockArea addExtents(float extentX, float extentY, BlockArea dest) {
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockArea region = (BlockArea) o;
        return rectangle.equals(region.rectangle);
    }

    @Override
    public int hashCode() {
        return rectangle.hashCode();
    }

    @Override
    public String toString() {
        return "BlockArea[" + this.min + "..." + this.max + "]";
    }

    /**
     * Recompute the bounding rectangle in world coordinates (continuous space).
     */
    private void updateRectangle() {
        if (dirty) {
            rectangle.minX = min.x() - 0.5f;
            rectangle.minY = min.y() - 0.5f;
            rectangle.maxX = max.x() + 0.5f;
            rectangle.maxY = max.y() + 0.5f;
        }
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
