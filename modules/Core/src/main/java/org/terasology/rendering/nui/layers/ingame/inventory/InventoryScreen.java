/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.engine.Time;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;

import java.util.ArrayList;
import java.util.List;


public class InventoryScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryCell.class);

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        inventory.setCellOffset(10);
    }

    @Override
    public boolean isModal() {
        return false;
    }

    /*
      The numbersBetween() and getTransferEntity() methods were
      originally in the InventoryCell class. They were copied over
      to here because they are private functions that the
      moveItemSmartly() method needs to function. The first section
      of code in onClosed() is based on the moveItemSmartly() method.
    */
    private List<Integer> numbersBetween(int start, int exclusiveEnd) {
        List<Integer> numbers = new ArrayList<>();
        for (int number = start; number < exclusiveEnd; number++) {
            numbers.add(number);
        }
        return numbers;
    }

    private EntityRef getTransferEntity() {
        return CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }


    @Override
    public void onClosed(){
        /*
          The code below was originally taken from moveItemSmartly() in
          InventoryCell.class and slightly modified to work here.

          The way items are being moved to and from the hotbar is really
          similar to what was needed here to take them out of the transfer
          slot and sort them into the inventory.
        */
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        EntityRef movingItem = playerEntity.getComponent(CharacterComponent.class).movingItem;

        EntityRef fromEntity = movingItem;
        int fromSlot = 0;

        InventoryComponent playerInventory = playerEntity.getComponent(InventoryComponent.class);
        if (playerInventory == null) {
            return;
        }
        CharacterComponent characterComponent = playerEntity.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Character entity of player had no character component");
            return;
        }
        int totalSlotCount = playerInventory.itemSlots.size();

        EntityRef interactionTarget = characterComponent.predictedInteractionTarget;
        InventoryComponent interactionTargetInventory = interactionTarget.getComponent(InventoryComponent.class);


        EntityRef targetEntity;
        List<Integer> toSlots = new ArrayList<>(totalSlotCount);
        if (fromEntity.equals(playerEntity)) {

            if (interactionTarget.exists() && interactionTargetInventory != null) {
                targetEntity = interactionTarget;
                toSlots = numbersBetween(0, interactionTargetInventory.itemSlots.size());
            } else {
                targetEntity = playerEntity;

                toSlots = numbersBetween(0, totalSlotCount);

            }
        } else {
            targetEntity = playerEntity;
            toSlots = numbersBetween(0, totalSlotCount);
        }

        CoreRegistry.get(InventoryManager.class).moveItemToSlots(getTransferEntity(), fromEntity, fromSlot, targetEntity, toSlots);



        /*
         The code below was taken from the InteractionListener in the
         DropItemRegion.class and slightly modified to work here.

         The code to drop an item right in front of the player that
         was in that class was almost exactly what was needed here.
        */
        EntityRef item  = InventoryUtils.getItemAt(movingItem, 0);

        int count = InventoryUtils.getStackCount(item);

        Vector3f position = localPlayer.getViewPosition();
        Vector3f direction = localPlayer.getViewDirection();
        Vector3f newPosition = new Vector3f(position.x + direction.x * 1.5f,
                position.y + direction.y * 1.5f,
                position.z + direction.z * 1.5f
        );

        //send DropItemRequest
        Vector3f impulseVector = new Vector3f(direction);
        playerEntity.send(new DropItemRequest(item, playerEntity,
                impulseVector,
                newPosition,
                count));
    }
}
