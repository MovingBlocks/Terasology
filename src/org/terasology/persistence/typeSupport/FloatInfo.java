package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class FloatInfo extends AbstractTypeInfo<Float> {
    public FloatInfo() {
        super(Float.class, (byte)4);
    }

    public void write(DataOutputStream out, Float value, LevelWriter writer) throws Exception {
        out.writeFloat(value);
    }

    public Float read(DataInputStream in, LevelReader reader) throws Exception {
        return in.readFloat();
    }
}
