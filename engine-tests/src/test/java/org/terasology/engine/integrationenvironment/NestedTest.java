// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;

import static com.google.common.truth.Truth.assertThat;

/**
 * Test the behavior of {@code @Nested} tests.
 * <p>
 * This uses the default {@code PER_METHOD} lifecycle.
 * <p>
 * 🚧 Combining nested tests with a {@code PER_CLASS} lifecycle complicates things
 * significantly, as there's no longer just one class. It may even be possible
 * to use different lifecycles for inner and outer classes.
 * <p>
 * 🚧 If do need that functionality, please contribute test cases.
 *
 * @see <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested"
 *     >JUnit User Guide: Nested Tests</a>
 */
@IntegrationEnvironment
public class NestedTest {
    @In
    public Engines outerEngines;

    @In
    public EntityManager outerManager;

    @Test
    public void outerTestHasFieldInjection() {
        assertThat(outerEngines).isNotNull();
        assertThat(outerManager).isNotNull();
    }

    @Nested
    class NestedTestClass {
        @In
        Engines innerEngines;

        @In
        EntityManager innerManager;

        @Test
        public void outerFieldsInjectedForInnerTest() {
            assertThat(outerEngines).isNotNull();
            assertThat(outerManager).isNotNull();
        }

        @Test
        public void innerFieldsInjectedSameAsOuterFields() {
            assertThat(innerManager).isSameInstanceAs(outerManager);
            assertThat(innerEngines).isSameInstanceAs(outerEngines);
        }
    }
}
