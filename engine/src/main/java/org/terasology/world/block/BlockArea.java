// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.Optional;

/**
 * A mutable, bounded, axis-aligned are denoting a collection of blocks contained within.
 */
public class BlockArea implements BlockAreac {

    public static final BlockAreac INVALID = new BlockArea();

    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    // -- CONSTRUCTORS -----------------------------------------------------------------------------------------------//

    BlockArea() {
    }

    public BlockArea(int minX, int minY, int maxX, int maxY) {
        this.set(minX, minY, maxX, maxY);
    }

    public BlockArea(Vector2ic min, Vector2ic max) {
        this.set(min.x(), min.y(), max.x(), max.y());
    }

    public BlockArea(int x, int y) {
        this.set(x, y, x, y);
    }

    public BlockArea(Vector2ic pos) {
        this.set(pos, pos);
    }

    public BlockArea(BlockAreac other) {
        this.set(other.minX(), other.minY(), other.maxX(), other.maxY());
    }

    // -- reset ------------------------------------------------------------------------------------------------------//

    public BlockArea set(int minX, int minY, int maxX, int maxY) {
        Preconditions.checkArgument(minX <= maxX || (minX == INVALID.minX() && maxX == INVALID.maxX()));
        Preconditions.checkArgument(minY <= maxY || (minY == INVALID.minY() && maxY == INVALID.maxY()));

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        return this;
    }

    public BlockArea set(Vector2ic min, Vector2ic max) {
        return this.set(min.x(), min.y(), max.x(), max.y());
    }

    public BlockArea set(BlockAreac other) {
        return this.set(other.minX(), other.minY(), other.maxX(), other.maxY());
    }

    // -- ITERABLE ---------------------------------------------------------------------------------------------------//

    @Override
    public Iterator<Vector2ic> iterator() {
        return new Iterator<Vector2ic>() {
            private Vector2i current = null;
            private final Vector2i next = getMin(new Vector2i());

            public boolean findNext() {
                if (current.equals(next)) {
                    next.y++;
                    if (next.y > maxY) {
                        next.y = minY;
                        next.x++;
                    }

                    return contains(next);
                }
                return true;
            }

            @Override
            public boolean hasNext() {
                if (!isValid()) {
                    return false;
                }
                if (current == null) {
                    return true;
                }

                if (current.equals(next)) {
                    return findNext();
                }
                return contains(next);
            }

            @Override
            public Vector2ic next() {
                if (current == null) {
                    current = new Vector2i(next);
                    return next;
                }

                if (current.equals(next)) {
                    if (findNext()) {
                        return next;
                    }
                    return null;
                }
                current.set(next);
                return next;
            }
        };
    }

    // -- GETTERS & SETTERS ------------------------------------------------------------------------------------------//

    @Override
    public int minX() {
        return minX;
    }

    @Override
    public int minY() {
        return minY;
    }

    /**
     * Set the minimum x-coordinate of the area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if {@code x} is greater than the maximum x coordinate
     */
    public BlockArea minX(int x) {
        return this.set(x, minY, maxX, maxY);
    }

    /**
     * Set the minimum y-coordinate of the area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if {@code y} is greater than the maximum y coordinate
     */
    public BlockArea minY(int y) {
        return this.set(minX, y, maxX, maxY);
    }

    /**
     * Set the coordinates of the minimum corner for this area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the maximum
     *         corner
     */
    public BlockArea setMin(int x, int y) {
        return this.set(x, y, maxX, maxY);
    }

    /**
     * Set the coordinates of the minimum corner for this area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if any dimension is greater than the respective component of the maximum
     *         corner
     */
    public BlockArea setMin(Vector2ic min) {
        return this.setMin(min.x(), min.y());
    }

    /**
     * Translate the minimum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dx the number of blocks to add to the minimum corner on the x axis
     * @param dy the number of blocks to add to the minimum corner on the y axis
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    public BlockArea addToMin(int dx, int dy) {
        return this.setMin(minX() + dx, minY() + dy);
    }

    /**
     * Translate the minimum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dmin the translation vector for the minimum corner
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    public BlockArea addToMin(Vector2ic dmin) {
        return this.addToMin(dmin.x(), dmin.y());
    }


    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public int maxX() {
        return maxX;
    }

    @Override
    public int maxY() {
        return maxY;
    }

    /**
     * Set the maximum x-coordinate of the area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    public BlockArea maxX(int x) {
        return this.set(minX, minY, x, maxY);
    }

    /**
     * Set the maximum y-coordinate of the area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if {@code x} is smaller than the minimum x-coordinate
     */
    public BlockArea maxY(int y) {
        return this.set(minX, minY, maxX, y);
    }

    /**
     * Set the coordinates of the maximum corner for this area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    public BlockArea setMax(int x, int y) {
        return this.set(minX, minY, x, y);
    }

    /**
     * Set the coordinates of the maximum corner for this area.
     *
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if any dimension is smaller than the respective component of the minimum
     *         corner
     */
    public BlockArea setMax(Vector2ic max) {
        return this.setMax(max.x(), max.y());
    }

