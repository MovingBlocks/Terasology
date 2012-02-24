/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
import static org.junit.Assert.*;

/**
 *
 * @author Immortius
 */
public class IntMathTest
{
    public IntMathTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of CeilPowerOfTwo method, of class IntMath.
     */
    @Test
    public void testCeilPowerOfTwo()
    {
        assertEquals(8, IntMath.ceilPowerOfTwo(8));
        assertEquals(8, IntMath.ceilPowerOfTwo(7));
        assertEquals(0, IntMath.ceilPowerOfTwo(-100));
    }
    
    @Test
    public void testSizeOfPower()
    {
        assertEquals(0, IntMath.sizeOfPower(1));
        assertEquals(1, IntMath.sizeOfPower(2));
        assertEquals(2, IntMath.sizeOfPower(4));
        assertEquals(3, IntMath.sizeOfPower(8));
        assertEquals(4, IntMath.sizeOfPower(16));
        assertEquals(5, IntMath.sizeOfPower(32));
    }
    
    @Test
    public void TestFloorToInt()
    {
        assertEquals(0, IntMath.floorToInt(0f));
        assertEquals(1, IntMath.floorToInt(1f));
        assertEquals(0, IntMath.floorToInt(0.5f));
        assertEquals(-1, IntMath.floorToInt(-0.5f));
        assertEquals(-1, IntMath.floorToInt(-1f));
    }
    
    @Test
    public void TestCeilToInt()
    {
        assertEquals(0, IntMath.ceilToInt(0f));
        assertEquals(1, IntMath.ceilToInt(1f));
        assertEquals(1, IntMath.ceilToInt(0.5f));
        assertEquals(0, IntMath.ceilToInt(-0.5f));
        assertEquals(-1, IntMath.ceilToInt(-1f));
    }
}
