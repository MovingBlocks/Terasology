/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.reflection.reflect;

import org.junit.Test;
import org.terasology.reflection.TypeInfo;

import java.util.List;

import static org.junit.Assert.*;

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
