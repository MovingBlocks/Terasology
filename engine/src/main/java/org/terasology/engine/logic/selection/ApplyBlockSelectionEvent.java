// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

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
