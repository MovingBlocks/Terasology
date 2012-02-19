package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;

import javax.vecmath.Vector3d;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3dInfo extends AbstractTypeInfo<Vector3d> {
    
    public Vector3dInfo() {
        super(Vector3d.class, (byte)(8 * 3));
    }

    public void write(DataOutputStream out, Vector3d value, LevelWriter writer) throws Exception {
        out.writeDouble(value.x);
        out.writeDouble(value.y);
        out.writeDouble(value.z);
    }

    public Vector3d read(DataInputStream in, LevelReader reader) throws Exception {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();
        return new Vector3d(x,y,z);
    }
}
