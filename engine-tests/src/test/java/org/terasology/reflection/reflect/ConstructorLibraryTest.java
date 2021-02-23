// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.reflect;

import org.junit.Test;
import org.terasology.reflection.TypeInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstructorLibraryTest {
    private final ConstructorLibrary library = new ConstructorLibrary();

    @Test
    public void testArray() {
        ObjectConstructor<List<String>[]> constructor = library.get(new TypeInfo<List<String>[]>() {});

        List<String>[] constructed = constructor.construct();

        assertEquals(0, constructed.length);
    }

    @Test
    public void testMultidimensionalArray() {
        ObjectConstructor<String[][]> constructor = library.get(new TypeInfo<String[][]>() {});

        String[][] constructed = constructor.construct();

        assertEquals(0, constructed.length);
    }
}
