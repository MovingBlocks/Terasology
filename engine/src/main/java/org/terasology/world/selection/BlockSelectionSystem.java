/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.selection;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.selection.event.SetBlockSelectionEndingPointEvent;
import org.terasology.world.selection.event.SetBlockSelectionStartingPointEvent;

/**
 * This system updates block selections based on the sender's location and the state of the block selection.
 *
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
