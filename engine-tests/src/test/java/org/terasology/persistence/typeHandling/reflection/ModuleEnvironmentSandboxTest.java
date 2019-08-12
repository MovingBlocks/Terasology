/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.reflection;

import org.junit.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;

import static org.junit.Assert.assertEquals;

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
