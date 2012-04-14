package org.terasology.entitySystem.pojo.persistence;

import com.google.common.collect.Lists;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractTypeHandler<T> implements TypeHandler<T> {

    public EntityData.Value serialize(Iterable<T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (T item : value) {
            result.addValue(serialize(item));
        }
        return result.build();
    }

    public List<T> deserializeList(EntityData.Value value) {
        List<T> result = Lists.newArrayListWithCapacity(value.getValueCount());
        for (EntityData.Value item : value.getValueList()) {
            result.add(deserialize(item));
        }
        return result;
    }
}
