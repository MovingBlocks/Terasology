/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.network.events.PingFromClientEvent;
import org.terasology.network.events.PingFromServerEvent;
import org.terasology.registry.In;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

/**
 * This system implement the server ping to all clients (include server local player).
 * It add a periodic action, which is ping from the server to a client, when the client is connected. Then the client will respond.
 */
@RegisterSystem
public class ServerPingSystem extends BaseComponentSystem implements Component{

    private static final String PING_ACTION_ID = "PING_ACTION";

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    private HashMap<EntityRef, Instant> startMap = new HashMap<>();

    private HashMap<EntityRef, Instant> endMap = new HashMap<>();

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent event, EntityRef entity) {
        if(networkSystem.getMode().isServer()) {
                delayManager.addPeriodicAction(entity, PING_ACTION_ID, 10000, 10000);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnect(DisconnectedEvent event, EntityRef entity) {
        if (networkSystem.getMode().isServer()) {
            delayManager.cancelPeriodicAction(entity, PING_ACTION_ID);
        }
    }

    @ReceiveEvent
    public void onPingAction(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(PING_ACTION_ID)) {
            Instant start = Instant.now();
            startMap.put(entity, start);
            entity.send(new PingFromServerEvent());
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onPingFromServer(PingFromServerEvent event, EntityRef entity){
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            entity.send(new PingFromClientEvent());
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onPingFromClient(PingFromClientEvent event, EntityRef entity){
        if (networkSystem.getMode().isServer()) {
            Instant end = Instant.now();
            endMap.put(entity, end);
            updatePing(entity);
        }
    }

    private void updatePing(EntityRef entity) {
        if(startMap.containsKey(entity) && endMap.containsKey(entity)) {
            ClientComponent clientComp = entity.getComponent(ClientComponent.class);
            long pingTime = Duration.between(startMap.get(entity), endMap.get(entity)).toMillis();
            clientComp.ping = String.valueOf(pingTime) + "ms";
            entity.saveComponent(clientComp);
        }
    }
}
