package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.AbstractTypeHandler;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ListTypeHandler<T> extends AbstractTypeHandler<List<T>> {
    private TypeHandler<T> contentsHandler;
    
    public ListTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

    public EntityData.Value serialize(List<T> value) {
        if (value.size() > 0) {
            return contentsHandler.serialize(value);
        }
        return null;
    }

    public List<T> deserialize(EntityData.Value value) {
        return contentsHandler.deserializeList(value);
    }

    public TypeHandler getContentsHandler() {
        return contentsHandler;
    }
}
