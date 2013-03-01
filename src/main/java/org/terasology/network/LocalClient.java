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

package org.terasology.network;

import org.terasology.components.DisplayInformationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * @author Immortius
 */
public class LocalClient extends AbstractClient {

    public LocalClient(String name, EntityManager entityManager) {
        createEntity(name, entityManager);
    }

    @Override
    public boolean isAwaitingConnectMessage() {
        return false;
    }

    @Override
    public String getName() {
        ClientComponent clientComp = getEntity().getComponent(ClientComponent.class);
        if (clientComp != null) {
            DisplayInformationComponent displayInfo = clientComp.clientInfo.getComponent(DisplayInformationComponent.class);
            if (displayInfo != null) {
                return displayInfo.name;
            }
        }
        return "Unknown";
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
    public void onChunkReady(Vector3i pos, Chunk chunk) {
    }
}
