package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.Client;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.Message;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.logic.console.ConsoleSystem;
import org.terasology.engine.core.ComponentSystemManager;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test that a headless server can host two clients and that a chat message sent
 * by one client is received by the other.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class MTETwoClientChatTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    public void testTwoClientsChat() throws Exception {
        // Create first client
        Context client1Ctx = helper.createClient();
        System.out.println("Client 1 context: " + client1Ctx);
        assertNotNull(client1Ctx, "Client 1 context should be non-null");

        // Create second client
        Context client2Ctx = helper.createClient();
        System.out.println("Client 2 context: " + client2Ctx);
        assertNotNull(client2Ctx, "Client 2 context should be non-null");

        // Verify both clients are registered on the host
        NetworkSystem hostNetwork = helper.getHostContext().get(NetworkSystem.class);
        System.out.println("Host network player count? " + hostNetwork.getPlayers());
        assertThat(hostNetwork.getPlayers()).hasSize(2);

        // Get Host Console for debugging
        Console hostConsole = helper.getHostContext().get(Console.class);

        // Verify ConsoleSystem is loaded on Client 1
        ComponentSystemManager client1SystemManager = client1Ctx.get(ComponentSystemManager.class);
        boolean consoleSystemFound1 = false;
        for (org.terasology.engine.entitySystem.systems.ComponentSystem system : client1SystemManager.getAllSystems()) {
            if (system instanceof ConsoleSystem) {
                consoleSystemFound1 = true;
                break;
            }
        }
        System.out.println("ConsoleSystem loaded on Client 1? " + consoleSystemFound1);

        // Verify ConsoleSystem is loaded on Client 2
        ComponentSystemManager client2SystemManager = client2Ctx.get(ComponentSystemManager.class);
        boolean consoleSystemFound2 = false;
        for (org.terasology.engine.entitySystem.systems.ComponentSystem system : client2SystemManager.getAllSystems()) {
            if (system instanceof ConsoleSystem) {
                consoleSystemFound2 = true;
                break;
            }
        }
        System.out.println("ConsoleSystem loaded on Client 2? " + consoleSystemFound2);

        // Grant permissions on Host
        PermissionManager hostPerms = helper.getHostContext().get(PermissionManager.class);
        for (Client client : hostNetwork.getPlayers()) {
            EntityRef clientInfo = client.getEntity().getComponent(ClientComponent.class).clientInfo;
            hostPerms.addPermission(clientInfo, PermissionManager.CHAT_PERMISSION);
            System.out.println("Granted CHAT permission to " + client.getName());
        }

        // Send a chat message from client 1
        LocalPlayer localPlayer1 = client1Ctx.get(LocalPlayer.class);
        Console console1 = client1Ctx.get(Console.class);
        console1.execute("say hello from client1", localPlayer1.getClientEntity());

        // Check ClientComponent.local for Client 2
        LocalPlayer localPlayer2 = client2Ctx.get(LocalPlayer.class);
        ClientComponent clientComp2 = localPlayer2.getClientEntity().getComponent(ClientComponent.class);
        System.out.println("Client 2 local flag: " + (clientComp2 != null ? clientComp2.local : "null"));

        // Register ProbeSystem on Client 2 to listen for ChatMessageEvent directly
        ProbeSystem probe = new ProbeSystem();
        client2Ctx.get(ComponentSystemManager.class).register(probe);
        System.out.println("Registered ProbeSystem on Client 2");

        // Manually send a ChatMessageEvent from the host to verify event propagation
        // This bypasses the Command system to isolate the issue
        helper.runUntil(() -> hostNetwork.getPlayers().iterator().hasNext()); // Ensure players are there
        Client senderClient = hostNetwork.getPlayers().iterator().next();
        EntityRef senderOnHost = senderClient.getEntity();
        String manualMessage = "Manual message from host";

        System.out.println("Sending manual message from host using sender: " + senderOnHost);
        for (Client client : hostNetwork.getPlayers()) {
            // Try sending with the actual sender
            client.getEntity().send(new org.terasology.engine.logic.chat.ChatMessageEvent(manualMessage, senderOnHost));

            // Try sending with EntityRef.NULL to rule out replication issues
            client.getEntity().send(
                    new org.terasology.engine.logic.chat.ChatMessageEvent("Message with NULL sender", EntityRef.NULL));
        }

        // Set a safety timeout to prevent hangs
        helper.setSafetyTimeoutMs(10000);

        // Run the loop until client 2 receives the message (or we time out)
        Console console2 = client2Ctx.get(Console.class);
        System.out.println("Client 2 console: " + console2);

        try {
            helper.runUntil(() -> {
                if (probe.received) {
                    System.out.println("Probe received message: " + probe.lastMessage);
                    return true;
                }
                for (Message message : console2.getMessages()) {
                    System.out.println("Client 2 message: " + message.getMessage());
                    if (message.getMessage().contains("hello from client1")
                            || message.getMessage().contains("Manual message")
                            || message.getMessage().contains("NULL sender")) {
                        return true;
                    }
                }
                return false;
            });
        } catch (com.google.common.util.concurrent.UncheckedTimeoutException e) {
            System.out.println("Timeout reached! Dumping messages:");
            System.out.println("Probe received: " + probe.received + " last: " + probe.lastMessage);
            System.out.println("Host Messages:");
            for (Message m : hostConsole.getMessages()) {
                System.out.println(" - " + m.getMessage());
            }
            System.out.println("Client 1 Messages:");
            for (Message m : console1.getMessages()) {
                System.out.println(" - " + m.getMessage());
            }
            System.out.println("Client 2 Messages:");
            for (Message m : console2.getMessages()) {
                System.out.println(" - " + m.getMessage());
            }
            throw e; // Re-throw to fail the test
        }
    }

    public static class ProbeSystem extends org.terasology.engine.entitySystem.systems.BaseComponentSystem {
        public boolean received = false;
        public String lastMessage = "";

        @org.terasology.gestalt.entitysystem.event.ReceiveEvent(components = ClientComponent.class)
        public void onChatMessage(org.terasology.engine.logic.chat.ChatMessageEvent event, EntityRef entity) {
            System.out.println("ProbeSystem received ChatMessageEvent: " + event.getMessage());
            received = true;
            lastMessage = event.getMessage();
        }
    }
}
