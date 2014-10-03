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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.*;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds interaction between an inventory slot and the player
 */
public class InventoryCell extends ItemCell {

    @LayoutConfig
    private Binding<Integer> targetSlot = new DefaultBinding<Integer>(0);

    private Binding<EntityRef> targetInventory = new DefaultBinding<>(EntityRef.NULL);

    private LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
    private InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
    private EntityManager entityManager = CoreRegistry.get(EntityManager.class);

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (MouseInput.MOUSE_LEFT == button) {
                if (Keyboard.isKeyDown(Keyboard.Key.LEFT_SHIFT.getId())) {
                    moveItemSmartly();
                } else {
                    swapItem();
                }
            } else if (MouseInput.MOUSE_RIGHT == button) {
                int stackSize = InventoryUtils.getStackCount(getTargetItem());
                if (stackSize > 0) {
                    giveAmount((stackSize + 1) / 2);
                }
            }
            return true;
        }

        @Override
        public boolean onMouseWheel(int wheelTurns, Vector2i pos) {
            int amount = (Keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || Keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL)) ? 2 : 1;

            //move item to the transfer slot
            if (wheelTurns > 0) {
                giveAmount(amount);
            } else {
                //get item from transfer slot
                takeAmount(amount);
            }
            return true;
        }
    };

    public InventoryCell() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.addInteractionRegion(interactionListener, icon.getTooltip(), canvas.getRegion());
    }

    @Override
    public EntityRef getTargetItem() {
        return InventoryUtils.getItemAt(getTargetInventory(), getTargetSlot());
    }

    public void bindTargetInventory(Binding<EntityRef> binding) {
        targetInventory = binding;
    }

    public EntityRef getTargetInventory() {
        return targetInventory.get();
    }

    public void setTargetInventory(EntityRef val) {
        targetInventory.set(val);
    }

    public void bindTargetSlot(Binding<Integer> binding) {
        targetSlot = binding;
    }

    public int getTargetSlot() {
        return targetSlot.get();
    }

    public void setTargetSlot(int val) {
        targetSlot.set(val);
    }

    private void swapItem() {
        inventoryManager.switchItem(getTransferEntity(), localPlayer.getCharacterEntity(), 0, getTargetInventory(), getTargetSlot());
    }

    private void giveAmount(int amount) {
        inventoryManager.moveItem(getTargetInventory(), localPlayer.getCharacterEntity(), getTargetSlot(), getTransferEntity(), 0, amount);
    }

    private void takeAmount(int amount) {
        inventoryManager.moveItem(getTransferEntity(), localPlayer.getCharacterEntity(), 0, getTargetInventory(), getTargetSlot(), amount);
    }

    private void moveItemSmartly() {
        EntityRef fromEntity = getTargetInventory();
        int fromSlot = getTargetSlot();
        EntityRef playerEntity= localPlayer.getCharacterEntity();
        InventoryComponent playerInventory = playerEntity.getComponent(InventoryComponent.class);
        int totalSlotCount = playerInventory.itemSlots.size();
        if (playerInventory == null) {
            return;
        }

        List<Integer> toSlots = new ArrayList<>(totalSlotCount);
        if (fromEntity.equals(playerEntity)) {
            int hudSlotCount = 10; // TODO use a constant once there is one
            boolean fromHud =  (fromSlot < hudSlotCount);
            boolean toHud = !fromHud;
            if (toHud) {
                for (int slot = 0; slot < hudSlotCount; slot++) {
                    toSlots.add(slot);
                }
            } else {
                for (int slot = hudSlotCount; slot < totalSlotCount; slot++) {
                    toSlots.add(slot);
                }
            }
        } else {
            for (int slot = 0; slot < totalSlotCount; slot++) {
                toSlots.add(slot);
            }
        }

        inventoryManager.moveItemToSlots(getTransferEntity(), fromEntity, fromSlot, playerEntity, toSlots);
    }


    private EntityRef getTransferEntity() {
        return localPlayer.getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    private EntityRef getTransferItem() {
        return InventoryUtils.getItemAt(getTransferEntity(), 0);
    }

}
