// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.selection;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.selection.event.SetBlockSelectionEndingPointEvent;
import org.terasology.engine.world.selection.event.SetBlockSelectionStartingPointEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

/**
 * This system updates block selections based on the sender's location and the state of the block selection.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockSelectionSystem extends BaseComponentSystem {

    @ReceiveEvent(components = {LocationComponent.class})
    public void onStartSelectionAtEntity(SetBlockSelectionStartingPointEvent event, EntityRef entity) {

        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        if (null == locationComponent) {
            // entity isn't LocationComponent, which shouldn't ever be the case
            return;
        }

        BlockSelectionComponent blockSelectionComponent = event.getBlockSelectionComponent();
        if (null == blockSelectionComponent) {
            // event did not provide a BlockSelection component to modify
            return;
        }

        Vector3f worldPosition = locationComponent.getWorldPosition();

        Vector3i startPosition = new Vector3i(worldPosition.x, worldPosition.y, worldPosition.z);
        blockSelectionComponent.startPosition = startPosition;
        Vector3i endPosition = startPosition;
        blockSelectionComponent.currentSelection = Region3i.createBounded(startPosition, endPosition);
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onEndSelectionAtEntity(SetBlockSelectionEndingPointEvent event, EntityRef entity) {

        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        if (null == locationComponent) {
            // entity isn't LocationComponent, which shouldn't ever be the case
            return;
        }

        BlockSelectionComponent blockSelectionComponent = event.getBlockSelectionComponent();
        if (null == blockSelectionComponent) {
            // event did not provide a BlockSelection component to modify
            return;
        }

        Vector3f worldPosition = locationComponent.getWorldPosition();

        Vector3i endPosition = new Vector3i(worldPosition.x, worldPosition.y, worldPosition.z);
        Vector3i startPosition = blockSelectionComponent.startPosition;
        if (null == startPosition) {
            startPosition = endPosition;
        }
        blockSelectionComponent.currentSelection = Region3i.createBounded(startPosition, endPosition);
    }
}
