/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class DirectionTest {
    
    public DirectionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
    public void testRotateAroundY() 
    {
        assertEquals(Direction.East, Direction.North.rotateClockwise());
        assertEquals(Direction.West, Direction.South.rotateClockwise());
        assertEquals(Direction.North, Direction.West.rotateClockwise());
        assertEquals(Direction.Up, Direction.Up.rotateClockwise());
    }
}
