// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.ModuleEnvironmentTest;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.naming.Name;

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
    @Disabled("Seems to intermittently stall, at least on Win10")
    @Test
    public void testNonModuleTypes() {
        assumeTrue(typeRegistry.getSubtypesOf(Collection.class).contains(TreeSet.class));

        assertTrue(typeRegistry.getSubtypesOf(Map.class).contains(LinkedHashMap.class));
    }

    // TODO: Re-enable as gradlew check seems to still stall even with the Ignore in place?
    @Disabled("Seems to intermittently stall, at least on Win10")
    @Test
    public void testModuleTypes() {
        Set<Name> modulesDeclaringComponents =
                typeRegistry.getSubtypesOf(Component.class).stream()
                        .map(componentClass -> moduleManager.getEnvironment().getModuleProviding(componentClass))
                        .collect(Collectors.toSet());

        assertTrue(modulesDeclaringComponents.contains(new Name("engine")), modulesDeclaringComponents::toString);
    }

    // TODO: Re-enable as gradlew check seems to still stall even with the Ignore in place?
    @Disabled("Seems to intermittently stall, at least on Win10")
    @Test
    public void testWhitelistedTypes() {
        Set<Class<?>> allTypes = typeRegistry.getSubtypesOf(Object.class);
        for (Class<?> whitelisted : ExternalApiWhitelist.CLASSES) {
            assumeTrue(allTypes.contains(whitelisted), () -> allTypes.toString() + " should contain " + whitelisted.getName());
        }
    }
}
