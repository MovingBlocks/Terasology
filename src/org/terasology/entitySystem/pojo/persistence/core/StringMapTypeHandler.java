package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.pojo.persistence.AbstractTypeHandler;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class StringMapTypeHandler<T> extends AbstractTypeHandler<Map<String, T>> {

    TypeHandler<T> contentsHandler;

    public StringMapTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

    public EntityData.Value serialize(Map<String, T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (Map.Entry<String, T> entry : value.entrySet()) {
            result.addNameValue(EntityData.NameValue.newBuilder().setName(entry.getKey()).setValue(contentsHandler.serialize(entry.getValue())));
        }
        return result.build();
    }

    public Map<String, T> deserialize(EntityData.Value value) {
        Map<String, T> result = Maps.newHashMap();
        for (EntityData.NameValue entry : value.getNameValueList()) {
            result.put(entry.getName(), contentsHandler.deserialize(entry.getValue()));
        }
        return result;
    }
}
