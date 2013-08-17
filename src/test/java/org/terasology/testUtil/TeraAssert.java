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
package org.terasology.testUtil;

import com.google.common.collect.Lists;

import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Immortius <immortius@gmail.com>
 */
public final class TeraAssert {
    private TeraAssert() {
    }

    public static <T> void assertEqualsContent(Collection<? extends T> expected, Collection<? extends T> actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            List<? extends T> copyActual = Lists.newArrayList(actual);
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
