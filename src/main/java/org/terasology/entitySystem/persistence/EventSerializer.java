package org.terasology.entitySystem.persistence;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Immortius
 */
public class EventSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ComponentSerializer.class);

    private EventLibrary eventLibrary;
    private BiMap<Class<? extends Event>, Integer> idTable = ImmutableBiMap.<Class<? extends Event>, Integer>builder().build();

    /**
     * Creates the event serializer.
     *
     * @param eventLibrary The event library used to provide information on each event and its fields.
     */
    public EventSerializer(EventLibrary eventLibrary) {
        this.eventLibrary = eventLibrary;
    }

    /**
     * Sets the mapping between event classes and the ids that are used for serialization
     *
     * @param table
     */
    public void setIdMapping(Map<Class<? extends Event>, Integer> table) {
        idTable = ImmutableBiMap.copyOf(table);
    }

    /**
     * Clears the mapping between event classes and ids. This causes event to be serialized with their event
     * name instead.
     */
    public void removeIdMapping() {
        idTable = ImmutableBiMap.<Class<? extends Event>, Integer>builder().build();
    }

    /**
     * @param eventData
     * @return The event described by the eventData, or null if it couldn't be deserialized
     */
    public Event deserialize(EntityData.Event eventData) {
        Class<? extends Event> eventClass = getEventClass(eventData);
        if (eventClass != null) {
            ClassMetadata<? extends Event> eventMetadata = eventLibrary.getMetadata(eventClass);
            Event event = eventMetadata.newInstance();
            return deserializeOnto(event, eventData, eventMetadata);
        } else {
            logger.warn("Unable to deserialize unknown event type: {}", eventData.getType());
        }
        return null;
    }


    private Event deserializeOnto(Event targetEvent, EntityData.Event eventData, ClassMetadata<? extends Event> eventMetadata) {
        try {
            for (EntityData.NameValue field : eventData.getFieldList()) {
                FieldMetadata fieldInfo = eventMetadata.getField(field.getName());
                if (fieldInfo == null) {
                    continue;
                }

                Object value = fieldInfo.deserialize(field.getValue());
                if (value != null) {
                    fieldInfo.setValue(targetEvent, value);
                }
            }
            return targetEvent;
        } catch (InvocationTargetException e) {
            logger.error("Exception during serializing event type: {}", targetEvent.getClass(), e);
        } catch (IllegalAccessException e) {
            logger.error("Exception during serializing event type: {}", targetEvent.getClass(), e);
        }
        return targetEvent;
    }


    /**
     * Serializes an event.
     *
     * @param event
     * @return The serialized event, or null if it could not be serialized.
     */
    public EntityData.Event serialize(Event event) {
        return serialize(event, FieldSerializeCheck.NullCheck.<Event>newInstance());
    }

    /**
     * Serializes an event.
     *
     * @param event
     * @param check A check to use to see if each field should be serialized.
     * @return The serialized event, or null if it could not be serialized.
     */
    public EntityData.Event serialize(Event event, FieldSerializeCheck<Event> check) {
        ClassMetadata<?> eventMetadata = eventLibrary.getMetadata(event.getClass());
        if (eventMetadata == null) {
            logger.error("Unregistered event type: {}", event.getClass());
            return null;
        }
        EntityData.Event.Builder eventData = EntityData.Event.newBuilder();
        serializeEventType(event, eventData);

        for (FieldMetadata field : eventMetadata.iterateFields()) {
            if (check.shouldSerializeField(field, event)) {
                EntityData.NameValue fieldData = field.serialize(event, false);
                if (fieldData != null) {
                    eventData.addField(fieldData);
                }
            }
        }

        return eventData.build();
    }

    private void serializeEventType(Event event, EntityData.Event.Builder eventData) {
        Integer compId = idTable.get(event.getClass());
        if (compId != null) {
            eventData.setTypeIndex(compId);
        } else {
            eventData.setType(eventLibrary.getMetadata(event).getId());
        }
    }

    /**
     * Determines the event class that the serialized event is for.
     *
     * @param eventData
     * @return The event class the given eventData describes, or null if it is unknown.
     */
    public Class<? extends Event> getEventClass(EntityData.Event eventData) {
        if (eventData.hasTypeIndex()) {
            ClassMetadata<? extends Event> metadata = null;
            if (!idTable.isEmpty()) {
                Class<? extends Event> eventClass = idTable.inverse().get(eventData.getTypeIndex());
                if (eventClass != null) {
                    metadata = eventLibrary.getMetadata(eventClass);
                }
            }
            if (metadata == null) {
                logger.warn("Unable to deserialize unknown event with id: {}", eventData.getTypeIndex());
                return null;
            }
            return metadata.getType();
        } else if (eventData.hasType()) {
            ClassMetadata<? extends Event> metadata = eventLibrary.getMetadata(eventData.getType());
            if (metadata == null) {
                logger.warn("Unable to deserialize unknown event type: {}", eventData.getType());
                return null;
            }
            return metadata.getType();
        }
        logger.warn("Unable to deserialize event, no type provided.");

        return null;
    }

    public Map<Class<? extends Event>, Integer> getIdMapping() {
        return ImmutableMap.copyOf(idTable);
    }
}
