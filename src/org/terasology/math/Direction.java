package org.terasology.math;

import javax.vecmath.Vector3f;
import java.util.EnumMap;

/**
 * Direction Enum. Provides the ability to work in terms of 3D Cardinal directions, iterating, rotating and reversing them.
 * @author Immortius <immortius@gmail.com>
 */
public enum Direction {
    North   (Vector3i.north(), new Vector3f(0,0,1), 0),
    East    (Vector3i.east(), new Vector3f(-1,0,0), 1),
    South   (Vector3i.south(), new Vector3f(0,0,-1), 2),
    West    (Vector3i.west(), new Vector3f(1,0,0), 3),
    Up      (Vector3i.up(), new Vector3f(0,1,0), 4),
    Down    (Vector3i.down(), new Vector3f(0,-1,0), 5);

    private static EnumMap<Direction,Direction> oppositeMap = new EnumMap<Direction, Direction>(Direction.class);
    private static EnumMap<Direction,Direction> clockwiseMap = new EnumMap<Direction,Direction>(Direction.class);

    private final Vector3i vector;
    private final Vector3f vector3f;
    private final int index;

    static
    {
        setupOpposites();
        setupClockwise();
    }

    private static void setupOpposites()
    {
        oppositeMap.put(North, South);
        oppositeMap.put(East, West);
        oppositeMap.put(South, North);
        oppositeMap.put(West, East);
        oppositeMap.put(Up, Down);
        oppositeMap.put(Down, Up);
    }

    private static void setupClockwise()
    {
        clockwiseMap.put(North, East);
        clockwiseMap.put(East, South);
        clockwiseMap.put(South, West);
        clockwiseMap.put(West, North);
        clockwiseMap.put(Up, Up);
        clockwiseMap.put(Down, Down);
    }

    Direction(Vector3i v, Vector3f vf, int index)
    {
        this.vector = v;
        this.vector3f = vf;
        this.index = index;
    }

    /**
     * @return The unit vector3i associated with this direction
     */
    public Vector3i toVector3i()
    {
        return vector;
    }

    /**
     * @return The unit vector3f associated with this direction
     */
    public Vector3f toVector3f()
    {
        return vector3f;
    }

    /**
     * @return The index of this direction
     */
    public int toIndex()
    {
        return index;
    }

    /**
     * @return The reverse of this direction
     */
    public Direction reverse()
    {
        return oppositeMap.get(this);
    }

    /**
     * @return The clockwise (facing Down-ward) direction from this direction
     */
    public Direction rotateClockwise()
    {
        return clockwiseMap.get(this);
    }

    /**
     * @return The clockwise (facing Down-ward) direction from this direction
     */
    public Direction rotateClockwise(int steps)
    {
        steps = steps % 4;
        Direction result = this;
        for (int i = 0; i < steps; ++i)
        {
            result = result.rotateClockwise();
        }
        return result;
    }
}
