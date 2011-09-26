package com.github.begla.blockmania.tests;


import com.github.begla.blockmania.utilities.MathHelper;
import org.junit.Test;

public class TestMathHelper extends junit.framework.TestCase {

    @Test
    public void testCantor() throws Exception {
        int test1 = MathHelper.mapToPositive(22);
        assertEquals(22, MathHelper.redoMapToPositive(test1));
        int test2 = MathHelper.mapToPositive(-22);
        assertEquals(-22, MathHelper.redoMapToPositive(test2));

        int cant = MathHelper.cantorize(MathHelper.mapToPositive(-22), MathHelper.mapToPositive(11));
        assertEquals(11, MathHelper.redoMapToPositive(MathHelper.cantorY(cant)));
        assertEquals(-22, MathHelper.redoMapToPositive(MathHelper.cantorX(cant)));
    }
}
