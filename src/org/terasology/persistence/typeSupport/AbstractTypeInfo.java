package org.terasology.persistence.typeSupport;

import org.terasology.persistence.BinaryLevelReader;
import org.terasology.persistence.BinaryLevelWriter;
import org.terasology.persistence.TypeInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractTypeInfo<T> implements TypeInfo<T> {
    private byte size;
    private short id;
    private Class<T> type;

    public AbstractTypeInfo(Class<T> type, byte size) {
        this.size = size;
        this.type = type;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public byte size() {
        return size;
    }

    public Class<T> getType() {
        return type;
    }

    public String getTypeName() {
        return type.getSimpleName();
    }
}
