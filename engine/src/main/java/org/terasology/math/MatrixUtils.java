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

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;

import java.nio.FloatBuffer;

/**
 * Collection of matrix utilities.
 *
 */
public final class MatrixUtils {

    private MatrixUtils() {
    }

    /**
     * Copies the given matrix into a newly allocated FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m the matrix to copy
     * @return A new FloatBuffer containing the matrix in column-major form.
     * @deprecated used JOML method that uses Matrix4fc
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4f m) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrixToFloatBuffer(m, buffer);
        return buffer;
    }

    /**
     *
     * Copies the given matrix into a newly allocated FloatBuffer
     * the other of the elements in row-major.
     * transformation operations happen on the inverse in the library
     *
     * @param m the matrix to copy
     * @return a new Float buffer containing the matrix in row-major form
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4fc m) {
        return m.getTransposed(BufferUtils.createFloatBuffer(16));
    }

    /**
     * Copies the given matrix into a newly allocated FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m the matrix to copy
     * @return A new FloatBuffer containing the matrix in column-major form.
     * @deprecated used JOML method that uses Matrix3fc
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix3f m) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        matrixToFloatBuffer(m, buffer);
        return buffer;
    }

    public static FloatBuffer matrixToFloatBuffer(Matrix3fc m) {
        return m.getTransposed(BufferUtils.createFloatBuffer(9));
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return The provided float buffer.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix3f m, FloatBuffer fb) {
        fb.put(m.m00);
        fb.put(m.m10);
        fb.put(m.m20);
        fb.put(m.m01);
        fb.put(m.m11);
        fb.put(m.m21);
        fb.put(m.m02);
        fb.put(m.m12);
        fb.put(m.m22);

        fb.flip();
        return fb;
    }

    public static FloatBuffer matrixToFloatBuffer(Matrix3fc m, FloatBuffer fb) {
        return m.getTransposed(fb);
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m  the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return The provided float buffer.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4f m, FloatBuffer fb) {
        fb.put(m.m00);
        fb.put(m.m10);
        fb.put(m.m20);
        fb.put(m.m30);
        fb.put(m.m01);
        fb.put(m.m11);
        fb.put(m.m21);
        fb.put(m.m31);
        fb.put(m.m02);
        fb.put(m.m12);
        fb.put(m.m22);
        fb.put(m.m32);
        fb.put(m.m03);
        fb.put(m.m13);
        fb.put(m.m23);
        fb.put(m.m33);

        fb.flip();
        return fb;
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m  the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return the provided float buffer with matrix elements in column major order.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4fc m, FloatBuffer fb) {
        return m.getTransposed(fb);
    }

    public static org.joml.Matrix4f createViewMatrix(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        return createViewMatrix(new org.joml.Vector3f(eyeX, eyeY, eyeZ), new org.joml.Vector3f(centerX, centerY, centerZ), new org.joml.Vector3f(upX, upY, upZ));
    }

    public static org.joml.Matrix4f createViewMatrix(org.joml.Vector3f eye, org.joml.Vector3f center, org.joml.Vector3f up) {
        org.joml.Matrix4f m = new org.joml.Matrix4f();

        org.joml.Vector3f f = new org.joml.Vector3f();
        center.sub(eye,f);

        f.normalize();
        up.normalize();

        org.joml.Vector3f s = new org.joml.Vector3f();
        f.cross(up,s);
        s.normalize();

        org.joml.Vector3f u = new org.joml.Vector3f();
        s.cross(f,u);
        u.normalize();

        m.m00(s.x);
        m.m10(s.y);
        m.m20(s.z);
        m.m30(0);
        m.m01(u.x);
        m.m11(u.y);
        m.m21(u.z);
        m.m31(0);
        m.m02(-f.x);
        m.m12(-f.y);
        m.m22(-f.z);
        m.m32(0);
        m.m03(0);
        m.m13(0);
        m.m23(0);
        m.m33(1);

        m.m30(-eye.x);
        m.m31(-eye.y);
        m.m32(-eye.z);

        m.transpose();

        return m;
    }


    public static org.joml.Matrix4f createOrthogonalProjectionMatrix(float left, float right, float top, float bottom, float near, float far) {
        org.joml.Matrix4f m = new org.joml.Matrix4f();

        float lateral = right - left;
        float vertical = top - bottom;
        float forward = far - near;
        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        m.m00(2.0f / lateral);
        m.m10(0.0f);
        m.m20(0.0f);
        m.m30(tx);
        m.m01(0.0f);
        m.m11(2.0f / vertical);
        m.m21(0.0f);
        m.m31(ty);
        m.m02(0.0f);
        m.m12(0.0f);
        m.m22(-2.0f / forward);
        m.m32(tz);
        m.m03(0.0f);
        m.m13(0.0f);
        m.m23(0.0f);
        m.m33(1.0f);

        m.transpose();

        return m;
    }

    public static org.joml.Matrix4f createPerspectiveProjectionMatrix(float fovY, float aspectRatio, float zNear, float zFar) {
        org.joml.Matrix4f m = new org.joml.Matrix4f();

        float f = 1.0f / (float) Math.tan((double) fovY * 0.5f);

        m.m00(f / aspectRatio);
        m.m10(0);
        m.m20(0);
        m.m30(0);
        m.m01(0);
        m.m11(f);
        m.m21(0);
        m.m31(0);
        m.m02(0);
        m.m12(0);
        m.m22((zFar + zNear) / (zNear - zFar));
        m.m32((2 * zFar * zNear) / (zNear - zFar));
        m.m03(0);
        m.m13(0);
        m.m23(-1);
        m.m33(0);

        m.transpose();

        return m;
    }

    public static org.joml.Matrix4f calcViewProjectionMatrix(org.joml.Matrix4f vm, org.joml.Matrix4f p) {
        return new org.joml.Matrix4f(p).mul(vm);
    }

    public static Matrix4f calcModelViewMatrix(Matrix4f m, Matrix4f vm) {
        Matrix4f result = new Matrix4f();
        result.mul(m, vm);
        return result;
    }

    public static Matrix3f calcNormalMatrix(Matrix4f mv) {
        Matrix3f result = new Matrix3f();
        result.m00 = mv.m00;
        result.m10 = mv.m10;
        result.m20 = mv.m20;
        result.m01 = mv.m01;
        result.m11 = mv.m11;
        result.m21 = mv.m21;
        result.m02 = mv.m02;
        result.m12 = mv.m12;
        result.m22 = mv.m22;

        result.invert();
        result.transpose();
        return result;
    }

    public static org.joml.Matrix3f calcNormalMatrix(Matrix4fc mv) {
        return mv.get3x3(new org.joml.Matrix3f()).invert().transpose();
    }
}
