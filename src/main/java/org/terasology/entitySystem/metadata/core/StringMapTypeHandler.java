package org.terasology.entitySystem.metadata.core;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.metadata.TypeHandler;
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

    public Map<String, T> copy(Map<String, T> value) {
        if (value != null) {
            Map<String, T> result = Maps.newHashMap();
            for (Map.Entry<String, T> entry : result.entrySet()) {
                result.put(entry.getKey(), contentsHandler.copy(entry.getValue()));
            }
            return result;
        }
        return null;
    }
}
