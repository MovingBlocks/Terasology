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

import org.terasology.components.DisplayInformationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;

/**
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
        clientEntity.getComponent(ClientComponent.class).clientInfo.destroy();
        clientEntity.destroy();
    }

    protected void createEntity(String name, EntityManager entityManager) {
        // Create player entity
        clientEntity = entityManager.create("engine:client");

        EntityRef clientInfo = entityManager.create("engine:clientInfo");
        DisplayInformationComponent displayInfo = clientInfo.getComponent(DisplayInformationComponent.class);
        displayInfo.name = name;
        clientInfo.saveComponent(displayInfo);

        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        clientComponent.clientInfo = clientInfo;
        clientEntity.saveComponent(clientComponent);
    }
}
