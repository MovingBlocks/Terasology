package org.terasology.persistence;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface TypeInfo<T> {
    public static final byte VARIABLE_LENGTH = 0;

    void write(DataOutputStream out, T value, LevelWriter writer) throws Exception;
    T read(DataInputStream in, LevelReader reader) throws Exception;
    
    short getId();
    void setId(short id);
    Class<T> getType();
    String getTypeName();

    /**
     * @return The size of this type in bytes
     */
    byte size();
}
