// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    @Tag("flaky")
    public void testClientNetworkMode(TestReporter reporter) throws IOException {
        long start = System.currentTimeMillis();
        Context clientContext = helper.createClient();
        reporter.publishEntry("client_connect_ms", String.valueOf(System.currentTimeMillis() - start));

        NetworkSystem networkSystem = clientContext.get(NetworkSystem.class);
        assertNotNull(networkSystem, "NetworkSystem should exist in client context");
        assertEquals(NetworkMode.CLIENT, networkSystem.getMode(),
                "Client NetworkSystem should be in CLIENT mode");

        reporter.publishEntry("total_ms", String.valueOf(System.currentTimeMillis() - start));
    }

    @Test
    @Tag("flaky")
    public void testClientHasNetworkSubsystem(TestReporter reporter) throws IOException {
        long start = System.currentTimeMillis();
        Context clientContext = helper.createClient();
        reporter.publishEntry("client_connect_ms", String.valueOf(System.currentTimeMillis() - start));

        GameEngine gameEngine = clientContext.get(GameEngine.class);
        assertNotNull(gameEngine, "Client should have a GameEngine in context");
        assertInstanceOf(TerasologyEngine.class, gameEngine, "GameEngine should be a TerasologyEngine");
        TerasologyEngine engine = (TerasologyEngine) gameEngine;

        boolean hasNetworkSubsystem = false;
        for (EngineSubsystem subsystem : engine.getSubsystems()) {
            if (subsystem instanceof NetworkSubsystem) {
                hasNetworkSubsystem = true;
                break;
            }
        }
        assertTrue(hasNetworkSubsystem,
                "NetworkSubsystem should be registered in the client engine");

        reporter.publishEntry("total_ms", String.valueOf(System.currentTimeMillis() - start));
    }
}
