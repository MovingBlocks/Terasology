package org.terasology.utilities;


import org.terasology.math.TeraMath;

public class TestMathHelper extends junit.framework.TestCase {
    public void testCantor() throws Exception {
        long test1 = TeraMath.mapToPositive(22);
        assertEquals(22, TeraMath.undoMapToPositive(test1));
        long test2 = TeraMath.mapToPositive(-22);
        assertEquals(-22, TeraMath.undoMapToPositive(test2));

        long cant = TeraMath.cantorize(TeraMath.mapToPositive(-22), TeraMath.mapToPositive(11));
        assertEquals(11, TeraMath.undoMapToPositive(TeraMath.cantorY(cant)));
        assertEquals(-22, TeraMath.undoMapToPositive(TeraMath.cantorX(cant)));
    }
}
