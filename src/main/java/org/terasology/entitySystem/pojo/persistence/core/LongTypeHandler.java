package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LongTypeHandler implements TypeHandler<Long> {

    public EntityData.Value serialize(Long value) {
        return EntityData.Value.newBuilder().addLong(value).build();
    }

    public Long deserialize(EntityData.Value value) {
        if (value.getLongCount() > 0) {
            return value.getLong(0);
        }
        return null;
    }

    public Long copy(Long value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Long> value) {
        return EntityData.Value.newBuilder().addAllLong(value).build();
    }

    public List<Long> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getLongList());
    }
}
