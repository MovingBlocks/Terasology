package org.terasology.model.inventory;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class InventoryTest {

	public static class StoreItemInSlot {
		private Inventory inventory;
		private Item item;

		@Before
		public void setUp() {
			inventory = new Inventory();
			item = mock(Item.class);

			when(item.getStackSize()).thenReturn(64);
		}

		@Test
		public void storesItemInSlot() {
			inventory.storeItemInSlot(0, item);

			assertEquals(item, inventory.getItemInSlot(0));
		}

		@Test
		public void returnsTrueGivenValidSlot() {
			assertTrue(inventory.storeItemInSlot(0, item));
		}

		@Test
		public void returnsFalseGivenNegativeSlot() {
			assertFalse(inventory.storeItemInSlot(-1, item));
		}

		@Test
		public void returnsFalseGivenSlotTaken() {
			Item otherItem = mock(Item.class);

			inventory.storeItemInSlot(0, otherItem);
			assertFalse(inventory.storeItemInSlot(0, item));
		}

		@Test
		public void returnsTrueGivenSlotTakenWithSameItem() {
			assertTrue(inventory.storeItemInSlot(0, item));
		}

		@Test
		public void increasesAmountOfItemGivenSlotTakenWithSameItem() {
			inventory.storeItemInSlot(0, item);
			inventory.storeItemInSlot(0, item);

			verify(item).increaseAmount();
		}
	}

	public static class RemoveOneItemInSlot {
		private Inventory inventory;
		private Item item;

		@Before
		public void setUp() {
			inventory = new Inventory();
			item = mock(Item.class);

			when(item.getStackSize()).thenReturn(64);
		}

		@Test
		public void returnsNullGivenItemNotSavedInSlot() {
			assertNull(inventory.getItemInSlot(0));
		}

		@Test
		public void returnsItemGivenItemSavedInSlot() {
			inventory.storeItemInSlot(0, item);
			assertEquals(item, inventory.removeOneItemInSlot(0));
		}

		@Test
		public void decreasesAmountGivenItemSavedInSlot() {
			inventory.storeItemInSlot(0, item);
			inventory.removeOneItemInSlot(0);

			verify(item).decreaseAmount();
		}

		@Test
		public void removesItemGivenAmountOfOne() {
			inventory.storeItemInSlot(0, item);
			inventory.removeOneItemInSlot(0);

			assertNull(inventory.getItemInSlot(0));
		}
	}

	public static class StoreItemInFreeSlot {
		private Inventory inventory;
		private Item item;

		@Before
		public void setUp() {
			inventory = new Inventory();
			item = mock(Item.class);
		}

		@Test
		public void storesItemInZeroGivenEmptyInventory() {
			inventory.storeItemInFreeSlot(item);
			assertEquals(item, inventory.getItemInSlot(0));
		}

		@Test
		public void storesItemInOneGivenItemStackFull() {
			when(item.getStackSize()).thenReturn(1);
			when(item.getAmount()).thenReturn(1);

			inventory.storeItemInFreeSlot(item);
			inventory.storeItemInFreeSlot(item);

			assertEquals(item, inventory.getItemInSlot(1));
		}
	}
}
