// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.reflection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;
import org.terasology.unittest.ExampleInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.terasology.engine.testUtil.Assertions.assertNotEmpty;

@SuppressWarnings("FieldCanBeLocal")
public class TestModuleEnvironmentSandbox {

    private ModuleEnvironmentSandbox sandbox;
    private TypeRegistry typeRegistry;
    private ModuleManager moduleManager;
    private ModuleEnvironment environment;
    private Path originalHomePath;

    @BeforeEach
    protected void provideSandbox(@TempDir Path tempHome) throws Exception {
        originalHomePath = PathManager.getInstance().getHomePath();
        PathManager.getInstance().useOverrideHomePath(tempHome);
        moduleManager = ModuleManagerFactory.create();
        environment = moduleManager.getEnvironment();

        // TODO: make unit test more focused by not relying on complex ModuleManager interactions
        typeRegistry = new ModuleTypeRegistry(environment);

        sandbox = new ModuleEnvironmentSandbox(moduleManager, typeRegistry);

        // module = environment.get(new Name("unittest"));
    }

    /**
     * Put the PathManager singleton back where we found it. Without this the global instance keeps
     * pointing at the {@link TempDir} above, which JUnit deletes once this class finishes - any later
     * test class reading {@code PathManager.getHomePath()} then hits a directory that is gone. Same
     * restore (and the same already-deleted guard) as PathManagerTest.
     */
    @AfterEach
    public void restorePathManager() throws IOException {
        if (originalHomePath != null && Files.isDirectory(originalHomePath)) {
            PathManager.getInstance().useOverrideHomePath(originalHomePath);
        }
    }

    @Test
    public void findSubtypeOfGlobalClass() {
        assertNotEmpty(sandbox.findSubTypeOf("org.joml.Vector3f", org.joml.Vector3fc.class));
    }

    @Test
    public void findSubtypeOfEngineClass() {
        assertNotEmpty(sandbox.findSubTypeOf("engine:AbstractBlockFamily", BlockFamily.class));
    }

    @Test
    public void findSubtypeOfModuleClass() {
        assertNotEmpty(sandbox.findSubTypeOf("unittest:ExampleClass", ExampleInterface.class));
    }
}
