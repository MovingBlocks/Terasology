// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world.viewDistance;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;

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
