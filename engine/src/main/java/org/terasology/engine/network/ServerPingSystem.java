// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.events.DisconnectedEvent;
import org.terasology.engine.network.events.PingFromClientEvent;
import org.terasology.engine.network.events.PingFromServerEvent;
import org.terasology.engine.network.events.SubscribePingEvent;
import org.terasology.engine.network.events.UnSubscribePingEvent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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

    /** The interval in which pings are send, in milliseconds. */
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
        Instant now = Instant.now();
        long time = Duration.between(lastPingTime, now).toMillis();
        if (time > PING_PERIOD) {
            // only collect ping information if anybody is interested
            if (entityManager.getCountOfEntitiesWith(PingSubscriberComponent.class) != 0) {
                startPings();
                updateSubscribers();
            }
            lastPingTime = now;
        }
    }

    /**
     * Send a ping signal ({@link PingFromServerEvent}) from the server to all
     * clients.
     * 
     * Any entity with a {@link PingSubscriberComponent} is considered as
     * subscriber.
     * 
     * Clients are supposed to answer with {@link PingFromClientEvent} to confirm
     * the ping.
     * 
     * @see PingFromServerEvent
     * @see PingFromClientEvent
     */
    private void startPings() {
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        for (EntityRef client : clients) {
            if (client.equals(localPlayer.getClientEntity())) {
                continue;
            }
            sendPingToClient(client);
        }
    }

    /**
     * Send a ping signal to the client.
     */
    private void sendPingToClient(EntityRef client) {
        Instant lastPingFromClient = endMap.get(client);
        Instant lastPingToClient = startMap.get(client);
        // Send ping only if the client has replied to the last ping. This happens when
        // there is still a ping in-flight, that is, the server hasn't receive an answer
        // from this client yet.
        if (lastPingFromClient != null && lastPingToClient != null
                && lastPingFromClient.isBefore(lastPingToClient)) {
            return;
        }

        Instant start = Instant.now();
        startMap.put(client, start);
        client.send(new PingFromServerEvent());
    }

    /**
     * Update the ping stock ({@link PingStockComponent}) on all subscribers
     */
    private void updateSubscribers() {
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

        // if there is no pingSubscriber, then clean the map
        if (entityManager.getCountOfEntitiesWith(PingSubscriberComponent.class) == 0) {
            startMap.clear();
            endMap.clear();
            pingMap.clear();
        }
    }
}
