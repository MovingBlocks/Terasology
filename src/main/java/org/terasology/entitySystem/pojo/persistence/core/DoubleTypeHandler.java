package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DoubleTypeHandler implements TypeHandler<Double> {

    public EntityData.Value serialize(Double value) {
        return EntityData.Value.newBuilder().addDouble(value).build();
    }

    public Double deserialize(EntityData.Value value) {
        if (value.getDoubleCount() > 0) {
            return value.getDouble(0);
        }
        return null;
    }

    public Double copy(Double value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Double> value) {
        return EntityData.Value.newBuilder().addAllDouble(value).build();
    }

    public List<Double> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getDoubleList());
    }
}
