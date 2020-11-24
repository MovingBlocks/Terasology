// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.gson.models;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TestClass {
    private final TestColor color;
    private final Set<TestVector4f> vector4fs;
    private final Map<String, TestRect2i> rect2iMap;

    // Will not be serialized
    private final Map<Integer, Integer> intMap;

    private final int i;

    public TestClass(TestColor color,
                     Set<TestVector4f> vector4fs, Map<String, TestRect2i> rect2iMap,
                     Map<Integer, Integer> intMap, int i) {
        this.color = color;
        this.vector4fs = vector4fs;
        this.rect2iMap = rect2iMap;
        this.intMap = intMap;
        this.i = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass testClass = (TestClass) o;
        return i == testClass.i &&
                Objects.equals(color, testClass.color) &&
                Objects.equals(vector4fs, testClass.vector4fs) &&
                Objects.equals(rect2iMap, testClass.rect2iMap);
    }
}
