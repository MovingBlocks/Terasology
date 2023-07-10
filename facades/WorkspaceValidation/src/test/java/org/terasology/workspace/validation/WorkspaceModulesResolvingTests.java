// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.naming.Name;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Example test for  check modules dependency validations.
 */
public class WorkspaceModulesResolvingTests {

    public static Stream<Arguments> modulesAndModuleManager() {
        System.setProperty(ModuleManager.LOAD_CLASSPATH_MODULES_PROPERTY, "true");
        ModuleManager temporary = new ModuleManager("");
        return temporary.getRegistry()
                .getModuleIds()
                .stream()
                .map((name) -> Arguments.of(temporary, name));
    }

    public static Stream<Arguments> modulePairsAndModuleManager() {
        System.setProperty(ModuleManager.LOAD_CLASSPATH_MODULES_PROPERTY, "true");
        ModuleManager temporary = new ModuleManager("");
        Set<Name> moduleIds = temporary.getRegistry()
                .getModuleIds();
        return moduleIds
                .stream()
                .flatMap((name) -> moduleIds.stream().map((name2) -> Arguments.of(temporary, name, name2)));
    }

    @DisplayName("Try to resolve and load module")
    @ParameterizedTest(name = "{displayName} - {1}")
    @MethodSource("modulesAndModuleManager")
    void resolveAndLoadModule(ModuleManager moduleManager, Name moduleName) {
        moduleManager.resolveAndLoadEnvironment(moduleName);
        Assertions.assertNotNull(moduleManager.getEnvironment());
    }

    @DisplayName("Try to resolve and load pair modules")
    @ParameterizedTest(name = "{displayName} - [{1}, {2}]")
    @MethodSource("modulePairsAndModuleManager")
    void resolveAndLoadPairModules(ModuleManager moduleManager, Name moduleName1, Name moduleName2) {
        moduleManager.resolveAndLoadEnvironment(moduleName1, moduleName2);
        Assertions.assertNotNull(moduleManager.getEnvironment());
    }
}
