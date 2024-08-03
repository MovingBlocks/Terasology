// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;


@IntegrationEnvironment
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LifecyclePerClassInjectionTest {
    private static GameEngine engineSeenByStaticBeforeAll;
    private static GameEngine engineSeenByInstanceBeforeAll;

    @In
    private Context contextByFieldInjection;
    
    @In
    private GameEngine engineByFieldInjection;

    private GameEngine engineSeenByBeforeEach;

    @BeforeAll
    static void checkingStaticBeforeAllParameterInjection(GameEngine engineParameter, Context contextParameter) {
        assertThat(engineParameter).isNotNull();
        engineSeenByStaticBeforeAll = engineParameter;

        contextParameter.put(FromStaticBeforeAll.class, new FromStaticBeforeAll());
    }

    @BeforeAll
    void checkingInstanceBeforeAllParameterInjection(GameEngine engineParameter, Context contextParameter) {
        assertThat(engineParameter).isNotNull();
        engineSeenByInstanceBeforeAll = engineParameter;
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);

        contextParameter.put(FromInstanceBeforeAll.class, new FromInstanceBeforeAll());
        assertThat(contextByFieldInjection.getMaybe(FromInstanceBeforeAll.class)).isPresent();
    }


    @BeforeEach
    void checkingBeforeEachParameterInjection(GameEngine engineParameter, Context contextParameter) {
        assertThat(engineParameter).isNotNull();
        assertThat(engineByFieldInjection).isNotNull();
        assertThat(engineParameter).isEqualTo(engineSeenByStaticBeforeAll);
        assertThat(engineParameter).isEqualTo(engineSeenByInstanceBeforeAll);
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);
        engineSeenByBeforeEach = engineParameter;

        assertThat(contextParameter.getMaybe(FromInstanceBeforeAll.class)).isPresent();
        assertThat(contextParameter.getMaybe(FromStaticBeforeAll.class)).isPresent();

        contextParameter.put(FromBeforeEach.class, new FromBeforeEach());
        assertThat(contextByFieldInjection.getMaybe(FromBeforeEach.class)).isPresent();
    }

    @Test
    @Order(1)
    void testMethodParameterInjection(GameEngine engineParameter, Context contextParameter) {
        assertThat(engineParameter).isNotNull();
        assertThat(engineParameter).isEqualTo(engineSeenByStaticBeforeAll);
        assertThat(engineParameter).isEqualTo(engineSeenByInstanceBeforeAll);
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);
        assertThat(engineParameter).isEqualTo(engineSeenByBeforeEach);

        assertThat(contextParameter.getMaybe(FromInstanceBeforeAll.class)).isPresent();
        assertThat(contextParameter.getMaybe(FromStaticBeforeAll.class)).isPresent();
        assertThat(contextParameter.getMaybe(FromBeforeEach.class)).isPresent();

        contextParameter.put(FromFirstMethod.class, new FromFirstMethod());
        assertThat(contextByFieldInjection.getMaybe(FromFirstMethod.class)).isPresent();
    }

    @Test
    @Order(2)
    void testSecondMethodSharesContextWithFirst(Context contextParameter) {
        assertThat(contextParameter.getMaybe(FromFirstMethod.class)).isPresent();
        assertThat(contextByFieldInjection.getMaybe(FromFirstMethod.class)).isPresent();
    }

    private static class FromStaticBeforeAll { }

    private static class FromInstanceBeforeAll { }

    private static class FromBeforeEach { }

    private static class FromFirstMethod { }
}
