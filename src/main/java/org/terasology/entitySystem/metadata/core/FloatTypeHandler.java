package org.terasology.entitySystem.metadata.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class FloatTypeHandler implements TypeHandler<Float> {

    public EntityData.Value serialize(Float value) {
        return EntityData.Value.newBuilder().addFloat(value).build();
    }

    public Float deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 0) {
            return value.getFloat(0);
        }
        return null;
    }

    public Float copy(Float value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<Float> value) {
        return EntityData.Value.newBuilder().addAllFloat(value).build();
    }

    public List<Float> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getFloatList());
    }
}
