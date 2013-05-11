/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.entitySystem.persistence;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

import java.io.IOException;
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
        for (int i = 0; i < eventData.getFieldIds().size(); ++i) {
            byte fieldId = eventData.getFieldIds().byteAt(i);
            FieldMetadata fieldInfo = eventMetadata.getFieldById(fieldId);
            if (fieldInfo == null) {
                logger.error("Unable to serialize field {}, out of bounds", fieldId);
                continue;
            }
            if (fieldInfo.isReplicated()) {
                fieldInfo.deserializeOnto(targetEvent, eventData.getFieldValue(i));
            }
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
        ClassMetadata<?> eventMetadata = eventLibrary.getMetadata(event.getClass());
        if (eventMetadata == null) {
            logger.error("Unregistered event type: {}", event.getClass());
            return null;
        }
        EntityData.Event.Builder eventData = EntityData.Event.newBuilder();
        serializeEventType(event, eventData);

        ByteString.Output fieldIds = ByteString.newOutput();
        for (FieldMetadata field : eventMetadata.iterateFields()) {
            if (field.isReplicated()) {
                EntityData.Value serializedValue = field.serialize(event);
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
            ClassMetadata<? extends Event> metadata = null;
            if (!idTable.isEmpty()) {
                Class<? extends Event> eventClass = idTable.inverse().get(eventData.getType());
                if (eventClass != null) {
                    metadata = eventLibrary.getMetadata(eventClass);
                }
            }
            if (metadata == null) {
                logger.warn("Unable to deserialize unknown event with id: {}", eventData.getType());
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
