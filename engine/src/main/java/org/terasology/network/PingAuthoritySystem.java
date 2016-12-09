/*
 * Copyright 2016 MovingBlocks
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
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.TeraMath;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.In;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

@RegisterSystem
public class PingAuthoritySystem extends BaseComponentSystem {
    @In
    NetworkSystem networkSystem;

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent connected, EntityRef entity) {
        refreshPing(entity);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(DisconnectedEvent disconnected, EntityRef entity) {
        entity.removeComponent(PingComponent.class);
    }

    private void refreshPing(EntityRef entity) {
        Server server = networkSystem.getServer();
        if (server != null) {
            Thread getPing = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String[] remoteAddress = server.getRemoteAddress().split("-");
                        String address = remoteAddress[1];
                        int port = Integer.valueOf(remoteAddress[2]);

                        Instant starts = Instant.now();
                        Socket sock = new Socket();
                        sock.connect(new InetSocketAddress(address, port), 10000);
                        Instant ends = Instant.now();
                        sock.close();

                        String response = String.valueOf(Duration.between(starts, ends));
                        String millis = response.replaceAll("[^\\d.]", "");
                        float intMillis = Float.valueOf(millis) * 1000;
                        int ping = TeraMath.floorToInt(intMillis);

                        PingComponent pingComponent = new PingComponent();
                        pingComponent.ping = ping;
                        if (entity.getComponent(PingComponent.class) == null) {
                            entity.addComponent(pingComponent);
                        } else {
                            entity.saveComponent(pingComponent);
                        }
                        refreshPing(entity);
                    } catch (IOException e) {
                        PingComponent pingComponent = new PingComponent();
                        pingComponent.ping = 0;
                        if (entity.getComponent(PingComponent.class) == null) {
                            entity.addComponent(pingComponent);
                        } else {
                            entity.saveComponent(pingComponent);
                        }
                        refreshPing(entity);
                    }
                }
            });
            getPing.start();
        }
    }
}
