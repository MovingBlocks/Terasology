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

import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.network.ColorComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.chunks.Chunk;

/**
 * A local client.
 *
 */
public class LocalClient extends AbstractClient {

    private Config config = CoreRegistry.get(Config.class);

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
    public void onChunkRelevant(Vector3i pos, Chunk chunk) {
    }

    @Override
    public void onChunkIrrelevant(Vector3i pos) {
    }
}
