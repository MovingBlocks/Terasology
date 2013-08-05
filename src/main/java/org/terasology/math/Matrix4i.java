/*
 * Copyright 2013 Moving Blocks
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

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Tobias 'skaldarnar' Nett Date: 17.11.12
 */
public class Matrix4i implements Serializable {

    public static final int M00 = 0;
    public static final int M01 = 1;
    public static final int M02 = 2;
    public static final int M03 = 3;
    public static final int M10 = 4;
    public static final int M11 = 5;
    public static final int M12 = 6;
    public static final int M13 = 7;
    public static final int M20 = 8;
    public static final int M21 = 9;
    public static final int M22 = 10;
    public static final int M23 = 11;
    public static final int M30 = 12;
    public static final int M31 = 13;
    public static final int M32 = 14;
    public static final int M33 = 15;

    public final int[] tmp = new int[16];
    public final int[] val = new int[16];

    /**
     * Constructs a new empty matrix
     */
    public Matrix4i() {
    }

    /**
     * Constructs a matrix from the given matrix
     *
     * @param matrix The matrix
     */
    public Matrix4i(Matrix4i matrix) {
        this.set(matrix);
    }

    /**
     * Constructs a matrix from the given int array. The array must have at least 16 elements
     *
     * @param values The float array
     */
    public Matrix4i(int[] values) {
        this.set(values);
    }

