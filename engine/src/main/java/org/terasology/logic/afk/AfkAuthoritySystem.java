// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.afk;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AfkAuthoritySystem extends BaseComponentSystem {

    private static final String PERIODIC_ID = "AFK_PERIODIC";

    private static final long AFK_PERIOD = 10 * 1000L;

    @In
    private Time time;

    @In
    private DelayManager delayManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private NetworkSystem networkSystem;


    @ReceiveEvent
    public void onConnected(ConnectedEvent event, EntityRef entity) {
        if (!delayManager.hasPeriodicAction(entity, PERIODIC_ID)) {
            delayManager.addPeriodicAction(entity, PERIODIC_ID, 0, AFK_PERIOD);
        }
    }

    @ReceiveEvent
    public void onDisconnected(DisconnectedEvent event, EntityRef entity) {
        if (delayManager.hasPeriodicAction(entity, PERIODIC_ID)) {
            delayManager.cancelPeriodicAction(entity, PERIODIC_ID);
            for (Client player : networkSystem.getPlayers()) {
                delayManager.addPeriodicAction(player.getEntity(), PERIODIC_ID, 0, AFK_PERIOD);
            }
        }
    }

    @ReceiveEvent
    public void onAfkRequest(AfkRequest request, EntityRef entity) {
        AfkEvent event = new AfkEvent(request.getInstigator(), request.isAfk());
        entity.send(event);
    }

    @ReceiveEvent
    public void onPeriodicTrigger(PeriodicActionTriggeredEvent triggeredEvent, EntityRef entity) {
        if (triggeredEvent.getActionId().equals(PERIODIC_ID)) {
            AfkDetectEvent event = new AfkDetectEvent();
            entity.send(event);
        }
    }

}
