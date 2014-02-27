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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtLeast;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;

import java.util.Arrays;
import java.util.LinkedList;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class InventoryAuthoritySystemTest {
    private InventoryAuthoritySystem inventoryAuthoritySystem;
    private EntityRef instigator;
    private EntityRef inventory;
    private InventoryComponent inventoryComp;
    private EntityManager entityManager;

    @Before
    public void setup() {
        inventoryAuthoritySystem = new InventoryAuthoritySystem();
        instigator = Mockito.mock(EntityRef.class);
        inventory = Mockito.mock(EntityRef.class);
        inventoryComp = new InventoryComponent(5);
        Mockito.when(inventory.getComponent(InventoryComponent.class)).thenReturn(inventoryComp);

        entityManager = Mockito.mock(EntityManager.class);
        inventoryAuthoritySystem.setEntityManager(entityManager);
    }

    @Test
    public void removePartOfStack() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        EntityRef itemCopy = Mockito.mock(EntityRef.class);
        ItemComponent itemCompCopy = new ItemComponent();
        setupItemRef(itemCopy, itemCompCopy, 2, 10);

        Mockito.when(entityManager.copy(item)).thenReturn(itemCopy);

        RemoveItemAction action = new RemoveItemAction(instigator, item, false, 1);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertEquals(1, itemComp.stackCount);
        assertEquals(1, itemCompCopy.stackCount);

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(itemCopy, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(itemCopy).saveComponent(itemCompCopy);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(Matchers.any(InventorySlotStackSizeChangedEvent.class));
        Mockito.verify(entityManager).copy(item);

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item, itemCopy);

        assertTrue(action.isConsumed());
        assertEquals(itemCopy, action.getRemovedItem());
    }

    @Test
    public void removeWholeStack() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        RemoveItemAction action = new RemoveItemAction(instigator, item, false, 2);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertTrue(action.isConsumed());
        assertEquals(item, action.getRemovedItem());

        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, new Times(2)).send(Matchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, new Times(2)).send(Matchers.any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);
    }

    @Test
    public void removePartOfStackWithDestroy() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        RemoveItemAction action = new RemoveItemAction(instigator, item, true, 1);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertEquals(1, itemComp.stackCount);

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(Matchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);

        assertTrue(action.isConsumed());
        assertNull(action.getRemovedItem());
    }

    @Test
    public void removeWholeStackWithDestroy() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        RemoveItemAction action = new RemoveItemAction(instigator, item, true, 2);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertTrue(action.isConsumed());
        assertNull(action.getRemovedItem());

        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(item).destroy();
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, new Times(2)).send(Matchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, new Times(2)).send(Matchers.any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);
    }

    @Test
    public void removeOverOneStack() {
        EntityRef item1 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp1 = new ItemComponent();
        setupItemRef(item1, itemComp1, 2, 10);

        inventoryComp.itemSlots.set(0, item1);

        EntityRef item2 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp2 = new ItemComponent();
        setupItemRef(item2, itemComp2, 2, 10);

        inventoryComp.itemSlots.set(1, item2);

        RemoveItemAction action = new RemoveItemAction(instigator, Arrays.asList(item1, item2), false, 3);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));
        assertEquals(3, itemComp1.stackCount);
        assertEquals(1, itemComp2.stackCount);
        assertTrue(action.isConsumed());
        assertEquals(item1, action.getRemovedItem());

        Mockito.verify(item1, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, new AtLeast(0)).exists();
        Mockito.verify(item1).saveComponent(itemComp1);
        Mockito.verify(item2, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2).saveComponent(itemComp2);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(InventorySlotChangedEvent.class));
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item1, item2);
    }

    @Test
    public void removeOverOneStackWithDestroy() {
        EntityRef item1 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp1 = new ItemComponent();
        setupItemRef(item1, itemComp1, 2, 10);

        inventoryComp.itemSlots.set(0, item1);

        EntityRef item2 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp2 = new ItemComponent();
        setupItemRef(item2, itemComp2, 2, 10);

        inventoryComp.itemSlots.set(1, item2);

        RemoveItemAction action = new RemoveItemAction(instigator, Arrays.asList(item1, item2), true, 3);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));
        assertEquals(1, itemComp2.stackCount);
        assertTrue(action.isConsumed());
        assertNull(action.getRemovedItem());

        Mockito.verify(item1, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, new AtLeast(0)).exists();
        Mockito.verify(item1).destroy();
        Mockito.verify(item2, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2).saveComponent(itemComp2);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(InventorySlotChangedEvent.class));
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item1, item2);
    }

    @Test
    public void removeWholeStackWithVeto() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        Mockito.when(inventory.send(Matchers.any(BeforeItemRemovedFromInventory.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        BeforeItemRemovedFromInventory event = (BeforeItemRemovedFromInventory) invocation.getArguments()[0];
                        event.consume();
                        return null;
                    }
                });

        RemoveItemAction action = new RemoveItemAction(instigator, item, true, 2);
        inventoryAuthoritySystem.removeItem(action, inventory);

        assertFalse(action.isConsumed());
        assertNull(action.getRemovedItem());

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(Matchers.any(BeforeItemRemovedFromInventory.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);
    }

    @Test
    public void addItemToEmpty() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, new Times(2)).send(Matchers.any(BeforeItemPutInInventory.class));
        Mockito.verify(inventory, new Times(2)).send(Matchers.any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);

        assertEquals(item, inventoryComp.itemSlots.get(0));
        assertTrue(action.isConsumed());
    }

    @Test
    public void addItemToPartial() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        ItemComponent partialItemComp = new ItemComponent();
        EntityRef partialItem = Mockito.mock(EntityRef.class);
        setupItemRef(partialItem, partialItemComp, 2, 10);

        inventoryComp.itemSlots.set(0, partialItem);

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(item, new AtLeast(0)).iterateComponents();
        Mockito.verify(item).destroy();
        Mockito.verify(partialItem, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(partialItem, new AtLeast(0)).exists();
        Mockito.verify(partialItem, new AtLeast(0)).iterateComponents();
        Mockito.verify(partialItem).saveComponent(partialItemComp);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(Matchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item, partialItem);

        assertEquals(partialItem, inventoryComp.itemSlots.get(0));
        assertEquals(4, partialItemComp.stackCount);
        assertTrue(action.isConsumed());
    }

    @Test
    public void addItemToPartialAndOverflow() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        ItemComponent partialItemComp = new ItemComponent();
        EntityRef partialItem = Mockito.mock(EntityRef.class);
        setupItemRef(partialItem, partialItemComp, 9, 10);

        inventoryComp.itemSlots.set(0, partialItem);

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(item, new AtLeast(0)).iterateComponents();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(partialItem, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(partialItem, new AtLeast(0)).exists();
        Mockito.verify(partialItem, new AtLeast(0)).iterateComponents();
        Mockito.verify(partialItem).saveComponent(partialItemComp);
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(InventorySlotStackSizeChangedEvent.class));
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(InventorySlotChangedEvent.class));
        Mockito.verify(inventory, new Times(3)).send(Matchers.any(BeforeItemPutInInventory.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item, partialItem);

        assertEquals(partialItem, inventoryComp.itemSlots.get(0));
        assertEquals(item, inventoryComp.itemSlots.get(1));
        assertEquals(10, partialItemComp.stackCount);
        assertEquals(1, itemComp.stackCount);
        assertTrue(action.isConsumed());
    }

    @Test
    public void addItemToEmptyWithVeto() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        Mockito.when(inventory.send(Matchers.any(BeforeItemPutInInventory.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        BeforeItemPutInInventory event = (BeforeItemPutInInventory) invocation.getArguments()[0];
                        event.consume();
                        return null;
                    }
                });

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, new AtLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, new AtLeast(0)).exists();
        Mockito.verify(inventory, new AtLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory, new Times(5)).send(Matchers.any(BeforeItemPutInInventory.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);

        assertFalse(action.isConsumed());

    }

    private void setupItemRef(EntityRef item, ItemComponent itemComp, int stackCount, int stackSize) {
        itemComp.stackCount = (byte) stackCount;
        itemComp.maxStackSize = (byte) stackSize;
        itemComp.stackId = "stackId";
        Mockito.when(item.exists()).thenReturn(true);
        Mockito.when(item.getComponent(ItemComponent.class)).thenReturn(itemComp);
        Mockito.when(item.iterateComponents()).thenReturn(new LinkedList<Component>());
    }
}
