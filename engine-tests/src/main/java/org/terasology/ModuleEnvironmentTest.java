// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.reflection.TypeRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class ModuleEnvironmentTest {
    protected ModuleManager moduleManager;
    protected TypeRegistry typeRegistry;

    @BeforeEach
    public void before(@TempDir Path tempHome) throws Exception {
        PathManager.getInstance().useOverrideHomePath(tempHome);

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
