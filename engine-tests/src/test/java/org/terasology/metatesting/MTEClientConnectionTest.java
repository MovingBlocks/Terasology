package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class MTEClientConnectionTest {

    @In
    private ModuleTestingHelper helper;

    @In
    private EntityManager hostEntityManager;

    @Test
    public void testClientConnection() throws IOException {
        System.out.println("Starting testClientConnection...");

        // 1. Verify Host Setup
        assertNotNull(helper, "ModuleTestingHelper should be injected");
        assertNotNull(hostEntityManager, "Host EntityManager should be injected");

        List<TerasologyEngine> engines = helper.getEngines();
        assertThat(engines).hasSize(1); // Should start with just the host
        System.out.println("Host engine verified.");

        // 2. Create Client
        System.out.println("Creating client...");
        Context clientContext = helper.createClient();
        assertNotNull(clientContext, "Client context should be returned");

        // 3. Verify Client Context Isolation
        EntityManager clientEntityManager = clientContext.get(EntityManager.class);
        assertNotNull(clientEntityManager, "Client should have its own EntityManager");
        assertThat(clientEntityManager).isNotEqualTo(hostEntityManager);
        System.out.println("Client context isolation verified.");

        // 4. Verify Connection
        // We need to run the loop to let the connection handshake complete
        // The client creation might have already advanced the loop somewhat, but let's
        // ensure stability
        helper.runUntil(() -> engines.size() == 2);

        assertThat(helper.getEngines()).hasSize(2); // Host + 1 Client
        System.out.println("Client engine instance verified in engine list.");

        // Check NetworkSystem on host to see if it acknowledges the client
        NetworkSystem hostNetwork = helper.getHostContext().get(NetworkSystem.class);

        // Wait until we have a client connected
        helper.runUntil(() -> {
            for (org.terasology.engine.network.Client client : hostNetwork.getPlayers()) {
                if (client.getEntity().getComponent(org.terasology.engine.network.ClientComponent.class) != null) {
                    return true;
                }
            }
            return false;
        });

        System.out.println("testClientConnection passed!");
    }
}
