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
package org.terasology.reflection.internal;

import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

public class TypeRegistryImplTest {
    private TypeRegistryImpl typeRegistry = new TypeRegistryImpl();

    @Before
    public void setup() {
        Reflections.log = null;
    }

    @Test
    public void testRegistry() {
        assertTrue(typeRegistry.getSubtypesOf(Collection.class).contains(TreeSet.class));
    }
}
