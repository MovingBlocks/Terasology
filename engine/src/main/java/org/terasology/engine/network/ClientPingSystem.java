// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.events.PingFromClientEvent;
import org.terasology.engine.network.events.PingFromServerEvent;

/**
 * This system, registered on the client, will respond to the ping event from server.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientPingSystem extends BaseComponentSystem {

    @ReceiveEvent(components = ClientComponent.class)
    public void onPingFromServer(PingFromServerEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            entity.send(new PingFromClientEvent());
        }
    }
}
