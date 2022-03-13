// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.afk;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.events.ConnectedEvent;
import org.terasology.engine.network.events.DisconnectedEvent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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
