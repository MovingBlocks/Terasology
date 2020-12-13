// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.joml.Math;
import org.joml.Rectangled;
import org.joml.Rectanglef;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * A bounded axis-aligned rectangle of blocks.
 * <p>
 * A block area is described and backed by an {@link org.joml.Rectanglei}
 *
 * @see BlockAreas
 */
public class BlockArea {
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    private Rectanglef bounds = new Rectanglef();

    public BlockArea() {
    }

    /**
     * @return a copy of this block area
     */
    public BlockArea copy() {
        return new BlockArea().set(this);
    }

    public boolean isValid() {
        return minX <= maxX && minY <= maxY;
    }

    public boolean isEmpty() {
        return !isValid();
    }

    public BlockArea set(BlockArea other) {
        this.minX = other.minX;
        this.minY = other.minY;
        this.maxX = other.maxX;
        this.maxY = other.maxY;

        Rectangles.set(this.bounds, other.bounds);
        return this;
    }

    /**
     * get the minimum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getMin(Vector2i dest) {
        return dest.set(minX, minY);
    }

    /**
     * get the maximum block coordinate
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getMax(Vector2i dest) {
        return dest.set(maxX, maxY);
    }

    /**
     * the maximum coordinate of the second block x
     *
     * @return the minimum coordinate x
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * the maximum coordinate of the second block y
     *
     * @return the minimum coordinate y
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * the minimum coordinate of the first block x
     *
     * @return the minimum coordinate x
     */
    public int getMinX() {
        return minX;
    }

    /**
     * the minimum coordinate of the first block y
     *
     * @return the minimum coordinate y
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Sets the minimum coordinate of the first block for <code>this</code> {@link BlockRegion}
     *
     * @param min the first coordinate of the first block
     * @return this
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public BlockArea setMin(Vector2ic min) {
        return setMin(min.x(), min.y());
    }

    /**
     * sets the minimum block for this {@link BlockRegion}
     *
     * @param minX the x coordinate of the first block
     * @param minY the y coordinate of the first block
     * @return this
     */
    public BlockArea setMin(int minX, int minY) {
        Preconditions.checkArgument(minX <= this.maxX ^ this.maxX == Integer.MIN_VALUE);
        Preconditions.checkArgument(minY <= this.maxY ^ this.maxY == Integer.MIN_VALUE);
        this.minX = minX;
        this.minY = minY;
        Rectangles.setMin(this.bounds, minX - 0.5f, minY - 0.5f);
        return this;
    }

    /**
     * Sets the maximum coordinate of the second block for <code>this</code> {@link BlockRegion}
     *
     * @param max the second coordinate of the second block
     * @return this
     */
    public BlockArea setMax(Vector2ic max) {
        return setMax(max.x(), max.y());
    }

    /**
     * sets the maximum block for this {@link BlockRegion}
     *
     * @param maxX the x coordinate of the first block
     * @param maxY the y coordinate of the first block
     * @return this
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public BlockArea setMax(int maxX, int maxY) {
        Preconditions.checkArgument(maxX >= this.minX ^ this.minX == Integer.MAX_VALUE);
        Preconditions.checkArgument(maxY >= this.minY ^ this.minY == Integer.MAX_VALUE);
        this.maxX = maxX;
        this.maxY = maxY;
        Rectangles.setMax(this.bounds, maxX + 0.5f, maxY + 0.5f);
        return this;
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and <code>other</code>.
     *
     * @param other {@link BlockRegion}
     * @return this
     */
    public BlockArea union(BlockArea other) {
        return union(other.minX, other.minY).union(other.minX, other.maxY);
    }

    /**
     * Set <code>this</code> to the union of <code>this</code> and the given block <code>p</code>.
     *
     * @param p the position of the block
     * @return this
     */
    public BlockArea union(Vector2ic p) {
        return union(p.x(), p.y());
    }

    public BlockArea union(int x, int y) {
        return union(x, y, this);
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
        return dest
                .setMin(Math.min(dest.minX, x), Math.min(dest.minY, y))
                .setMax(Math.max(dest.maxX, x), Math.max(dest.maxY, y));
    }

