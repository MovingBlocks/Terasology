// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.ClientInfoComponent;
import org.terasology.engine.network.ColorComponent;
import org.terasology.nui.Color;

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

    /**
     * Function to find the clients entity reference and return it.
     * @param entityManager - Passes the entity manager for looping through all entities to find ones with client components
     * @return the entity reference of the client playerID
     */
    private EntityRef findClientEntityRef(EntityManager entityManager) {
        for (EntityRef entityRef: entityManager.getEntitiesWith(ClientInfoComponent.class)) {
            ClientInfoComponent clientInfoComponent = entityRef.getComponent(ClientInfoComponent.class);
            if (clientInfoComponent.playerId.equals(getId())) {
                return entityRef;
            }
        }
        return EntityRef.NULL;
    }

    /**
     * Creates an entity for the client connection, checking if name and color options can be used.
     * @param preferredName Passes players preferred name to check availability, giving a best alternative if it is used already.
     * @param color Creates or changes the player's color component to match argument
     * @param entityManager
     */
    protected void createEntity(String preferredName, Color color, EntityManager entityManager) {
        // Create player entity
        clientEntity = entityManager.create("engine:client");

        // TODO: Send event for clientInfo creation, don't create here.

        EntityRef clientInfo = findClientEntityRef(entityManager);
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

    /**
     * Used to change or add a color to the client entity.
     * @param clientInfo
     * @param color Used to change the clients color to this
     */
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

    /**
     * Used to change or add a display name to the client entity.
     * @param clientInfo
     * @param name Function will set the client entities name to this.
     */
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

    /**
     * Provides an alternative name to the client when the preferred name is taken or unavailable, appending a suffix to the end.
     * @param preferredName Used to build new name based on preferred option.
     * @param entityManager
     * @param player Used to mark client name as not to be checked, ensuring the client doesn't block its own name.
     * @return Returns the new name to the client.
     */
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


    /**
     * Creates a {@code HashSet<String>} of all connected player names.
     * @param entityManager
     * @param player Client name to make sure it doesn't put its own name in the list.
     * @return Returns all connected player names.
     */
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

    /**
     * Creates a client information entity on the current entity.
     * @param entityManager
     * @return Returns the client information.
     */
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
