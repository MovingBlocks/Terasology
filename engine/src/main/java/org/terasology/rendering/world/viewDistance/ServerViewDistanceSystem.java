// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.world.viewDistance;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.world.chunks.localChunkProvider.RelevanceSystem;

/**
 * Handles view distance changes on the server
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ServerViewDistanceSystem extends BaseComponentSystem {

    @In
    private NetworkSystem networkSystem;

    @In
    private RelevanceSystem relevanceSystem;

    @ReceiveEvent(components = ClientComponent.class)
    public void onChangeViewDistanceChanged(ViewDistanceChangedEvent request, EntityRef entity) {
        Client client = networkSystem.getOwner(entity);
        if (client != null) {
            client.setViewDistanceMode(request.getNewViewRange());
            relevanceSystem.updateRelevanceEntityDistance(entity, client.getViewDistance().getChunkDistance());
        }
    }

}
