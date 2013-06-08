/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.network.internal;

import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedEvent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedEvent;
import org.terasology.entitySystem.lifecycleEvents.OnDeactivatedEvent;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.events.ChangeViewRangeRequest;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.chunks.ChunkConstants;

/**
 * This system handles a number of events relevant to the Network System:
 * <ul>
 *     <li>Notifies the network system when network entities are created, destroyed or updated</li>
 *     <li>Notifies the network system when a client requests a change of view range</li>
 * </ul>
 * @author Immortius
 */
public class NetworkEntitySystem implements ComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    private NetworkSystemImpl networkSystem;

    public NetworkEntitySystem(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.listEntitiesWith(NetworkComponent.class)) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = NetworkComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void onAddNetworkComponent(OnActivatedEvent event, EntityRef entity) {
        if (networkSystem.getMode() == NetworkMode.SERVER) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onNetworkComponentChanged(OnChangedEvent event, EntityRef entity) {

        networkSystem.updateNetworkEntity(entity);
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onDeactivateNetworkComponent(OnDeactivatedEvent event, EntityRef entity) {
        networkSystem.unregisterNetworkEntity(entity);
        NetworkComponent networkComp = entity.getComponent(NetworkComponent.class);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onChangeViewRequest(ChangeViewRangeRequest request, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            Client client = networkSystem.getOwner(entity);
            if (client != null) {
                client.setViewDistanceMode(request.getNewViewRange());
                worldRenderer.getChunkProvider().updateRelevanceEntity(entity, client.getViewDistance() + ChunkConstants.FULL_GENERATION_DISTANCE);
            }
        }
    }

    @Override
    public void shutdown() {

    }
}
