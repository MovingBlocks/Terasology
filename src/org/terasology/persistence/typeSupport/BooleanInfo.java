package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BooleanInfo extends AbstractTypeInfo<Boolean> {

    public BooleanInfo() {
        super(Boolean.class, (byte)1);
    }

    public void write(DataOutputStream out, Boolean value, LevelWriter writer) throws Exception {
        out.writeBoolean(value);
    }

    public Boolean read(DataInputStream in, LevelReader reader) throws Exception {
        return in.readBoolean();
    }
}