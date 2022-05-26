// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.items;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event is sent when a block in the world is turned into an item, to allow modification of the item entity.
 *
 */
public class OnBlockToItem implements Event {

    private EntityRef item;

    public OnBlockToItem(EntityRef item) {
        this.item = item;
    }

    public EntityRef getItem() {
        return item;
    }
}
