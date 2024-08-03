// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import com.google.common.collect.ImmutableList;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.TeraMath;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The six sides of a block and a slew of related utility.
 * <br><br>
 * Note that the FRONT of the block faces towards the player - this means Left and Right are a player's right and left.
 * See Direction for an enumeration of directions in terms of the player's perspective.
 *
 */
public enum Side {
    TOP(new Vector3i(0, 1, 0), (byte) 0b000001),            //  1
    LEFT(new Vector3i(-1, 0, 0), (byte) 0b000010),          //  2
    FRONT(new Vector3i(0, 0, -1), (byte) 0b000100),         //  4
    BOTTOM(new Vector3i(0, -1, 0), (byte) 0b001000),        //  8
    RIGHT(new Vector3i(1, 0, 0), (byte) 0b010000),          // 16
    BACK(new Vector3i(0, 0, 1), (byte) 0b100000);           // 32

    public static final ImmutableList<Side> X_TANGENT_SIDE = ImmutableList.of(TOP, BOTTOM, FRONT, BACK);
    public static final ImmutableList<Side> Y_TANGENT_SIDE = ImmutableList.of(LEFT, RIGHT, FRONT, BACK);
    public static final ImmutableList<Side> Z_TANGENT_SIDE = ImmutableList.of(TOP, BOTTOM, LEFT, RIGHT);

    public static final ImmutableList<Side> X_VERTICAL_SIDE = ImmutableList.of(LEFT, RIGHT);
    public static final ImmutableList<Side> Y_VERTICAL_SIDE = ImmutableList.of(TOP, BOTTOM);
    public static final ImmutableList<Side> Z_VERTICAL_SIDE = ImmutableList.of(FRONT, BACK);

    private static final ImmutableList<Side> ALL_SIDES = ImmutableList.of(TOP, BOTTOM, LEFT, RIGHT, FRONT, BACK);
    private final Vector3ic direction;
    private final byte flag;

    Side(Vector3i vector3i, byte flag) {
        this.direction = vector3i;
        this.flag = flag;
    }

    public static byte toFlags(Set<Side> sides) {
        byte result = 0;
        for (Side side : sides) {
            result |= side.getFlag();
        }
        return result;
    }

    public static byte toFlags(Side... sides) {
        byte result = 0;
        for (Side side : sides) {
            result |= side.getFlag();
        }
        return result;
    }

    public static byte setFlags(byte flags, Side... sides) {
        byte result = flags;
        for (Side side : sides) {
            result |= side.getFlag();
        }
        return result;
    }

    public static EnumSet<Side> getSides(final byte data) {
        final EnumSet<Side> result = EnumSet.noneOf(Side.class);
        for (Side side : ALL_SIDES) {
            if ((data & side.getFlag()) > 0) {
                result.add(side);
            }
        }
        return result;
    }

    public static byte reverseSides(byte sides) {
        return (byte) ((sides / 8) + ((sides % 8) * 8));
    }

    /**
     * @return The horizontal sides, for iteration
     */
    public static List<Side> horizontalSides() {
        return Y_TANGENT_SIDE;
    }

    /**
     * This provides a static List of all Sides defined in the enumeration. The result contains the same values as
     * calling {@code Side#values} but this does not create a new copy on every call.<br>
     *
     * @return All available sides
     */
    public static List<Side> allSides() {
        return ALL_SIDES;
    }

