package org.terasology.model.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class InventoryTest {
	public static class GetItemCount {
		private Inventory inventory;
		@Before
		public void setUp() {
			inventory = new Inventory();
		}
		@Test
		public void returnsZeroGivenNoItemInSlot() {
			assertEquals(0, inventory.getItemCount(0));
		}
		@Test
		public void returnsZeroGivenMissingItem() {
			assertEquals(0, inventory.getItemCount(new ItemImpl()));
		}
	}
	public static class StoreItemAt {
		private Inventory inventory;
		private Item item;

		@Before
		public void setUp() {
			inventory = new Inventory();
			item = new ItemImpl();
		}

		@Test
		public void storesItemGivenSlot() {
			inventory.addItemAt(0, item);
			assertEquals(item, inventory.getItemAt(0));
		}

		@Test
		public void returnsTrueGivenValidSlot() {
			assertTrue(inventory.addItemAt(0, item));
		}

		@Test
		public void returnsFalseGivenNegativeSlot() {
			assertFalse(inventory.addItemAt(-1, item));
		}

		@Test
		public void returnsFalseGivenSlotTaken() {
			Item otherItem = new ItemImpl();

			inventory.addItemAt(0, otherItem);
			assertFalse(inventory.addItemAt(0, item));
		}

		@Test
		public void returnsTrueGivenSlotTakenWithSameItem() {
			assertTrue(inventory.addItemAt(0, item));
		}
		
		@Test
		public void incrementsAmountGivenSameItemAndStackable() {
			inventory.addItemAt(0, item);
			inventory.addItemAt(0, item);
			
			assertEquals(2, inventory.getItemCount(0));
		}
	}

	public static class RemoveOneItemInSlot {
		private Inventory inventory;
		private Item item;

		@Before
		public void setUp() {
			inventory = new Inventory();
			item = new ItemImpl();
		}

		@Test
		public void returnsNullGivenItemNotSavedInSlot() {
			assertNull(inventory.getItemAt(0));
		}

		@Test
		public void returnsItemGivenItemSavedInSlot() {
			inventory.addItemAt(0, item);
			assertEquals(item, inventory.removeOneItemAt(0));
		}
		
		@Test
		public void decrementsAmountGivenItemInSlot() {
			inventory.addItemAt(0, item);
			inventory.removeOneItemAt(0);
			
			assertEquals(0, inventory.getItemCount(0));
		}

		@Test
		public void removesItemGivenAmountOfOne() {
			inventory.addItemAt(0, item);
			inventory.removeOneItemAt(0);

			assertNull(inventory.getItemAt(0));
		}
	}

	public static class StoreItem {
		private Inventory inventory;
		private ItemImpl item;

		@Before
		public void setUp() {
			inventory = new Inventory();
			item = new ItemImpl();
		}

		@Test
		public void storesItemInZeroGivenEmptyInventory() {
			inventory.addItem(item);
			assertEquals(item, inventory.getItemAt(0));
		}

		@Test
		public void storesItemInFirstEmptyGivenFirstSlotFull() {
			item.setStackSize(1);
			inventory.addItem(item);
			inventory.addItem(item);

			assertEquals(item, inventory.getItemAt(1));
		}
		@Test
		public void storesItemsGivenCount() {
			item.setStackSize(10);
			inventory.addItem(item, 5);
			assertEquals(5, inventory.getItemCount(0));
		}
	}
	
	private static class ItemImpl extends Item {
		public void setStackSize(int size) {
			this._stackSize = size;
		}
	}
}
