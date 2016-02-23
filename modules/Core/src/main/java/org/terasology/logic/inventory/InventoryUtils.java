/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public final class InventoryUtils {
    private InventoryUtils() {
    }

    public static int getSlotWithItem(EntityRef entity, EntityRef item) {
        int slotCount = getSlotCount(entity);
        for (int i = 0; i < slotCount; i++) {
            if (getItemAt(entity, i) == item) {
                return i;
            }
        }
        return -1;
    }

    public static int getStackCount(EntityRef item) {
        ItemComponent component = item.getComponent(ItemComponent.class);
        if (component == null) {
            return 0;
        }
        return component.stackCount;
    }

    public static int getSlotCount(EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (inventory == null) {
            return 0;
        }
        return inventory.itemSlots.size();
    }

    public static EntityRef getItemAt(EntityRef entity, int slot) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (inventory == null || slot < 0 || slot >= inventory.itemSlots.size()) {
            return EntityRef.NULL;
        }

        return inventory.itemSlots.get(slot);
    }

    public static boolean canStackInto(EntityRef from, EntityRef to) {
        ItemComponent itemFrom = from.getComponent(ItemComponent.class);
        ItemComponent itemTo = to.getComponent(ItemComponent.class);
        if (itemFrom == null) {
            return false;
        }

        if (itemTo == null) {
            return true;
        }

        return isSameItem(from, to) && itemFrom.stackCount + itemTo.stackCount <= itemFrom.maxStackSize;
    }

    public static boolean isSameItem(EntityRef item1, EntityRef item2) {
        ItemComponent itemComp1 = item1.getComponent(ItemComponent.class);
        ItemComponent itemComp2 = item2.getComponent(ItemComponent.class);

        if (itemComp1 == null || itemComp2 == null) {
            return false;
        }

        if (!isSameStackId(itemComp1, itemComp2)) {
            return false;
        }

        if (!hasSameAttributes(item1, item2)) {
            return false;
        }

        return true;
    }

    private static boolean hasSameAttributes(EntityRef from, EntityRef to) {
        Set<Component> differentiatingComponentsFrom = new HashSet<>();
        for (Component component : from.iterateComponents()) {
            if (component instanceof ItemDifferentiating) {
                differentiatingComponentsFrom.add(component);
            }
        }

        Map<Class<?>, Component> differentiatingComponentsTo = new HashMap<>();
        for (Component component : to.iterateComponents()) {
            if (component instanceof ItemDifferentiating) {
                differentiatingComponentsTo.put(component.getClass(), component);
            }
        }

        if (differentiatingComponentsFrom.size() != differentiatingComponentsTo.size()) {
            return false;
        }

        for (Component component : differentiatingComponentsFrom) {
            Component componentInTarget = differentiatingComponentsTo.get(component.getClass());
            if (componentInTarget == null) {
                return false;
            }

            if (!component.equals(componentInTarget)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSameStackId(ItemComponent item1, ItemComponent item2) {
        if (item1.stackId == null || item1.stackId.isEmpty() || item2.stackId == null || item2.stackId.isEmpty()) {
            return false;
        }

        return item1.stackId.equals(item2.stackId);
    }

    private static boolean validateMove(EntityRef instigator, EntityRef from, int slotFrom, EntityRef to, int slotTo) {
        // Validate the move
        EntityRef itemFrom = InventoryUtils.getItemAt(from, slotFrom);
        EntityRef itemTo = InventoryUtils.getItemAt(to, slotTo);

        if (itemFrom.exists()) {
            BeforeItemRemovedFromInventory removeFrom = new BeforeItemRemovedFromInventory(instigator, itemFrom, slotFrom);
            from.send(removeFrom);
            if (removeFrom.isConsumed()) {
                return false;
            }
        }

        if (itemTo.exists()) {
            BeforeItemRemovedFromInventory removeTo = new BeforeItemRemovedFromInventory(instigator, itemTo, slotTo);
            to.send(removeTo);
            if (removeTo.isConsumed()) {
                return false;
            }
        }

        if (itemTo.exists()) {
            BeforeItemPutInInventory putFrom = new BeforeItemPutInInventory(instigator, itemTo, slotFrom);
            from.send(putFrom);
            if (putFrom.isConsumed()) {
                return false;
            }
        }

        if (itemFrom.exists()) {
            BeforeItemPutInInventory putTo = new BeforeItemPutInInventory(instigator, itemFrom, slotTo);
            to.send(putTo);
            if (putTo.isConsumed()) {
                return false;
            }
        }

        return true;
    }

    private static boolean validateMoveAmount(EntityRef instigator, EntityRef from, int slotFrom, EntityRef to, int slotTo, int amount) {
        ItemComponent itemFrom = InventoryUtils.getItemAt(from, slotFrom).getComponent(ItemComponent.class);
        ItemComponent itemTo = InventoryUtils.getItemAt(to, slotTo).getComponent(ItemComponent.class);

        if (itemFrom == null || (itemTo != null && !InventoryUtils.isSameStackId(itemFrom, itemTo))) {
            return false;
        }
        int countOnFrom = itemFrom.stackCount;

        if (amount > countOnFrom) {
            return false;
        }

        int countOnTo = 0;
        if (itemTo != null) {
            countOnTo = itemTo.stackCount;
        }
        if (countOnTo + amount > itemFrom.maxStackSize) {
            return false;
        }

        BeforeItemRemovedFromInventory removeFrom = new BeforeItemRemovedFromInventory(instigator, InventoryUtils.getItemAt(from, slotFrom), slotFrom);
        from.send(removeFrom);
        if (removeFrom.isConsumed()) {
            return false;
        }

        if (itemTo == null) {
            BeforeItemPutInInventory putTo = new BeforeItemPutInInventory(instigator, InventoryUtils.getItemAt(from, slotFrom), slotTo);
            to.send(putTo);
            if (putTo.isConsumed()) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param from     has to provide {@link InventoryComponent} for a successful transfer\
     * @param fromSlot slot number to take the item from
     * @param to       has to provide {@link InventoryComponent} for a successful transfer
     * @param toSlots  slots that will be checked if they contain already the same type of item and have space
     * @return true if any amount > 0 got moved to the target
     */
    private static int moveToExistingStacksInSlots(EntityRef from, int fromSlot, EntityRef to, List<Integer> toSlots) {
        EntityRef fromItem = getItemAt(from, fromSlot);
        ItemComponent fromItemComp = fromItem.getComponent(ItemComponent.class);
        if (fromItemComp == null) {
            return 0;
        }

        int oldFromStackCount = fromItemComp.stackCount;
        int newFromStackCount = fromItemComp.stackCount;
        for (int toSlot : toSlots) {
            EntityRef toItem = getItemAt(to, toSlot);
            if (isSameItem(toItem, fromItem)) {
                ItemComponent toItemComp = toItem.getComponent(ItemComponent.class);
                if (toItemComp == null) {
                    continue;
                }
                int spaceLeft = toItemComp.maxStackSize - toItemComp.stackCount;
                if (spaceLeft > 0) {
                    int amountToTransfer = Math.min(spaceLeft, newFromStackCount);
                    newFromStackCount -= amountToTransfer;
                    if (newFromStackCount == 0) {
                        putItemIntoSlot(from, EntityRef.NULL, fromSlot);
                        fromItem.destroy();
                    } else {
                        adjustStackSize(from, fromSlot, newFromStackCount);
                    }
                    adjustStackSize(to, toSlot, toItemComp.stackCount + amountToTransfer);
                }
            }
            if (newFromStackCount == 0) {
                break;
            }
        }
        return oldFromStackCount - newFromStackCount;
    }

    /**
     * @param instigator used to verify if the action is allowed
     * @param to         has to provide {@link InventoryComponent} for a successful transfer
     * @param slotFrom   slot number to take the items from.
     * @param from       has to provide {@link InventoryComponent} for a successful transfer
     * @param toSlots    slots that will be checked if they are free
     * @return true if at least 1 item got moved from the specified location.
     */
    private static boolean moveToFreeSlots(EntityRef instigator, EntityRef from, int slotFrom, EntityRef to, List<Integer> toSlots) {
        EntityRef fromItem = getItemAt(from, slotFrom);
        ItemComponent fromItemComp = fromItem.getComponent(ItemComponent.class);
        if (fromItemComp == null) {
            return false;
        }

        for (int toSlot : toSlots) {
            EntityRef toItem = getItemAt(to, toSlot);
            if (!toItem.exists()) {
                BeforeItemPutInInventory putTo = new BeforeItemPutInInventory(instigator, fromItem, toSlot);
                to.send(putTo);
                boolean allowed = !putTo.isConsumed();
                if (allowed) {
                    putItemIntoSlot(from, EntityRef.NULL, slotFrom);
                    putItemIntoSlot(to, fromItem, toSlot);
                    return true;
                }
            }
        }
        return false;
    }


    static boolean moveItemToSlots(EntityRef instigator, EntityRef from, int fromSlot, EntityRef to, List<Integer> toSlots) {
        EntityRef fromItem = InventoryUtils.getItemAt(from, fromSlot);
        BeforeItemRemovedFromInventory removeFrom = new BeforeItemRemovedFromInventory(instigator, fromItem, fromSlot);
        from.send(removeFrom);
        if (removeFrom.isConsumed()) {
            return false;
        }

        int stackCount = InventoryUtils.getStackCount(fromItem);

        int movedToStack = moveToExistingStacksInSlots(from, fromSlot, to, toSlots);

        boolean movedToFreeSlot = false;
        if (stackCount != movedToStack) {
            movedToFreeSlot = moveToFreeSlots(instigator, from, fromSlot, to, toSlots);
        }

        return movedToStack > 0 || movedToFreeSlot;
    }


    static boolean moveItem(EntityRef instigator, EntityRef from, int slotFrom, EntityRef to, int slotTo) {
        if (checkForStacking(from, slotFrom, to, slotTo)) {
            return true;
        }

        if (!InventoryUtils.validateMove(instigator, from, slotFrom, to, slotTo)) {
            return false;
        }
        EntityRef itemFrom = getItemAt(from, slotFrom);
        EntityRef itemTo = getItemAt(to, slotTo);

        putItemIntoSlot(from, itemTo, slotFrom);
        putItemIntoSlot(to, itemFrom, slotTo);

        return true;
    }

    private static boolean checkForStacking(EntityRef from, int slotFrom, EntityRef to, int slotTo) {
        EntityRef itemFrom = getItemAt(from, slotFrom);
        EntityRef itemTo = getItemAt(to, slotTo);

        if (itemFrom.exists() && itemTo.exists() && canStackInto(itemFrom, itemTo)) {
            int fromCount = itemFrom.getComponent(ItemComponent.class).stackCount;
            int toCount = itemTo.getComponent(ItemComponent.class).stackCount;
            putItemIntoSlot(from, EntityRef.NULL, slotFrom);
            adjustStackSize(to, slotTo, fromCount + toCount);

            return true;
        }

        return false;
    }

    static boolean moveItemAmount(EntityRef instigator, EntityRef from, int slotFrom, EntityRef to, int slotTo, int amount) {
        if (!InventoryUtils.validateMoveAmount(instigator, from, slotFrom, to, slotTo, amount)) {
            return false;
        }

        EntityRef itemFrom = getItemAt(from, slotFrom);
        EntityRef itemTo = getItemAt(to, slotTo);

        if (!itemTo.exists()) {
            EntityRef fromCopy = itemFrom.copy();

            ItemComponent copyItem = fromCopy.getComponent(ItemComponent.class);
            copyItem.stackCount = (byte) amount;
            fromCopy.saveComponent(copyItem);

            ItemComponent fromItem = itemFrom.getComponent(ItemComponent.class);
            if (fromItem.stackCount == amount) {
                putItemIntoSlot(from, EntityRef.NULL, slotFrom);
            } else {
                adjustStackSize(from, slotFrom, fromItem.stackCount - amount);
            }
            putItemIntoSlot(to, fromCopy, slotTo);
        } else {
            ItemComponent itemFromComponent = itemFrom.getComponent(ItemComponent.class);
            if (itemFromComponent.stackCount == amount) {
                putItemIntoSlot(from, EntityRef.NULL, slotFrom);
            } else {
                adjustStackSize(from, slotFrom, itemFromComponent.stackCount - amount);
            }

            ItemComponent itemToComponent = itemTo.getComponent(ItemComponent.class);
            adjustStackSize(to, slotTo, itemToComponent.stackCount + amount);
        }

        return true;
    }

    static void putItemIntoSlot(EntityRef entity, EntityRef item, int slot) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        EntityRef oldItem = inventory.itemSlots.get(slot);
        inventory.itemSlots.set(slot, item);
        entity.saveComponent(inventory);
        entity.send(new InventorySlotChangedEvent(slot, oldItem, item));
    }

    static void adjustStackSize(EntityRef entity, int slot, int newCount) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        EntityRef item = inventory.itemSlots.get(slot);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        byte oldSize = itemComponent.stackCount;
        itemComponent.stackCount = (byte) newCount;
        item.saveComponent(itemComponent);
        entity.send(new InventorySlotStackSizeChangedEvent(slot, oldSize, newCount));
    }

}
