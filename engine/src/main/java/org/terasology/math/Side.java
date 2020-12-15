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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * The six sides of a block and a slew of related utility.
 * <br><br>
 * Note that the FRONT of the block faces towards the player - this means Left and Right are a player's right and left.
 * See Direction for an enumeration of directions in terms of the player's perspective.
 *
 */
public enum Side {
    TOP(Vector3i.up(), true, false, true),
    BOTTOM(Vector3i.down(), true, false, true),
    LEFT(new Vector3i(-1, 0, 0), false, true, true),
    RIGHT(new Vector3i(1, 0, 0), false, true, true),
    FRONT(new Vector3i(0, 0, -1), true, true, false),
    BACK(new Vector3i(0, 0, 1), true, true, false);

    private static final EnumSet<Side> ALL_SIDES = EnumSet.allOf(Side.class);

    private static final EnumMap<Side, Side> REVERSE_MAP;
    private static final ImmutableList<Side> HORIZONTAL_SIDES;
    private static final ImmutableList<Side> VERTICAL_SIDES;
    private static final EnumMap<Side, Side> CLOCKWISE_YAW_SIDE;
    private static final EnumMap<Side, Side> ANTICLOCKWISE_YAW_SIDE;
    private static final EnumMap<Side, Side> CLOCKWISE_PITCH_SIDE;
    private static final EnumMap<Side, Side> ANTICLOCKWISE_PITCH_SIDE;
    private static final EnumMap<Side, Side> CLOCKWISE_ROLL_SIDE;
    private static final EnumMap<Side, Side> ANTICLOCKWISE_ROLL_SIDE;
    private static final EnumMap<Side, Direction> CONVERSION_MAP;
    private static final EnumMap<Side, ImmutableList<Side>> TANGENTS;

    static {
        TANGENTS = new EnumMap<>(Side.class);
        TANGENTS.put(TOP, ImmutableList.of(LEFT, RIGHT, FRONT, BACK));
        TANGENTS.put(BOTTOM, ImmutableList.of(LEFT, RIGHT, FRONT, BACK));
        TANGENTS.put(LEFT, ImmutableList.of(TOP, BOTTOM, FRONT, BACK));
        TANGENTS.put(RIGHT, ImmutableList.of(TOP, BOTTOM, FRONT, BACK));
        TANGENTS.put(FRONT, ImmutableList.of(TOP, BOTTOM, LEFT, RIGHT));
        TANGENTS.put(BACK, ImmutableList.of(TOP, BOTTOM, LEFT, RIGHT));

        REVERSE_MAP = new EnumMap<>(Side.class);
        REVERSE_MAP.put(TOP, BOTTOM);
        REVERSE_MAP.put(LEFT, RIGHT);
        REVERSE_MAP.put(RIGHT, LEFT);
        REVERSE_MAP.put(FRONT, BACK);
        REVERSE_MAP.put(BACK, FRONT);
        REVERSE_MAP.put(BOTTOM, TOP);

        CONVERSION_MAP = new EnumMap<>(Side.class);
        CONVERSION_MAP.put(TOP, Direction.UP);
        CONVERSION_MAP.put(BOTTOM, Direction.DOWN);
        CONVERSION_MAP.put(BACK, Direction.FORWARD);
        CONVERSION_MAP.put(FRONT, Direction.BACKWARD);
        CONVERSION_MAP.put(RIGHT, Direction.LEFT);
        CONVERSION_MAP.put(LEFT, Direction.RIGHT);

        CLOCKWISE_YAW_SIDE = new EnumMap<>(Side.class);
        ANTICLOCKWISE_YAW_SIDE = new EnumMap<>(Side.class);
        CLOCKWISE_YAW_SIDE.put(Side.FRONT, Side.LEFT);
        ANTICLOCKWISE_YAW_SIDE.put(Side.FRONT, Side.RIGHT);
        CLOCKWISE_YAW_SIDE.put(Side.RIGHT, Side.FRONT);
        ANTICLOCKWISE_YAW_SIDE.put(Side.RIGHT, Side.BACK);
        CLOCKWISE_YAW_SIDE.put(Side.BACK, Side.RIGHT);
        ANTICLOCKWISE_YAW_SIDE.put(Side.BACK, Side.LEFT);
        CLOCKWISE_YAW_SIDE.put(Side.LEFT, Side.BACK);
        ANTICLOCKWISE_YAW_SIDE.put(Side.LEFT, Side.FRONT);

        CLOCKWISE_PITCH_SIDE = Maps.newEnumMap(Side.class);
        ANTICLOCKWISE_PITCH_SIDE = Maps.newEnumMap(Side.class);
        CLOCKWISE_PITCH_SIDE.put(Side.FRONT, Side.TOP);
        ANTICLOCKWISE_PITCH_SIDE.put(Side.FRONT, Side.BOTTOM);
        CLOCKWISE_PITCH_SIDE.put(Side.BOTTOM, Side.FRONT);
        ANTICLOCKWISE_PITCH_SIDE.put(Side.BOTTOM, Side.BACK);
        CLOCKWISE_PITCH_SIDE.put(Side.BACK, Side.BOTTOM);
        ANTICLOCKWISE_PITCH_SIDE.put(Side.BACK, Side.TOP);
        CLOCKWISE_PITCH_SIDE.put(Side.TOP, Side.BACK);
        ANTICLOCKWISE_PITCH_SIDE.put(Side.TOP, Side.FRONT);

        CLOCKWISE_ROLL_SIDE = Maps.newEnumMap(Side.class);
        ANTICLOCKWISE_ROLL_SIDE = Maps.newEnumMap(Side.class);
        CLOCKWISE_ROLL_SIDE.put(Side.TOP, Side.LEFT);
        ANTICLOCKWISE_ROLL_SIDE.put(Side.TOP, Side.RIGHT);
        CLOCKWISE_ROLL_SIDE.put(Side.LEFT, Side.BOTTOM);
        ANTICLOCKWISE_ROLL_SIDE.put(Side.LEFT, Side.TOP);
        CLOCKWISE_ROLL_SIDE.put(Side.BOTTOM, Side.RIGHT);
        ANTICLOCKWISE_ROLL_SIDE.put(Side.BOTTOM, Side.LEFT);
        CLOCKWISE_ROLL_SIDE.put(Side.RIGHT, Side.TOP);
        ANTICLOCKWISE_ROLL_SIDE.put(Side.RIGHT, Side.BOTTOM);

        HORIZONTAL_SIDES = ImmutableList.of(LEFT, RIGHT, FRONT, BACK);
        VERTICAL_SIDES = ImmutableList.of(TOP, BOTTOM);
    }

