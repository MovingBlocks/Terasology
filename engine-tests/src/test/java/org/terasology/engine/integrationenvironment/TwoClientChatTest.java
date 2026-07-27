// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that a ChatMessageEvent sent from the host reaches a connected
 * client via network event propagation in a LISTEN_SERVER environment.
 * <p>
 * ChatMessageEvent is an {@code @OwnerEvent} — it replicates to the entity's
 * owner. The host sends it to each connected client entity individually,
 * mimicking what ChatSystem does during normal chat.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class TwoClientChatTest {
    private static final Logger logger = LoggerFactory.getLogger(TwoClientChatTest.class);

    @In
    private ModuleTestingHelper helper;

    @Test
    @Tag("flaky")
    public void testChatReachesClient(TestReporter reporter) throws Exception {
        long startTime = System.currentTimeMillis();

        // Create two clients
        helper.createClient();
        long client1Ms = System.currentTimeMillis() - startTime;
        reporter.publishEntry("client1_connect_ms", String.valueOf(client1Ms));
        logger.info("Client 1 connected in {}ms", client1Ms);

        long client2Start = System.currentTimeMillis();
        Context client2Ctx = helper.createClient();
        assertNotNull(client2Ctx, "Client 2 context should be created");
        long client2Ms = System.currentTimeMillis() - client2Start;
        long client2TotalMs = System.currentTimeMillis() - startTime;
        reporter.publishEntry("client2_connect_ms", String.valueOf(client2Ms));
        logger.info("Client 2 connected in {}ms (total: {}ms)", client2Ms, client2TotalMs);

        // Wait for both clients to register on the host
        NetworkSystem hostNetwork = helper.getHostContext().get(NetworkSystem.class);
        helper.awaitClients(2);
        assertThat(hostNetwork.getPlayers()).hasSize(2);
        long registeredMs = System.currentTimeMillis() - startTime;
        reporter.publishEntry("both_registered_ms", String.valueOf(registeredMs));
        logger.info("Both clients registered on host at {}ms", registeredMs);

        // Register a ChatMessageEvent probe on client 2
        ChatProbe probe = new ChatProbe();
        client2Ctx.get(ComponentSystemManager.class).register(probe);

        // Pick a sender and send ChatMessageEvent to each client entity from the host
        // (mimics ChatSystem — sends to each client's entity individually)
        Client senderClient = hostNetwork.getPlayers().iterator().next();
        EntityRef senderInfo = senderClient.getEntity().getComponent(ClientComponent.class).clientInfo;
        String testMessage = "hello from host";

        for (Client client : hostNetwork.getPlayers()) {
            client.getEntity().send(new ChatMessageEvent(testMessage, senderInfo));
        }
        long messagesSentMs = System.currentTimeMillis() - startTime;
        reporter.publishEntry("messages_sent_ms", String.valueOf(messagesSentMs));
        logger.info("Chat messages sent at {}ms", messagesSentMs);

        // Wait for client 2's probe to receive the message
        helper.awaitUntil("client 2 to receive the chat message", () -> probe.received);

        long totalMs = System.currentTimeMillis() - startTime;
        reporter.publishEntry("total_ms", String.valueOf(totalMs));
        logger.info("Probe received at {}ms (total test time)", totalMs);

        assertThat(probe.received).isTrue();
        assertThat(probe.lastMessage).contains(testMessage);
    }

    public static class ChatProbe extends BaseComponentSystem {
        boolean received;
        String lastMessage = "";

        @ReceiveEvent(components = ClientComponent.class)
        public void onChatMessage(ChatMessageEvent event, EntityRef entity) {
            received = true;
            lastMessage = event.getMessage();
        }
    }
}
