// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@IntegrationEnvironment
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LifecyclePerMethodInjectionTest {

    @In
    private Context contextByFieldInjection;
    
    @In
    private GameEngine engineByFieldInjection;
    
    private GameEngine engineSeenByBeforeEach;

    @BeforeAll
    static void checkingBeforeAllParameterInjection() {
        // Static @BeforeAll is not associated with a TestInstance,
        // and is not provided with parameter injection.
    }

    @BeforeEach
    void checkingBeforeEachParameterInjection(GameEngine engineParameter, Context contextParameter) {
        assertThat(engineParameter).isNotNull();
        assertThat(engineByFieldInjection).isNotNull();
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);
        engineSeenByBeforeEach = engineParameter;

        // Add a new thing to the context.
        contextParameter.put(FromBeforeEach.class, new FromBeforeEach());

        // It should also be visible on the field.
        assertThat(contextByFieldInjection.getMaybe(FromBeforeEach.class)).isPresent();
    }

    @Test
    void testMethodParameterInjection(GameEngine engineParameter, Context contextParameter) {
        assertThat(engineParameter).isNotNull();
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);
        assertThat(engineParameter).isEqualTo(engineSeenByBeforeEach);

        assertThat(contextParameter.getMaybe(FromBeforeEach.class)).isPresent();
        assertThat(contextByFieldInjection.getMaybe(FromBeforeEach.class)).isPresent();

        assertThat(contextByFieldInjection.getMaybe(FromSecondMethod.class)).isEmpty();

        // Add a new thing to the context.
        contextParameter.put(FromFirstMethod.class, new FromFirstMethod());

        // It should also be visible on the field.
        assertThat(contextByFieldInjection.getMaybe(FromFirstMethod.class)).isPresent();
    }

    @Test
    void testSecondMethodIsNotPollutedByFirst(Context contextParameter) {
        assertThat(contextByFieldInjection.getMaybe(FromFirstMethod.class)).isEmpty();
        contextParameter.put(FromSecondMethod.class, new FromSecondMethod());
    }

    private static class FromBeforeEach { }

    private static class FromFirstMethod { }

    private static class FromSecondMethod { }
}