    /**
     * Translate the maximum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dx the number of blocks to add to the maximum corner on the x axis
     * @param dy the number of blocks to add to the maximum corner on the y axis
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    public BlockArea addToMax(int dx, int dy) {
        return this.setMax(maxX() + dx, maxY() + dy);
    }

    /**
     * Translate the maximum corner of the area by adding given {@code (dx, dy)}.
     *
     * @param dmax the translation vector for the maximum corner
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the resulting area would be {@link #isValid() invalid}.
     */
    public BlockArea addToMax(Vector2ic dmax) {
        return this.addToMax(dmax.x(), dmax.y());
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Set the size of the block area from the minimum corner.
     *
     * @param sizeX the x coordinate to set the size; must be > 0
     * @param sizeY the y coordinate to set the size; must be > 0
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    public BlockArea setSize(int sizeX, int sizeY) {
        return this.set(minX, minY, minX + sizeX, minY + sizeY);
    }

    /**
     * Set the size of the block area from the minimum corner.
     *
     * @param size the size to set; all dimensions must be > 0
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if the size is smaller than or equal to 0 in any dimension
     */
    public BlockArea setSize(Vector2ic size) {
        return this.setSize(size.x(), size.y());
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Compute the union of this area and the given block coordinate.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @return {@code this} (after modification)
     */
    public BlockArea union(int x, int y) {
        return this.set(
                Math.min(this.minX, x), Math.min(this.minY, y),
                Math.max(this.maxX, x), Math.max(this.maxY, y));
    }

    /**
     * Compute the union of this area and the given block coordinate.
     *
     * @param pos the position of the block
     * @return {@code this} (after modification)
     */
    public BlockArea union(Vector2ic pos) {
        return union(pos.x(), pos.y());
    }

    /**
     * Compute the union of this area and the other area.
     *
     * @param other {@link BlockArea}
     * @return @code this} (after modification)
     */
    public BlockArea union(BlockArea other) {
        return this.union(other.minX(), other.minY())
                .union(other.maxX(), other.maxY());
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Compute the intersection of this area with the {@code other} area.
     * <p>
     * NOTE: If the areas don't intersect the destination area will become invalid!
     *
     * @param other the other area
     * @return {@code this} (after modification) or {@link Optional#empty()} if the areas don't intersect
     */
    public Optional<BlockArea> intersect(BlockAreac other) {
        this.minX = Math.max(minX, other.minX());
        this.minY = Math.max(minY, other.minY());

        this.maxX = Math.min(maxX, other.maxX());
        this.maxY = Math.min(maxY, other.maxY());

        if (this.isValid()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Translate this area by the given vector {@code (x, y))}.
     *
     * @param dx the x coordinate to translate by
     * @param dy the y coordinate to translate by
     * @return {@code this} (after modification)
     */
    public BlockArea translate(int dx, int dy) {
        this.minX = minX + dx;
        this.minY = minY + dy;
        this.maxX = maxX + dx;
        this.maxY = maxY + dy;
        return this;
    }

    /**
     * Translate this area by the given vector {@code vec}.
     *
     * @param dv the vector to translate by
     * @return {@code this} (after modification)
     */
    public BlockArea translate(Vector2ic dv) {
        return translate(dv.x(), dv.y());
    }

    /**
     * Move this area to the given vector {@code (x, y))}.
     *
     * @param x the x coordinate to move to
     * @param y the y coordinate to move to
     * @return {@code this} (after modification)
     */
    public BlockArea setPosition(int x, int y) {
        return translate(x - this.minX, y - this.minY);
    }

    /**
     * Move this area to the given vector {@code vec}.
     *
     * @param dv the vector to move to
     * @return {@code this} (after modification)
     */
    public BlockArea setPosition(Vector2ic dv) {
        return setPosition(dv.x(), dv.y());
    }
    // ---------------------------------------------------------------------------------------------------------------//

    /**
     * Expand this area by adding the given {@code extents} for each face of the area.
     *
     * @param dx the amount of blocks to extend this area by along the x axis in both directions
     * @param dy the amount of blocks to extend this area by along the y axis in both directions
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if extending this area would result in any non-positive dimension
     */
    public BlockArea expand(int dx, int dy) {
        return this.set(minX - dx, minY - dy, maxX + dx, maxY + dy);
    }

    /**
     * Expand this area by adding the given {@code extents} for each face of a area.
     *
     * @param dv the amount of blocks to expand this area by
     * @return {@code this} (after modification)
     * @throws IllegalArgumentException if extending this area would result in any non-positive dimension
     */
    public BlockArea expand(Vector2ic dv) {
        return expand(dv.x(), dv.y());
    }

    // ---------------------------------------------------------------------------------------------------------------//

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
}
