// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.modes.StateIngame;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;

import java.io.IOException;
import java.util.List;

@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class ClientConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionTest.class);

    @Test
    public void testClientConnection(ModuleTestingHelper helper) throws IOException {
        helper.createClient();
        List<TerasologyEngine> engines = helper.getEngines();
        Assertions.assertEquals(2, engines.size());

        // Host binds to an OS-assigned ephemeral port (0), not the fixed default.cfg one - so
        // parallel test hosts never collide on the same port. See NetworkSystem#getBoundPort().
        int boundPort = helper.getHostContext().get(NetworkSystem.class).getBoundPort();
        Assertions.assertTrue(boundPort > 0, "expected an OS-assigned port, got " + boundPort);
        Assertions.assertNotEquals(25777, boundPort, "host used the fixed default port instead of an ephemeral one");
        logger.info("Engine 0 is {}", engines.get(0));
        logger.info("Engine 1 is {}", engines.get(1));
        Assertions.assertAll(engines
                .stream()
                .map((engine) ->
                        () -> Assertions.assertEquals(StateIngame.class, engine.getState().getClass(),
                                "Unexpected engine state: " + engine + " is in state " + engine.getState().toString())));
    }
}
