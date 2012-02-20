package org.terasology.model.inventory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.terasology.model.blocks.Block;

@RunWith(Enclosed.class)
public class ItemTest {
	public static class GetExtraction {
		private ItemImpl item;
		private Block block;
		
		@Before
		public void setUp() {
			block = new Block();
			item = new ItemImpl();
		}
		@Test
		public void returnsOneGivenNoExtractionSet() {
			assertEquals(1, item.getExtraction(block));
		}
	}
	
	public static class SetExtraction {
		private ItemImpl item;
		private Block block;
		
		@Before
		public void setUp() {
			block = new Block();
			item = new ItemImpl();
		}
		@Test
		public void returnsAssignedAmountGivenExtraction() {
			item.setExtraction(block, 2);
			assertEquals(2, item.getExtraction(block));
		}
	}
	
	private static class ItemImpl extends Item {
		
	}
}
