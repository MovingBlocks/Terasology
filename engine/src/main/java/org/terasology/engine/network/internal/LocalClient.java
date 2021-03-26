// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import org.joml.Vector3ic;
import org.terasology.engine.config.Config;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.ColorComponent;
import org.terasology.nui.Color;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.chunks.Chunk;

/**
 * A local client.
 *
 */
public class LocalClient extends AbstractClient {

    private Config config = CoreRegistry.get(Config.class);

    /**
     * Creates an entity for the new local client.
     * @param preferredName Clients preferred name.
     * @param color Clients preferred color.
     * @param entityManager Entity manager for the clients entity creation.
     */
    public LocalClient(String preferredName, Color color, EntityManager entityManager) {
        createEntity(preferredName, color, entityManager);
    }

    @Override
    public String getName() {
        ClientComponent clientComp = getEntity().getComponent(ClientComponent.class);
        if (clientComp != null) {
            DisplayNameComponent displayInfo = clientComp.clientInfo.getComponent(DisplayNameComponent.class);
            if (displayInfo != null) {
                return displayInfo.name;
            }
        }
        return "Unknown";
    }

    @Override
    public Color getColor() {
        ClientComponent clientComp = getEntity().getComponent(ClientComponent.class);
        if (clientComp != null) {
            ColorComponent colorComp = clientComp.clientInfo.getComponent(ColorComponent.class);
            if (colorComp != null) {
                return colorComp.color;
            }
        }
        return Color.WHITE;
    }

    @Override
    public String getId() {
        return "local";
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void update(boolean netTick) {
    }

    @Override
    public void send(Event event, EntityRef target) {
    }

    @Override
    public ViewDistance getViewDistance() {
        return config.getRendering().getViewDistance();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public void setViewDistanceMode(ViewDistance newViewRange) {
        // Handled by Configuration change
    }

    @Override
    public void onChunkRelevant(Vector3ic pos, Chunk chunk) {
    }

    @Override
    public void onChunkIrrelevant(Vector3ic pos) {
    }
}
