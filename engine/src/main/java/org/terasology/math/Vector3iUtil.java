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

import org.terasology.math.geom.Vector3i;

/**
 * Vector3i - integer vector class in the style of VecMath.
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class Vector3iUtil {
    private Vector3iUtil() {
    }

    public static Vector3i unitX() {
        return new Vector3i(1, 0, 0);
    }

    public static Vector3i unitY() {
        return new Vector3i(0, 1, 0);
    }

    public static Vector3i unitZ() {
        return new Vector3i(0, 0, 1);
    }

    public static Vector3i unitXY() {
        return new Vector3i(1, 1, 0);
    }

    public static Vector3i unitXZ() {
        return new Vector3i(1, 0, 1);
    }

    public static Vector3i unitYZ() {
        return new Vector3i(0, 1, 1);
    }

    /**
     * Returns true if this vector is a unit vector (lengthSquared() == 1),
     * returns false otherwise.
     *
     * @return true if this vector is a unit vector (lengthSquared() == 1),
     * or false otherwise.
     */
    public static boolean isUnitVector(Vector3i original) {
        return (Math.abs(original.x) + Math.abs(original.y) + Math.abs(original.z)) == 1;
    }

    /**
     * Calculates the total distance in axis-aligned steps between this and
     * other vector (manhattan distance). This is the distance that is traveled
     * if movement is restricted to adjacent vectors.
     *
     * @param other the other vector to test
     * @return the total distance in axis-aligned steps between this and
     * other vector (manhattan distance)
     */
    public static int gridDistance(Vector3i original, Vector3i other) {
        return Math.abs(other.x - original.x) + Math.abs(other.y - original.y) + Math.abs(other.z - original.z);
    }

    /**
     * Calculates the total magnitude of the vector as a sum of its axis aligned
     * dimensions (manhattan distance)
     *
     * @return the total magnitude of the vector as a sum of its axis aligned
     * dimensions (manhattan distance)
     */
    public static int gridMagnitude(Vector3i original) {
        return Math.abs(original.x) + Math.abs(original.y) + Math.abs(original.z);
    }

    /**
     * <code>lengthSquared</code> calculates the squared value of the
     * magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public static int lengthSquared(Vector3i original) {
        return original.x * original.x + original.y * original.y + original.z * original.z;
    }

    /**
     * @return the magnitude of the vector.
     */
    public static double length(Vector3i original) {
        return (float) Math.sqrt(lengthSquared(original));
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between
     * this vector and vector v.
     *
     * @param v the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public static int distanceSquared(Vector3i original, Vector3i v) {
        int dx = original.x - v.x;
        int dy = original.y - v.y;
        int dz = original.z - v.z;
        return (dx * dx + dy * dy + dz * dz);
    }

    /**
     * <code>distance</code> calculates the distance between this vector and
     * vector v.
     *
     * @param v the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public static float distance(Vector3i original, Vector3i v) {
        return (float) Math.sqrt(distanceSquared(original, v));
    }

    /**
     * <code>reset</code> resets this vector's data to zero internally.
     */
    public static Vector3i reset(Vector3i original) {
        original.x = 0;
        original.y = 0;
        original.z = 0;
        return original;
    }
}