    /**
     * calculate the BlockRegion that is intersected between another region
     *
     * @param other the other BlockRegion
     * @param dest holds the result
     * @return dest
     */
    public BlockArea intersection(BlockArea other, BlockArea dest) {
        return dest
                .setMin(Math.max(this.minX, other.minX), Math.max(this.minY, other.minY))
                .setMax(Math.min(this.maxX, other.maxX), Math.min(this.maxY, other.maxY));
    }

    /**
     * the number of blocks for the +x, +y, +z from the minimum to the maximum
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector2i getSize(Vector2i dest) {
        return dest.set(getSizeX(), getSizeY());
    }

    /**
     * The number of blocks on the X axis
     *
     * @return number of blocks in the X axis
     */
    public int getSizeX() {
        return this.maxX - this.minX;
    }

    /**
     * The number of blocks on the Y axis
     *
     * @return number of blocks in the Y axis
     */
    public int getSizeY() {
        return this.maxY - this.minY;
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
        dest.minX += x;
        dest.minY += y;
        dest.maxX += x;
        dest.maxY += y;
        dest.bounds.translate(x, y);
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
        return x >= minX && y >= minY && x <= maxX && y <= maxY;
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
        return getWorldArea().containsPoint(x, y);
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
     * @param area the area to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsArea(BlockArea area) {
        return this.containsBlock(area.minX, area.minY) && this.containsBlock(area.maxX, area.maxY);
    }

    /**
     * Test whether <code>this</code> and <code>other</code> intersect.
     *
     * @param other the other BlockRegion
     * @return <code>true</code> iff both AABBs intersect; <code>false</code> otherwise
     */
    public boolean intersectsArea(BlockArea other) {
        return this.minX <= other.maxX && this.maxX >= other.minX && this.maxY >= other.minY && this.minY <= other.maxY;
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
        Preconditions.checkArgument(dest.getSizeX() + 2 * extentX >= 0 && dest.getSizeY() + 2 * extentY >= 0);
        dest.minX -= extentX;
        dest.minY -= extentY;
        dest.maxX += extentX;
        dest.maxY += extentY;
        Rectangles.expand(this.bounds, extentX, extentY);
        return dest;
    }


    /**
     * Test whether the given {@link Rectanglef}  lies inside the {@link BlockArea}
     *
     * @param rect the rectangle to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsRectangle(Rectanglef rect) {
        return getWorldArea().containsRectangle(rect);
    }

    /**
     * Test whether the given {@link Rectangled}  lies inside the {@link BlockArea}
     *
     * @param rect the rectangle to test
     * @return <code>true</code> iff the given value lies inside this {@link BlockArea}; <code>false</code> otherwise
     */
    public boolean containsRectangle(Rectangled rect) {
        return getWorldArea().containsRectangle(rect);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockArea blockArea = (BlockArea) o;
        return minX == blockArea.minX
                && minY == blockArea.minY
                && maxX == blockArea.maxX
                && maxY == blockArea.maxY;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(minX, minY, maxX, maxY);
    }

    @Override
    public String toString() {
        return "BlockArea[(" + this.minX + ", " + this.minY + ")...(" + this.maxX + ", " + this.maxY + ")]";
    }

    /**
     * Compute the bounding rectangle in world coordinates (continuous space).
     */
    public Rectanglef getWorldArea() {
        //TODO: would be better to return a Rectanglefc here...
        return new Rectanglef(bounds);
    }

    //TODO: replace with JOML-native code
    static final class Rectangles {
        private Rectangles() {
        }

        public static Rectanglef set(Rectanglef rect, Rectanglef other) {
            return set(rect, other.minX, other.minY, other.maxX, other.maxY);
        }

        public static Rectanglef set(Rectanglef rect, float minX, float minY, float maxX, float maxY) {
            setMin(rect, minX, minY);
            setMax(rect, maxX, maxY);
            return rect;
        }

        public static Rectanglef setMin(Rectanglef rect, float minX, float minY) {
            rect.minX = minX;
            rect.minY = minY;
            return rect;
        }

        public static Rectanglef setMax(Rectanglef rect, float maxX, float maxY) {
            rect.maxX = maxX;
            rect.maxY = maxY;
            return rect;
        }

        public static Rectanglef expand(Rectanglef rect, float extentX, float extentY) {
            rect.minX -= extentX;
            rect.minY -= extentY;
            rect.maxX += extentX;
            rect.maxY += extentY;
            return rect;
        }

    }
}
