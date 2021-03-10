// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.persistence.serializers.GsonSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and deserializes RecordedEvents.
 */
class RecordedEventSerializer {

    private static final Logger logger = LoggerFactory.getLogger(RecordedEventSerializer.class);

    private GsonSerializer gsonSerializer;

    public RecordedEventSerializer(EntityManager entityManager, ModuleManager moduleManager, TypeRegistry typeRegistry) {
        TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);
        typeHandlerLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler((EngineEntityManager) entityManager));

        gsonSerializer = new GsonSerializer(typeHandlerLibrary);
    }

    /**
     * Serializes RecordedEvent's list.
     *
     * @param events RecordedEvent's list.
     * @param filePath path where the data should be saved.
     */
    public void serializeRecordedEvents(List<RecordedEvent> events, String filePath) {
        try {
            gsonSerializer.writeJson(events, new TypeInfo<List<RecordedEvent>>() {}, filePath);
        } catch (IOException | SerializationException e) {
            logger.error("Error while serializing recorded events", e);
        }
    }

    /**
     * Deserializes RecordedEvent's list.
     *
     * @param filePath path where the data should be saved.
     */
    public List<RecordedEvent> deserializeRecordedEvents(String filePath) {
        List<RecordedEvent> events = new ArrayList<>();

        try {
            List<RecordedEvent> recordedEvents =
                    gsonSerializer.fromJson(new File(filePath), new TypeInfo<List<RecordedEvent>>() {});
            events.addAll(recordedEvents);
        } catch (SerializationException | IOException e) {
            logger.error("Error while serializing recorded events", e);
        }

        return events;
    }
}
