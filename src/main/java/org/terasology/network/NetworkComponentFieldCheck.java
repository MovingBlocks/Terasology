package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;

/**
 * @author Immortius
 */
public class NetworkComponentFieldCheck implements FieldSerializeCheck<Component> {
    @Override
    public boolean shouldSerializeField(FieldMetadata field, Component component) {
        return field.isReplicated();
    }
}
