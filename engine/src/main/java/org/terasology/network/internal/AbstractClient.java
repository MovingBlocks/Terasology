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
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.ColorComponent;
import org.terasology.rendering.nui.Color;

/**
 * The common behaviour of all clients - whether local or remote
 *
 * @author Immortius
 */
public abstract class AbstractClient implements Client {

    private EntityRef clientEntity = EntityRef.NULL;

    @Override
    public EntityRef getEntity() {
        return clientEntity;
    }

    @Override
    public void disconnect() {
        ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
        if (clientComp != null) {
            clientComp.clientInfo.destroy();
        }
        clientEntity.destroy();
    }

    protected void createEntity(String name, Color color, EntityManager entityManager) {
        // Create player entity
        clientEntity = entityManager.create("engine:client");

        // TODO: Send event for clientInfo creation, don't create here.
        EntityRef clientInfo = entityManager.create("engine:clientInfo");
        DisplayNameComponent displayInfo = clientInfo.getComponent(DisplayNameComponent.class);
        displayInfo.name = name;
        clientInfo.saveComponent(displayInfo);
        
        ColorComponent colorComp = new ColorComponent();
        colorComp.color = color;
        clientInfo.addComponent(colorComp);

        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        clientComponent.clientInfo = clientInfo;
        clientEntity.saveComponent(clientComponent);
    }
}
