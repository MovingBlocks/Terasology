// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.registry.In;

import static com.google.common.truth.Truth.assertThat;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
public class SharedInjectionTest {
    private static GameEngine engineSeenByBeforeAll;

    @In
    private GameEngine engineByFieldInjection;

    private GameEngine engineSeenByBeforeEach;

    @BeforeAll
    static void checkingBeforeAllParameterInjection(GameEngine engineParameter) {
        assertThat(engineParameter).isNotNull();
        engineSeenByBeforeAll = engineParameter;
    }

    @BeforeEach
    void checkingBeforeEachParameterInjection(GameEngine engineParameter) {
        assertThat(engineParameter).isNotNull();
        assertThat(engineByFieldInjection).isNotNull();
        assertThat(engineParameter).isEqualTo(engineSeenByBeforeAll);
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);
        engineSeenByBeforeEach = engineParameter;
    }

    @Test
    void testMethodParameterInjection(GameEngine engineParameter) {
        assertThat(engineParameter).isNotNull();
        assertThat(engineParameter).isEqualTo(engineSeenByBeforeAll);
        assertThat(engineParameter).isEqualTo(engineByFieldInjection);
        assertThat(engineParameter).isEqualTo(engineSeenByBeforeEach);
    }

}
