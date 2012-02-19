/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terasology.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import javax.vecmath.Vector3d;

import static org.junit.Assert.*;

/**
 *
 * @author Immortius
 */
public class BinaryPersisterTest 
{
    String name = "name";
    String stringData = "DATA";
    ByteArrayOutputStream memStream;
    PersistenceManager persistenceManager;

    @Before
    public void setup() throws IOException {
        persistenceManager = new PersistenceManager();
    }

    private LevelWriter createLevelWriter() throws IOException {
        memStream = new ByteArrayOutputStream();
        return persistenceManager.newWriter(memStream);
    }
    
    private LevelReader createLevelReader() throws IOException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(memStream.toByteArray());
        return persistenceManager.newReader(inStream);
    }

    @Test
    public void testReadAndWrite() throws IOException{
        persistenceManager.registerPersistableClasses(TestPersistable.class);

        TestPersistable testPersist = new TestPersistable();
        testPersist.doubleVal = 3.5;
        testPersist.stringVal = "Test";
        testPersist.vector3dVal = new Vector3d(1,2,3);

        LevelWriter writer = createLevelWriter();
        writer.write(testPersist);
        writer.close();
        assertFalse(writer.isInErrorState());
        
        LevelReader reader = createLevelReader();
        assertTrue(reader.hasNext());
        TestPersistable result = (TestPersistable)reader.next();
        assertFalse(reader.hasNext());
        
        assertEquals(testPersist.doubleVal, result.doubleVal, 0.0001);
        assertEquals(testPersist.stringVal, result.stringVal);
        assertEquals(testPersist.vector3dVal, result.vector3dVal);
    }

}
