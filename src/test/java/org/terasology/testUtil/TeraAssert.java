package org.terasology.testUtil;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import java.util.Collection;
import java.util.List;


/**
 * @author Immortius <immortius@gmail.com>
 */
public final class TeraAssert {
    private TeraAssert() {}

    public static void assertEqualsContent(Collection expected, Collection actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            List copyActual = Lists.newArrayList(actual);
            for (Object obj : expected) {
                assertTrue("Missing element: " + obj, copyActual.remove(obj));
            }
            assertTrue("Unexpected additional elements: " + copyActual.toString(), copyActual.isEmpty());
        }
    }

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
