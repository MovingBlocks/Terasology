// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.modes.StateIngame;

import java.io.IOException;
import java.util.List;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
public class ClientConnectionTest {

    @Test
    public void testClientConnection(ModuleTestingHelper helper) throws IOException {
        Context clientContext = helper.createClient();
        List<TerasologyEngine> engines = helper.getEngines();
        Assertions.assertEquals(2, engines.size());
        Assertions.assertAll(engines
                .stream()
                .map((engine) ->
                        () -> Assertions.assertEquals(StateIngame.class, engine.getState().getClass())));
    }
}
