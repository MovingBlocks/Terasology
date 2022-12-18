// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.integrationenvironment.fixtures.BaseTestingClass;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;

@IntegrationEnvironment
public class SubclassInjectionTest extends BaseTestingClass {
    @Test
    public void testInjection() {
        // ensure the superclass's private fields were injected correctly
        Assertions.assertNotNull(getEntityManager());
        Assertions.assertNotNull(getHelper());
    }
}
