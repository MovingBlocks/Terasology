// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.entitySystem.metadata.EventMetadata;
import org.terasology.engine.entitySystem.metadata.ReplicatedFieldMetadata;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedData;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.persistence.typeHandling.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;

import java.util.Map;

public class EventSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ComponentSerializer.class);

    private EventLibrary eventLibrary;
    private TypeHandlerLibrary typeHandlerLibrary;
    private BiMap<Class<? extends Event>, Integer> idTable = ImmutableBiMap.<Class<? extends Event>, Integer>builder().build();
    private PersistedDataSerializer persistedDataSerializer;

    /**
     * Creates the event serializer.
     *
     * @param eventLibrary The event library used to provide information on each event and its fields.
     */
    public EventSerializer(EventLibrary eventLibrary, TypeHandlerLibrary typeHandlerLibrary) {
        this.eventLibrary = eventLibrary;
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.persistedDataSerializer = new ProtobufPersistedDataSerializer();
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
     * @return The event described by the eventData
     * @throws DeserializationException if an error occurs when deserializing
     */
    public Event deserialize(EntityData.Event eventData) {
        Class<? extends Event> eventClass = getEventClass(eventData);
        if (eventClass != null) {
            EventMetadata<?> eventMetadata = eventLibrary.getMetadata(eventClass);
            if (!eventMetadata.isConstructable()) {
                throw new DeserializationException("Cannot deserialize " + eventMetadata + " - lacks default constructor");
            } else {
                Event event = eventMetadata.newInstance();
                return deserializeOnto(event, eventData, eventMetadata);
            }
        } else {
            throw new DeserializationException("Unable to deserialize unknown event type: " + eventData.getType());
        }
    }


    private Event deserializeOnto(Event targetEvent, EntityData.Event eventData, EventMetadata<? extends Event> eventMetadata) {
        Serializer serializer = typeHandlerLibrary.getSerializerFor(eventMetadata);
        for (int i = 0; i < eventData.getFieldIds().size(); ++i) {
            byte fieldId = eventData.getFieldIds().byteAt(i);
            ReplicatedFieldMetadata<?, ?> fieldInfo = eventMetadata.getField(fieldId);
            if (fieldInfo == null) {
                logger.error("Unable to serialize field {}, out of bounds", fieldId);
                continue;
            }
            if (fieldInfo.isReplicated()) {
                serializer.deserializeOnto(targetEvent, fieldInfo, new ProtobufPersistedData(eventData.getFieldValue(i)));
            }
        }
        return targetEvent;
    }

    /**
     * Serializes an event.
     *
     * @param event
     * @return The serialized event
     * @throws DeserializationException if an error occurs during serialization
     */
    public EntityData.Event serialize(Event event) {
        EventMetadata<?> eventMetadata = eventLibrary.getMetadata(event.getClass());
        if (eventMetadata == null) {
            throw new SerializationException("Unregistered event type: " + event.getClass());
        } else if (!eventMetadata.isConstructable()) {
            throw new SerializationException("Cannot serialize event '" + eventMetadata
                    + "' - lacks default constructor so cannot be deserialized");
        }
        EntityData.Event.Builder eventData = EntityData.Event.newBuilder();
        serializeEventType(event, eventData);

        Serializer eventSerializer = typeHandlerLibrary.getSerializerFor(eventMetadata);
        ByteString.Output fieldIds = ByteString.newOutput();
        for (ReplicatedFieldMetadata field : eventMetadata.getFields()) {
            if (field.isReplicated()) {
                EntityData.Value serializedValue = ((ProtobufPersistedData) eventSerializer
                        .serialize(field, event, persistedDataSerializer))
                        .getValue();
                if (serializedValue != null) {
                    eventData.addFieldValue(serializedValue);
                    fieldIds.write(field.getId());
                }
            }
        }
        eventData.setFieldIds(fieldIds.toByteString());

        return eventData.build();
    }

    private void serializeEventType(Event event, EntityData.Event.Builder eventData) {
        Integer compId = idTable.get(event.getClass());
        eventData.setType(compId);
    }

    /**
     * Determines the event class that the serialized event is for.
     *
     * @param eventData
     * @return The event class the given eventData describes, or null if it is unknown.
     */
    public Class<? extends Event> getEventClass(EntityData.Event eventData) {
        if (eventData.hasType()) {
            EventMetadata<? extends Event> metadata = null;
            if (!idTable.isEmpty()) {
                Class<? extends Event> eventClass = idTable.inverse().get(eventData.getType());
                if (eventClass != null) {
                    metadata = eventLibrary.getMetadata(eventClass);
                }
            }
            if (metadata == null) {
                logger.warn("Unable to deserialize unknown event with id: {}", eventData.getType()); //NOPMD
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
