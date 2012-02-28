package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

    public EntityData.Value serialize(Boolean value) {
        return EntityData.Value.newBuilder().addBoolean(value).build();
    }

    public Boolean deserialize(EntityData.Value value) {
        if (value.getBooleanCount() > 0) {
            return value.getBoolean(0);
        }
        return null;
    }

    public EntityData.Value serialize(Iterable<Boolean> value) {
        return EntityData.Value.newBuilder().addAllBoolean(value).build();
    }

    public List<Boolean> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getBooleanList());
    }
}
