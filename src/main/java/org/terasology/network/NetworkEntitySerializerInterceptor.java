package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.metadata.FieldMetadata;

/**
 * @author Immortius
 */
public class NetworkEntitySerializerInterceptor implements SerializationInterceptor {

    @Override
    public boolean shouldSerializeField(FieldMetadata metadata, EntityRef entity, Component component) {
        // TODO: Extend to use replication flags
        return metadata.isReplicated();
    }

    @Override
    public boolean shouldSerializeField(FieldMetadata metadata, Prefab prefab, Component component) {
        return true;
    }
}
