package org.terasology.metatesting;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.MainLoop;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@IntegrationEnvironment
public class IntegrationEnvValidationTest {

    @In
    private EntityManager entityManager;

    @In
    private Context context;

    @Test
    public void testEnvironmentSetup(MainLoop mainLoop) {
        System.out.println("Starting testEnvironmentSetup...");

        // 1. Verify Injection
        System.out.println("Verify injection of EntityManager: " + entityManager + ", Context: " + context
                + ", MainLoop: " + mainLoop);
        assertNotNull(entityManager, "EntityManager should be injected by the harness");
        assertNotNull(context, "Context should be injected by the harness");
        assertNotNull(mainLoop, "MainLoop should be injected by JUnit parameter resolution");

        // 2. Verify Game Loop Interaction
        // Run the loop until a simple immediate future completes
        ListenableFuture<String> future = Futures.immediateFuture("Success");
        String result = mainLoop.runUntil(future);

        System.out.println("Game loop interaction verification result: " + result);
        assertThat(result).isEqualTo("Success");

        // 3. Verify Entity System Access
        // Create an entity to ensure the engine is truly active
        // Set a safety timeout to ensure we don't hang if something goes wrong
        mainLoop.setSafetyTimeoutMs(5000);
        System.out.println("Safety timeout set to 5000ms. Waiting for entity creation...");

        boolean timedOut = mainLoop.runUntil(() -> {
            boolean created = entityManager.create() != null;
            if (created) {
                System.out.println("Entity created successfully within loop.");
            }
            return created;
        });
        assertFalse(timedOut, "Entity creation within loop verified.");
    }
}
