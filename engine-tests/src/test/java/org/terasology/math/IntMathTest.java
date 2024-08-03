// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.math;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.context.internal.MockContext;
import org.terasology.engine.registry.CoreRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class IntMathTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntMathTest.class);
    public IntMathTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Config config = new Config(new MockContext());
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, config);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
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
        assertEquals(0, TeraMath.ceilPowerOfTwo(0), "0");
        assertEquals(0, TeraMath.ceilPowerOfTwo(0), "-1");
        assertEquals(0, TeraMath.ceilPowerOfTwo(Integer.MIN_VALUE), "Integer.MIN_VALUE");
        assertEquals(0, TeraMath.ceilPowerOfTwo(Integer.MAX_VALUE), "Integer.MAX_VALUE");
        assertEquals(0, TeraMath.ceilPowerOfTwo(largestIntegerPowerOfTwo + 1), "Largest integer power of two + 1");
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

        assertEquals(currentPowerOfTwo, TeraMath.ceilPowerOfTwo(currentPowerOfTwo),
                () -> "input " + currentPowerOfTwo
        );


        int expectedValue = (currentPowerOfTwo == 1) ? 0
                : (currentPowerOfTwo == 2) ? 1 : currentPowerOfTwo;

        assertEquals(expectedValue, TeraMath.ceilPowerOfTwo(currentPowerOfTwo - 1),
                "input " + currentPowerOfTwo + " - 1"
        );

        assertEquals(nextPowerOfTwo, TeraMath.ceilPowerOfTwo(currentPowerOfTwo + 1),
                () -> "input " + currentPowerOfTwo + " + 1"
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

        LOGGER.info(String.valueOf(powersOfTwo.get(powersOfTwo.size() - 1)));

        return powersOfTwo;
    }
}
