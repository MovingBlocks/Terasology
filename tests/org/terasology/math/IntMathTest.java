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
        assertEquals(8, TeraMath.ceilPowerOfTwo(8));
        assertEquals(8, TeraMath.ceilPowerOfTwo(7));
        assertEquals(0, TeraMath.ceilPowerOfTwo(-100));
    }
    
    @Test
    public void testSizeOfPower()
    {
        assertEquals(0, TeraMath.sizeOfPower(1));
        assertEquals(1, TeraMath.sizeOfPower(2));
        assertEquals(2, TeraMath.sizeOfPower(4));
        assertEquals(3, TeraMath.sizeOfPower(8));
        assertEquals(4, TeraMath.sizeOfPower(16));
        assertEquals(5, TeraMath.sizeOfPower(32));
    }
    
    @Test
    public void TestFloorToInt()
    {
        assertEquals(0, TeraMath.floorToInt(0f));
        assertEquals(1, TeraMath.floorToInt(1f));
        assertEquals(0, TeraMath.floorToInt(0.5f));
        assertEquals(-1, TeraMath.floorToInt(-0.5f));
        assertEquals(-1, TeraMath.floorToInt(-1f));
    }
    
    @Test
    public void TestCeilToInt()
    {
        assertEquals(0, TeraMath.ceilToInt(0f));
        assertEquals(1, TeraMath.ceilToInt(1f));
        assertEquals(1, TeraMath.ceilToInt(0.5f));
        assertEquals(0, TeraMath.ceilToInt(-0.5f));
        assertEquals(-1, TeraMath.ceilToInt(-1f));
    }
}
