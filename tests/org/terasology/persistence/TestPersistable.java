package org.terasology.persistence;

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
        vector3dVal = reader.read("vector3d", Vector3d.class);
    }
}
