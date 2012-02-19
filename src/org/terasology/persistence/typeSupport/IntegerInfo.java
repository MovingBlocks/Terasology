package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class IntegerInfo extends AbstractTypeInfo<Integer> {

    public IntegerInfo() {
        super(Integer.class, (byte)4);
    }

    public void write(DataOutputStream out, Integer value, LevelWriter writer) throws Exception {
        out.writeInt(value);
    }

    public Integer read(DataInputStream in, LevelReader reader) throws Exception {
        return in.readInt();
    }
}