    public static final Matrix4i id() {
        return new Matrix4i(new int[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
    }

    /**
     * Sets the matrix to the given matrix.
     *
     * @param matrix The matrix
     * @return This matrix for chaining
     */
    public Matrix4i set(Matrix4i matrix) {
        return this.set(matrix.val);
    }

    /**
     * Sets the matrix to the given matrix as an int array.
     * <p/>
     * The given array must have at least 16 values.
     *
     * @param values The matrix
     * @return This matrix for chaining
     */
    private Matrix4i set(int[] values) {
        val[M00] = values[M00];
        val[M10] = values[M10];
        val[M20] = values[M20];
        val[M30] = values[M30];
        val[M01] = values[M01];
        val[M11] = values[M11];
        val[M21] = values[M21];
        val[M31] = values[M31];
        val[M02] = values[M02];
        val[M12] = values[M12];
        val[M22] = values[M22];
        val[M32] = values[M32];
        val[M03] = values[M03];
        val[M13] = values[M13];
        val[M23] = values[M23];
        val[M33] = values[M33];
        return this;
    }

    /**
     * Sets the four columns of the matrix which correspond to the x-, y- and z-axis of the vector space that is spanned by this matrix, as
     * well as the 4th column representing the translation of any point that is multiplied by this matrix.
     *
     * @param xAxis The x-axis
     * @param yAxis The y-axis
     * @param zAxis The z-axis
     * @param pos   The translation vector
     */
    public void set(Vector3i xAxis, Vector3i yAxis, Vector3i zAxis, Vector3i pos) {
        val[M00] = xAxis.x;
        val[M10] = xAxis.y;
        val[M20] = xAxis.z;
        val[M01] = yAxis.x;
        val[M11] = yAxis.y;
        val[M21] = yAxis.z;
        val[M02] = -zAxis.x;
        val[M12] = -zAxis.y;
        val[M22] = -zAxis.z;
        val[M03] = pos.x;
        val[M13] = pos.y;
        val[M23] = pos.z;
        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
    }

    /**
     * Clones this matrix
     *
     * @return a copy of this matrix
     */
    public Matrix4i clone() {
        return new Matrix4i(this);
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the x axis.
     *
     * @param angle
     * @return this matrix for chaining
     */
    public Matrix4i rotX(float angle) {
        val[M00] = 1;
        val[M01] = 0;
        val[M02] = 0;
        val[M03] = 0;

        val[M10] = 0;
        val[M11] = (int) Math.cos(angle);
        val[M12] = (int) Math.sin(angle);
        val[M13] = 0;

        val[M20] = 0;
        val[M21] = -(int) Math.sin(angle);
        val[M22] = (int) Math.cos(angle);
        val[M23] = 0;

        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the y axis.
     *
     * @param angle
     * @return this matrix for chaining
     */
    public Matrix4i rotY(float angle) {
        val[M00] = (int) Math.round(Math.cos(angle));
        val[M01] = 0;
        val[M02] = (int) Math.round(Math.sin(angle));
        val[M03] = 0;

        val[M10] = 0;
        val[M11] = 1;
        val[M12] = 0;
        val[M13] = 0;

        val[M20] = -(int) Math.round(Math.sin(angle));
        val[M21] = 0;
        val[M22] = (int) Math.round(Math.cos(angle));
        val[M23] = 0;

        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the z axis.
     *
     * @param angle
     * @return this matrix for chaining
     */
    public Matrix4i rotZ(float angle) {
        val[M00] = (int) Math.cos(angle);
        val[M01] = -(int) Math.sin(angle);
        val[M02] = 0;
        val[M03] = 0;

        val[M10] = (int) Math.sin(angle);
        val[M11] = (int) Math.cos(angle);
        val[M12] = 0;
        val[M13] = 0;

        val[M20] = 0;
        val[M21] = 0;
        val[M22] = 1;
        val[M23] = 0;

        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    /**
     * Adds a translational component to the matrix in the 4th column. The other columns are untouched. This is equal to moving the origin
     * of the coordinate system described by this matrix.
     *
     * @param vector The translation vector
     * @return This matrix for chaining
     */
    public Matrix4i translate(Vector3i vector) {
        val[M03] += vector.x;
        val[M13] += vector.y;
        val[M23] += vector.z;
        return this;
    }

    /**
     * Adds a translational component to the matrix in the 4th column. The other columns are untouched. This is equal to moving the origin
     * of the coordinate system described by this matrix.
     *
     * @param dx the translation along x axis
     * @param dy the translation along y axis
     * @param dz the translation along z axis
     * @return This matrix for chaining
     */
    public Matrix4i translate(int dx, int dy, int dz) {
        val[M03] += dx;
        val[M13] += dy;
        val[M23] += dz;
        return this;
    }

    /**
     * Sets the matrix to an identity matrix
     *
     * @return This matrix for chaining
     */
    public Matrix4i identity() {
        val[M00] = 1;
        val[M01] = 0;
        val[M02] = 0;
        val[M03] = 0;
        val[M10] = 0;
        val[M11] = 1;
        val[M12] = 0;
        val[M13] = 0;
        val[M20] = 0;
        val[M21] = 0;
        val[M22] = 1;
        val[M23] = 0;
        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    /**
     * @return the backing float array
     */
    public int[] getValues() {
        return val;
    }

    /**
     * Sets the value of this matrix to the result of multiplying itself with matrix m1.
     *
     * @param m1 The other matrix
     * @return This matrix for chaining.
     */
    public Matrix4i mul(Matrix4i m1) {
        tmp[M00] = val[M00] * m1.val[M00] + val[M01] * m1.val[M10] + val[M02] * m1.val[M20] + val[M03]
                * m1.val[M30];
        tmp[M01] = val[M00] * m1.val[M01] + val[M01] * m1.val[M11] + val[M02] * m1.val[M21] + val[M03]
                * m1.val[M31];
        tmp[M02] = val[M00] * m1.val[M02] + val[M01] * m1.val[M12] + val[M02] * m1.val[M22] + val[M03]
                * m1.val[M32];
        tmp[M03] = val[M00] * m1.val[M03] + val[M01] * m1.val[M13] + val[M02] * m1.val[M23] + val[M03]
                * m1.val[M33];
        tmp[M10] = val[M10] * m1.val[M00] + val[M11] * m1.val[M10] + val[M12] * m1.val[M20] + val[M13]
                * m1.val[M30];
        tmp[M11] = val[M10] * m1.val[M01] + val[M11] * m1.val[M11] + val[M12] * m1.val[M21] + val[M13]
                * m1.val[M31];
        tmp[M12] = val[M10] * m1.val[M02] + val[M11] * m1.val[M12] + val[M12] * m1.val[M22] + val[M13]
                * m1.val[M32];
        tmp[M13] = val[M10] * m1.val[M03] + val[M11] * m1.val[M13] + val[M12] * m1.val[M23] + val[M13]
                * m1.val[M33];
        tmp[M20] = val[M20] * m1.val[M00] + val[M21] * m1.val[M10] + val[M22] * m1.val[M20] + val[M23]
                * m1.val[M30];
        tmp[M21] = val[M20] * m1.val[M01] + val[M21] * m1.val[M11] + val[M22] * m1.val[M21] + val[M23]
                * m1.val[M31];
        tmp[M22] = val[M20] * m1.val[M02] + val[M21] * m1.val[M12] + val[M22] * m1.val[M22] + val[M23]
                * m1.val[M32];
        tmp[M23] = val[M20] * m1.val[M03] + val[M21] * m1.val[M13] + val[M22] * m1.val[M23] + val[M23]
                * m1.val[M33];
        tmp[M30] = val[M30] * m1.val[M00] + val[M31] * m1.val[M10] + val[M32] * m1.val[M20] + val[M33]
                * m1.val[M30];
        tmp[M31] = val[M30] * m1.val[M01] + val[M31] * m1.val[M11] + val[M32] * m1.val[M21] + val[M33]
                * m1.val[M31];
        tmp[M32] = val[M30] * m1.val[M02] + val[M31] * m1.val[M12] + val[M32] * m1.val[M22] + val[M33]
                * m1.val[M32];
        tmp[M33] = val[M30] * m1.val[M03] + val[M31] * m1.val[M13] + val[M32] * m1.val[M23] + val[M33]
                * m1.val[M33];
        return this.set(tmp);
    }

    /**
     * Transposes the matrix
     *
     * @return This matrix for chaining
     */
    public Matrix4i transpose() {
        tmp[M00] = val[M00];
        tmp[M01] = val[M10];
        tmp[M02] = val[M20];
        tmp[M03] = val[M30];
        tmp[M10] = val[M01];
        tmp[M11] = val[M11];
        tmp[M12] = val[M21];
        tmp[M13] = val[M31];
        tmp[M20] = val[M02];
        tmp[M21] = val[M12];
        tmp[M22] = val[M22];
        tmp[M23] = val[M32];
        tmp[M30] = val[M03];
        tmp[M31] = val[M13];
        tmp[M32] = val[M23];
        tmp[M33] = val[M33];
        return this.set(tmp);
    }

    /**
     * Generates the determinate of this matrix.
     *
     * @return the determinate
     */
    public float determinant() {
        float fA0 = val[M00] * val[M11] - val[M01] * val[M10];
        float fA1 = val[M00] * val[M12] - val[M02] * val[M10];
        float fA2 = val[M00] * val[M13] - val[M03] * val[M10];
        float fA3 = val[M01] * val[M12] - val[M02] * val[M11];
        float fA4 = val[M01] * val[M13] - val[M03] * val[M11];
        float fA5 = val[M02] * val[M13] - val[M03] * val[M12];
        float fB0 = val[M20] * val[M31] - val[M21] * val[M30];
        float fB1 = val[M20] * val[M32] - val[M22] * val[M30];
        float fB2 = val[M20] * val[M33] - val[M23] * val[M30];
        float fB3 = val[M21] * val[M32] - val[M22] * val[M31];
        float fB4 = val[M21] * val[M33] - val[M23] * val[M31];
        float fB5 = val[M22] * val[M33] - val[M23] * val[M32];
        float fDet = fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;
        return fDet;
    }


    public Vector3i getTranslation() {
        return new Vector3i(val[M03], val[M13], val[M23]);
    }

    /**
     * This method will set the matrix's translation values.
     *
     * @param x value of the translation on the x axis
     * @param y value of the translation on the y axis
     * @param z value of the translation on the z axis
     */
    public void setTranslation(int x, int y, int z) {
        val[M03] = x;
        val[M13] = y;
        val[M23] = z;
    }

    /**
     * This method will set the matrix's translation values.
     *
     * @param translation the new values for the translation.
     */
    public void setTranslation(Vector3i translation) {
        val[M03] = translation.x;
        val[M13] = translation.y;
        val[M23] = translation.z;
    }

    /**
     * Transforms a vector by this matrix and stores the result in the vector dest. If dest is null, a new vector is created. The w
     * component of the vector is assumed to be zero.
     *
     * @param vec  the vector to transform
     * @param dest the vector to store the result in
     * @return the transformed vector (for chaining)
     */
    public Vector3i transform(Vector3i vec, Vector3i dest) {
        if (dest == null) {
            dest = new Vector3i();
        }
        int x = val[M00] * vec.x + val[M01] * vec.y + val[M02] * vec.z;
        int y = val[M10] * vec.x + val[M11] * vec.y + val[M12] * vec.z;
        int z = val[M20] * vec.x + val[M21] * vec.y + val[M22] * vec.z;
        dest.x = x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    /**
     * Transforms a point (given as vector) by this matrix and stores the result in the vector dest. If dest is null, a new vector is
     * created. The w component of the vector is assumed to be one.
     *
     * @param vec  the vector to transform
     * @param dest the vector to store the result in
     * @return the transformed vector (for chaining)
     */
    public Vector3i transformPoint(Vector3i vec, Vector3i dest) {
        if (dest == null) {
            dest = new Vector3i();
        }
        int x = val[M00] * vec.x + val[M01] * vec.y + val[M02] * vec.z + val[M03];
        int y = val[M10] * vec.x + val[M11] * vec.y + val[M12] * vec.z + val[M13];
        int z = val[M20] * vec.x + val[M21] * vec.y + val[M22] * vec.z + val[M23];
        dest.x = x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    /**
     * Transforms a point (given as vector) by this matrix and stores the result in the same vector. The w component (fourth component) of
     * the vector is assumed to be one.
     *
     * @param vec the vector to transform
     * @return the transformed vector (for chaining)
     */
    public Vector3i transformPoint(Vector3i vec) {
        int x = val[M00] * vec.x + val[M01] * vec.y + val[M02] * vec.z + val[M03];
        int y = val[M10] * vec.x + val[M11] * vec.y + val[M12] * vec.z + val[M13];
        int z = val[M20] * vec.x + val[M21] * vec.y + val[M22] * vec.z + val[M23];
        vec.x = x;
        vec.y = y;
        vec.z = z;
        return vec;
    }

    /**
     * <code>toString</code> returns the string representation of this object. It is in a format of a 4x4 matrix. For example, an identity
     * matrix would be represented by the following string. 1  0  0  0 <br> 0  1  0  0 <br> 0  0  1  0 <br> 0  0  0  1 <br>
     *
     * @return the string representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Matrix4i\n[\n");
        result.append(" ");
        result.append(val[M00]);
        result.append("  ");
        result.append(val[M01]);
        result.append("  ");
        result.append(val[M02]);
        result.append("  ");
        result.append(val[M03]);
        result.append(" \n");
        result.append(" ");
        result.append(val[M10]);
        result.append("  ");
        result.append(val[M11]);
        result.append("  ");
        result.append(val[M12]);
        result.append("  ");
        result.append(val[M13]);
        result.append(" \n");
        result.append(" ");
        result.append(val[M20]);
        result.append("  ");
        result.append(val[M21]);
        result.append("  ");
        result.append(val[M22]);
        result.append("  ");
        result.append(val[M23]);
        result.append(" \n");
        result.append(" ");
        result.append(val[M30]);
        result.append("  ");
        result.append(val[M31]);
        result.append("  ");
        result.append(val[M32]);
        result.append("  ");
        result.append(val[M33]);
        result.append(" \n]");
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Matrix4i)) {
            return false;
        }

        Matrix4i matrix4i = (Matrix4i) o;

        if (!Arrays.equals(val, matrix4i.val)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return val != null ? Arrays.hashCode(val) : 0;
    }
}
