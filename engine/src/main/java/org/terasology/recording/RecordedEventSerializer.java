// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.recording;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and deserializes RecordedEvents.
 */
class RecordedEventSerializer {

    private static final Logger logger = LoggerFactory.getLogger(RecordedEventSerializer.class);
    private final Serializer<?> serializer;

    public RecordedEventSerializer(EntityManager entityManager, ModuleManager moduleManager, TypeRegistry typeRegistry) {
        TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);
        typeHandlerLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler((EngineEntityManager) entityManager));
        Gson gson = new Gson();
        serializer = new Serializer<>(
                typeHandlerLibrary,
                new GsonPersistedDataSerializer(),
                new GsonPersistedDataWriter(gson),
                new GsonPersistedDataReader(gson)
        );
    }

    /**
     * Serializes RecordedEvent's list.
     *
     * @param events RecordedEvent's list.
     * @param filePath path where the data should be saved.
     */
    public void serializeRecordedEvents(List<RecordedEvent> events, String filePath) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            serializer.serialize(events, new TypeInfo<List<RecordedEvent>>() {
            }, fileOutputStream);
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

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            List<RecordedEvent> recordedEvents =
                    serializer.deserialize(new TypeInfo<List<RecordedEvent>>() {}, fileInputStream).get();
            events.addAll(recordedEvents);
        } catch (SerializationException | IOException e) {
            logger.error("Error while serializing recorded events", e);
        }

        return events;
    }
}
