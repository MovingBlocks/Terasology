package org.terasology.math;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;

import java.nio.FloatBuffer;

public class MatrixUtilsTest {

    @Test
    public void given3fMatrix_whenConvertToFloatBufferValuesAreAllZero_thenFloatBufferShouldBeAllZeros() {
        Matrix3f testMatrix = new Matrix3f(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        FloatBuffer sut = MatrixUtils.matrixToFloatBuffer(testMatrix);
        for (int i = 0; i < sut.capacity(); i++) {
            Assert.assertEquals(sut.get(i), 0.0f, 0.001f);
        }
    }

    @Test
    public void given4fMatrix_whenConvertToFloatBufferAndValuesAreAllZero_thenFloatBufferShouldBeAllZeros() {
        Matrix4f testMatrix = new Matrix4f(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        FloatBuffer sut = MatrixUtils.matrixToFloatBuffer(testMatrix);
        for (int i = 0; i < sut.capacity(); i++) {
            Assert.assertEquals(sut.get(i), 0.0f, 0.001f);
        }
    }

    @Test
    public void given3fMatrix_whenConvertToFloatBuffer_thenFloatBufferShouldBeInColumnMajorOrder() {
        Matrix3f testMatrix = new Matrix3f(1.0f, 2.0f, 3.0f, 2.1f, 4.1f, 10.9f, 0.0f, 0.1f, 2.1f);
        FloatBuffer sut = MatrixUtils.matrixToFloatBuffer(testMatrix);

        Assert.assertEquals(sut.get(0), testMatrix.get(0, 0), 0.001f);
        Assert.assertEquals(sut.get(1), testMatrix.get(1, 0), 0.001f);
        Assert.assertEquals(sut.get(2), testMatrix.get(2, 0), 0.001f);
        Assert.assertEquals(sut.get(3), testMatrix.get(0, 1), 0.001f);
        Assert.assertEquals(sut.get(4), testMatrix.get(1, 1), 0.001f);
        Assert.assertEquals(sut.get(5), testMatrix.get(2, 1), 0.001f);
        Assert.assertEquals(sut.get(6), testMatrix.get(0, 2), 0.001f);
        Assert.assertEquals(sut.get(7), testMatrix.get(1, 2), 0.001f);
        Assert.assertEquals(sut.get(8), testMatrix.get(2, 2), 0.001f);
    }


    @Test
    public void given4fMatrix_whenConvertToFloatBuffer_thenFloatBufferShouldBeInColumnMajorOrder() {
        Matrix4f testMatrix = new Matrix4f(1.0f, 2.0f, 3.0f, 2.1f, 4.1f, 10.9f, 0.0f, 0.1f, 2.1f, 10.1f, -1.4f, -2.4f, 2.2f, 1.1f, 3.3f, 4.4f);
        FloatBuffer sut = MatrixUtils.matrixToFloatBuffer(testMatrix);

        Assert.assertEquals(sut.get(0), testMatrix.get(0, 0), 0.001f);
        Assert.assertEquals(sut.get(1), testMatrix.get(1, 0), 0.001f);
        Assert.assertEquals(sut.get(2), testMatrix.get(2, 0), 0.001f);
        Assert.assertEquals(sut.get(3), testMatrix.get(3, 0), 0.001f);
        Assert.assertEquals(sut.get(4), testMatrix.get(0, 1), 0.001f);
        Assert.assertEquals(sut.get(5), testMatrix.get(1, 1), 0.001f);
        Assert.assertEquals(sut.get(6), testMatrix.get(2, 1), 0.001f);
        Assert.assertEquals(sut.get(7), testMatrix.get(3, 1), 0.001f);
        Assert.assertEquals(sut.get(8), testMatrix.get(0, 2), 0.001f);
        Assert.assertEquals(sut.get(9), testMatrix.get(1, 2), 0.001f);
        Assert.assertEquals(sut.get(10), testMatrix.get(2, 2), 0.001f);
        Assert.assertEquals(sut.get(11), testMatrix.get(3, 2), 0.001f);
        Assert.assertEquals(sut.get(12), testMatrix.get(0, 3), 0.001f);
        Assert.assertEquals(sut.get(13), testMatrix.get(1, 3), 0.001f);
        Assert.assertEquals(sut.get(14), testMatrix.get(2, 3), 0.001f);
        Assert.assertEquals(sut.get(15), testMatrix.get(3, 3), 0.001f);
    }

    @Test
    public void given4fMatrix_whenViewMatrixIsCreated_thenViewMatrixPropertiesShouldBeSetCorrectly() {
        float eyeX = 1.0f;
        float eyeY = 1.0f;
        float eyeZ = 1.0f;
        float centerX = 2.0f;
        float centerY = 2.0f;
        float centerZ = 2.0f;
        float upX = 3.0f;
        float upY = 3.0f;
        float upZ = 3.0f;

        Vector3f eyeVec = new Vector3f(eyeX, eyeY, eyeZ);
        Vector3f centerVec = new Vector3f(centerX, centerY, centerZ);
        Vector3f upVec = new Vector3f(upX, upY, upZ);
        upVec.normalize();

        Vector3f f = new Vector3f();
        f.sub(centerVec, eyeVec);
        f.normalize();

        Vector3f s = new Vector3f();
        s.cross(f, upVec);
        s.normalize();

        Vector3f u = new Vector3f();
        u.cross(s, f);
        u.normalize();

        Matrix4f sut = MatrixUtils.createViewMatrix(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        sut.transpose();

        Assert.assertEquals(sut.get(0, 0), s.x, 0.001f);
        Assert.assertEquals(sut.get(1, 0), s.y, 0.001f);
        Assert.assertEquals(sut.get(2, 0), s.z, 0.001f);
        Assert.assertEquals(sut.get(0, 1), u.x, 0.001f);
        Assert.assertEquals(sut.get(1, 1), u.y, 0.001f);
        Assert.assertEquals(sut.get(2, 1), u.z, 0.001f);
        Assert.assertEquals(sut.get(0, 2), -f.x, 0.001f);
        Assert.assertEquals(sut.get(1, 2), -f.y, 0.001f);
        Assert.assertEquals(sut.get(2, 2), -f.z, 0.001f);
        Assert.assertEquals(sut.get(0, 3), 0, 0.001f);
        Assert.assertEquals(sut.get(1, 3), 0, 0.001f);
        Assert.assertEquals(sut.get(2, 3), 0, 0.001f);
        Assert.assertEquals(sut.get(3, 3), 1, 0.001f);
        Assert.assertEquals(sut.get(3, 0), -eyeVec.x, 0.001f);
        Assert.assertEquals(sut.get(3, 1), -eyeVec.y, 0.001f);
        Assert.assertEquals(sut.get(3, 2), -eyeVec.z, 0.001f);
    }


}
