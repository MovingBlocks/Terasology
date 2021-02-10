// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.selection;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.BlockRegion;

/**
 * This event is fired once a player finished a selection using an item with a BlockSelectionComponent. The item used
 * is included in the event.
 *
 */
@API
public class ApplyBlockSelectionEvent implements Event {
    private final BlockRegion selection;
    private final EntityRef selectedItemEntity;

    public ApplyBlockSelectionEvent(EntityRef selectedItemEntity, BlockRegion selection) {
        this.selectedItemEntity = selectedItemEntity;
        this.selection = selection;
    }

    public BlockRegion getSelection() {
        return selection;
    }

    public EntityRef getSelectedItemEntity() {
        return selectedItemEntity;
    }
}
