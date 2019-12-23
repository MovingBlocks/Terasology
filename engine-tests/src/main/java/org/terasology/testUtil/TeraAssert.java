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
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
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
                assertTrue(copyActual.remove(obj), () -> "Missing element: " + obj);
            }
            assertTrue(copyActual.isEmpty(), () -> "Unexpected additional elements: " + copyActual.toString());
        }
    }

    public static void assertEquals(Vector3f expected, Vector3f actual, float error) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            Supplier<String> errorMessageSupplier = () -> "Expected " + expected + ", actual" + actual;
            org.junit.jupiter.api.Assertions.assertEquals(expected.x, actual.x, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.y, actual.y, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.z, actual.z, error, errorMessageSupplier);
        }
    }

    public static void assertEquals(Vector4f expected, Vector4f actual, float error) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            Supplier<String> errorMessageSupplier = () -> "Expected " + expected + ", actual" + actual;
            org.junit.jupiter.api.Assertions.assertEquals(expected.x, actual.x, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.y, actual.y, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.z, actual.z, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.w, actual.w, error, errorMessageSupplier);
        }
    }

    public static void assertEquals(Quat4f expected, Quat4f actual, float error) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            Supplier<String> errorMessageSupplier = () -> "Expected " + expected + ", actual" + actual;
            org.junit.jupiter.api.Assertions.assertEquals(expected.x, actual.x, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.y, actual.y, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.z, actual.z, error, errorMessageSupplier);
            org.junit.jupiter.api.Assertions.assertEquals(expected.w, actual.w, error, errorMessageSupplier);
        }
    }
}
