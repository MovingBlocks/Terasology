package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.In;

import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class MTEClientSystemTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    public void testClientSystemExecution() throws java.io.IOException {
        System.out.println("Starting testClientSystemExecution...");

        // Create a client
        Context clientContext = helper.createClient();
        System.out.println("Client Context created.");

        // Set a safety timeout for the run loop
        helper.setSafetyTimeoutMs(30000);

        // Inspect ComponentSystemManager
        ComponentSystemManager csm = clientContext.get(ComponentSystemManager.class);
        System.out.println("Client CSM: " + csm);
        System.out.println("Client CSM Active? " + (csm != null ? csm.isActive() : "null"));

        // Register a test system on the client
        TestSystem testSystem = new TestSystem();
        if (csm != null) {
            csm.register(testSystem);
            System.out.println("Registered TestSystem on Client.");
        }

        // Try to get Engine to check state
        GameEngine engine = clientContext.get(GameEngine.class);
        System.out.println("Client Engine: " + engine);
        if (engine != null) {
            System.out.println("Client Engine State: " + engine.getState());
        }

        // Run the engine for a few ticks (wait for the system to be updated multiple times)
        int targetUpdates = 10;
        try {
            helper.runUntil(() -> {
                if (testSystem.updateCount % 10 == 0 && testSystem.updateCount > 0) {
                    // Log every 10 updates to show progress without spamming
                    System.out.println("Updates received: " + testSystem.updateCount);
                }
                return testSystem.updateCount >= targetUpdates;
            });
        } catch (com.google.common.util.concurrent.UncheckedTimeoutException e) {
            System.out.println("TIMEOUT REACHED!");
            System.out.println("Final Update Count: " + testSystem.updateCount);
            System.out.println("Client CSM Active? " + (csm != null ? csm.isActive() : "null"));
            if (engine != null) {
                System.out.println("Client Engine State: " + engine.getState());
            }
            throw e;
        }

        System.out.println("Client system received " + testSystem.updateCount + " updates.");
        assertTrue(testSystem.updateCount >= targetUpdates, "Client system should have received at least " + targetUpdates + " updates");
    }

    public static class TestSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
        public int updateCount = 0;

        @Override
        public void update(float delta) {
            updateCount++;
        }
    }
}
