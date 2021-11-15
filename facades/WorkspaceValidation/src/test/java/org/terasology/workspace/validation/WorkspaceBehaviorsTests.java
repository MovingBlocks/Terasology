// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import org.junit.jupiter.params.provider.Arguments;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.management.AssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.gestalt.naming.Name;

import java.util.Set;
import java.util.stream.Stream;

public class WorkspaceBehaviorsTests {

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

    void behaviorTest() {

    }
}
