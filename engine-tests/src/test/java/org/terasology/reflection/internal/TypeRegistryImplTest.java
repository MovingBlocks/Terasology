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

import org.junit.Test;
import org.reflections.Reflections;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.entitySystem.Component;
import org.terasology.naming.Name;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TypeRegistryImplTest extends ModuleEnvironmentTest {
    static {
        Reflections.log = null;
    }

    private TypeRegistryImpl typeRegistry = new TypeRegistryImpl();

    @Override
    protected TypeRegistryImpl getTypeRegistry() {
        return typeRegistry;
    }

    @Test
    public void testRegistry() {
        assertTrue(typeRegistry.getSubtypesOf(Collection.class).contains(TreeSet.class));

        Set<Name> modulesDeclaringComponents =
            typeRegistry.getSubtypesOf(Component.class).stream()
                .map(componentClass -> moduleManager.getEnvironment().getModuleProviding(componentClass))
                .collect(Collectors.toSet());

        assertTrue(modulesDeclaringComponents.size() > 2);

        assertTrue(modulesDeclaringComponents.contains(new Name("engine")));
        assertTrue(modulesDeclaringComponents.contains(new Name("Core")));
    }
}
