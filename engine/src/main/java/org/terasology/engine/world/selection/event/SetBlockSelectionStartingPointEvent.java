// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.selection.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.world.selection.BlockSelectionComponent;

/**
 * Sent to BlockSelectionSystem to indicate that the current sender should be used as the selection starting position
 */
public class SetBlockSelectionStartingPointEvent extends AbstractConsumableEvent {

    private EntityRef blockSelectionComponentEntity;

    public SetBlockSelectionStartingPointEvent(EntityRef blockSelectionComponentEntity) {
        this.blockSelectionComponentEntity = blockSelectionComponentEntity;
    }

    public BlockSelectionComponent getBlockSelectionComponent() {
        return blockSelectionComponentEntity.getComponent(BlockSelectionComponent.class);
    }
}
