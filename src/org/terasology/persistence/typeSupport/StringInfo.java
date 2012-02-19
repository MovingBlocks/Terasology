package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;
import org.terasology.persistence.TypeInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class StringInfo extends AbstractTypeInfo<String> {

    public StringInfo() {
        super(String.class, TypeInfo.VARIABLE_LENGTH);
    }

    public void write(DataOutputStream out, String value, LevelWriter writer) throws Exception {
        out.writeUTF(value);
    }

    public String read(DataInputStream in, LevelReader reader) throws Exception {
        return in.readUTF();
    }
}
