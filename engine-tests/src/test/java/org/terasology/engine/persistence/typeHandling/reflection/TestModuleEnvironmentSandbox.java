// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.TypeRegistry;

import static org.terasology.engine.testUtil.Assertions.assertNotEmpty;

public class TestModuleEnvironmentSandbox {

    private ModuleEnvironmentSandbox sandbox;
    private TypeRegistry typeRegistry;

    @BeforeEach
    protected void provideSandbox() throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();
        // TODO: make unit test more focused by not relying on complex ModuleManager interactions
        typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());

        sandbox = new ModuleEnvironmentSandbox(moduleManager, typeRegistry);
    }

    @Test
    public void findSubtypeOfGlobalClass() {
        assertNotEmpty(sandbox.findSubTypeOf("Vector3f", org.joml.Vector3fc.class));
    }

    @Test
    public void findSubtypeOfEngineClass() {
        // assertNotEmpty(typeRegistry.getSubtypesOf(Component.class));
        assertNotEmpty(sandbox.findSubTypeOf("BlockComponent", Component.class));
    }

    @Disabled("TODO")
    public void findSubtypeOfModuleClass() {

    }
}
