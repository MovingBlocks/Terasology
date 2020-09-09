// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.math.Region3i;
import org.terasology.gestalt.module.sandbox.API;

/**
 * This event is fired once a player finished a selection using an item with a BlockSelectionComponent. The item used is
 * included in the event.
 */
@API
public class ApplyBlockSelectionEvent implements Event {
    private final Region3i selection;
    private final EntityRef selectedItemEntity;

    public ApplyBlockSelectionEvent(EntityRef selectedItemEntity, Region3i selection) {
        this.selectedItemEntity = selectedItemEntity;
        this.selection = selection;
    }

    public Region3i getSelection() {
        return selection;
    }

    public EntityRef getSelectedItemEntity() {
        return selectedItemEntity;
    }
}
