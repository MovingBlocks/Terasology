package org.terasology.entitySystem.metadata;

import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface TypeHandler<T> {
    public EntityData.Value serialize(T value);

    public T deserialize(EntityData.Value value);

    public T copy(T value);

    public EntityData.Value serialize(Iterable<T> value);

    public List<T> deserializeList(EntityData.Value value);
}
