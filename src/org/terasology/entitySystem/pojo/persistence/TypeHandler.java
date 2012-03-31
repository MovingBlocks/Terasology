package org.terasology.entitySystem.pojo.persistence;

import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface TypeHandler<T> {
    public EntityData.Value serialize(T value);
    public T deserialize(EntityData.Value value);

    public EntityData.Value serialize(Iterable<T> value);
    public List<T> deserializeList(EntityData.Value value);
}
