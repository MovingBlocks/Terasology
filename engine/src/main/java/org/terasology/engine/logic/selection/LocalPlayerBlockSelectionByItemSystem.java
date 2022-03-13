// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.engine.input.events.LeftMouseDownButtonEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.selection.BlockSelectionComponent;
import org.terasology.engine.world.selection.event.SetBlockSelectionEndingPointEvent;
import org.terasology.engine.world.selection.event.SetBlockSelectionStartingPointEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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

    @ReceiveEvent(components = OnItemActivateSelectionComponent.class)
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

    @ReceiveEvent(components = LocationComponent.class)
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
    @ReceiveEvent(components = BlockSelectionComponent.class)
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
