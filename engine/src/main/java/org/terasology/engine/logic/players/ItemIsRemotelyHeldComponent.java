// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This is used on the client side to track and clean up items that are no longer held by remote players,  but are still location linked
 */
public class ItemIsRemotelyHeldComponent implements Component<ItemIsRemotelyHeldComponent> {
    public EntityRef remotePlayer = EntityRef.NULL;

    @Override
    public void copy(ItemIsRemotelyHeldComponent other) {
        this.remotePlayer = other.remotePlayer;
    }
}
