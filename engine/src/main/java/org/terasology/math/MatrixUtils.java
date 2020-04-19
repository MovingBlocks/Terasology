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

import org.joml.*;
import org.lwjgl.BufferUtils;

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
     * @deprecated used JOML method that uses Matrix3fc
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix3fc m) {
        return matrixToFloatBuffer(m, BufferUtils.createFloatBuffer(9));
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m  the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return The provided float buffer.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix3fc m, FloatBuffer fb) {
        return m.get(fb);
    }

    /**
     * Copies the given matrix into a newly allocated FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m the matrix to copy
     * @return A new FloatBuffer containing the matrix in column-major form.
     * @deprecated used JOML method that uses Matrix4fc
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4fc m) {
        return matrixToFloatBuffer(m, BufferUtils.createFloatBuffer(16));
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m  the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return The provided float buffer.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4fc m, FloatBuffer fb) {
        return m.get(fb);
    }

    public static Matrix4f createViewMatrix(
            float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ
    ) {
        return new Matrix4f().setLookAt(
                eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                upX, upY, upZ
        );
    }

    public static Matrix4f createViewMatrix(Vector3f eye, Vector3f center, Vector3f up) {
        return new Matrix4f().setLookAt(eye, center, up);
    }

    public static Matrix4f createOrthogonalProjectionMatrix(float left, float right, float top, float bottom, float near, float far) {
        return new Matrix4f().setOrtho(left, right, bottom, top, near, far);
    }

    public static Matrix4f createPerspectiveProjectionMatrix(float fovY, float aspectRatio, float zNear, float zFar) {
        return new Matrix4f().setPerspective(fovY, aspectRatio, zNear, zFar);
    }

    public static Matrix4f calcViewProjectionMatrix(Matrix4f view, Matrix4f projection) {
        return new Matrix4f(projection).mul(view);
    }

    public static Matrix4f calcModelViewMatrix(Matrix4f model, Matrix4f view) {
        return new Matrix4f(model).mul(view);
    }

    public static Matrix3f calcNormalMatrix(Matrix4f m) {
        return m.normal(new Matrix3f());
    }
}
