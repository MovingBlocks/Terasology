package org.terasology.math;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Tobias 'skaldarnar' Nett Date: 17.11.12
 */
public class Matrix4i implements Serializable {

    public static final int m00 = 0;
    public static final int m01 = 1;
    public static final int m02 = 2;
    public static final int m03 = 3;
    public static final int m10 = 4;
    public static final int m11 = 5;
    public static final int m12 = 6;
    public static final int m13 = 7;
    public static final int m20 = 8;
    public static final int m21 = 9;
    public static final int m22 = 10;
    public static final int m23 = 11;
    public static final int m30 = 12;
    public static final int m31 = 13;
    public static final int m32 = 14;
    public static final int m33 = 15;

    public final int tmp[] = new int[16];
    public final int val[] = new int[16];

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
        val[m00] = values[m00];
        val[m10] = values[m10];
        val[m20] = values[m20];
        val[m30] = values[m30];
        val[m01] = values[m01];
        val[m11] = values[m11];
        val[m21] = values[m21];
        val[m31] = values[m31];
        val[m02] = values[m02];
        val[m12] = values[m12];
        val[m22] = values[m22];
        val[m32] = values[m32];
        val[m03] = values[m03];
        val[m13] = values[m13];
        val[m23] = values[m23];
        val[m33] = values[m33];
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
        val[m00] = xAxis.x;
        val[m10] = xAxis.y;
        val[m20] = xAxis.z;
        val[m01] = yAxis.x;
        val[m11] = yAxis.y;
        val[m21] = yAxis.z;
        val[m02] = -zAxis.x;
        val[m12] = -zAxis.y;
        val[m22] = -zAxis.z;
        val[m03] = pos.x;
        val[m13] = pos.y;
        val[m23] = pos.z;
        val[m30] = 0;
        val[m31] = 0;
        val[m32] = 0;
        val[m33] = 1;
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
        val[m00] = 1;
        val[m01] = 0;
        val[m02] = 0;
        val[m03] = 0;

        val[m10] = 0;
        val[m11] = (int) Math.cos(angle);
        val[m12] = (int) Math.sin(angle);
        val[m13] = 0;

        val[m20] = 0;
        val[m21] = -(int) Math.sin(angle);
        val[m22] = (int) Math.cos(angle);
        val[m23] = 0;

