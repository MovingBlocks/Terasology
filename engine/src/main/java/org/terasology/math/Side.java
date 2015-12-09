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
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.EnumMap;

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

    private static EnumMap<Side, Side> reverseMap;
    private static ImmutableList<Side> horizontalSides;
    private static ImmutableList<Side> verticalSides;
    private static EnumMap<Side, Side> clockwiseYawSide;
    private static EnumMap<Side, Side> anticlockwiseYawSide;
    private static EnumMap<Side, Side> clockwisePitchSide;
    private static EnumMap<Side, Side> anticlockwisePitchSide;
    private static EnumMap<Side, Side> clockwiseRollSide;
    private static EnumMap<Side, Side> anticlockwiseRollSide;
    private static EnumMap<Side, Direction> conversionMap;
    private static EnumMap<Side, ImmutableList<Side>> tangents;

    static {
        tangents = new EnumMap<>(Side.class);
        tangents.put(TOP, ImmutableList.of(LEFT, RIGHT, FRONT, BACK));
        tangents.put(BOTTOM, ImmutableList.of(LEFT, RIGHT, FRONT, BACK));
        tangents.put(LEFT, ImmutableList.of(TOP, BOTTOM, FRONT, BACK));
        tangents.put(RIGHT, ImmutableList.of(TOP, BOTTOM, FRONT, BACK));
        tangents.put(FRONT, ImmutableList.of(TOP, BOTTOM, LEFT, RIGHT));
        tangents.put(BACK, ImmutableList.of(TOP, BOTTOM, LEFT, RIGHT));

        reverseMap = new EnumMap<>(Side.class);
        reverseMap.put(TOP, BOTTOM);
        reverseMap.put(LEFT, RIGHT);
        reverseMap.put(RIGHT, LEFT);
        reverseMap.put(FRONT, BACK);
        reverseMap.put(BACK, FRONT);
        reverseMap.put(BOTTOM, TOP);

        conversionMap = new EnumMap<>(Side.class);
        conversionMap.put(TOP, Direction.UP);
        conversionMap.put(BOTTOM, Direction.DOWN);
        conversionMap.put(BACK, Direction.FORWARD);
        conversionMap.put(FRONT, Direction.BACKWARD);
        conversionMap.put(RIGHT, Direction.LEFT);
        conversionMap.put(LEFT, Direction.RIGHT);

        clockwiseYawSide = new EnumMap<>(Side.class);
        anticlockwiseYawSide = new EnumMap<>(Side.class);
        clockwiseYawSide.put(Side.FRONT, Side.LEFT);
        anticlockwiseYawSide.put(Side.FRONT, Side.RIGHT);
        clockwiseYawSide.put(Side.RIGHT, Side.FRONT);
        anticlockwiseYawSide.put(Side.RIGHT, Side.BACK);
        clockwiseYawSide.put(Side.BACK, Side.RIGHT);
        anticlockwiseYawSide.put(Side.BACK, Side.LEFT);
        clockwiseYawSide.put(Side.LEFT, Side.BACK);
        anticlockwiseYawSide.put(Side.LEFT, Side.FRONT);

        clockwisePitchSide = Maps.newEnumMap(Side.class);
        anticlockwisePitchSide = Maps.newEnumMap(Side.class);
        clockwisePitchSide.put(Side.FRONT, Side.TOP);
        anticlockwisePitchSide.put(Side.FRONT, Side.BOTTOM);
        clockwisePitchSide.put(Side.BOTTOM, Side.FRONT);
        anticlockwisePitchSide.put(Side.BOTTOM, Side.BACK);
        clockwisePitchSide.put(Side.BACK, Side.BOTTOM);
        anticlockwisePitchSide.put(Side.BACK, Side.TOP);
        clockwisePitchSide.put(Side.TOP, Side.BACK);
        anticlockwisePitchSide.put(Side.TOP, Side.FRONT);

        clockwiseRollSide = Maps.newEnumMap(Side.class);
        anticlockwiseRollSide = Maps.newEnumMap(Side.class);
        clockwiseRollSide.put(Side.TOP, Side.LEFT);
        anticlockwiseRollSide.put(Side.TOP, Side.RIGHT);
        clockwiseRollSide.put(Side.LEFT, Side.BOTTOM);
        anticlockwiseRollSide.put(Side.LEFT, Side.TOP);
        clockwiseRollSide.put(Side.BOTTOM, Side.RIGHT);
        anticlockwiseRollSide.put(Side.BOTTOM, Side.LEFT);
        clockwiseRollSide.put(Side.RIGHT, Side.TOP);
        anticlockwiseRollSide.put(Side.RIGHT, Side.BOTTOM);

        horizontalSides = ImmutableList.of(LEFT, RIGHT, FRONT, BACK);
        verticalSides = ImmutableList.of(TOP, BOTTOM);
    }

    private Vector3i vector3iDir;
    private boolean canYaw;
    private boolean canPitch;
    private boolean canRoll;

    private Side(Vector3i vector3i, boolean canPitch, boolean canYaw, boolean canRoll) {
        this.vector3iDir = vector3i;
        this.canPitch = canPitch;
        this.canYaw = canYaw;
        this.canRoll = canRoll;
    }

    /**
     * @return The horizontal sides, for iteration
     */
    public static ImmutableList<Side> horizontalSides() {
        return horizontalSides;
    }

    /**
     * @return The vertical sides, for iteration
     */
    public static ImmutableList<Side> verticalSides() {
        return verticalSides;
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

    public static Side inDirection(Vector3f dir) {
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
     * @return The vector3i in the direction of the side. Do not modify.
     */
    public Vector3i getVector3i() {
        return vector3iDir;
    }

    /**
     * @return Whether this is one of the horizontal directions.
     */
    public boolean isHorizontal() {
        return canYaw;
    }

    /**
     * @return The opposite side to this side.
     */
    public Side reverse() {
        return reverseMap.get(this);
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
                return clockwiseYawSide.get(this);
            case 2:
                return reverseMap.get(this);
            case 3:
                return anticlockwiseYawSide.get(this);
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
                return clockwisePitchSide.get(this);
            case 2:
                return reverseMap.get(this);
            case 3:
                return anticlockwisePitchSide.get(this);
            default:
                return this;
        }
    }

    public Direction toDirection() {
        return conversionMap.get(this);
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
                return clockwiseRollSide.get(this);
            case 2:
                return reverseMap.get(this);
            case 3:
                return anticlockwiseRollSide.get(this);
            default:
                return this;
        }
    }

    public Vector3i getAdjacentPos(Vector3i position) {
        Vector3i result = new Vector3i(position);
        result.add(vector3iDir);
        return result;
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

    public boolean isVertical() {
        return !canYaw;
    }


    public Iterable<Side> tangents() {
        return tangents.get(this);
    }
}
