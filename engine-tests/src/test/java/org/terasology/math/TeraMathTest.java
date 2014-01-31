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

import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

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


    // Power of two function tests ///////////////////////////

    //TODO Only used for exhaustive test, so remove this as well
    private boolean slowButCorrectIsPowerOfTwo(int value) {
        // powers of two only have a single high bit
        // 2^n in binary is a '1' followed by n zeros
        return Integer.bitCount(value) == 1 &&
                // However,
                // the most negative number, Integer.MINIMUM_VALUE, is also a single '1' followed by zeros,
                // so that needs to be ruled out.
                // This is done by simply stating that powers of two are always positive.
                value > 0;
    }

    private final static List<Integer> generateAllPowersOfTwo() {
        ArrayList<Integer> powersOfTwo = new ArrayList<>();

        int value = 1;

        while(value > 0) {
            powersOfTwo.add(value);
            value <<= 1;
        }

        return powersOfTwo;
    }

    private final static int[] notPowersOfTwo = {0, -1, 126, 257, Integer.MAX_VALUE, Integer.MIN_VALUE};
    private final static int[] notPowersOfTwoExpectedCeils = {0, 0, 126, 257, Integer.MAX_VALUE, Integer.MIN_VALUE};

    @Test
    public void testIsPowerOfTwo() {
        Collection<Integer> powersOfTwo = generateAllPowersOfTwo();

        for(Integer powerOfTwo: powersOfTwo) {
            assertTrue("isPowerOfTwo(" + powerOfTwo + ")", TeraMath.isPowerOfTwo(powerOfTwo));
        }

        for(int notAPowerOfTwo: notPowersOfTwo) {
            assertFalse("not isPowerOfTwo(" + notAPowerOfTwo + ")", TeraMath.isPowerOfTwo(notAPowerOfTwo));
        }
    }

    @Test //TODO rename to testIsPowerOfTwo?
    public void testFastIsPowerOfTwo() {
        Collection<Integer> powersOfTwo = generateAllPowersOfTwo();

        for(Integer powerOfTwo: powersOfTwo) {
            assertTrue("isPowerOfTwo(" + powerOfTwo + ")", TeraMath.fastIsPowerOfTwo(powerOfTwo));
        }

        for(int notAPowerOfTwo: notPowersOfTwo) {
            assertFalse("not isPowerOfTwo(" + notAPowerOfTwo + ")", TeraMath.fastIsPowerOfTwo(notAPowerOfTwo));
        }
    }

    @Test //TODO remove this exhaustive but slow test?
    public void testFastIsPowerOfTwoForAllInputs() {
        int integer = 0;
        do {
            assertEquals("value " + 0, slowButCorrectIsPowerOfTwo(integer), TeraMath.fastIsPowerOfTwo(integer));
            integer++;
        } while(integer != 0);
    }

    @Test //TODO remove this exhaustive but slow test?
    public void testIsPowerOfTwoForAllForAllInputs() {
        int integer = 0;
        do {
            assertEquals("value " + 0, slowButCorrectIsPowerOfTwo(integer), TeraMath.isPowerOfTwo(integer));
            integer++;
        } while(integer != 0);
    }


    @Test
    public void testCeilPowerOfTwo() {
        List<Integer> powersOfTwo = generateAllPowersOfTwo();
        for(int i = 1; i < powersOfTwo.size(); i++) {
            //test inputs on and around powers of two. Skips tests on zero
            testCeilPowerOfTwo(powersOfTwo.get(i-1), powersOfTwo.get(i));
        }

        int largestIntegerPowerOfTwo = powersOfTwo.get(powersOfTwo.size()-1);
        //test other boundary values
        assertEquals("0", 0, TeraMath.ceilPowerOfTwo(0));
        assertEquals("-1", 0, TeraMath.ceilPowerOfTwo(0));
        assertEquals("Integer.MIN_VALUE", 0, TeraMath.ceilPowerOfTwo(Integer.MIN_VALUE));

        assertEquals("Largest power of two integer + 1", 0, TeraMath.ceilPowerOfTwo(largestIntegerPowerOfTwo + 1));
        assertEquals("Integer.MAX_VALUE", 0, TeraMath.ceilPowerOfTwo(Integer.MIN_VALUE));
        fail("Needs specification and tests for cases where the correct result does not fit in an int");
    }

    /**
     * Tests TeraMath.ceilPowerOfTwo for inputs that are
     * powers of two themselves, or are have a distance of 1 to a power of two.
     * @param currentPowerOfTwo The power of two used to produce the input
     * @param nextPowerOfTwo The next power of two, sometimes used as expected output
     */
    private void testCeilPowerOfTwo(int currentPowerOfTwo, int nextPowerOfTwo){

        assertEquals("input " + currentPowerOfTwo,
                currentPowerOfTwo,  TeraMath.ceilPowerOfTwo(currentPowerOfTwo)
        );


        int expectedValue = (currentPowerOfTwo == 1) ? 0 :
                            (currentPowerOfTwo == 2) ? 1 :
                            currentPowerOfTwo;

        assertEquals("input " + currentPowerOfTwo + " - 1",
                expectedValue,  TeraMath.ceilPowerOfTwo(currentPowerOfTwo - 1)
        );

        assertEquals("input " + currentPowerOfTwo + " + 1",
                nextPowerOfTwo,     TeraMath.ceilPowerOfTwo(currentPowerOfTwo + 1)
        );
    }

    // Utility ///////////////////////////////////////////////

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
