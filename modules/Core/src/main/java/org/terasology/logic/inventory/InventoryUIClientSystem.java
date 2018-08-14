/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.inventory.InventoryButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryCell;

import java.util.ArrayList;
import java.util.List;

@RegisterSystem(RegisterMode.CLIENT)
public class InventoryUIClientSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(InventoryCell.class);

    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("inventoryHud");
        nuiManager.addOverlay("core:transferItemCursor", ControlWidget.class);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleInventory(InventoryButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen("core:inventoryScreen");
            event.consume();
        }
    }

    /*
     * At the activation of the inventory the current dialog needs to be closed instantly.
     *
     * The close of the dialog triggers {@link #onScreenLayerClosed} which resets the
     * interactionTarget.
     */
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onToggleInventory(InventoryButton event, EntityRef entity, ClientComponent clientComponent) {
        if (event.getState() != ButtonState.DOWN) {
            return;
        }

        EntityRef character = clientComponent.character;
        ResourceUrn activeInteractionScreenUri = InteractionUtil.getActiveInteractionScreenUri(character);
        if (activeInteractionScreenUri != null) {
            InteractionUtil.cancelInteractionAsClient(character);
            // do not consume the event, so that the inventory will still open
        }
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
    public void preAutoSave(){
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

    }

    @Override
    public void postAutoSave() {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        EntityRef movingItem = playerEntity.getComponent(CharacterComponent.class).movingItem;

        EntityRef targetEntity = movingItem;
        EntityRef fromEntity = playerEntity;
        int fromSlot = 0;

        CoreRegistry.get(InventoryManager.class).switchItem(fromEntity, getTransferEntity(), fromSlot, targetEntity, 0);
    }
}
