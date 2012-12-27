package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.metadata.FieldMetadata;

/**
 * @author Immortius
 */
public interface SerializationInterceptor {

    /**
     * @param metadata
     * @param entity
     * @param component
     * @return Whether the field be serialized
     */
    boolean shouldSerializeField(FieldMetadata metadata, EntityRef entity, Component component);

    boolean shouldSerializeField(FieldMetadata metadata, Prefab prefab, Component component);
}
