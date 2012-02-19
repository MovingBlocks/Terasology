package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;
import org.terasology.persistence.TypeInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Dummy Unknown type used by the reader to eat unknown info.
 * @author Immortius <immortius@gmail.com>
 */
public class UnknownTypeInfo implements TypeInfo {
    private byte size;
    
    public UnknownTypeInfo(byte size) {
        this.size = size;
    }
    
    public void write(DataOutputStream out, Object value, LevelWriter writer) throws Exception {
        
    }

    public Object read(DataInputStream in, LevelReader reader) throws Exception {
        byte[] result = new byte[size];
        in.readFully(result);
        return result;
    }

    public short getId() {
        return 0;
    }

    public void setId(short id) {
    }

    public Class getType() {
        return byte[].class;
    }

    public String getTypeName() {
        return "Unknown";
    }

    public byte size() {
        return size;
    }
}
