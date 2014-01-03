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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.selection.event.BlockEndSelectionEvent;
import org.terasology.logic.selection.event.BlockSelectionCompletedEvent;
import org.terasology.logic.selection.event.BlockStartSelectionEvent;
import org.terasology.logic.selection.event.RegisterBlockSelectionForRenderingEvent;
import org.terasology.logic.selection.event.UnregisterBlockSelectionForRenderingEvent;

/**
 * System to allow the use of BlockSelectionComponents. This system is a client only system, though no other player
 * will see selections done by one player.  This system uses an item's activate event to start and end a selection,
 * then triggers a ApplyBlockSelectionEvent event. 
 *
 * @author synopia
 */
@RegisterSystem(RegisterMode.CLIENT)
public class LocalPlayerBlockSelectionByItemSystem implements ComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private LocalPlayer localPlayer;

    private BlockSelectionComponent blockSelectionComponent;

    @ReceiveEvent(components = {BlockSelectionComponent.class})
    public void onPlaced(ActivateEvent event, EntityRef itemEntity) {
        if (event.getTargetLocation() == null) {
            return;
        }

        LocationComponent targetLocation = new LocationComponent();
        targetLocation.setWorldPosition(event.getTargetLocation());

        LocalPlayerBlockSelectionByItemSystemListener listener = new LocalPlayerBlockSelectionByItemSystemListener(itemEntity);

        EntityRef targetLocationEntity = entityManager.create(targetLocation, listener);

        this.blockSelectionComponent = itemEntity.getComponent(BlockSelectionComponent.class);
        targetLocationEntity.send(new RegisterBlockSelectionForRenderingEvent(blockSelectionComponent));

        if (null == blockSelectionComponent.startPosition) {
            // on the first item click, we start selecting blocks
            targetLocationEntity.send(new BlockStartSelectionEvent(blockSelectionComponent));
        } else {
            // on the second item click, we will get a BlockSelectionCompletedEvent afterward with our listener component
            targetLocationEntity.send(new BlockEndSelectionEvent(blockSelectionComponent));
        }
    }

    @ReceiveEvent(components = {LocalPlayerBlockSelectionByItemSystemListener.class})
    public void onBlockSelectionCompleted(BlockSelectionCompletedEvent event, EntityRef targetLocationEntity) {
        // This will only be reached with our listener component from a second item activate event
        LocalPlayerBlockSelectionByItemSystemListener listener = targetLocationEntity.getComponent(LocalPlayerBlockSelectionByItemSystemListener.class);
        localPlayer.getCharacterEntity().send(new ApplyBlockSelectionEvent(listener.getItemEntity(), event.getBlockSelectionComponent().currentSelection));
        targetLocationEntity.send(new UnregisterBlockSelectionForRenderingEvent(event.getBlockSelectionComponent()));
        blockSelectionComponent.currentSelection = null;
        blockSelectionComponent.startPosition = null;
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCamTargetChanged(CameraTargetChangedEvent event, EntityRef entity) {
        // This method will update the block selection to whatever block is targeted in the players view
        if (null == blockSelectionComponent) {
            return;
        }

        if (blockSelectionComponent.startPosition == null) {
            return;
        }
        EntityRef target = event.getNewTarget();
        target.send(new BlockEndSelectionEvent(blockSelectionComponent));
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }
}
