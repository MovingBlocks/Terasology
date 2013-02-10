package org.terasology.math;

import javax.vecmath.Vector3f;
import java.util.EnumMap;

/**
 * @author Immortius
 */
public enum Direction {
    UP(Vector3i.up(), new Vector3f(0, 1, 0)),
    RIGHT(new Vector3i(-1, 0, 0), new Vector3f(-1, 0, 0)),
    LEFT(new Vector3i(1, 0, 0), new Vector3f(1, 0, 0)),
    BACKWARD(new Vector3i(0, 0, -1), new Vector3f(0, 0, -1)),
    FORWARD(new Vector3i(0, 0, 1), new Vector3f(0, 0, 1)),
    DOWN(Vector3i.down(), new Vector3f(0, -1, 0));

    private static EnumMap<Direction, Direction> reverseMap;

    static {
        reverseMap = new EnumMap<Direction, Direction>(Direction.class);
        reverseMap.put(UP, DOWN);
        reverseMap.put(LEFT, RIGHT);
        reverseMap.put(RIGHT, LEFT);
        reverseMap.put(FORWARD, BACKWARD);
        reverseMap.put(BACKWARD, FORWARD);
        reverseMap.put(DOWN, UP);
    }


    public static Direction inDirection(int x, int y, int z) {
        if (TeraMath.fastAbs(x) > TeraMath.fastAbs(y)) {
            if (TeraMath.fastAbs(x) > TeraMath.fastAbs(z)) {
                return (x > 0) ? LEFT : RIGHT;
            }
        } else if (TeraMath.fastAbs(y) > TeraMath.fastAbs(z)) {
            return (y > 0) ? UP : DOWN;
        }
        return (z > 0) ? FORWARD : BACKWARD;
    }

    public static Direction inDirection(Vector3f dir) {
        return inDirection(dir.x, dir.y, dir.z);
    }

    /**
     * Determines which direction the player is facing
     *
     * @param x right/left
     * @param y top/bottom
     * @param z back/front
     * @return Side enum with the appropriate direction
     */
    public static Direction inDirection(float x, float y, float z) {
        if (TeraMath.fastAbs(x) > TeraMath.fastAbs(y)) {
            if (TeraMath.fastAbs(x) > TeraMath.fastAbs(z)) {
                return (x > 0) ? LEFT : RIGHT;
            }
        } else if (TeraMath.fastAbs(y) > TeraMath.fastAbs(z)) {
            return (y > 0) ? UP : DOWN;
        }
        return (z > 0) ? FORWARD : BACKWARD;
    }

    /**
     * Determines which horizontal direction the player is facing
     *
     * @param x right/left
     * @param z back/front
     * @return Side enum with the appropriate direction
     */
    public static Direction inHorizontalDirection(float x, float z) {
        if (TeraMath.fastAbs(x) > TeraMath.fastAbs(z)) {
            return (x > 0) ? LEFT : RIGHT;
        }
        return (z > 0) ? FORWARD : BACKWARD;
    }

    private Vector3i vector3iDir;
    private Vector3f vector3fDir;

    Direction(Vector3i vector3i, Vector3f vector3f) {
        this.vector3iDir = vector3i;
        this.vector3fDir = vector3f;
    }

    /**
     * @return The vector3i in the direction of the side. Do not modify.
     */
    public Vector3i getVector3i() {
        return new Vector3i(vector3iDir);
    }

    public Vector3f getVector3f() {
        return new Vector3f(vector3fDir);
    }

    /**
     * @return The opposite side to this side.
     */
    public Direction reverse() {
        return reverseMap.get(this);
    }

}