    private final Vector3i vector3iDir;
    private final boolean canYaw;
    private final boolean canPitch;
    private final boolean canRoll;

    Side(Vector3i vector3i, boolean canPitch, boolean canYaw, boolean canRoll) {
        this.vector3iDir = vector3i;
        this.canPitch = canPitch;
        this.canYaw = canYaw;
        this.canRoll = canRoll;
    }

    /**
     * @return The horizontal sides, for iteration
     */
    public static ImmutableList<Side> horizontalSides() {
        return HORIZONTAL_SIDES;
    }

    /**
     * @return The vertical sides, for iteration
     */
    public static ImmutableList<Side> verticalSides() {
        return VERTICAL_SIDES;
    }

    public static Side inDirection(int x, int y, int z) {
        if (TeraMath.fastAbs(x) > TeraMath.fastAbs(y)) {
            if (TeraMath.fastAbs(x) > TeraMath.fastAbs(z)) {
                return (x > 0) ? RIGHT : LEFT;
            }
        } else if (TeraMath.fastAbs(y) > TeraMath.fastAbs(z)) {
            return (y > 0) ? TOP : BOTTOM;
        }
        return (z > 0) ? BACK : FRONT;
    }

    /**
     * @param dir
     * @return
     * @deprecated This method is scheduled for removal in an upcoming version. Use the JOML implementation instead:
     *     {@link #inDirection(Vector3fc)}.
     */
    @Deprecated
    public static Side inDirection(Vector3f dir) {
        return inDirection(dir.x, dir.y, dir.z);
    }

    /**
     * The side normal closes to dir
     * 
     * @param dir direction
     * @return side
     */
    public static Side inDirection(Vector3fc dir) {
        return inDirection(dir.x(), dir.y(), dir.z());
    }

    /**
     * Determines which direction the player is facing
     *
     * @param x right/left
     * @param y top/bottom
     * @param z back/front
     * @return Side enum with the appropriate direction
     */
    public static Side inDirection(double x, double y, double z) {
        if (TeraMath.fastAbs(x) > TeraMath.fastAbs(y)) {
            if (TeraMath.fastAbs(x) > TeraMath.fastAbs(z)) {
                return (x > 0) ? RIGHT : LEFT;
            }
        } else if (TeraMath.fastAbs(y) > TeraMath.fastAbs(z)) {
            return (y > 0) ? TOP : BOTTOM;
        }
        return (z > 0) ? BACK : FRONT;
    }

