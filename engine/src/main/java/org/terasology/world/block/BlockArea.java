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

    @Override
    public BlockArea minX(int x, BlockArea dest) {
        return dest.set(x, minY, maxX, maxY);
    }

    public BlockArea minX(int x) {
        return minX(x, this);
    }

    @Override
    public BlockArea minY(int y, BlockArea dest) {
        return dest.set(minX, y, maxX, maxY);
    }

    public BlockArea minY(int y) {
        return minY(y, this);
    }

    @Override
    public BlockArea setMin(int x, int y, BlockArea dest) {
        return dest.set(x, y, maxX, maxY);
    }

    public BlockArea setMin(int x, int y) {
        return this.setMin(x, y, this);
    }

    public BlockArea setMin(Vector2ic min) {
        return this.setMin(min.x(), min.y(), this);
    }

    public BlockArea addToMin(int x, int y) {
        return this.addToMin(x, y, this);
    }

    public BlockArea addToMin(Vector2ic min) {
        return this.addToMin(min.x(), min.y(), this);
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

    @Override
    public BlockArea maxX(int x, BlockArea dest) {
        return dest.set(minX, minY, x, maxY);
    }

    public BlockArea maxX(int x) {
        return maxX(x, this);
    }

    @Override
    public BlockArea maxY(int y, BlockArea dest) {
        return dest.set(minX, minY, maxX, y);
    }

    public BlockArea maxY(int y) {
        return maxY(y, this);
    }

    @Override
    public BlockArea setMax(int x, int y, BlockArea dest) {
        return dest.set(minX, minY, x, y);
    }

    public BlockArea setMax(int x, int y) {
        return this.setMax(x, y, this);
    }

    public BlockArea setMax(Vector2ic max) {
        return this.setMax(max.x(), max.y(), this);
    }

    public BlockArea addToMax(int x, int y) {
        return this.addToMax(x, y, this);
    }

    public BlockArea addToMax(Vector2ic max) {
        return this.addToMax(max.x(), max.y(), this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public BlockArea setSize(int sizeX, int sizeY, BlockArea dest) {
        return dest.set(minX, minY, minX + sizeX - 1, minY + sizeY - 1);
    }

    public BlockArea setSize(int sizeX, int sizeY) {
        return this.setSize(sizeX, sizeY, this);
    }

    public BlockArea setSize(Vector2ic size) {
        return this.setSize(size.x(), size.y(), this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public BlockArea union(int x, int y, BlockArea dest) {
        return dest.set(
                Math.min(this.minX, x), Math.min(this.minY, y),
                Math.max(this.maxX, x), Math.max(this.maxY, y));
    }

    public BlockArea union(int x, int y) {
        return union(x, y, this);
    }

    public BlockArea union(Vector2ic p) {
        return union(p.x(), p.y(), this);
    }

    public BlockArea union(BlockArea other) {
        return union(other, this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public Optional<BlockArea> intersect(BlockAreac other, BlockArea dest) {
        dest.minX = Math.max(minX, other.minX());
        dest.minY = Math.max(minY, other.minY());

        dest.maxX = Math.min(maxX, other.maxX());
        dest.maxY = Math.min(maxY, other.maxY());

        if (dest.isValid()) {
            return Optional.of(dest);
        } else {
            return Optional.empty();
        }
    }

    public Optional<BlockArea> intersect(BlockAreac other) {
        return this.intersect(other, this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public BlockArea translate(int dx, int dy, BlockArea dest) {
        dest.minX = minX + dx;
        dest.minY = minY + dy;
        dest.maxX = maxX + dx;
        dest.maxY = maxY + dy;
        return dest;
    }

    public BlockArea translate(int dx, int dy) {
        return translate(dx, dy, this);
    }

    public BlockArea translate(Vector2ic dv) {
        return translate(dv.x(), dv.y(), this);
    }

    public BlockArea setPosition(int x, int y) {
        return setPosition(x, y, this);
    }

    public BlockArea setPosition(Vector2i pos) {
        return setPosition(pos.x(), pos.y(), this);
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Override
    public BlockArea expand(int dx, int dy, BlockArea dest) {
        return dest.set(minX - dx, minY - dy, maxX + dx, maxY + dy);
    }

    public BlockArea expand(int dx, int dy) {
        return expand(dx, dy, this);
    }

    public BlockArea expand(Vector2ic dv) {
        return expand(dv.x(), dv.y(), this);
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
