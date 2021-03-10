// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * This is used on the client side to track and clean up items that are no longer held by remote players,  but are still location linked
 */
public class ItemIsRemotelyHeldComponent implements Component {
    public EntityRef remotePlayer = EntityRef.NULL;
}
