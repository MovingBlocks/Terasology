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
package org.terasology.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.terasology.persistence.interfaces.LevelReader;
import org.terasology.persistence.interfaces.LevelWriter;

import javax.vecmath.Vector3d;

import static org.junit.Assert.*;

/**
 *
 * @author Immortius <immortius@gmail.com>
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
