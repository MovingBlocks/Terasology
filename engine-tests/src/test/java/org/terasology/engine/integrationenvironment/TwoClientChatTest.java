// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.permission.PermissionManager;
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
 * is received by another client via network event propagation.
 * <p>
 * This exercises the full ChatMessageEvent broadcast path through the
 * LISTEN_SERVER network stack with two connected clients.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class TwoClientChatTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    public void testChatPropagatesBetweenClients() throws Exception {
        // Set up two clients
        Context client1Ctx = helper.createClient();
        Context client2Ctx = helper.createClient();
        assertNotNull(client1Ctx, "Client 1 context should be created");
        assertNotNull(client2Ctx, "Client 2 context should be created");

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

        // Grant chat permission to all connected clients
        PermissionManager hostPerms = helper.getHostContext().get(PermissionManager.class);
        for (Client client : hostNetwork.getPlayers()) {
            EntityRef clientInfo = client.getEntity().getComponent(ClientComponent.class).clientInfo;
            hostPerms.addPermission(clientInfo, PermissionManager.CHAT_PERMISSION);
        }

        // Register a ChatMessageEvent probe on client 2
        ChatProbe probe = new ChatProbe();
        ComponentSystemManager client2Csm = client2Ctx.get(ComponentSystemManager.class);
        client2Csm.register(probe);
        // Also register with EventSystem for post-init registration
        client2Ctx.get(EventSystem.class).registerEventHandler(probe);

        // Send a ChatMessageEvent from the host targeting all connected clients
        // (mimics what ChatSystem does when a player sends a message)
        Client senderClient = hostNetwork.getPlayers().iterator().next();
        EntityRef senderInfo = senderClient.getEntity().getComponent(ClientComponent.class).clientInfo;
        String testMessage = "hello from client 1";

        for (Client client : hostNetwork.getPlayers()) {
            client.getEntity().send(new ChatMessageEvent(testMessage, senderInfo));
        }

        // Wait for client 2's probe to receive the message
        helper.setSafetyTimeoutMs(10000);
        helper.runUntil(() -> probe.received);

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
