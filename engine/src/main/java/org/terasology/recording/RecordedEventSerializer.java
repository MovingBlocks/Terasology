/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.serializers.GsonSerializer;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.utilities.ReflectionUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serializes and deserializes RecordedEvents.
 */
class RecordedEventSerializer {

    private static final Logger logger = LoggerFactory.getLogger(RecordedEventSerializer.class);

    private GsonSerializer gsonSerializer = new GsonSerializer();
    private TypeHandler<List<RecordedEvent>> recordedEventListTypeHandler;

    public RecordedEventSerializer(EntityManager entityManager, ModuleEnvironment moduleEnvironment) {
        ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);

        TypeSerializationLibrary typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(reflectFactory, copyStrategyLibrary);
        typeSerializationLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler((EngineEntityManager) entityManager));

        ClassLoader[] classLoaders = ReflectionUtil.getComprehensiveEngineClassLoaders(moduleEnvironment);

        this.recordedEventListTypeHandler = typeSerializationLibrary.getTypeHandler(
                new TypeInfo<List<RecordedEvent>>() {}, classLoaders).get();
    }

    /**
     * Serializes RecordedEvent's list.
     *
     * @param events RecordedEvent's list.
     * @param filePath path where the data should be saved.
     */
    public void serializeRecordedEvents(List<RecordedEvent> events, String filePath) {
        try {
            gsonSerializer.writeJson(events, recordedEventListTypeHandler, filePath);
        } catch (IOException e) {
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
            PersistedData persistedData = gsonSerializer.persistedDatafromJson(new File(filePath));

            Optional<List<RecordedEvent>> recordedEvents = recordedEventListTypeHandler.deserialize(persistedData);
            recordedEvents.ifPresent(events::addAll);

            if (!recordedEvents.isPresent()) {
                logger.error("Some problem occurred during deserialization, no recorded events was found");
            }
        } catch (IOException e) {
            logger.error("Error while serializing recorded events", e);
        }

        return events;
    }
}
