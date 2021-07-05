// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import com.google.common.collect.ImmutableList;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;

import java.util.List;

public enum BlockPart {
    TOP(Side.TOP),
    LEFT(Side.LEFT),
    RIGHT(Side.RIGHT),
    FRONT(Side.FRONT),
    BACK(Side.BACK),
    BOTTOM(Side.BOTTOM),
    CENTER;

    private static final List<BlockPart> HORIZONTAL_SIDES = ImmutableList.of(LEFT, RIGHT, FRONT, BACK);
    private static final List<BlockPart> SIDES = ImmutableList.of(TOP, LEFT, RIGHT, FRONT, BACK, BOTTOM, CENTER);
    private Side side;

    BlockPart() {
    }

    BlockPart(Side side) {
        this.side = side;
    }

    public static BlockPart fromSide(Side side) {
        switch (side) {
            case BOTTOM:
                return BOTTOM;
            case RIGHT:
                return RIGHT;
            case LEFT:
                return LEFT;
            case BACK:
                return BACK;
            case FRONT:
                return FRONT;
            case TOP:
                return TOP;
            default:
                throw new IllegalArgumentException();
        }
    }
    /**
     * @return The horizontal sides
     */
    public static List<BlockPart> getHorizontal() {
        return HORIZONTAL_SIDES;
    }

    public static List<BlockPart> getAll() {
        return SIDES;
    }

    public boolean isSide() {
        return side != null;
    }

    public Side getSide() {
        return side;
    }

    public BlockPart rotate(Rotation rot) {
        if (isSide()) {
            return BlockPart.fromSide(rot.rotate(getSide()));
        }
        return this;
    }
}
