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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.events.DeactivatePingClientEvent;
import org.terasology.network.events.PingFromClientEvent;
import org.terasology.network.events.PingFromServerEvent;
import org.terasology.network.events.PingValueEvent;

/**
 * This system, registered on the remote client, will respond to the ping event from server.
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class ClientPingSystem extends BaseComponentSystem{

    @ReceiveEvent(components = ClientComponent.class)
    public void onPingFromServer(PingFromServerEvent event, EntityRef entity){
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            entity.send(new PingFromClientEvent());
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onPingInformation(PingValueEvent event, EntityRef entity) {
        if (!entity.hasComponent(PingStockComponent.class)) {
            entity.addComponent(new PingStockComponent(event.getPingValue()));
        }
        else {
            entity.getComponent(PingStockComponent.class).pingValue = event.getPingValue();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDeActivatePing(DeactivatePingClientEvent event, EntityRef entity) {
        entity.removeComponent(PingStockComponent.class);
    }
}
