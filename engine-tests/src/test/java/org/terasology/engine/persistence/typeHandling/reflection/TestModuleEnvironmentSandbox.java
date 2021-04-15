// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;
import org.terasology.unittest.ExampleInterface;

import static org.terasology.engine.testUtil.Assertions.assertNotEmpty;

@SuppressWarnings("FieldCanBeLocal")
public class TestModuleEnvironmentSandbox {

    private ModuleEnvironmentSandbox sandbox;
    private TypeRegistry typeRegistry;
    private ModuleManager moduleManager;
    private ModuleEnvironment environment;

    @BeforeEach
    protected void provideSandbox() throws Exception {
        moduleManager = ModuleManagerFactory.create();
        environment = moduleManager.getEnvironment();

        // TODO: make unit test more focused by not relying on complex ModuleManager interactions
        typeRegistry = new ModuleTypeRegistry(environment);

        sandbox = new ModuleEnvironmentSandbox(moduleManager, typeRegistry);

        // module = environment.get(new Name("unittest"));
    }

    @Test
    public void findSubtypeOfGlobalClass() {
        assertNotEmpty(sandbox.findSubTypeOf("org.joml.Vector3f", org.joml.Vector3fc.class));
    }

    @Test
    public void findSubtypeOfEngineClass() {
        assertNotEmpty(sandbox.findSubTypeOf("engine:BlockComponent", Component.class));
    }

    @Test
    public void findSubtypeOfModuleClass() {
        assertNotEmpty(sandbox.findSubTypeOf("unittest:ExampleClass", ExampleInterface.class));
    }
}
