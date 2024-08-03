// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.joml.geom.Rectanglef;

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

    /**
     * Set the minimum x-coordinate of the area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is greater than the maximum x coordinate
     */
    BlockArea minX(int x, BlockArea dest);

    /**
     * Set the minimum y-coordinate of the area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code y} is greater than the maximum y coordinate
     */
    BlockArea minY(int y, BlockArea dest);

    /**
     * Set the coordinates of the minimum corner for this area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the maximum
     *         corner
     */
    BlockArea setMin(int x, int y, BlockArea dest);

    /**
     * Set the coordinates of the minimum corner for this area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the maximum
     *         corner
     */
    default BlockArea setMin(Vector2ic min, BlockArea dest) {
        return this.setMin(min.x(), min.y(), dest);
    }

    /**
     * Translate the minimum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dx the number of blocks to add to the minimum corner on the x axis
     * @param dy the number of blocks to add to the minimum corner on the y axis
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    default BlockArea addToMin(int dx, int dy, BlockArea dest) {
        return this.setMin(minX() + dx, minY() + dy, dest);
    }

    /**
     * Translate the minimum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dmin the translation vector for the minimum corner
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    default BlockArea addToMin(Vector2ic dmin, BlockArea dest) {
        return this.addToMin(dmin.x(), dmin.y(), dest);
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

    /**
     * Set the maximum x-coordinate of the area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    BlockArea maxX(int x, BlockArea dest);

    /**
     * Set the maximum y-coordinate of the area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    BlockArea maxY(int y, BlockArea dest);

    /**
     * Set the coordinates of the maximum corner for this area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    BlockArea setMax(int x, int y, BlockArea dest);

    /**
     * Set the coordinates of the maximum corner for this area.
     *
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    default BlockArea setMax(Vector2ic max, BlockArea dest) {
        return this.setMax(max.x(), max.y(), dest);
    }

    /**
     * Translate the maximum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dx the number of blocks to add to the maximum corner on the x axis
     * @param dy the number of blocks to add to the maximum corner on the y axis
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    default BlockArea addToMax(int dx, int dy, BlockArea dest) {
        return this.setMax(maxX() + dx, maxY() + dy, dest);
    }

    /**
     * Translate the maximum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dmax the translation vector for the maximum corner
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    default BlockArea addToMax(Vector2ic dmax, BlockArea dest) {
        return this.addToMax(dmax.x(), dmax.y(), dest);
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
     * Set the size of the block area from the minimum corner.
     *
     * @param x the x coordinate to set the size; must be greater than 0
     * @param y the y coordinate to set the size; must be greater than 0
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    BlockArea setSize(int x, int y, BlockArea dest);

    /**
     * Set the size of the block area from the minimum corner.
     *
     * @param size the size to set; all dimensions must be greater than 0
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    default BlockArea setSize(Vector2ic size, BlockArea dest) {
        return this.setSize(size.x(), size.y(), dest);
    }

    /**
     * The are of this area in blocks, i.e., the number of blocks contained in this area.
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
    // -- union -----------------------------------------------------------------------------------------------------//

    /**
     * Compute the union of this area and the given block coordinate.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    BlockArea union(int x, int y, BlockArea dest);

    /**
     * Compute the union of this area and the given block coordinate.
     *
     * @param pos the position of the block
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockArea union(Vector2ic pos, BlockArea dest) {
        return this.union(pos.x(), pos.y(), dest);
    }

    /**
     * Compute the union of this area and the other area.
     *
     * @param other {@link BlockArea}
     * @param dest destination; will hold the result
     * @return dest (after modification)
     */
    default BlockArea union(BlockAreac other, BlockArea dest) {
        return this.union(other.minX(), other.minY(), dest)
                .union(other.maxX(), other.maxY(), dest);
    }

    // -- intersect -------------------------------------------------------------------------------------------------//

    /**
     * Compute the intersection of this area with the {@code other} area.
     * <p>
     * NOTE: If the areas don't intersect the destination area will become invalid!
     *
     * @param other the other area
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification) or {@link Optional#empty()} if the areas don't intersect
     */
    Optional<BlockArea> intersect(BlockAreac other, BlockArea dest);

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Translate this area by the given vector {@code (x, y, z))}.
     *
     * @param dx the x coordinate to translate by
     * @param dy the y coordinate to translate by
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    BlockArea translate(int dx, int dy, BlockArea dest);

    /**
     * Translate this area by the given vector {@code vec}.
     *
     * @param vec the vector to translate by
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockArea translate(Vector2ic vec, BlockArea dest) {
        return this.translate(vec.x(), vec.y(), dest);
    }

    /**
     * Move this area to the given position {@code (x, y)}. The position is defined by the minimum corner.
     *
     * @param x the new x coordinate of the minimum corner
     * @param y the new y coordinate of the minimum corner
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockArea setPosition(int x, int y, BlockArea dest) {
        return this.translate(x - minX(), y - minY(), dest);
    }

    /**
     * Move this area to the given position {@code (x, y)}. The position is defined by the minimum corner.
     *
     * @param pos the new coordinates of the minimum corner
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     */
    default BlockArea setPosition(Vector2ic pos, BlockArea dest) {
        return this.setPosition(pos.x(), pos.y(), dest);
    }

    // -- expand -----------------------------------------------------------------------------------------------------//

    /**
     * Expand this area by adding the given {@code extents} for each face of the area.
     *
     * @param dx the amount of blocks to extend this area by along the x axis in both directions
     * @param dy the amount of blocks to extend this area by along the y axis in both directions
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if extending this area would result in any non-positive dimension
     */
    BlockArea expand(int dx, int dy, BlockArea dest);

    /**
     * Expand this area by adding the given {@code extents} for each face of a area.
     *
     * @param vec the amount of blocks to expand this area by
     * @param dest destination; will hold the result
     * @return {@code dest} (after modification)
     * @throws IllegalArgumentException if extending this area would result in any non-positive dimension
     */
    default BlockArea expand(Vector2ic vec, BlockArea dest) {
        return this.expand(vec.x(), vec.y(), dest);
    }

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

    // -- distance --------------------------------------------------------------------------------------------------//

    /**
     * The squared distance to a point {@code p}.
     *
     * @param p the coordinates of the point
     * @return the squared distance between this area and the point {@code p}
     */
    long distanceSquared(Vector2ic p);

    /**
     * The squared distance to a point {@code p} given by coordinates (x, y).
     *
     * @param px the x coordinate of the point
     * @param py the y coordinate of the point
     * @return the squared distance between this area and the point {@code p}
     */
    long distanceSquared(int px, int py);

    // ---------------------------------------------------------------------------------------------------------------//
    boolean equals(Object obj);

    int hashCode();

    String toString();
}
