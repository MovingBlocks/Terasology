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
package org.terasology.reflection;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.engine.module.ExternalApiWhitelist;
import org.terasology.entitySystem.Component;
import org.terasology.naming.Name;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TypeRegistryTest extends ModuleEnvironmentTest {
    static {
        Reflections.log = null;
    }

    // TODO: Re-enable as gradlew check seems to still stall even with the Ignore in place?
    //@Ignore("Seems to intermittently stall, at least on Win10")
    //@Test
    public void testNonModuleTypes() {
        assumeTrue(typeRegistry.getSubtypesOf(Collection.class).contains(TreeSet.class));

        assertTrue(typeRegistry.getSubtypesOf(Map.class).contains(LinkedHashMap.class));
    }

    // TODO: Re-enable as gradlew check seems to still stall even with the Ignore in place?
    //@Ignore("Seems to intermittently stall, at least on Win10")
    //@Test
    public void testModuleTypes() {
        Set<Name> modulesDeclaringComponents =
                typeRegistry.getSubtypesOf(Component.class).stream()
                        .map(componentClass -> moduleManager.getEnvironment().getModuleProviding(componentClass))
                        .collect(Collectors.toSet());

        assertTrue(modulesDeclaringComponents.contains(new Name("engine")), modulesDeclaringComponents::toString);
    }

    // TODO: Re-enable as gradlew check seems to still stall even with the Ignore in place?
    //@Ignore("Seems to intermittently stall, at least on Win10")
    //@Test
    public void testWhitelistedTypes() {
        Set<Class<?>> allTypes = typeRegistry.getSubtypesOf(Object.class);
        for (Class<?> whitelisted : ExternalApiWhitelist.CLASSES) {
            assumeTrue(allTypes.contains(whitelisted), () -> allTypes.toString() + " should contain " + whitelisted.getName());
        }
    }
}
