// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block;

import com.google.common.collect.ImmutableList;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;

import java.util.EnumMap;
import java.util.List;

/**
 */
public enum BlockPart {
    TOP(Side.TOP),
    LEFT(Side.LEFT),
    RIGHT(Side.RIGHT),
    FRONT(Side.FRONT),
    BACK(Side.BACK),
    BOTTOM(Side.BOTTOM),
    CENTER;

    private static final EnumMap<Side, BlockPart> SIDE_MAP;
    private static final BlockPart[] SIDES;
    private static final List<BlockPart> HORIZONTAL_SIDES;

    private Side side;

    BlockPart() {
    }

    BlockPart(Side side) {
        this.side = side;
    }

    static {
        SIDE_MAP = new EnumMap<>(Side.class);
        SIDE_MAP.put(Side.BOTTOM, BOTTOM);
        SIDE_MAP.put(Side.RIGHT, RIGHT);
        SIDE_MAP.put(Side.LEFT, LEFT);
        SIDE_MAP.put(Side.BACK, BACK);
        SIDE_MAP.put(Side.FRONT, FRONT);
        SIDE_MAP.put(Side.TOP, TOP);

        SIDES = new BlockPart[]{TOP, LEFT, RIGHT, FRONT, BACK, BOTTOM};

        HORIZONTAL_SIDES = ImmutableList.of(LEFT, RIGHT, FRONT, BACK);
    }

    public static BlockPart fromSide(Side side) {
        return SIDE_MAP.get(side);
    }

    /**
     * @return The block parts corresponding to sides (so not the center)
     */
    public static BlockPart[] sideValues() {
        return SIDES;
    }

    /**
     * @return The horizontal sides
     */
    public static List<BlockPart> horizontalSides() {
        return HORIZONTAL_SIDES;
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
