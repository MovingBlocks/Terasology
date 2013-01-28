package org.terasology.network.serialization;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;

/**
 * @author Immortius
 */
public class ClientComponentFieldCheck implements FieldSerializeCheck<Component> {

    @Override
    public boolean shouldSerializeField(FieldMetadata field, Component component) {
        return field.isReplicated() && field.getReplicationInfo().value().isReplicateFromOwner();
    }

    @Override
    public boolean shouldDeserializeField(FieldMetadata fieldInfo) {
        return true;
    }
}
