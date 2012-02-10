package org.terasology.math;

import org.terasology.model.blocks.Block;

import java.util.EnumMap;

/**
 * The six sides of a block.
 */
public enum Side {
    TOP(Vector3i.up()),
    LEFT(new Vector3i(-1,0,0)),
    RIGHT(new Vector3i(1,0,0)),
    FRONT(new Vector3i(0,0,-1)),
    BACK(new Vector3i(0,0,1)),
    BOTTOM(Vector3i.down());

    private static EnumMap<Side, Side> reverseMap;
    private static Side[] horizontalSides;

    static
    {
        reverseMap = new EnumMap<Side, Side>(Side.class);
        reverseMap.put(TOP, BOTTOM);
        reverseMap.put(LEFT, RIGHT);
        reverseMap.put(RIGHT, LEFT);
        reverseMap.put(FRONT, BACK);
        reverseMap.put(BACK, FRONT);
        reverseMap.put(BOTTOM, TOP);
        horizontalSides = new Side[] {LEFT, RIGHT, FRONT, BACK};
    }

    /**
     *
     * @return The horizontal sides, for iteration
     */
    public static Side[] horizontalSides()
    {
        return horizontalSides;
    }

    private Vector3i vector3iDir;

    Side(Vector3i vector3i)
    {
        this.vector3iDir = vector3i;
    }

    /**
     *
     * @return The vector3i in the direction of the side. Do not modify.
     */
    public Vector3i getVector3i()
    {
        return vector3iDir;
    }

    /**
     *
     * @return The opposite side to this side.
     */
    public Side reverse()
    {
        return reverseMap.get(this);
    }
}
