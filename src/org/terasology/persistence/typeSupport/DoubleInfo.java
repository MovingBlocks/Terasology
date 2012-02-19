package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DoubleInfo extends AbstractTypeInfo<Double> {

    public DoubleInfo() {
        super(Double.class, (byte)8);
    }

    public void write(DataOutputStream out, Double value, LevelWriter writer) throws Exception {
        out.writeDouble(value);
    }

    public Double read(DataInputStream in, LevelReader reader) throws Exception {
        return in.readDouble();
    }
}
