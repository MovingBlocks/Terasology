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

import org.terasology.math.geom.Vector3d;
import org.terasology.math.geom.Vector3f;

import java.io.Serializable;

/**
 * Vector3i - integer vector class in the style of VecMath.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3i extends org.terasology.math.geom.Vector3i implements Serializable {
    private static final long serialVersionUID = -1965792038041767639L;

    /**
     * Constructor instantiates a new <code>Vector3i</code> with default
     * values of (0,0,0).
     */
    public Vector3i() {
        x = 0;
        y = 0;
        z = 0;
    }

    /**
     * Constructs the integer version of a Vector3f, by flooring it
     *
     * @param other
     */
    public Vector3i(Vector3f other) {
        this.x = TeraMath.floorToInt(other.x);
        this.y = TeraMath.floorToInt(other.y);
        this.z = TeraMath.floorToInt(other.z);
    }

    public Vector3i(float x, float y, float z) {
        this.x = TeraMath.floorToInt(x);
        this.y = TeraMath.floorToInt(y);
        this.z = TeraMath.floorToInt(z);
    }

    /**
     * Constructs the integer version of a Vector3f, by adding an offset and flooring it
     *
     * @param other
     */
    public Vector3i(Vector3f other, float offset) {
        this.x = TeraMath.floorToInt(other.x + offset);
        this.y = TeraMath.floorToInt(other.y + offset);
        this.z = TeraMath.floorToInt(other.z + offset);
    }

    public Vector3i(float x, float y, float z, float offset) {
        this.x = TeraMath.floorToInt(x + offset);
        this.y = TeraMath.floorToInt(y + offset);
        this.z = TeraMath.floorToInt(z + offset);
    }

    /**
     * Constructor instantiates a new <code>Vector3i</code> with provides
     * values.
     *
     * @param x the x value of the vector.
     * @param y the y value of the vector.
     * @param z the z value of the vector.
     */
    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructor instantiates a new <code>Vector3i</code> that is a copy
     * of the provided vector
     *
     * @param other The Vector3i to copy
     */
    public Vector3i(Vector3i other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public static Vector3i zero() {
        return new Vector3i(0, 0, 0);
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

    public static Vector3i one() {
        return new Vector3i(1, 1, 1);
    }

    public static Vector3i min() {
        return new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static Vector3i max() {
        return new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static Vector3i north() {
        return unitZ();
    }

    public static Vector3i south() {
        return new Vector3i(0, 0, -1);
    }

    public static Vector3i west() {
        return unitX();
    }

    public static Vector3i east() {
        return new Vector3i(-1, 0, 0);
    }

    public static Vector3i up() {
        return unitY();
    }

    public static Vector3i down() {
        return new Vector3i(0, -1, 0);
    }

    /**
     * Returns true if this vector is a unit vector (lengthSquared() == 1),
     * returns false otherwise.
     *
     * @return true if this vector is a unit vector (lengthSquared() == 1),
     *         or false otherwise.
     */
    public boolean isUnitVector() {
        return (Math.abs(x) + Math.abs(y) + Math.abs(z)) == 1;
    }

    /**
     * Calculates the total distance in axis-aligned steps between this and
     * other vector (manhattan distance). This is the distance that is traveled
     * if movement is restricted to adjacent vectors.
     *
     * @param other the other vector to test
     * @return the total distance in axis-aligned steps between this and
     *         other vector (manhattan distance)
     */
    public int gridDistance(Vector3i other) {
        return Math.abs(other.x - x) + Math.abs(other.y - y) + Math.abs(other.z - z);
    }

    /**
     * Calculates the total magnitude of the vector as a sum of its axis aligned
     * dimensions (manhattan distance)
     *
     * @return the total magnitude of the vector as a sum of its axis aligned
     *         dimensions (manhattan distance)
     */
    public int gridMagnitude() {
        return Math.abs(x) + Math.abs(y) + Math.abs(z);
    }

    /**
     * <code>lengthSquared</code> calculates the squared value of the
     * magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public int lengthSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * @return the magnitude of the vector.
     */
    public double length() {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between
     * this vector and vector v.
     *
     * @param v the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public int distanceSquared(Vector3i v) {
        int dx = x - v.x;
        int dy = y - v.y;
        int dz = z - v.z;
        return (dx * dx + dy * dy + dz * dz);
    }

    /**
     * <code>distance</code> calculates the distance between this vector and
     * vector v.
     *
     * @param v the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public float distance(Vector3i v) {
        return (float) Math.sqrt(distanceSquared(v));
    }

    /**
     * <code>mult</code> multiplies this vector by a scalar. The resultant
     * vector is returned.
     *
     * @param scalar the value to multiply this vector by.
     */
    public void mult(int scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
    }

    /**
     * <code>mult</code> pairwise multiplies this vector by the provided values.
     *
     * @param x
     * @param y
     * @param z
     */
    public void mult(int x, int y, int z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
    }

    /**
     * <code>mult</code> pairwise multiplies this vector by <code>other</code>.
     *
     * @param other
     */
    public void mult(Vector3i other) {
        mult(other.x, other.y, other.z);
    }

    /**
     * <code>divide</code> divides the values of this vector by a scalar.
     *
     * @param scalar the value to divide this vectors attributes by.
     */
    public void divide(int scalar) {
        x /= scalar;
        y /= scalar;
        z /= scalar;
    }

    /**
     * <code>divide</code> pairwise divides the values of this vector.
     *
     * @param x
     * @param y
     * @param z
     */
    public void divide(int x, int y, int z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
    }

    /**
     * <code>divide</code> pairwise divides the values of this vector by a <code>other</code>.
     *
     * @param other the value to divide this vectors attributes by.
     */
    public void divide(Vector3i other) {
        divide(other.x, other.y, other.z);
    }

    /**
     * <code>min</code> sets each component to the min of this and <code>other</code>
     *
     * @param other
     */
    public void min(Vector3i other) {
        x = Math.min(x, other.x);
        y = Math.min(y, other.y);
        z = Math.min(z, other.z);
    }

    /**
     * <code>max</code> sets each component to the max of this and <code>other</code>
     *
     * @param other
     */
    public void max(Vector3i other) {
        x = Math.max(x, other.x);
        y = Math.max(y, other.y);
        z = Math.max(z, other.z);
    }

    /**
     * <code>reset</code> resets this vector's data to zero internally.
     */
    public Vector3i reset() {
        x = 0;
        y = 0;
        z = 0;
        return this;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * @return The equivalent Vector3f
     */
    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }

    /**
     * @return The equivalent Vector3d
     */
    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }
}