    /**
     * Determines which horizontal direction the player is facing
     *
     * @param x right/left
     * @param z back/front
     * @return Side enum with the appropriate direction
     */
    public static Side inHorizontalDirection(double x, double z) {
        if (TeraMath.fastAbs(x) > TeraMath.fastAbs(z)) {
            return (x > 0) ? RIGHT : LEFT;
        }
        return (z > 0) ? BACK : FRONT;
    }

    /**
     * This provides a static EnumSet of all Sides defined in the enumeration. The result contains the same values as
     * calling {@code Side#values} but this does not create a new copy on every call. <br/>
     * <b>Warning:</b> Do not change the content of the returned enum set! It will be reflected on all calls to this
     * method.
     *
     * @return All available sides
     */
    public static EnumSet<Side> getAllSides() {
        return ALL_SIDES;
    }

    /**
     * @return The vector3i in the direction of the side. Do not modify.
     * @deprecated This method is scheduled for removal in an upcoming version. Use the JOML implementation instead:
     *     {@link #direction()} .
     */
    @Deprecated
    public Vector3i getVector3i() {
        return vector3iDir;
    }

    /**
     * the normal vector in the direction of the side
     *
     * @return a normalized vector
     */
    public Vector3ic direction() {
        return JomlUtil.from(vector3iDir);
    }

    /**
     * @return Whether this is one of the horizontal directions (LEFT, FRONT, RIGHT, BACK).
     */
    public boolean isHorizontal() {
        return canYaw;
    }

    /**
     * @return Whether this is one of the vertical directions (TOP, BOTTOM).
     */
    public boolean isVertical() {
        return !canYaw;
    }

    /**
     * @return The opposite side to this side.
     */
    public Side reverse() {
        return REVERSE_MAP.get(this);
    }

    public Side yawClockwise(int turns) {
        if (!canYaw) {
            return this;
        }
        int steps = turns;
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                return CLOCKWISE_YAW_SIDE.get(this);
            case 2:
                return REVERSE_MAP.get(this);
            case 3:
                return ANTICLOCKWISE_YAW_SIDE.get(this);
            default:
                return this;
        }
    }

    public Side pitchClockwise(int turns) {
        if (!canPitch) {
            return this;
        }
        int steps = turns;
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                return CLOCKWISE_PITCH_SIDE.get(this);
            case 2:
                return REVERSE_MAP.get(this);
            case 3:
                return ANTICLOCKWISE_PITCH_SIDE.get(this);
            default:
                return this;
        }
    }

    public Direction toDirection() {
        return CONVERSION_MAP.get(this);
    }

    public Side rollClockwise(int turns) {
        if (!canRoll) {
            return this;
        }
        int steps = turns;
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                return CLOCKWISE_ROLL_SIDE.get(this);
            case 2:
                return REVERSE_MAP.get(this);
            case 3:
                return ANTICLOCKWISE_ROLL_SIDE.get(this);
            default:
                return this;
        }
    }

    /**
     * @param position
     * @return
     * @deprecated This method is scheduled for removal in an upcoming version. Use the JOML implementation instead:
     *     {@link #getAdjacentPos(Vector3ic, org.joml.Vector3i)} .
     **/
    @Deprecated
    public Vector3i getAdjacentPos(Vector3i position) {
        Vector3i result = new Vector3i(position);
        result.add(vector3iDir);
        return result;
    }

    /**
     * take the current pos and add the direction
     *
     * @param pos current position
     * @param dest will hold the result
     * @return dest
     */
    public org.joml.Vector3i getAdjacentPos(Vector3ic pos, org.joml.Vector3i dest) {
        return dest.set(pos).add(direction());
    }

    public Side getRelativeSide(Direction direction) {
        if (direction == Direction.UP) {
            return pitchClockwise(1);
        } else if (direction == Direction.DOWN) {
            return pitchClockwise(-1);
        } else if (direction == Direction.LEFT) {
            return yawClockwise(1);
        } else if (direction == Direction.RIGHT) {
            return yawClockwise(-1);
        } else if (direction == Direction.BACKWARD) {
            return reverse();
        } else {
            return this;
        }
    }

    public Iterable<Side> tangents() {
        return TANGENTS.get(this);
    }
}
