package org.terasology.rendering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FontColorTest {
	public static class IsValid {
		
		@Test
		public void testResetColor() {
			char resetColor = 0xF000;
			assertTrue(FontColor.isValid(resetColor));
		}
		
		@Test
		public void testFirstColor() {
			char firstColor = 0xE000;
			assertTrue(FontColor.isValid(firstColor));
		}
		
		@Test
		public void testLastColor() {
			char lastColor = 0xEFFF;
			assertTrue(FontColor.isValid(lastColor));
		}
		
		@Test
		public void testBetweenColor() {
			char betweenColor = 0xEB8F;
			assertTrue(FontColor.isValid(betweenColor));
		}
		
		@Test
		public void testInvalidColor() {
			char invalidColor = 0xA10F;
			assertFalse(FontColor.isValid(invalidColor));
		}
	}
}