        val[m30] = 0;
        val[m31] = 0;
        val[m32] = 0;
        val[m33] = 1;
        return this;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the y axis.
     *
     * @param angle
     * @return this matrix for chaining
     */
    public Matrix4i rotY(float angle) {
        val[m00] = (int) Math.round(Math.cos(angle));
        val[m01] = 0;
        val[m02] = (int) Math.round(Math.sin(angle));
        val[m03] = 0;

        val[m10] = 0;
        val[m11] = 1;
        val[m12] = 0;
        val[m13] = 0;

        val[m20] = -(int) Math.round(Math.sin(angle));
        val[m21] = 0;
        val[m22] = (int) Math.round(Math.cos(angle));
        val[m23] = 0;

        val[m30] = 0;
        val[m31] = 0;
        val[m32] = 0;
        val[m33] = 1;
        return this;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the z axis.
     *
     * @param angle
     * @return this matrix for chaining
     */
    public Matrix4i rotZ(float angle) {
        val[m00] = (int) Math.cos(angle);
        val[m01] = -(int) Math.sin(angle);
        val[m02] = 0;
        val[m03] = 0;

        val[m10] = (int) Math.sin(angle);
        val[m11] = (int) Math.cos(angle);
        val[m12] = 0;
        val[m13] = 0;

        val[m20] = 0;
        val[m21] = 0;
        val[m22] = 1;
        val[m23] = 0;

        val[m30] = 0;
        val[m31] = 0;
        val[m32] = 0;
        val[m33] = 1;
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
        val[m03] += vector.x;
        val[m13] += vector.y;
        val[m23] += vector.z;
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
        val[m03] += dx;
        val[m13] += dy;
        val[m23] += dz;
        return this;
    }

    /**
     * Sets the matrix to an identity matrix
     *
     * @return This matrix for chaining
     */
    public Matrix4i identity() {
        val[m00] = 1;
        val[m01] = 0;
        val[m02] = 0;
        val[m03] = 0;
        val[m10] = 0;
        val[m11] = 1;
        val[m12] = 0;
        val[m13] = 0;
        val[m20] = 0;
        val[m21] = 0;
        val[m22] = 1;
        val[m23] = 0;
        val[m30] = 0;
        val[m31] = 0;
        val[m32] = 0;
        val[m33] = 1;
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
        tmp[m00] = val[m00] * m1.val[m00] + val[m01] * m1.val[m10] + val[m02] * m1.val[m20] + val[m03]
                * m1.val[m30];
        tmp[m01] = val[m00] * m1.val[m01] + val[m01] * m1.val[m11] + val[m02] * m1.val[m21] + val[m03]
                * m1.val[m31];
        tmp[m02] = val[m00] * m1.val[m02] + val[m01] * m1.val[m12] + val[m02] * m1.val[m22] + val[m03]
                * m1.val[m32];
        tmp[m03] = val[m00] * m1.val[m03] + val[m01] * m1.val[m13] + val[m02] * m1.val[m23] + val[m03]
                * m1.val[m33];
        tmp[m10] = val[m10] * m1.val[m00] + val[m11] * m1.val[m10] + val[m12] * m1.val[m20] + val[m13]
                * m1.val[m30];
        tmp[m11] = val[m10] * m1.val[m01] + val[m11] * m1.val[m11] + val[m12] * m1.val[m21] + val[m13]
                * m1.val[m31];
        tmp[m12] = val[m10] * m1.val[m02] + val[m11] * m1.val[m12] + val[m12] * m1.val[m22] + val[m13]
                * m1.val[m32];
        tmp[m13] = val[m10] * m1.val[m03] + val[m11] * m1.val[m13] + val[m12] * m1.val[m23] + val[m13]
                * m1.val[m33];
        tmp[m20] = val[m20] * m1.val[m00] + val[m21] * m1.val[m10] + val[m22] * m1.val[m20] + val[m23]
                * m1.val[m30];
        tmp[m21] = val[m20] * m1.val[m01] + val[m21] * m1.val[m11] + val[m22] * m1.val[m21] + val[m23]
                * m1.val[m31];
        tmp[m22] = val[m20] * m1.val[m02] + val[m21] * m1.val[m12] + val[m22] * m1.val[m22] + val[m23]
                * m1.val[m32];
        tmp[m23] = val[m20] * m1.val[m03] + val[m21] * m1.val[m13] + val[m22] * m1.val[m23] + val[m23]
                * m1.val[m33];
        tmp[m30] = val[m30] * m1.val[m00] + val[m31] * m1.val[m10] + val[m32] * m1.val[m20] + val[m33]
                * m1.val[m30];
        tmp[m31] = val[m30] * m1.val[m01] + val[m31] * m1.val[m11] + val[m32] * m1.val[m21] + val[m33]
                * m1.val[m31];
        tmp[m32] = val[m30] * m1.val[m02] + val[m31] * m1.val[m12] + val[m32] * m1.val[m22] + val[m33]
                * m1.val[m32];
        tmp[m33] = val[m30] * m1.val[m03] + val[m31] * m1.val[m13] + val[m32] * m1.val[m23] + val[m33]
                * m1.val[m33];
        return this.set(tmp);
    }

    /**
     * Transposes the matrix
     *
     * @return This matrix for chaining
     */
    public Matrix4i transpose() {
        tmp[m00] = val[m00];
        tmp[m01] = val[m10];
        tmp[m02] = val[m20];
        tmp[m03] = val[m30];
        tmp[m10] = val[m01];
        tmp[m11] = val[m11];
        tmp[m12] = val[m21];
        tmp[m13] = val[m31];
        tmp[m20] = val[m02];
        tmp[m21] = val[m12];
        tmp[m22] = val[m22];
        tmp[m23] = val[m32];
        tmp[m30] = val[m03];
        tmp[m31] = val[m13];
        tmp[m32] = val[m23];
        tmp[m33] = val[m33];
        return this.set(tmp);
    }

    /**
     * Generates the determinate of this matrix.
     *
     * @return the determinate
     */
    public float determinant() {
        float fA0 = val[m00] * val[m11] - val[m01] * val[m10];
        float fA1 = val[m00] * val[m12] - val[m02] * val[m10];
        float fA2 = val[m00] * val[m13] - val[m03] * val[m10];
        float fA3 = val[m01] * val[m12] - val[m02] * val[m11];
        float fA4 = val[m01] * val[m13] - val[m03] * val[m11];
        float fA5 = val[m02] * val[m13] - val[m03] * val[m12];
        float fB0 = val[m20] * val[m31] - val[m21] * val[m30];
        float fB1 = val[m20] * val[m32] - val[m22] * val[m30];
        float fB2 = val[m20] * val[m33] - val[m23] * val[m30];
        float fB3 = val[m21] * val[m32] - val[m22] * val[m31];
        float fB4 = val[m21] * val[m33] - val[m23] * val[m31];
        float fB5 = val[m22] * val[m33] - val[m23] * val[m32];
        float fDet = fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;
        return fDet;
    }


    public Vector3i getTranslation() {
        return new Vector3i(val[m03], val[m13], val[m23]);
    }

    /**
     * This method will set the matrix's translation values.
     *
     * @param x value of the translation on the x axis
     * @param y value of the translation on the y axis
     * @param z value of the translation on the z axis
     */
    public void setTranslation(int x, int y, int z) {
        val[m03] = x;
        val[m13] = y;
        val[m23] = z;
    }

    /**
     * This method will set the matrix's translation values.
     *
     * @param translation the new values for the translation.
     */
    public void setTranslation(Vector3i translation) {
        val[m03] = translation.x;
        val[m13] = translation.y;
        val[m23] = translation.z;
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
        if (dest == null)
            dest = new Vector3i();
        int x = val[m00] * vec.x + val[m01] * vec.y + val[m02] * vec.z;
        int y = val[m10] * vec.x + val[m11] * vec.y + val[m12] * vec.z;
        int z = val[m20] * vec.x + val[m21] * vec.y + val[m22] * vec.z;
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
        if (dest == null)
            dest = new Vector3i();
        int x = val[m00] * vec.x + val[m01] * vec.y + val[m02] * vec.z + val[m03];
        int y = val[m10] * vec.x + val[m11] * vec.y + val[m12] * vec.z + val[m13];
        int z = val[m20] * vec.x + val[m21] * vec.y + val[m22] * vec.z + val[m23];
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
        int x = val[m00] * vec.x + val[m01] * vec.y + val[m02] * vec.z + val[m03];
        int y = val[m10] * vec.x + val[m11] * vec.y + val[m12] * vec.z + val[m13];
        int z = val[m20] * vec.x + val[m21] * vec.y + val[m22] * vec.z + val[m23];
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
        result.append(val[m00]);
        result.append("  ");
        result.append(val[m01]);
        result.append("  ");
        result.append(val[m02]);
        result.append("  ");
        result.append(val[m03]);
        result.append(" \n");
        result.append(" ");
        result.append(val[m10]);
        result.append("  ");
        result.append(val[m11]);
        result.append("  ");
        result.append(val[m12]);
        result.append("  ");
        result.append(val[m13]);
        result.append(" \n");
        result.append(" ");
        result.append(val[m20]);
        result.append("  ");
        result.append(val[m21]);
        result.append("  ");
        result.append(val[m22]);
        result.append("  ");
        result.append(val[m23]);
        result.append(" \n");
        result.append(" ");
        result.append(val[m30]);
        result.append("  ");
        result.append(val[m31]);
        result.append("  ");
        result.append(val[m32]);
        result.append("  ");
        result.append(val[m33]);
        result.append(" \n]");
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix4i)) return false;

        Matrix4i matrix4i = (Matrix4i) o;

        if (!Arrays.equals(val, matrix4i.val)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return val != null ? Arrays.hashCode(val) : 0;
    }
}
