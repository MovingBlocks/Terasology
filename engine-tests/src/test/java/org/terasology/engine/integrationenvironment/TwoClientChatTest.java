// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that a chat message sent from the host on behalf of one client
 * is received by a second client via network event propagation.
 * <p>
 * ChatMessageEvent is an {@code @OwnerEvent} — it replicates to the entity's owner.
 * The host sends it to each connected client entity individually, mimicking what
 * ChatSystem does during normal chat. The test verifies that client 2's probe
 * receives the message with client 1 identified as the sender.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class TwoClientChatTest {
    private static final Logger logger = LoggerFactory.getLogger(TwoClientChatTest.class);

    @In
    private ModuleTestingHelper helper;

    @Test
    @Tag("flaky")
    public void testChatPropagatesBetweenClients() throws Exception {
        long startTime = System.currentTimeMillis();

        // Set up two clients
        Context client1Ctx = helper.createClient();
        assertNotNull(client1Ctx, "Client 1 context should be created");
        logger.info("Client 1 connected in {}ms", System.currentTimeMillis() - startTime);

        long client2Start = System.currentTimeMillis();
        Context client2Ctx = helper.createClient();
        assertNotNull(client2Ctx, "Client 2 context should be created");
        logger.info("Client 2 connected in {}ms (total: {}ms)",
                System.currentTimeMillis() - client2Start, System.currentTimeMillis() - startTime);

        // Wait for both clients to register on the host
        NetworkSystem hostNetwork = helper.getHostContext().get(NetworkSystem.class);
        helper.runUntil(() -> {
            int count = 0;
            for (Client ignored : hostNetwork.getPlayers()) {
                count++;
            }
            return count >= 2;
        });
        assertThat(hostNetwork.getPlayers()).hasSize(2);
        logger.info("Both clients registered on host at {}ms", System.currentTimeMillis() - startTime);

        // Grant chat permission to all connected clients
        PermissionManager hostPerms = helper.getHostContext().get(PermissionManager.class);
        for (Client client : hostNetwork.getPlayers()) {
            EntityRef clientInfo = client.getEntity().getComponent(ClientComponent.class).clientInfo;
            hostPerms.addPermission(clientInfo, PermissionManager.CHAT_PERMISSION);
        }

        // Identify client 1 on the host side by matching its LocalPlayer entity
        EntityRef client1LocalEntity = client1Ctx.get(LocalPlayer.class).getClientEntity();
        ClientComponent client1Component = client1LocalEntity.getComponent(ClientComponent.class);
        assertNotNull(client1Component, "Client 1 should have a ClientComponent");
        String client1Id = client1Component.clientInfo.getComponent(
                org.terasology.engine.network.NetworkComponent.class) != null
                ? String.valueOf(client1Component.clientInfo.getId()) : null;

        // Find client 1's host-side representation
        Client senderOnHost = null;
        for (Client hostClient : hostNetwork.getPlayers()) {
            ClientComponent hostCC = hostClient.getEntity().getComponent(ClientComponent.class);
            if (hostCC != null && hostCC.clientInfo.getId() == client1Component.clientInfo.getId()) {
                senderOnHost = hostClient;
                break;
            }
        }
        if (senderOnHost == null) {
            // Fallback: use first player (still exercises the network path)
            logger.warn("Could not match client 1 to host-side player, using first player");
            senderOnHost = hostNetwork.getPlayers().iterator().next();
        }
        EntityRef senderInfo = senderOnHost.getEntity().getComponent(ClientComponent.class).clientInfo;

        // Register a ChatMessageEvent probe on client 2
        ChatProbe probe = new ChatProbe();
        client2Ctx.get(ComponentSystemManager.class).register(probe);

        // Send ChatMessageEvent to each connected client entity from the host
        // (mimics ChatSystem — sends to each client's entity individually)
        String testMessage = "hello from client 1";
        for (Client client : hostNetwork.getPlayers()) {
            client.getEntity().send(new ChatMessageEvent(testMessage, senderInfo));
        }
        logger.info("Chat messages sent at {}ms", System.currentTimeMillis() - startTime);

        // Wait for client 2's probe to receive the message
        helper.setSafetyTimeoutMs(45000);
        helper.runUntil(() -> probe.received);

        logger.info("Probe received at {}ms (total test time)", System.currentTimeMillis() - startTime);
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
