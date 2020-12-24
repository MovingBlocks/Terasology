// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Math;
import org.joml.Rectanglef;
import org.joml.RoundingMode;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.Optional;

/**
 * An immutable, bounded, axis-aligned are denoting a collection of blocks contained within.
 */
public interface BlockAreac extends Iterable<Vector2ic> {

    // -- ITERABLE ---------------------------------------------------------------------------------------------------//

    /**
     * Iterate over the blocks in this block area, where the same {@link Vector2ic} is reused for low memory footprint.
     * <p>
     * Do not store the elements directly or use them outside the context of the iterator as they will change when the
     * iterator is advanced. You may create new vectors from the elements if necessary, e.g.:
     * <pre>
     *     for (Vector2ic p : area) {
     *         Vector2i pos = new Vector2i(p);
     *         // use 'pos' instead of 'p'
     *     }
     * </pre>
     */
    Iterator<Vector2ic> iterator();

    // -- min -------------------------------------------------------------------------------------------------------//

    /**
     * The x-coordinate of the minimum corner
     */
    int minX();

    /**
     * The y-coordinate of the minimum corner
     */
    int minY();

    /**
     * Get the block coordinate of the minimum corner.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Vector2i getMin(Vector2i dest) {
        return dest.set(minX(), minY());
    }

    // -- max -------------------------------------------------------------------------------------------------------//

    /**
     * The x-coordinate of the maximum corner
     */
    int maxX();

    /**
     * The y-coordinate of the maximum corner
     */
    int maxY();

    /**
     * Get the block coordinate of the maximum corner.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Vector2i getMax(Vector2i dest) {
        return dest.set(maxX(), maxY());
    }

    // -- size ------------------------------------------------------------------------------------------------------//

    /**
     * The number of blocks on the x axis.
     */
    default int getSizeX() {
        return maxX() - minX() + 1;
    }

    /**
     * The number of blocks on the y axis.
     */
    default int getSizeY() {
        return maxY() - minY() + 1;
    }

    /**
     * The number of blocks in this area along the +x, +y  axis from the minimum to the maximum corner.
     *
     * @param dest will hold the result
     * @return dest
     */
    default Vector2i getSize(Vector2i dest) {
        return dest.set(getSizeX(), getSizeY());
    }

    /**
     * The area of this area in blocks, i.e., the number of blocks contained in this area.
     * <p>
     * The area is computed by
     * <pre>
     *     area = sizeX * sizeY;
     * </pre>
     *
     * @return the area in blocks
     */
    default int area() {
        return getSizeX() * getSizeY();
    }

    // -- world -----------------------------------------------------------------------------------------------------//

    /**
     * The bounding area in world coordinates.
     * <p>
     * The bounding box of a single block at {@code (x, y)} is centered at the integer coordinate {@code (x, y)} and
     * extents by {@code 0.5} in each dimension.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Rectanglef getBounds(Rectanglef dest) {
        dest.minX = minX() - .5f;
        dest.minY = minY() - .5f;

        dest.maxX = maxX() + .5f;
        dest.maxY = maxY() + .5f;

        return dest;
    }

    /**
     * The center of the area in world coordinates if the area is valid; {@link Float#NaN} otherwise.
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    default Vector2f center(Vector2f dest) {
        if (!this.isValid()) {
            return dest.set(Float.NaN);
        }
        return dest.set(
                (this.minX() - .5f) + ((this.maxX() - this.minX() + 1.0f) / 2.0f),
                (this.minY() - .5f) + ((this.maxY() - this.minY() + 1.0f) / 2.0f)
        );
    }

    // -- IN-PLACE MUTATION -----------------------------------------------------------------------------------------//
    // -- transform --------------------------------------------------------------------------------------------------//

    //TODO: does this make sense for a BlockArea?    
    // BlockArea transform(Matrix4fc m, BlockArea dest);

    // -- CHECKS -----------------------------------------------------------------------------------------------------//

    /**
     * Check whether this area is valid.
     * <p>
     * A block area is valid iff the minimum corner is component-wise smaller or equal to the maximum corner.
     *
     * @return {@code true} iff this area is valid; {@code false} otherwise
     */
    default boolean isValid() {
        return minX() <= maxX() && minY() <= maxY();
    }

    // -- contains ---------------------------------------------------------------------------------------------------//

    /**
     * Test whether the block {@code (x, y)} lies inside this area.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @return {@code true} iff the given point lies inside this area; {@code false} otherwise
     */
    default boolean contains(int x, int y) {
        return x >= minX() && y >= minY() && x <= maxX() && y <= maxY();
    }

    /**
     * Test whether the block at position {@code pos} lies inside this area.
     *
     * @param pos the coordinates of the block
     * @return {@code true} iff the given point lies inside this area; {@code false} otherwise
     */
    default boolean contains(Vector2ic pos) {
        return this.contains(pos.x(), pos.y());
    }

    /**
     * Test whether the point {@code (x, y)} lies inside this area.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return {@code true} iff the given point lies inside this area; {@code false} otherwise
     * @see #getBounds(Rectanglef)
     */
    default boolean contains(float x, float y) {
        return x >= (this.minX() - .5f)
                && y >= (this.minY() - .5f)
                && x <= (this.maxX() + .5f)
                && y <= (this.maxY() + .5f);
    }

    /**
     * Test whether the {@code point} lies inside this area.
     *
     * @param point the coordinates of the point
     * @return {@code true} iff the given point lies inside this area; {@code false} otherwise
     */
    default boolean contains(Vector2fc point) {
        return this.contains(point.x(), point.y());
    }

    /**
     * Test whether the given area {@code other} is fully enclosed by this area.
     *
     * @param other the other area
     * @return {@code true} iff the given area is fully enclosed by this area; {@code false} otherwise
     */
    default boolean contains(BlockAreac other) {
        return this.contains(other.minX(), other.minY())
                && this.contains(other.maxX(), other.maxY());
    }

    // -- intersects -------------------------------------------------------------------------------------------------//

    //TODO: which intersection tests make sense for BlockArea?

    /**
     * Test whether this area and {@code other} intersect.
     *
     * @param other the other BlockArea
     * @return {@code true} iff both areas intersect; {@code false} otherwise
     */
    default boolean intersectsBlockArea(BlockAreac other) {
        return this.maxX() >= other.minX() && this.maxY() >= other.minY()
                && this.minX() <= other.maxX() && this.minY() <= other.maxY();
    }

    // ---------------------------------------------------------------------------------------------------------------//
    public boolean equals(Object obj);

    public int hashCode();

    public String toString();
}
