package org.terasology.model.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class InventoryTest {
	public static class SetSelectedCubby {
		private Inventory inventory;
		@Before
		public void setUp() {
			inventory = new Inventory();
		}
		@Test
		public void setsSelectedCubby() {
			inventory.setSelctedCubbyhole(5);
			assertEquals(5, inventory.getSelectedCubbyhole());
		}
	}
	public static class GetItemCountAt {
		private Inventory inventory;
		@Before
		public void setUp() {
			inventory = new Inventory();
		}
		@Test
		public void returnsZeroGivenNoItemInSlot() {
			assertEquals(0, inventory.getItemCountAt(0));
		}
		@Test
		public void returnsZeroGivenMissingItem() {
			assertEquals(0, inventory.getItemCountAt(0));
		}
	}
	public static class GetItemAt {
		private Inventory inventory;
		@Before
		public void setUp() {
			inventory = new Inventory();
		}
		@Test
		public void returnsNullGivenNoItem() {
			assertNull(inventory.getItemAt(0));
		}
	}
	public static class AddItem {
		private Inventory inventory;
		private ItemImpl item;
		
		@Before
		public void setUp() {
			inventory = new Inventory();
			item = new ItemImpl();
		}
		@Test
		public void addsOneItemToFirstGivenEmpty() {
			inventory.addItem(item, 1);
			assertEquals(1, inventory.getItemCountAt(0));
		}
		@Test
		public void addsOneItemToNextGivenFirstFull() {
			item.setStackSize(64);
			inventory.addItem(item, 64);
			inventory.addItem(item, 3);
			assertEquals(3, inventory.getItemCountAt(1));
		}
		@Test
		public void fillsFirstNonFullCubby() {
			item.setStackSize(64);
			inventory.addItem(item, 62);
			inventory.addItem(item, 10);
			assertEquals(64, inventory.getItemCountAt(0));
		}
		@Test
		public void fillsNextCubbyWithOverflow() {
			item.setStackSize(64);
			inventory.addItem(item,  62);
			inventory.addItem(item, 10);
			assertEquals(8, inventory.getItemCountAt(1));
		}
	}
	public static class RemoveItemAt {
		private Inventory inventory;
		private ItemImpl item;
		
		@Before
		public void setUp() {
			inventory = new Inventory();
			item = new ItemImpl();
		}
		@Test
		public void decrementsItemCount() {
			inventory.addItem(item, 5);
			inventory.removeItemAt(0, 2);
			assertEquals(3, inventory.getItemCountAt(0));
		}
		@Test
		public void returnsItem() {
			inventory.addItem(item, 5);
			assertEquals(item, inventory.removeItemAt(0, 2));
		}
	}
	
	private static class ItemImpl extends Item {
		public void setStackSize(int size) {
			this._stackSize = size;
		}
	}
}
