// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.joml.Math;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.EnumMap;

/**
 * Defines orientation in a cartographic sense
 */
public enum Orientation {

    /**
     * North (0, -1)
     */
    NORTH(0, -1),

    /**
     * North-East (1, -1)
     */
    NORTHEAST(1, -1),

    /**
     * East (1, 0)
     */
    EAST(1, 0),

    /**
     * South-East (1, 1)
     */
    SOUTHEAST(1, 1),

    /**
     * South (0, 1)
     */
    SOUTH(0, 1),

    /**
     * South-West (-1, 1)
     */
    SOUTHWEST(-1, 1),

    /**
     * West (-1, 0)
     */
    WEST(-1, 0),

    /**
     * North-West (-1, -1)
     */
    NORTHWEST(-1, -1);

    private static final EnumMap<Orientation, Orientation> OPPOSITES = Maps.newEnumMap(Orientation.class);
    private static final EnumMap<Orientation, Orientation> ROTATE_CW = Maps.newEnumMap(Orientation.class);
    private static final EnumMap<Orientation, Orientation> ROTATE_CCW = Maps.newEnumMap(Orientation.class);

    static {
        OPPOSITES.put(NORTH, SOUTH);
        OPPOSITES.put(NORTHEAST, SOUTHWEST);
        OPPOSITES.put(EAST, WEST);
        OPPOSITES.put(SOUTHEAST, NORTHWEST);
        OPPOSITES.put(SOUTH, NORTH);
        OPPOSITES.put(SOUTHWEST, NORTHEAST);
        OPPOSITES.put(WEST, EAST);
        OPPOSITES.put(NORTHWEST, SOUTHEAST);

        ROTATE_CW.put(NORTH, NORTHEAST);
        ROTATE_CW.put(NORTHEAST, EAST);
        ROTATE_CW.put(EAST, SOUTHEAST);
        ROTATE_CW.put(SOUTHEAST, SOUTH);
        ROTATE_CW.put(SOUTH, SOUTHWEST);
        ROTATE_CW.put(SOUTHWEST, WEST);
        ROTATE_CW.put(WEST, NORTHWEST);
        ROTATE_CW.put(NORTHWEST, NORTH);

        ROTATE_CCW.put(NORTH, NORTHWEST);
        ROTATE_CCW.put(NORTHWEST, WEST);
        ROTATE_CCW.put(WEST, SOUTHWEST);
        ROTATE_CCW.put(SOUTHWEST, SOUTH);
        ROTATE_CCW.put(SOUTH, SOUTHEAST);
        ROTATE_CCW.put(SOUTHEAST, EAST);
        ROTATE_CCW.put(EAST, NORTHEAST);
        ROTATE_CCW.put(NORTHEAST, NORTH);
    }

    private final Vector2ic dir;

    Orientation(int dx, int dz) {
        this.dir = new Vector2i(dx, dz);
    }

    /**
     * @return the opposite orientation
     */
    public Orientation getOpposite() {
        return OPPOSITES.get(this);
    }

    /**
     * @return true for NORTH, WEST, SOUTH and EAST, false otherwise
     */
    public boolean isCardinal() {
        // there is no need for Objects.equals(), comparing with == is safe here
        return this == NORTH || this == WEST || this == SOUTH || this == EAST;
    }

    /**
     * @param degrees the rotation in degrees at 45° intervals (clockwise)
     * @return the orientation
     */
    public Orientation getRotated(int degrees) {
        Preconditions.checkArgument(degrees % 45 == 0, "degrees must be a multiple of 45 (is %s)", degrees);

        // normalize degrees to be in [0..360[
        int norm = (degrees % 360);
        if (norm < 0) {
            norm += 360;
        }

        if (norm == 0) {
            return this;
        }

        if (norm == 180) {
            return OPPOSITES.get(this);
        }

        Orientation result = this;

        // this requires 3 map lookups maximum

        while (norm < 180 && norm > 0) {
            result = ROTATE_CW.get(result);
            norm -= 45;
        }

        while (norm > 180 && norm < 360) {
            result = ROTATE_CCW.get(result);
            norm += 45;
        }

        return result;
    }

    /**
     * @return the orientation
     */
    public Vector2ic direction() {
        return dir;
    }

    /**
     * convert direction to orientation
     * @param dir directions vector on a cardinal plain
     * @return the orientation
     */
    public static Orientation fromDirection(Vector2fc dir) {
       return fromDirection(dir.x(), dir.y());
    }

    /**
     * convert direction to orientation
     * @param x the x direction
     * @param z the z direction
     * @return the orientation
     */
    public static Orientation fromDirection(float x, float z) {
        float angle = Math.atan2(-x, z);
        int octant = (int) (Math.round((8 * (angle / (2 * Math.PI))) + 8) % 8);
        return Orientation.values()[octant];
    }

    /**
     * Finds the best-matching cardinal orientation.
     * @param dir an arbitrary direction vector
     * @return either EAST, WEST, NORTH or SOUTH
     */
    public static Orientation cardinalFromDirection(Vector2ic dir) {
        if (Math.abs(dir.x()) >= Math.abs(dir.y())) {
            if (dir.x() > 0) {
                return Orientation.EAST;
            } else {
                return Orientation.WEST;
            }
        } else {
            if (dir.y() > 0) {
                return Orientation.SOUTH;
            } else {
                return Orientation.NORTH;
            }
        }
    }
}

