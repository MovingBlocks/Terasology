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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.network.events.PingFromClientEvent;
import org.terasology.network.events.PingFromServerEvent;
import org.terasology.network.events.SubscribePingEvent;
import org.terasology.network.events.UnSubscribePingEvent;
import org.terasology.registry.In;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * This system implement the server ping to clients on need base.
 * It runs on the server, pings to all clients who subscribe this function.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ServerPingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final long PING_PERIOD = 200;

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    private Map<EntityRef, Instant> startMap = new HashMap<>();

    private Map<EntityRef, Instant> endMap = new HashMap<>();

    private Map<EntityRef, Long> pingMap = new HashMap<>();

    private Instant lastPingTime;

    @Override
    public void initialise() {
        lastPingTime = Instant.now();
    }

    @Override
    public void update(float delta) {
        long time = Duration.between(lastPingTime, Instant.now()).toMillis();
        if (time > PING_PERIOD) {

            // Server ping to all clients only if there are clients who subscribe
            if (entityManager.getCountOfEntitiesWith(PingSubscriberComponent.class) != 0) {
                Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
                for (EntityRef client : clients) {
                    if (client.equals(localPlayer.getClientEntity())) {
                        continue;
                    }

                    // send ping only if client replied the last ping
                    Instant lastPingFromClient = endMap.get(client);
                    Instant lastPingToClient = startMap.get(client);
                    // Only happens when server doesn't receive ping back yet
                    if (lastPingFromClient != null && lastPingToClient != null && lastPingFromClient.isBefore(lastPingToClient)) {
                        continue;
                    }

                    Instant start = Instant.now();
                    startMap.put(client, start);
                    client.send(new PingFromServerEvent());
                }
            }

            //update ping data for all clients
            for (EntityRef client : entityManager.getEntitiesWith(PingSubscriberComponent.class)) {
                PingStockComponent pingStockComponent;
                if (!client.hasComponent(PingStockComponent.class)) {
                    pingStockComponent = new PingStockComponent();
                } else {
                    pingStockComponent = client.getComponent(PingStockComponent.class);
                }
                if (localPlayer != null && localPlayer.getClientEntity() != null) {
                    pingMap.put(localPlayer.getClientEntity(), new Long(5));
                }
                pingStockComponent.setValues(pingMap);
                client.addOrSaveComponent(pingStockComponent);
            }

            lastPingTime = Instant.now();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onPingFromClient(PingFromClientEvent event, EntityRef entity) {
        Instant end = Instant.now();
        endMap.put(entity, end);
        updatePing(entity);
    }

    private void updatePing(EntityRef entity) {
        if (startMap.containsKey(entity) && endMap.containsKey(entity)) {
            long pingTime = Duration.between(startMap.get(entity), endMap.get(entity)).toMillis();
            pingMap.put(entity, pingTime);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnected(DisconnectedEvent event, EntityRef entity) {
        startMap.remove(entity);
        endMap.remove(entity);
        pingMap.remove(entity);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onSubscribePing(SubscribePingEvent event, EntityRef entity) {
        entity.addOrSaveComponent(new PingSubscriberComponent());
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onUnSubscribePing(UnSubscribePingEvent event, EntityRef entity) {
        entity.removeComponent(PingSubscriberComponent.class);
        entity.removeComponent(PingStockComponent.class);

        //if there is no pingSubscriber, then clean the map
        if (entityManager.getCountOfEntitiesWith(PingSubscriberComponent.class) == 0) {
            startMap.clear();
            endMap.clear();
            pingMap.clear();
        }
    }
}
