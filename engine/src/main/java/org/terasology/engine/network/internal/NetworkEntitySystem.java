// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.NetFilterEvent;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.world.WorldRenderer;

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

    @NetFilterEvent(netFilter = RegisterMode.AUTHORITY)
    @ReceiveEvent(components = NetworkComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void onAddNetworkComponent(OnActivatedComponent event, EntityRef entity) {
        if (networkSystem.getMode().isServer()) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = EntityInfoComponent.class)
    public void onOwnershipChanged(OnChangedComponent event, EntityRef entity) {
        networkSystem.updateOwnership(entity);
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onDeactivateNetworkComponent(BeforeDeactivateComponent event, EntityRef entity) {
        networkSystem.unregisterNetworkEntity(entity);
    }

}
