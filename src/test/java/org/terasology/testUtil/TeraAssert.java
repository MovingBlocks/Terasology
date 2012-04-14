package org.terasology.testUtil;

import groovy.lang.Tuple;

import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;

import static org.junit.Assert.*;


/**
 * @author Immortius <immortius@gmail.com>
 */
public final class TeraAssert {
    private TeraAssert() {}
    
    public static void assertEquals(Tuple3f expected, Tuple3f actual, float error) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            String errorMessage = "Expected " + expected + ", actual" + actual;
            org.junit.Assert.assertEquals(errorMessage, expected.x, actual.x, error);
            org.junit.Assert.assertEquals(errorMessage, expected.y, actual.y, error);
            org.junit.Assert.assertEquals(errorMessage, expected.z, actual.z, error);
        }
    }

    public static void assertEquals(Tuple4f expected, Tuple4f actual, float error) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            String errorMessage = "Expected " + expected + ", actual" + actual;
            org.junit.Assert.assertEquals(errorMessage, expected.x, actual.x, error);
            org.junit.Assert.assertEquals(errorMessage, expected.y, actual.y, error);
            org.junit.Assert.assertEquals(errorMessage, expected.z, actual.z, error);
            org.junit.Assert.assertEquals(errorMessage, expected.w, actual.w, error);
        }
    }
}
