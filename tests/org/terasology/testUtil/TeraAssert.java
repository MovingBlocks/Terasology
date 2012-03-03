package org.terasology.testUtil;

import javax.vecmath.Vector3f;

import static org.junit.Assert.*;


/**
 * @author Immortius <immortius@gmail.com>
 */
public final class TeraAssert {
    private TeraAssert() {}
    
    public static void assertEquals(Vector3f expected, Vector3f actual, float error) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            org.junit.Assert.assertEquals(expected.x, actual.x, error);
            org.junit.Assert.assertEquals(expected.y, actual.y, error);
            org.junit.Assert.assertEquals(expected.z, actual.z, error);
        }
    }
}
