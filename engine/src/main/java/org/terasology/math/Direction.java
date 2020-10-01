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
import org.joml.Vector3fc;
import org.joml.Vector3ic;
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

    private static final EnumMap<Direction, Direction> REVERSE_MAP;
    private static final EnumMap<Direction, Side> CONVERSION_MAP;

    private final Vector3i vector3iDir;
    private final Vector3f vector3fDir;

    static {
        REVERSE_MAP = new EnumMap<>(Direction.class);
        REVERSE_MAP.put(UP, DOWN);
        REVERSE_MAP.put(LEFT, RIGHT);
        REVERSE_MAP.put(RIGHT, LEFT);
        REVERSE_MAP.put(FORWARD, BACKWARD);
        REVERSE_MAP.put(BACKWARD, FORWARD);
        REVERSE_MAP.put(DOWN, UP);
        CONVERSION_MAP = Maps.newEnumMap(Direction.class);
        CONVERSION_MAP.put(UP, Side.TOP);
        CONVERSION_MAP.put(DOWN, Side.BOTTOM);
        CONVERSION_MAP.put(FORWARD, Side.BACK);
        CONVERSION_MAP.put(BACKWARD, Side.FRONT);
        CONVERSION_MAP.put(LEFT, Side.RIGHT);
        CONVERSION_MAP.put(RIGHT, Side.LEFT);
    }

    Direction(Vector3i vector3i, Vector3f vector3f) {
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
        return CONVERSION_MAP.get(this);
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
     * readonly normalized {@link Vector3ic} in the given {@link Direction}
     *
     * @return vector pointing in the direction
     */
    public Vector3ic asVector3i() {
        return JomlUtil.from(vector3iDir);
    }


    /**
     * readonly normalized {@link Vector3fc} in the given {@link Direction}
     *
     * @return vector pointing in the direction
     */
    public Vector3fc asVector3f() {
        return JomlUtil.from(vector3fDir);
    }

    /**
     * @return The vector3i in the direction of the side. Do not modify.
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #asVector3i()}.
     */
    public Vector3i getVector3i() {
        return new Vector3i(vector3iDir);
    }

    /**
     * @return
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #asVector3f()}
     */
    public Vector3f getVector3f() {
        return new Vector3f(vector3fDir);
    }

    /**
     * @return The opposite side to this side.
     */
    public Direction reverse() {
        return REVERSE_MAP.get(this);
    }

}
