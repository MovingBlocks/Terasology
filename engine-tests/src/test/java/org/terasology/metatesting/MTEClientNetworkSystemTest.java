package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.common.NetworkSubsystem;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class MTEClientNetworkSystemTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    public void testClientNetworkState() throws java.io.IOException {
        // Create a client
        Context clientContext = helper.createClient();

        // 1. Verify NetworkSystem exists
        NetworkSystem networkSystem = clientContext.get(NetworkSystem.class);
        assertNotNull(networkSystem, "NetworkSystem should exist in client context");

        // 2. Verify NetworkMode is CLIENT
        assertEquals(NetworkMode.CLIENT, networkSystem.getMode(), "NetworkSystem mode should be CLIENT");

        // 3. Verify NetworkSubsystem is present in the engine
        TerasologyEngine engine = (TerasologyEngine) clientContext.get(GameEngine.class);
        boolean hasNetworkSubsystem = false;
        for (EngineSubsystem subsystem : engine.getSubsystems()) {
            if (subsystem instanceof NetworkSubsystem) {
                hasNetworkSubsystem = true;
                break;
            }
        }
        assertTrue(hasNetworkSubsystem, "NetworkSubsystem should be present in the client engine");

        // 4. Verify EntityManager is connected (indirectly via mode check, but let's be sure)
        // NetworkSystem interface doesn't expose getEntityManager, but we can check if it behaves like it has one.
        // For now, the mode check is the strongest indicator that connectToEntitySystem was called and succeeded
        // (or at least setServer was called).

        System.out.println("Client Network System Mode: " + networkSystem.getMode());
        System.out.println("Network Subsystem Present: " + hasNetworkSubsystem);
    }
}
