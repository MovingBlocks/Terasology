package org.terasology.math;

import org.terasology.model.blocks.Block;

import java.util.EnumMap;

/**
 * The six sides of a block.
 */
public enum Side {
    TOP(Vector3i.up(), false),
    LEFT(new Vector3i(-1, 0, 0), true),
    RIGHT(new Vector3i(1, 0, 0), true),
    FRONT(new Vector3i(0, 0, -1), true),
    BACK(new Vector3i(0, 0, 1), true),
    BOTTOM(Vector3i.down(), false);

    private static EnumMap<Side, Side> reverseMap;
    private static Side[] horizontalSides;

    static {
        reverseMap = new EnumMap<Side, Side>(Side.class);
        reverseMap.put(TOP, BOTTOM);
        reverseMap.put(LEFT, RIGHT);
        reverseMap.put(RIGHT, LEFT);
        reverseMap.put(FRONT, BACK);
        reverseMap.put(BACK, FRONT);
        reverseMap.put(BOTTOM, TOP);
        horizontalSides = new Side[]{LEFT, RIGHT, FRONT, BACK};
    }

    /**
     * @return The horizontal sides, for iteration
     */
    public static Side[] horizontalSides() {
        return horizontalSides;
    }

    private Vector3i vector3iDir;
    private boolean horizontal;

    Side(Vector3i vector3i, boolean horizontal) {
        this.vector3iDir = vector3i;
        this.horizontal = horizontal;
    }

    /**
     * @return The vector3i in the direction of the side. Do not modify.
     */
    public Vector3i getVector3i() {
        return vector3iDir;
    }

    /**
     * @return Whether this is one of the horizontal directions.
     */
    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     * @return The opposite side to this side.
     */
    public Side reverse() {
        return reverseMap.get(this);
    }
}
