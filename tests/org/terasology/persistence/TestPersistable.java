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

import org.terasology.persistence.interfaces.Persistable;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Vector3d;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class TestPersistable implements Persistable {
    String stringVal;
    Vector3d vector3dVal;
    double doubleVal;
    
    public void store(StorageWriter writer) {
        writer.write("string", stringVal);
        writer.write("vector3d", vector3dVal);
        writer.write("double", doubleVal);
    }

    public void retrieve(StorageReader reader) {
        stringVal = reader.readString("string");
        doubleVal = reader.readDouble("double",0);
        vector3dVal = reader.read("vector3d", Vector3d.class, new Vector3d());
    }
}
