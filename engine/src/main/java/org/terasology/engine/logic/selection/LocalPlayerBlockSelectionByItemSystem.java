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
package org.terasology.logic.selection;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.input.events.LeftMouseDownButtonEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.selection.BlockSelectionComponent;
import org.terasology.world.selection.event.SetBlockSelectionEndingPointEvent;
import org.terasology.world.selection.event.SetBlockSelectionStartingPointEvent;

/**
 * System to allow the use of BlockSelectionComponents. This system is a client only system, though no other player
 * will see selections done by one player.  This system uses an item's activate event to both start and end a selection,
 * then triggers a ApplyBlockSelectionEvent event.
 *
 */
@RegisterSystem(RegisterMode.CLIENT)
public class LocalPlayerBlockSelectionByItemSystem extends BaseComponentSystem {
    @In
    private LocalPlayer localPlayer;

    private EntityRef blockSelectionComponentEntity;

    @ReceiveEvent(components = {OnItemActivateSelectionComponent.class})
    public void onPlaced(ActivateEvent event, EntityRef itemEntity) {
        if (event.getTargetLocation() == null) {
            return;
        }

        EntityRef targetLocationEntity = event.getTarget();

        this.blockSelectionComponentEntity = itemEntity;
        BlockSelectionComponent blockSelectionComponent = itemEntity.getComponent(BlockSelectionComponent.class);

        if (null == blockSelectionComponent.startPosition) {
            // on the first item click, we start selecting blocks
            targetLocationEntity.send(new SetBlockSelectionStartingPointEvent(itemEntity));

            blockSelectionComponent.shouldRender = true;
        } else {
            // on the second item click, we will set the ending selection point and send an ApplyBlockSelectionEvent
            targetLocationEntity.send(new SetBlockSelectionEndingPointEvent(itemEntity));

            localPlayer.getCharacterEntity().send(new ApplyBlockSelectionEvent(itemEntity, blockSelectionComponent.currentSelection));
            blockSelectionComponent.shouldRender = false;
            blockSelectionComponent.currentSelection = null;
            blockSelectionComponent.startPosition = null;
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCamTargetChanged(CameraTargetChangedEvent event, EntityRef entity) {
        // This method will update the block selection to whatever block is targeted in the players view
        if (null == blockSelectionComponentEntity) {
            return;
        }

        BlockSelectionComponent blockSelectionComponent = blockSelectionComponentEntity.getComponent(BlockSelectionComponent.class);

        if (blockSelectionComponent == null) {
            return;
        }

        EntityRef target = event.getNewTarget();
        LocationComponent locationComponent = target.getComponent(LocationComponent.class);

        if (locationComponent == null) {
            return;
        }

        Vector3f targetLocation = locationComponent.getWorldPosition(new Vector3f());

        if (blockSelectionComponent.isMovable) {

            Vector3i pos = new Vector3i(targetLocation, RoundingMode.FLOOR);
            Vector3i size = blockSelectionComponent.currentSelection.getSize(new Vector3i());
            blockSelectionComponent.currentSelection.set(
                pos, pos).expand(size.x() / 2, 0, size.z() / 2);
            blockSelectionComponentEntity.saveComponent(blockSelectionComponent);

            return;
        }

        if (blockSelectionComponent.startPosition == null) {
            return;
        }

        target.send(new SetBlockSelectionEndingPointEvent(blockSelectionComponentEntity));
    }

    /**
     * This event is sent after the size of a region is finalized and the location is to yet to be decided by the player.
     * This event marks the start of the camera position binding with the region.
     * @param event The event sent
     * @param blockSelectionComponentEntity The entity sending the event. This entity must have the {@link BlockSelectionComponent}
     */
    @ReceiveEvent(components = {BlockSelectionComponent.class})
    public void onMovableBlockSelectionStart(MovableSelectionStartEvent event, EntityRef blockSelectionComponentEntity) {
        this.blockSelectionComponentEntity = blockSelectionComponentEntity;
    }

    /**
     * This marks the end of the camera position binding with the region position.
     * @param event LeftMouseButtonDownEvent
     * @param entity Entity sending the event
     */
    @ReceiveEvent
    public void onLeftMouseButtonDown(LeftMouseDownButtonEvent event, EntityRef entity) {
        if (this.blockSelectionComponentEntity != null && this.blockSelectionComponentEntity != EntityRef.NULL) {
            BlockSelectionComponent blockSelectionComponent = blockSelectionComponentEntity.getComponent(BlockSelectionComponent.class);
            if (blockSelectionComponent != null && blockSelectionComponent.isMovable) {
                blockSelectionComponentEntity.send(new MovableSelectionEndEvent(blockSelectionComponent.currentSelection));

                blockSelectionComponentEntity.destroy();
            }
        }
    }

}
