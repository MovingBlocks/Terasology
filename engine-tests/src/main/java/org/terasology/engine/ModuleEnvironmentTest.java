// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Tag("MteTest")
public abstract class ModuleEnvironmentTest {
    protected ModuleManager moduleManager;
    protected ModuleTypeRegistry typeRegistry;

    @BeforeEach
    public void before(@TempDir Path tempHome) throws IOException {
        PathManager.getInstance().useOverrideHomePath(tempHome);

        moduleManager = ModuleManagerFactory.create();
        TypeRegistry.WHITELISTED_CLASSES = ExternalApiWhitelist.CLASSES.stream().map(Class::getName).collect(Collectors.toSet());
        TypeRegistry.WHITELISTED_PACKAGES = ExternalApiWhitelist.PACKAGES;
        typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());

        setup();
    }

    protected void setup() {

    }
}
