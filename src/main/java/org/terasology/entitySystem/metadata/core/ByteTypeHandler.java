package org.terasology.entitySystem.metadata.core;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import gnu.trove.list.array.TByteArrayList;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ByteTypeHandler implements TypeHandler<Byte> {

    public EntityData.Value serialize(Byte value) {
        return EntityData.Value.newBuilder().addInteger(value).build();
    }

    public Byte deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return (byte) value.getInteger(0);
        }
        return null;
    }

    // Probably never use bytes this way I hope
    public EntityData.Value serialize(Iterable<Byte> value) {
        TByteArrayList bytes = new TByteArrayList();
        bytes.addAll(Lists.newArrayList(value));
        ByteString byteString = ByteString.copyFrom(bytes.toArray(new byte[bytes.size()]));
        return EntityData.Value.newBuilder().setBytes(byteString).build();
    }

    public Byte copy(Byte value) {
        return value;
    }

    public List<Byte> deserializeList(EntityData.Value value) {
        if (value.hasBytes()) {
            List<Byte> result = Lists.newArrayListWithCapacity(value.getBytes().size());
            for (byte b : value.getBytes().toByteArray()) {
                result.add(b);
            }
            return result;
        }
        return null;
    }
}
