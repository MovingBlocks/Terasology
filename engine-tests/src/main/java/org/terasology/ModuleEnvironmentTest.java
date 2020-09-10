// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology;

import org.junit.jupiter.api.BeforeEach;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.nui.reflection.TypeRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class ModuleEnvironmentTest {
    protected ModuleManager moduleManager;
    protected TypeRegistry typeRegistry;

    @BeforeEach
    public void before() throws Exception {
        PathManager.getInstance().useOverrideHomePath(
                Paths.get(System.getProperty("java.io.tmpdir"),
                        "terasology",
                        UUID.randomUUID().toString()));

        moduleManager = ModuleManagerFactory.create();

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(moduleManager.getRegistry().getModuleIds());

        assumeTrue(result.isSuccess());

        ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), true);
        typeRegistry = new TypeRegistry(environment);

        setup();
    }

    protected void setup() {

    }
}