    /**
     * @return The vertical sides, for iteration
     */
    public static List<Side> verticalSides() {
        return Y_VERTICAL_SIDE;
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
     * the normal vector in the direction of the side
     *
     * @return a normalized vector
     */
    public Vector3ic direction() {
        return direction;
    }

    /**
     * the bit flag associated with this side
     * <ol>
     * <li>TOP - 0b000001 </li>
     * <li>LEFT - 0b000010 </li>
     * <li>FRONT - 0b000100 </li>
     * <li>BOTTOM - 0b001000 </li>
     * <li>RIGHT - 0b010000 </li>
     * <li>BACK - 0b100000 </li>
     * </ol>
     * @return bit flag
     */
    public byte getFlag() {
        return this.flag;
    }

    public boolean hasFlag(byte flags) {
        return (flags & this.getFlag()) > 0;
    }

    /**
     * @return Whether this is one of the horizontal directions (LEFT, FRONT, RIGHT, BACK).
     */
    public boolean isHorizontal() {
        switch (this) {
            case LEFT:
            case FRONT:
            case RIGHT:
            case BACK:
                return true;
        }
        return false;
    }

    /**
     * @return Whether this is one of the vertical directions (TOP, BOTTOM).
     */
    public boolean isVertical() {
        switch (this) {
            case TOP:
            case BOTTOM:
                return true;
        }
        return false;
    }

    /**
     * @return The opposite side to this side.
     */
    public Side reverse() {
        switch (this) {
            case TOP:
                return BOTTOM;
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case FRONT:
                return BACK;
            case BACK:
                return FRONT;
            default:
                throw new IllegalStateException("Unexpected value: " + this);

        }
    }

    public Side yawClockwise(int turns) {
        int steps = turns;
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                switch (this) {
                    case FRONT:
                        return LEFT;
                    case RIGHT:
                        return FRONT;
                    case BACK:
                        return RIGHT;
                    case LEFT:
                        return BACK;
                }
                break;
            case 2:
                switch (this) {
                    case FRONT:
                        return BACK;
                    case RIGHT:
                        return LEFT;
                    case BACK:
                        return FRONT;
                    case LEFT:
                        return RIGHT;
                }
            case 3:
                switch (this) {
                    case FRONT:
                        return RIGHT;
                    case RIGHT:
                        return BACK;
                    case BACK:
                        return LEFT;
                    case LEFT:
                        return FRONT;
                }
                break;
        }
        return this;
    }

    public Side pitchClockwise(int turns) {
        int steps = turns;
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                switch (this) {
                    case FRONT:
                        return TOP;
                    case BOTTOM:
                        return FRONT;
                    case BACK:
                        return BOTTOM;
                    case TOP:
                        return BACK;
                }
                break;
            case 2:
                switch (this) {
                    case FRONT:
                        return BACK;
                    case BOTTOM:
                        return TOP;
                    case BACK:
                        return FRONT;
                    case TOP:
                        return BOTTOM;
                }
            case 3:
                switch (this) {
                    case FRONT:
                        return BOTTOM;
                    case BOTTOM:
                        return BACK;
                    case BACK:
                        return TOP;
                    case TOP:
                        return FRONT;
                }
                break;
        }
        return this;
    }

    public Direction toDirection() {
        switch (this) {
            case TOP:
                return Direction.UP;
            case BOTTOM:
                return Direction.DOWN;
            case BACK:
                return Direction.FORWARD;
            case FRONT:
                return Direction.BACKWARD;
            case RIGHT:
                return Direction.LEFT;
            case LEFT:
                return Direction.RIGHT;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public Side rollClockwise(int turns) {
        int steps = turns;
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                switch (this) {
                    case TOP:
                        return LEFT;
                    case LEFT:
                        return BOTTOM;
                    case BOTTOM:
                        return RIGHT;
                    case RIGHT:
                        return TOP;
                }
                break;
            case 2:
                switch (this) {
                    case TOP:
                        return BOTTOM;
                    case LEFT:
                        return RIGHT;
                    case BOTTOM:
                        return TOP;
                    case RIGHT:
                        return LEFT;
                }
                break;
            case 3:
                switch (this) {
                    case TOP:
                        return RIGHT;
                    case LEFT:
                        return TOP;
                    case BOTTOM:
                        return LEFT;
                    case RIGHT:
                        return BOTTOM;
                }
                break;
        }
        return this;
    }

    /**
     * take the current pos and add the direction
     *
     * @param pos current position
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getAdjacentPos(Vector3ic pos, Vector3i dest) {
        return dest.set(pos).add(direction());
    }

    public Side getRelativeSide(Direction direction) {
        switch (direction) {
            case UP:
                return pitchClockwise(1);
            case DOWN:
                return pitchClockwise(-1);
            case LEFT:
                return yawClockwise(1);
            case RIGHT:
                return yawClockwise(-1);
            case BACKWARD:
                return reverse();
        }
        return this;
    }

    public Iterable<Side> tangents() {
        switch (this) {
            case TOP:
            case BOTTOM:
                return Y_TANGENT_SIDE;
            case LEFT:
            case RIGHT:
                return Z_TANGENT_SIDE;
            case FRONT:
            case BACK:
                return Y_TANGENT_SIDE;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
