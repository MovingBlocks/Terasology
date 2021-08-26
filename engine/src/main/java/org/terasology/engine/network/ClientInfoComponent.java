// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * The component that marks an entity as being a Client Info Entity.
 */
@Replicate
public final class ClientInfoComponent implements Component<ClientInfoComponent> {

    /**
     * When a client connects, the game searches a client info component for the client id ({@link Client#getId()}).
     * If it finds one it is gets reused, otherwise a new one will be created.
     *
     * The field does not get replicated as there is no need to tell the clients the player ids.
     *
     */
    @NoReplicate
    public String playerId;

    /**
     * Set to the client entity if it is connected, otherwise it is EntityRef.NULL.
     */
    @Replicate
    public EntityRef client = EntityRef.NULL;

    @Override
    public void copyFrom(ClientInfoComponent other) {
        this.playerId = other.playerId;
        this.client = other.client;
    }
}
