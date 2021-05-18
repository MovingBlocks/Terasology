// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import com.google.common.collect.Maps;
import net.logstash.logback.encoder.org.apache.commons.lang.UnhandledException;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.EnumMap;

/**
 * An enumeration of the axis of the world from the player perspective. There is also
 *
 */
public enum Direction {
    UP(new Vector3i(0, 1, 0), new Vector3f(0, 1, 0)),
    RIGHT(new Vector3i(-1, 0, 0), new Vector3f(-1, 0, 0)),
    LEFT(new Vector3i(1, 0, 0), new Vector3f(1, 0, 0)),
    BACKWARD(new Vector3i(0, 0, -1), new Vector3f(0, 0, -1)),
    FORWARD(new Vector3i(0, 0, 1), new Vector3f(0, 0, 1)),
    DOWN(new Vector3i(0, -1, 0), new Vector3f(0, -1, 0));

    private final Vector3i vector3iDir;
    private final Vector3f vector3fDir;

    Direction(Vector3i vector3i, Vector3f vector3f) {
        this.vector3iDir = vector3i;
        this.vector3fDir = vector3f;
    }

    public static Direction inDirection(int x, int y, int z) {
        if (Math.abs(x) > Math.abs(y)) {
            if (Math.abs(x) > Math.abs(z)) {
                return (x > 0) ? LEFT : RIGHT;
            }
        } else if (Math.abs(y) > Math.abs(z)) {
            return (y > 0) ? UP : DOWN;
        }
        return (z > 0) ? FORWARD : BACKWARD;
    }

    public static Direction inDirection(Vector3fc dir) {
        return inDirection(dir.x(), dir.y(), dir.z());
    }

    public Side toSide() {
        switch (this) {
            case UP:
                return Side.TOP;
            case DOWN:
                return Side.BOTTOM;
            case FORWARD:
                return Side.BACK;
            case BACKWARD:
                return Side.FRONT;
            case LEFT:
                return Side.RIGHT;
            case RIGHT:
                return Side.LEFT;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
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
        if (Math.abs(x) > Math.abs(y)) {
            if (Math.abs(x) > Math.abs(z)) {
                return (x > 0) ? LEFT : RIGHT;
            }
        } else if (Math.abs(y) > Math.abs(z)) {
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
        if (Math.abs(x) > Math.abs(z)) {
            return (x > 0) ? LEFT : RIGHT;
        }
        return (z > 0) ? FORWARD : BACKWARD;
    }


    /**
     * readonly normalized {@link Vector3ic} in the given {@link Direction}
     *
     * @return vector pointing in the direction
     */
    public Vector3ic asVector3i() {
        return vector3iDir;
    }

    /**
     * readonly normalized {@link Vector3fc} in the given {@link Direction}
     *
     * @return vector pointing in the direction
     */
    public Vector3fc asVector3f() {
        return vector3fDir;
    }

    /**
     * @return The opposite side to this side.
     */
    public Direction reverse() {
        switch (this) {
            case RIGHT:
                return LEFT;
            case LEFT:
                return RIGHT;
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case FORWARD:
                return BACKWARD;
            case BACKWARD:
                return FORWARD;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

}
