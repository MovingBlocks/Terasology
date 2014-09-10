/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TeraMathTest {

    private static final double MAX_DOUBLE_ERROR = 0.00001;


    @Test
    public void getEdgeRegion() {
        Region3i region = Region3i.createFromMinAndSize(new Vector3i(16, 0, 16), new Vector3i(16, 128, 16));
        assertEquals(Region3i.createFromMinMax(new Vector3i(16, 0, 16), new Vector3i(16, 127, 31)), TeraMath.getEdgeRegion(region, Side.LEFT));
    }

    // This function mimicks a power function using ints only
    private long longPow(int base, int exp) {
        // MAX_DOUBLE_ERROR fixes small rounding errors
        double result = Math.pow(base, exp);
        if (!TeraMath.isFinite(result)) {
            throw new ArithmeticException(Double.toString(result));
        }
        if (result < 0) {
            return (long) (result - MAX_DOUBLE_ERROR);
        }
        return (long) (result + MAX_DOUBLE_ERROR);
    }

    @Test
    public void powDouble() {
        for (int exp = -5; exp <= 5; exp++) {
            for (double base = -10.0; base <= 10.0; base += 0.2f) {
                assertEqualsRatio(base + "^" + exp, Math.pow(base, exp), TeraMath.pow(base, exp), MAX_DOUBLE_ERROR);
            }
        }
    }

    @Test
    public void powInt() {
        // Only from -2 because most negative exponents return a 0
        for (int exp = -2; exp <= 8; exp++) {
            for (int base = -8; base <= 8; base++) {
                long javaMathResult = 9001;
                boolean exception = false;
                try {
                    javaMathResult = longPow(base, exp);
                } catch (ArithmeticException e) {
                    exception = true;
                }

                try {
                    int result = TeraMath.pow(base, exp);
                    assertFalse("(int)" + base + "^" + exp + " did not throw an exception as expected", exception);
                    assertEquals(base + "^" + exp, javaMathResult, (long) result);
                } catch (ArithmeticException e) {
                    assertTrue("(int)" + base + "^" + exp + " threw an unexpected exception", exception);
                }

                try {
                    long result = TeraMath.pow((long) base, exp);
                    assertFalse("(long)" + base + "^" + exp + " did not throw an exception as expected", exception);
                    assertEquals(base + "^" + exp, javaMathResult, result);
                } catch (ArithmeticException e) {
                    assertTrue("(long)" + base + "^" + exp + " threw an unexpected exception", exception);
                }
            }
        }
    }


    // JUnit's assertEquals(expected, value, delta) uses delta as the maximum difference from expected and value
    // This approach is not acceptable for large doubles whose precision decreases as numbers grows
    // Therefore this function uses delta as the maximum deviation of the actual from the expected value
    private void assertEqualsRatio(String msg, double expected, double actual, double error) {
        // If not finite, ignore error. Its value must be exact
        if (!TeraMath.isFinite(expected) && expected != actual) {
            fail(msg);
            return;
        }

        double ratio = expected / actual;
        if (ratio < 0.0) {
            ratio = 1.0 / ratio;
        }
        ratio = TeraMath.fastAbs(ratio - 1.0);
        if (ratio >= error) {
            fail(msg);
        }
    }


}
