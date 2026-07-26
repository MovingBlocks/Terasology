// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.In;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a system registered on a client receives update ticks
 * through the MTE game loop.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class ClientSystemUpdateTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    @Tag("flaky")
    public void testClientSystemReceivesUpdates(TestReporter reporter) throws IOException {
        long start = System.currentTimeMillis();
        Context clientContext = helper.createClient();
        reporter.publishEntry("client_connect_ms", String.valueOf(System.currentTimeMillis() - start));

        ComponentSystemManager csm = clientContext.get(ComponentSystemManager.class);
        assertNotNull(csm, "Client should have a ComponentSystemManager");
        assertTrue(csm.isActive(), "Client ComponentSystemManager should be active");

        TickCountingSystem tickCounter = new TickCountingSystem();
        csm.register(tickCounter);

        int targetTicks = 5;
        helper.awaitUntil("the client system to receive " + targetTicks + " updates",
                () -> tickCounter.updateCount >= targetTicks);

        reporter.publishEntry("ticks_received", String.valueOf(tickCounter.updateCount));
        reporter.publishEntry("total_ms", String.valueOf(System.currentTimeMillis() - start));

        assertTrue(tickCounter.updateCount >= targetTicks,
                "Client system should have received at least " + targetTicks
                        + " updates, got " + tickCounter.updateCount);
    }

    public static class TickCountingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
        int updateCount;

        @Override
        public void update(float delta) {
            updateCount++;
        }
    }
}
