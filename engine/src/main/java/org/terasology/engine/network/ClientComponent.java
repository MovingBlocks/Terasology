// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * The component that marks an entity as being a Client Entity (essentially, a player) and ties them to a
 * client info entity (for replicated information) and character entity (their body).
 *
 */
public class ClientComponent implements Component<ClientComponent> {
    public boolean local;

    @Replicate
    public EntityRef clientInfo = EntityRef.NULL;

    @Replicate
    public EntityRef character = EntityRef.NULL;

    public EntityRef camera = EntityRef.NULL;


    @Override
    public void copy(ClientComponent other) {
        this.local = other.local;
        this.clientInfo = other.clientInfo;
        this.character = other.character;
        this.camera = other.camera;
    }
}
