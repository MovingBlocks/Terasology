// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.selection;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.selection.event.SetBlockSelectionEndingPointEvent;
import org.terasology.engine.world.selection.event.SetBlockSelectionStartingPointEvent;

/**
 * This system updates block selections based on the sender's location and the state of the block selection.
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockSelectionSystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onStartSelectionAtEntity(SetBlockSelectionStartingPointEvent event, EntityRef entity, LocationComponent locationComponent) {
        if (null == locationComponent) {
            // entity isn't LocationComponent, which shouldn't ever be the case
            return;
        }

        BlockSelectionComponent blockSelectionComponent = event.getBlockSelectionComponent();
        if (null == blockSelectionComponent) {
            // event did not provide a BlockSelection component to modify
            return;
        }

        Vector3i startPosition = new Vector3i(locationComponent.getWorldPosition(new Vector3f()), RoundingMode.FLOOR);
        blockSelectionComponent.startPosition = startPosition;
        blockSelectionComponent.currentSelection = new BlockRegion(startPosition);
    }

    @ReceiveEvent
    public void onEndSelectionAtEntity(SetBlockSelectionEndingPointEvent event, EntityRef entity, LocationComponent locationComponent) {
        if (null == locationComponent) {
            // entity isn't LocationComponent, which shouldn't ever be the case
            return;
        }

        BlockSelectionComponent blockSelectionComponent = event.getBlockSelectionComponent();
        if (null == blockSelectionComponent) {
            // event did not provide a BlockSelection component to modify
            return;
        }

        Vector3i endPosition = new Vector3i(locationComponent.getWorldPosition(new Vector3f()), RoundingMode.FLOOR);
        Vector3i startPosition = blockSelectionComponent.startPosition;
        if (null == startPosition) {
            startPosition = endPosition;
        }
        blockSelectionComponent.currentSelection =
            new BlockRegion(startPosition).union(endPosition);
    }
}
