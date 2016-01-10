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
import org.terasology.network.ClientInfoComponent;
import org.terasology.network.ColorComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Color;

import java.util.HashSet;
import java.util.Set;

/**
 * The common behaviour of all clients - whether local or remote
 *
 */
public abstract class AbstractClient implements Client {

    private EntityRef clientEntity = EntityRef.NULL;

    @Override
    public EntityRef getEntity() {
        return clientEntity;
    }

    @Override
    public void disconnect() {
        EntityRef clientInfoEntity = clientEntity.getComponent(ClientComponent.class).clientInfo;
        ClientInfoComponent clientInfoComp = clientInfoEntity.getComponent(ClientInfoComponent.class);
        clientInfoComp.client = EntityRef.NULL;
        clientInfoEntity.saveComponent(clientInfoComp);
        clientEntity.destroy();
    }

    private EntityRef findClientEntityRef() {
        for (EntityRef entityRef: CoreRegistry.get(EntityManager.class).getEntitiesWith(ClientInfoComponent.class)) {
            ClientInfoComponent clientInfoComponent = entityRef.getComponent(ClientInfoComponent.class);
            if (clientInfoComponent.playerId.equals(getId())) {
                return entityRef;
            }
        }
        return EntityRef.NULL;
    }

    protected void createEntity(String preferredName, Color color, EntityManager entityManager) {
        // Create player entity
        clientEntity = entityManager.create("engine:client");

        // TODO: Send event for clientInfo creation, don't create here.

        EntityRef clientInfo = findClientEntityRef();
        if (!clientInfo.exists()) {
            clientInfo = createClientInfoEntity(entityManager);
        }
        ClientInfoComponent clientInfoComp = clientInfo.getComponent(ClientInfoComponent.class);
        clientInfoComp.client = clientEntity;
        clientInfo.saveComponent(clientInfoComp);

        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        clientComponent.clientInfo = clientInfo;
        clientEntity.saveComponent(clientComponent);

        addOrSetColorComponent(clientInfo, color);

        DisplayNameComponent displayNameComponent = clientInfo.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null || !displayNameComponent.name.equals(preferredName)) {
            String bestAvailableName = findUniquePlayerName(preferredName, entityManager, clientInfo);
            addOrSetDisplayNameComponent(clientInfo, bestAvailableName);
        }
    }

    private void addOrSetColorComponent(EntityRef clientInfo, Color color) {
        ColorComponent colorComp = clientInfo.getComponent(ColorComponent.class);
        if (colorComp != null) {
            colorComp.color = color;
            clientInfo.saveComponent(colorComp);
        } else {
            colorComp = new ColorComponent();
            colorComp.color = color;
            clientInfo.addComponent(colorComp);
        }
    }

    private void addOrSetDisplayNameComponent(EntityRef clientInfo, String name) {
        DisplayNameComponent component = clientInfo.getComponent(DisplayNameComponent.class);
        if (component != null) {
            component.name = name;
            clientInfo.saveComponent(component);
        } else {
            component = new DisplayNameComponent();
            component.name = name;
            clientInfo.addComponent(component);
        }
    }

    protected String findUniquePlayerName(String preferredName, EntityManager entityManager, EntityRef player) {
        Set<String> usedNames = findNamesOfOtherPlayers(entityManager, player);

        String name = preferredName;
        int nextSuffix = 2;
        while (usedNames.contains(name)) {
            name = preferredName + nextSuffix;
            nextSuffix++;
        }
        return name;
    }

    private Set<String> findNamesOfOtherPlayers(EntityManager entityManager, EntityRef player) {
        Set<String> otherNames = new HashSet<>();
        for (EntityRef clientInfo: entityManager.getEntitiesWith(ClientInfoComponent.class)) {
            if (!clientInfo.equals(player)) {
                DisplayNameComponent displayInfo = clientInfo.getComponent(DisplayNameComponent.class);
                String usedName = displayInfo.name;
                otherNames.add(usedName);
            }
        }
        return otherNames;
    }

    private EntityRef createClientInfoEntity(EntityManager entityManager) {
        EntityRef clientInfo;
        clientInfo = entityManager.create("engine:clientInfo");

        // mark clientInfo entities with a dedicated component
        ClientInfoComponent cic = new ClientInfoComponent();
        cic.playerId = getId();
        clientInfo.addComponent(cic);

        return clientInfo;
    }
}
