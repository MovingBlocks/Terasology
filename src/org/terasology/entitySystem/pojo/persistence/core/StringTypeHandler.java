package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class StringTypeHandler implements TypeHandler<String> {

    public EntityData.Value serialize(String value) {
        return EntityData.Value.newBuilder().addString(value).build();
    }

    public String deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return value.getString(0);
        }
        return null;
    }

    public EntityData.Value serialize(Iterable<String> value) {
        return EntityData.Value.newBuilder().addAllString(value).build();
    }

    public List<String> deserializeList(EntityData.Value value) {
        return Lists.newArrayList(value.getStringList());
    }
}
