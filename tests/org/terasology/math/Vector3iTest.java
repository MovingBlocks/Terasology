/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
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
public class Vector3iTest {
    
    private Vector3i v1;
    private Vector3i v2;
    private Vector3i v3;
    
    public Vector3iTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        v1 = new Vector3i(1,3,7);
        v2 = new Vector3i(2,6,14);
        v3 = new Vector3i(3,9,21);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void emptyConstructor() 
    {
        Vector3i v = new Vector3i();
        assertEquals(0, v.x);
        assertEquals(0, v.y);
        assertEquals(0, v.z);
    }
    
    @Test
    public void tripleConstructor() 
    {
        Vector3i v = new Vector3i(1,2,3);
        assertEquals(1, v.x);
        assertEquals(2, v.y);
        assertEquals(3, v.z);
    }
    
    @Test
    public void copyConstructor()
    {
        Vector3i copy = new Vector3i(v1);
        assertEquals(v1.x, copy.x);
        assertEquals(v1.y, copy.y);
        assertEquals(v1.z, copy.z);
    }
    
    @Test
    public void testEquals()
    {
        assertFalse(v1.equals(v2));
        assertTrue(v1.equals(new Vector3i(v1.x, v1.y, v1.z)));
        assertFalse(v1.equals(null));
    }
    
    @Test
    public void testSetTriple()
    {
        Vector3i v = new Vector3i(v1);
        v.set(v2.x, v2.y, v2.z);
        assertEquals(v2, v);
    }
    
    @Test
    public void testSetCopy()
    {
        Vector3i v = new Vector3i();
        v.set(v2);
        assertEquals(v2, v);
    }
    
    @Test
    public void testAdd()
    {
        Vector3i v = new Vector3i(v1);
        v.add(v2);
        assertEquals(v3, v);
    }
        
    @Test
    public void testAddTriple()
    {
        Vector3i v = new Vector3i(v1);
        v.add(v2.x, v2.y, v2.z);
        assertEquals(v3, v);
    }

    @Test
    public void testMin()
    {
        Vector3i v = new Vector3i(v1);
        v.min(new Vector3i(v1.z, v1.y, v1.x));
        assertEquals(Math.min(v1.x, v1.z), v.x);
        assertEquals(v1.y, v.y);
        assertEquals(Math.min(v1.x, v1.z), v.z);
    }
    
    @Test
    public void testMax()
    {
        Vector3i v = new Vector3i(v1);
        v.max(new Vector3i(v1.z, v1.y, v1.x));
        assertEquals(Math.max(v1.x, v1.z), v.x);
        assertEquals(v1.y, v.y);
        assertEquals(Math.max(v1.x, v1.z), v.z);
    }
    
    @Test
    public void testIsUnitVector()
    {
        assertFalse(Vector3i.zero().isUnitVector());
        assertTrue(Vector3i.unitX().isUnitVector());
        assertTrue(Vector3i.unitY().isUnitVector());
        assertTrue(Vector3i.unitZ().isUnitVector());
        Vector3i v = Vector3i.unitX();
        v.negate();
        assertTrue(v.isUnitVector());
        assertFalse(Vector3i.one().isUnitVector());
    }
    
    @Test
    public void testManhattanDistance()
    {
        assertEquals(0, Vector3i.zero().gridDistance(Vector3i.zero()));
        assertEquals(1, Vector3i.zero().gridDistance(Vector3i.unitX()));
        assertEquals(1, Vector3i.zero().gridDistance(Vector3i.unitY()));
        assertEquals(1, Vector3i.zero().gridDistance(Vector3i.unitZ()));
        assertEquals(3, Vector3i.zero().gridDistance(Vector3i.one()));
        assertEquals(3, Vector3i.zero().gridDistance(new Vector3i(1,-1,1)));
    }
    
    @Test
    public void testManhattanMagnitude()
    {
        assertEquals(0, Vector3i.zero().gridMagnitude());
        assertEquals(1, Vector3i.unitX().gridMagnitude());
        assertEquals(1, Vector3i.unitY().gridMagnitude());
        assertEquals(1, Vector3i.unitZ().gridMagnitude());
        assertEquals(3, Vector3i.one().gridMagnitude());
        assertEquals(3, new Vector3i(1,-1,1).gridMagnitude());
    }
}
