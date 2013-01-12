package org.terasology.network.serialization;

import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;

/**
 * @author Immortius
 */
public class NetworkEventFieldCheck implements FieldSerializeCheck<Event> {

    @Override
    public boolean shouldSerializeField(FieldMetadata field, Event event) {
        return field.isReplicated();
    }

    @Override
    public boolean shouldDeserializeField(FieldMetadata field) {
        return field.isReplicated();
    }
}
