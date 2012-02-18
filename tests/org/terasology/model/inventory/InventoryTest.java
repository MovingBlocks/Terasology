package org.terasology.model.inventory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.terasology.logic.characters.Player;

@RunWith(Enclosed.class)
public class InventoryTest {
	
	public static class StoreItemInSlot {
		private Inventory inventory;
		private Player player;
		private Item item;
		
		@Before
		public void setUp() {
			player = mock(Player.class);
			inventory = new Inventory(player);
			item = mock(Item.class);
		}
		@Test
		public void storesItemInSlot() {
			inventory.storeItemInSlot(0, item);
			
			assertEquals(item, inventory.getItemInSlot(0));
		}
	}
}
