// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.common.NetworkSubsystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a client created via MTE has the correct network state:
 * NetworkSystem in CLIENT mode and a NetworkSubsystem registered.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class ClientNetworkStateTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    public void testClientNetworkMode() throws IOException {
        Context clientContext = helper.createClient();

        NetworkSystem networkSystem = clientContext.get(NetworkSystem.class);
        assertNotNull(networkSystem, "NetworkSystem should exist in client context");
        assertEquals(NetworkMode.CLIENT, networkSystem.getMode(),
                "Client NetworkSystem should be in CLIENT mode");
    }

    @Test
    public void testClientHasNetworkSubsystem() throws IOException {
        Context clientContext = helper.createClient();

        TerasologyEngine engine = (TerasologyEngine) clientContext.get(GameEngine.class);
        assertNotNull(engine, "Client should have a GameEngine in context");

        boolean hasNetworkSubsystem = false;
        for (EngineSubsystem subsystem : engine.getSubsystems()) {
            if (subsystem instanceof NetworkSubsystem) {
                hasNetworkSubsystem = true;
                break;
            }
        }
        assertTrue(hasNetworkSubsystem,
                "NetworkSubsystem should be registered in the client engine");
    }
}
