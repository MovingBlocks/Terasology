/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.math;

import com.google.common.collect.Maps;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.EnumMap;

/**
 * An enumeration of the axis of the world from the player perspective. There is also
 *
 */
public enum Direction {
    UP(Vector3i.up(), new Vector3f(0, 1, 0)),
    RIGHT(new Vector3i(-1, 0, 0), new Vector3f(-1, 0, 0)),
    LEFT(new Vector3i(1, 0, 0), new Vector3f(1, 0, 0)),
    BACKWARD(new Vector3i(0, 0, -1), new Vector3f(0, 0, -1)),
    FORWARD(new Vector3i(0, 0, 1), new Vector3f(0, 0, 1)),
    DOWN(Vector3i.down(), new Vector3f(0, -1, 0));

    private static EnumMap<Direction, Direction> reverseMap;
    private static EnumMap<Direction, Side> conversionMap;

    private Vector3i vector3iDir;
    private Vector3f vector3fDir;

    static {
        reverseMap = new EnumMap<>(Direction.class);
        reverseMap.put(UP, DOWN);
        reverseMap.put(LEFT, RIGHT);
        reverseMap.put(RIGHT, LEFT);
        reverseMap.put(FORWARD, BACKWARD);
        reverseMap.put(BACKWARD, FORWARD);
        reverseMap.put(DOWN, UP);
        conversionMap = Maps.newEnumMap(Direction.class);
        conversionMap.put(UP, Side.TOP);
        conversionMap.put(DOWN, Side.BOTTOM);
        conversionMap.put(FORWARD, Side.BACK);
        conversionMap.put(BACKWARD, Side.FRONT);
        conversionMap.put(LEFT, Side.RIGHT);
        conversionMap.put(RIGHT, Side.LEFT);
    }

    private Direction(Vector3i vector3i, Vector3f vector3f) {
        this.vector3iDir = vector3i;
        this.vector3fDir = vector3f;
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

    public Side toSide() {
        return conversionMap.get(this);
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
