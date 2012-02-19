package org.terasology.model.inventory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class CubbyholeTest {
	public static class Insert {
		private Cubbyhole cubby;
		private ItemImpl item;
		@Before
		public void setUp() {
			cubby = new Cubbyhole();
			item = new ItemImpl();
		}
		@Test
		public void insertsItem() {
			cubby.insert(item);
			assertEquals(item, cubby.getItem());
		}
		@Test
		public void returnsNullGivenNoOldItem() {
			assertNull(cubby.insert(item));
		}
		@Test
		public void returnsNullGivenNoOverflow() {
			item.setStackSize(64);
			
			cubby.insert(item, 63);
			assertNull(cubby.insert(item));
		}
		@Test
		public void setsItemCountToOneGivenNoOldItem() {
			cubby.insert(item);
			assertEquals(1, cubby.getItemCount());
		}
		@Test
		public void setsItemCountGivenCount() {
			cubby.insert(item, 10);
			assertEquals(10, cubby.getItemCount());
		}
		@Test
		public void replacesItemGivenNewItem() {
			ItemImpl newItem = new ItemImpl();
			cubby.insert(newItem);
			assertEquals(newItem, cubby.getItem());
		}
		@Test
		public void resetsItemCountGivenNewItem() {
			ItemImpl newItem = new ItemImpl();
			cubby.insert(item, 10);
			cubby.insert(newItem);
			assertEquals(1, cubby.getItemCount());
		}
		@Test
		public void returnsCubbyholeWithOldItemGivenExistingItem() {
			Item newItem = new ItemImpl();
			cubby.insert(item);
			assertEquals(item, cubby.insert(newItem).getItem());
		}
		@Test
		public void returnsCubbyholeWithOldItemCountGivenExistingItem() {
			Item newItem = new ItemImpl();
			cubby.insert(item);
			assertEquals(1, cubby.insert(newItem).getItemCount());
		}
		@Test
		public void returnsCubbyholeWithOverflowItemCountGivenSameItem() {
			item.setStackSize(64);
			cubby.insert(item, 60);
			assertEquals(6, cubby.insert(item, 10).getItemCount());
		}
		@Test
		public void addsToItemCountGivenSameItem() {
			cubby.insert(item);
			cubby.insert(item);
			assertEquals(2, cubby.getItemCount());
		}
	}
	
	public static class Remove {
		private Cubbyhole cubby;
		private ItemImpl item;
		@Before
		public void setUp() {
			cubby = new Cubbyhole();
			item = new ItemImpl();
		}
		@Test(expected=IllegalArgumentException.class)
		public void throwsGivenNotEnoughItems() {
			cubby.insert(item, 10);
			cubby.remove(11);
		}
		@Test
		public void removesItemGivenSingleCount() {
			cubby.insert(item);
			cubby.remove(1);
			assertNull(cubby.getItem());
		}
		@Test
		public void returnsItemGivenLastOne() {
			cubby.insert(item);
			assertEquals(item, cubby.remove(1));
		}
		@Test
		public void decrementsItemCount() {
			cubby.insert(item, 10);
			cubby.remove(5);
			assertEquals(5, cubby.getItemCount());
		}
	}
	public static class IsEmpty {
		private Cubbyhole cubby;
		private ItemImpl item;
		@Before
		public void setUp() {
			cubby = new Cubbyhole();
			item = new ItemImpl();
		}
		@Test
		public void returnsTrueGivenNoItems() {
			assertTrue(cubby.isEmpty());
		}
		@Test
		public void returnsFalseGivenItems() {
			cubby.insert(item);
			assertFalse(cubby.isEmpty());
		}
	}
	public static class Clear {
		private Cubbyhole cubby;
		private ItemImpl item;
		@Before
		public void setUp() {
			cubby = new Cubbyhole();
			item = new ItemImpl();
		}
		@Test
		public void removesItem() {
			cubby.insert(item);
			cubby.clear();
			assertNull(cubby.getItem());
		}
		@Test
		public void clearsItemCount() {
			cubby.insert(item);
			cubby.clear();
			assertEquals(0, cubby.getItemCount());
		}
		@Test
		public void returnsCubbyWithExistingItem() {
			cubby.insert(item);
			assertEquals(item, cubby.clear().getItem());
		}
		@Test
		public void returnsCubbyWithExistingCount() {
			cubby.insert(item, 10);
			assertEquals(10, cubby.clear().getItemCount());
		}
		@Test
		public void returnsNullGivenEmpty() {
			assertNull(cubby.clear());
		}
	}
	
	private static class ItemImpl extends Item {
		public void setStackSize(int size) {
			this._stackSize = size;
		}
	}
}
