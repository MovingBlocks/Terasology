// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.reflection;

import org.junit.jupiter.api.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.StandardCollisionGroup;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModuleEnvironmentSandboxTest extends ModuleEnvironmentTest {
    private ModuleEnvironmentSandbox sandbox;

    @Override
    protected void setup() {
        sandbox = new ModuleEnvironmentSandbox(moduleManager, typeRegistry);
    }

    @Test
    public void getSubTypeIdentifier() {
        assertEquals("engine:StandardCollisionGroup", sandbox.getSubTypeIdentifier(StandardCollisionGroup.class, CollisionGroup.class));
    }
}
