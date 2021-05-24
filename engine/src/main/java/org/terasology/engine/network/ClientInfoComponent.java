// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * The component that marks an entity as being a Client Info Entity.
 */
@Replicate
public final class ClientInfoComponent implements Component {

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
}
