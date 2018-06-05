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

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;

import java.io.FileWriter;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for serializing and saving every Recording data.
 */
public final class RecordAndReplaySerializer {

    private static final Logger logger = LoggerFactory.getLogger(RecordAndReplaySerializer.class);
    private static final String EVENT_DIR = "/events";
    private static final String JSON = ".json";
    private static final String REF_ID_MAP = "/ref_id_map" + JSON;
    private static final String FILE_AMOUNT = "/file_amount" + JSON;

    private EntityManager entityManager;
    private RecordedEventStore recordedEventStore;
    private EntityIdMap entityIdMap;
    private RecordAndReplayUtils recordAndReplayUtils;

    public RecordAndReplaySerializer(EntityManager manager, RecordedEventStore store, EntityIdMap idMap, RecordAndReplayUtils recordAndReplayUtils) {
        this.entityManager = manager;
        this.recordedEventStore = store;
        this.entityIdMap = idMap;
        this.recordAndReplayUtils = recordAndReplayUtils;
    }

    /**
     * Serialize RecordedEvents, EntityIdMap and some RecordAndReplayUtils data.
     */
    public void serializeRecordAndReplayData() {
        String recordingPath = PathManager.getInstance().getRecordingPath(recordAndReplayUtils.getGameTitle()).toString();
        serializeRecordedEvents(recordingPath);
        Gson gson = new GsonBuilder().create();
        serializeRefIdMap(gson, recordingPath);
        serializeFileAmount(gson, recordingPath);
    }

    /**
     * Serialize RecordedEvents.
     * @param recordingPath path where the data should be saved.
     */
    public void serializeRecordedEvents(String recordingPath) {
        RecordedEventSerializer serializer = new RecordedEventSerializer(entityManager);
        String filepath = recordingPath + EVENT_DIR + recordAndReplayUtils.getFileCount() + JSON;
        recordAndReplayUtils.setFileAmount(recordAndReplayUtils.getFileAmount() + 1);
        recordAndReplayUtils.setFileCount(recordAndReplayUtils.getFileCount() + 1);
        serializer.serializeRecordedEvents(recordedEventStore.popEvents(), filepath);
        logger.info("RecordedEvents Serialization completed!");
    }

    /**
     * Deserialize RecordedEvents, EntityIdMap and some RecordAndReplayUtils data.
     */
    public void deserializeRecordAndReplayData() {
        String recordingPath = PathManager.getInstance().getRecordingPath(recordAndReplayUtils.getGameTitle()).toString();
        deserializeRecordedEvents(recordingPath);
        Gson gson = new GsonBuilder().create();
        deserializeRefIdMap(gson, recordingPath);
        deserializeFileAmount(gson, recordingPath);
    }

    /**
     * Deserialize RecordedEvents.
     * @param recordingPath path where the data was saved.
     */
    void deserializeRecordedEvents(String recordingPath) {
        RecordedEventSerializer serializer = new RecordedEventSerializer(entityManager);
        String filepath = recordingPath + EVENT_DIR + recordAndReplayUtils.getFileCount() + JSON;
        recordAndReplayUtils.setFileCount(recordAndReplayUtils.getFileCount() + 1);
        recordedEventStore.setEvents(serializer.deserializeRecordedEvents(filepath));
        logger.info("RecordedEvents Deserialization completed!");
    }

    private void serializeRefIdMap(Gson gson, String recordingPath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + REF_ID_MAP));
            gson.toJson(entityIdMap.getCurrentMap(), HashBiMap.class, writer);
            writer.close();
            logger.info("RefIdMap Serialization completed!");
        } catch (Exception e) {
            logger.error("Error while serializing Entity ID Map:", e);
        }
    }

    private void deserializeRefIdMap(Gson gson, String recordingPath) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(recordingPath + REF_ID_MAP));
            Type typeOfHashMap = new TypeToken<HashMap<String, Long>>() { }.getType();
            Map<String, Long> previousMap = gson.fromJson(jsonElement, typeOfHashMap);
            entityIdMap.setPreviousMap(HashBiMap.create(previousMap));
            logger.info("RefIdMap Deserialization completed!");
        } catch (Exception e) {
            logger.error("Error while deserializing Entity ID Map:", e);
        }
    }

    private void serializeFileAmount(Gson gson, String recordingPath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + FILE_AMOUNT));
            gson.toJson(recordAndReplayUtils.getFileAmount(), Integer.class, writer);
            writer.close();
            logger.info("File Amount Serialization completed!");
        } catch (Exception e) {
            logger.error("Error while serializing file amount:", e);
        }
    }

    private void deserializeFileAmount(Gson gson, String recordingPath) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(recordingPath + FILE_AMOUNT));
            Type typeOfCount = new TypeToken<Integer>() { }.getType();
            recordAndReplayUtils.setFileAmount(gson.fromJson(jsonElement, typeOfCount));
            logger.info("File Amount Deserialization completed!");
        } catch (Exception e) {
            logger.error("Error while deserializing file amount:", e);
        }
    }





}
