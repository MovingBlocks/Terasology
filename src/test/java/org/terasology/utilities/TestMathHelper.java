package org.terasology.utilities;


import org.terasology.math.TeraMath;

public class TestMathHelper extends junit.framework.TestCase {
    public void testCantor() throws Exception {
        int test1 = TeraMath.mapToPositive(22);
        assertEquals(22, TeraMath.redoMapToPositive(test1));
        int test2 = TeraMath.mapToPositive(-22);
        assertEquals(-22, TeraMath.redoMapToPositive(test2));

        int cant = TeraMath.cantorize(TeraMath.mapToPositive(-22), TeraMath.mapToPositive(11));
        assertEquals(11, TeraMath.redoMapToPositive(TeraMath.cantorY(cant)));
        assertEquals(-22, TeraMath.redoMapToPositive(TeraMath.cantorX(cant)));
    }
}
