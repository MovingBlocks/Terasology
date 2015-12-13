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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.context.internal.ContextImpl;
import org.terasology.registry.CoreRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 */
public class IntMathTest {
    public IntMathTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Config config = new Config();
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, config);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testCeilPowerOfTwo() {
        List<Integer> powersOfTwo = generateAllPowersOfTwo();
        for (int i = 1; i < powersOfTwo.size(); i++) {
            //test inputs on and around powers of two. Skips tests on zero
            testCeilPowerOfTwo(powersOfTwo.get(i - 1), powersOfTwo.get(i));
        }

        int largestIntegerPowerOfTwo = powersOfTwo.get(powersOfTwo.size() - 1);
        //test other boundary values
        assertEquals("0", 0, TeraMath.ceilPowerOfTwo(0));
        assertEquals("-1", 0, TeraMath.ceilPowerOfTwo(0));
        assertEquals("Integer.MIN_VALUE", 0, TeraMath.ceilPowerOfTwo(Integer.MIN_VALUE));
        assertEquals("Integer.MAX_VALUE", 0, TeraMath.ceilPowerOfTwo(Integer.MAX_VALUE));
        assertEquals("Largest integer power of two + 1", 0, TeraMath.ceilPowerOfTwo(largestIntegerPowerOfTwo + 1));
    }

    @Test
    public void testSizeOfPower() {
        assertEquals(0, TeraMath.sizeOfPower(1));
        assertEquals(1, TeraMath.sizeOfPower(2));
        assertEquals(2, TeraMath.sizeOfPower(4));
        assertEquals(3, TeraMath.sizeOfPower(8));
        assertEquals(4, TeraMath.sizeOfPower(16));
        assertEquals(5, TeraMath.sizeOfPower(32));
    }

    @Test
    public void testFloorToInt() {
        assertEquals(0, TeraMath.floorToInt(0f));
        assertEquals(1, TeraMath.floorToInt(1f));
        assertEquals(0, TeraMath.floorToInt(0.5f));
        assertEquals(-1, TeraMath.floorToInt(-0.5f));
        assertEquals(-1, TeraMath.floorToInt(-1f));
    }

    @Test
    public void testCeilToInt() {
        assertEquals(0, TeraMath.ceilToInt(0f));
        assertEquals(1, TeraMath.ceilToInt(1f));
        assertEquals(1, TeraMath.ceilToInt(0.5f));
        assertEquals(0, TeraMath.ceilToInt(-0.5f));
        assertEquals(-1, TeraMath.ceilToInt(-1f));
    }

    /**
     * Tests TeraMath.ceilPowerOfTwo for inputs that are
     * powers of two themselves, or are have a distance of 1 to a power of two.
     *
     * @param currentPowerOfTwo The power of two used to produce the input
     * @param nextPowerOfTwo    The next power of two, sometimes used as expected output
     */
    private void testCeilPowerOfTwo(int currentPowerOfTwo, int nextPowerOfTwo) {

        assertEquals("input " + currentPowerOfTwo,
                currentPowerOfTwo, TeraMath.ceilPowerOfTwo(currentPowerOfTwo)
        );


        int expectedValue = (currentPowerOfTwo == 1) ? 0
                : (currentPowerOfTwo == 2) ? 1 : currentPowerOfTwo;

        assertEquals("input " + currentPowerOfTwo + " - 1",
                expectedValue, TeraMath.ceilPowerOfTwo(currentPowerOfTwo - 1)
        );

        assertEquals("input " + currentPowerOfTwo + " + 1",
                nextPowerOfTwo, TeraMath.ceilPowerOfTwo(currentPowerOfTwo + 1)
        );
    }

    /**
     * Generates a list of all powers of two that fit within a int
     *
     * @return list of powers of two
     */
    private static List<Integer> generateAllPowersOfTwo() {
        List<Integer> powersOfTwo = new ArrayList<>();

        int value = 1;

        while (value > 0) {
            powersOfTwo.add(value);
            value <<= 1;
        }

        System.out.println(powersOfTwo.get(powersOfTwo.size() - 1));

        return powersOfTwo;
    }
}
