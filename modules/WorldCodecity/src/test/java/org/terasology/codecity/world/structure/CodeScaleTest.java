package org.terasology.codecity.world.structure;

import org.junit.Assert;
import org.junit.Test;

public class CodeScaleTest {
	private static final CodeScale squared = new SquaredCodeScale();

	@Test
    public void testSquaredScale() {
        Assert.assertEquals(squared.getScaledSize(100), 10);
    }
}
