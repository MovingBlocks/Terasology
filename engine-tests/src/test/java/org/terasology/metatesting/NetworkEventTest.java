package org.terasology.metatesting;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class NetworkEventTest {
    private static final Logger logger = LoggerFactory.getLogger(NetworkEventTest.class);

    @In
    private ModuleTestingHelper helper;

    @Test
    public void testNetworkEvents() throws Exception {
        Context clientContext = helper.createClient();

        // Register test system on Host and Client
        TestEventSystem hostSystem = new TestEventSystem();
        helper.getHostContext().get(ComponentSystemManager.class).register(hostSystem);

        TestEventSystem clientSystem = new TestEventSystem();
        clientContext.get(ComponentSystemManager.class).register(clientSystem);

        // Wait for client to be ready
        helper.runUntil(() -> helper.getHostContext().get(org.terasology.engine.network.NetworkSystem.class)
                .getPlayers().iterator().hasNext());

        EntityRef clientEntityOnHost = helper.getHostContext().get(org.terasology.engine.network.NetworkSystem.class)
                .getPlayers().iterator().next().getEntity();
        EntityRef clientInfoOnHost = clientEntityOnHost.getComponent(ClientComponent.class).clientInfo;
        logger.info("ClientInfo on Host: " + clientInfoOnHost + ", client entity on host: " + clientEntityOnHost);

        // 1. Test Broadcast Event (Host -> Client)
        logger.info("Sending Broadcast Event from Host...");
        clientEntityOnHost.send(new TestBroadcastEvent("Broadcast"));

        helper.runUntil(2000, () -> clientSystem.receivedBroadcast);
        assertTrue(clientSystem.receivedBroadcast, "Client should receive BroadcastEvent");

        // 2. Test Owner Event (Host -> Client)
        logger.info("Sending Owner Event from Host...");
        clientEntityOnHost.send(new TestOwnerEvent("Owner"));

        helper.runUntil(2000, () -> clientSystem.receivedOwner);
        assertTrue(clientSystem.receivedOwner, "Client should receive OwnerEvent");

        // 3. Test Server Event (Client -> Host) - We need the client's local player entity
        EntityRef localPlayerEntity = clientContext.get(org.terasology.engine.logic.players.LocalPlayer.class)
                .getClientEntity();
        logger.info("Sending Server Event from Client...");
        localPlayerEntity.send(new TestServerEvent("Server"));

        helper.runUntil(2000, () -> hostSystem.receivedServer);
        assertTrue(hostSystem.receivedServer, "Host should receive ServerEvent");
    }

    @BroadcastEvent
    public static class TestBroadcastEvent extends NetworkEvent {
        public String text;

        public TestBroadcastEvent() {
        }

        public TestBroadcastEvent(String text) {
            this.text = text;
        }
    }

    @OwnerEvent
    public static class TestOwnerEvent extends NetworkEvent {
        public String text;

        public TestOwnerEvent() {
        }

        public TestOwnerEvent(String text) {
            this.text = text;
        }
    }

    @ServerEvent
    public static class TestServerEvent extends NetworkEvent {
        public String text;

        public TestServerEvent() {
        }

        public TestServerEvent(String text) {
            this.text = text;
        }
    }

    @RegisterSystem(RegisterMode.ALWAYS)
    public static class TestEventSystem extends BaseComponentSystem {
        public boolean receivedBroadcast = false;
        public boolean receivedOwner = false;
        public boolean receivedServer = false; // EventReceiver

        @ReceiveEvent(components = ClientComponent.class)
        public void onBroadcast(TestBroadcastEvent event, EntityRef entity) {
            logger.info("Received BroadcastEvent: " + event.text);
            receivedBroadcast = true;
        }

        @ReceiveEvent(components = ClientComponent.class)
        public void onOwner(TestOwnerEvent event, EntityRef entity) {
            logger.info("Received OwnerEvent: " + event.text);
            receivedOwner = true;
        }

        @ReceiveEvent(components = ClientComponent.class)
        public void onServer(TestServerEvent event, EntityRef entity) {
            logger.info("Received ServerEvent: " + event.text);
            receivedServer = true;
        }
    }
}
