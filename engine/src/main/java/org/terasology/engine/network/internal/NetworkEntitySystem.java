/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;

/**
 * This system handles a number of events relevant to the Network System:
 * <ul>
 * <li>Notifies the network system when network entities are created, destroyed or updated</li>
 * <li>Notifies the network system when a client requests a change of view range</li>
 * </ul>
 *
 */
public class NetworkEntitySystem extends BaseComponentSystem {

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
        for (EntityRef entity : entityManager.getEntitiesWith(NetworkComponent.class)) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = NetworkComponent.class, priority = EventPriority.PRIORITY_CRITICAL, netFilter = RegisterMode.AUTHORITY)
    public void onAddNetworkComponent(OnActivatedComponent event, EntityRef entity) {
        if (networkSystem.getMode().isServer()) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = {EntityInfoComponent.class})
    public void onOwnershipChanged(OnChangedComponent event, EntityRef entity) {
        networkSystem.updateOwnership(entity);
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onDeactivateNetworkComponent(BeforeDeactivateComponent event, EntityRef entity) {
        networkSystem.unregisterNetworkEntity(entity);
    }

}
