/*
 * Copyright 2017 MovingBlocks
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

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;

public class RecordAndReplaySerializer {
    private static EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(RecordAndReplaySerializer.class);
    private static final String EVENT_DIR = "/events";
    private static final String JSON = ".json";
    private static final String REF_ID_MAP = "/ref_id_map" + JSON;
    private static final String FILE_AMOUNT = "/file_amount" + JSON;

    private RecordAndReplaySerializer() {

    }

    public static void setEntityManager(EntityManager manager) {
        entityManager = manager;
    }

    //This will probably not exist once serialization is done. Saves recorded events as String
    /*public static void saveEventsString() {
        StringBuffer sb = new StringBuffer();
        for (RecordedEvent re : RecordedEventStore.getEvents()) {
            saveOneEventAsString(re, sb);
        }
        try {
            int recordCount = RecordAndReplayUtils.getRecordCount();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Recorded_Events"+recordCount+".txt")));
            recordCount++;
            RecordAndReplayUtils.setRecordCount(recordCount);
            writer.write(sb.toString());
            writer.flush();
            writer.close();

        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
    }

    //This will probably not exist once serialization is done
    private static void saveOneEventAsString(RecordedEvent re, StringBuffer sb) {
        sb.append("==================================================\n");
        sb.append("Position: " + re.getPosition() + " Timestamp:" + re.getTimestamp() + "\n");
        sb.append("Event: " + re.getEvent().toString() + "\n");
        sb.append("Entity: " + re.getEntityRefId() + "\n");
        if (re.getComponent() != null) {
            sb.append("Component: " + re.getComponent().toString() + "\n");
        }

    }*/

    public static void serializeRecordAndReplayData() {
        String recordingPath = PathManager.getInstance().getRecordingPath(RecordAndReplayUtils.getGameTitle()).toString();
        serializeRecordedEvents(recordingPath);
        Gson gson = new GsonBuilder().create();
        serializeRefIdMap(gson, recordingPath);
        serializeFileAmount(gson, recordingPath);
    }

    public static void serializeRecordedEvents(String recordingPath) {
        RecordedEventSerializer serializer = new RecordedEventSerializer(entityManager);
        String filepath = recordingPath + EVENT_DIR + RecordAndReplayUtils.getFileCount() + JSON;
        RecordAndReplayUtils.setFileAmount(RecordAndReplayUtils.getFileAmount() + 1);
        RecordAndReplayUtils.setFileCount(RecordAndReplayUtils.getFileCount() + 1);
        serializer.serializeRecordedEvents(RecordedEventStore.popEvents(), filepath);
        logger.info("RecordedEvents Serialization completed!");
    }

    public static void deserializeRecordAndReplayData() {
        String recordingPath = PathManager.getInstance().getRecordingPath(RecordAndReplayUtils.getGameTitle()).toString();
        deserializeRecordedEvents(recordingPath);
        Gson gson = new GsonBuilder().create();
        deserializeRefIdMap(gson, recordingPath);
        deserializeFileAmount(gson, recordingPath);
    }

    public static void deserializeRecordedEvents(String recordingPath) {
        RecordedEventSerializer serializer = new RecordedEventSerializer(entityManager);
        String filepath = recordingPath + EVENT_DIR + RecordAndReplayUtils.getFileCount() + JSON;
        RecordAndReplayUtils.setFileCount(RecordAndReplayUtils.getFileCount() + 1);
        RecordedEventStore.setEvents(serializer.deserializeRecordedEvents(filepath));
        logger.info("RecordedEvents Deserialization completed!");
    }

    private static void serializeRefIdMap(Gson gson, String recordingPath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + REF_ID_MAP));
            gson.toJson(EntityRefIdMap.getCurrentMap(), HashMap.class, writer);
            writer.close();
            logger.info("RefIdMap Serialization completed!");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void deserializeRefIdMap(Gson gson, String recordingPath) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(recordingPath + REF_ID_MAP));
            Type typeOfHashMap = new TypeToken<HashMap<String, Long>>() { }.getType();
            EntityRefIdMap.setPreviousMap(gson.fromJson(jsonElement, typeOfHashMap));
            logger.info("RefIdMap Deserialization completed!");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void serializeFileAmount(Gson gson, String recordingPath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + FILE_AMOUNT));
            gson.toJson(RecordAndReplayUtils.getFileAmount(), Integer.class, writer);
            writer.close();
            logger.info("File Amount Serialization completed!");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void deserializeFileAmount(Gson gson, String recordingPath) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(recordingPath + FILE_AMOUNT));
            Type typeOfCount = new TypeToken<Integer>() { }.getType();
            RecordAndReplayUtils.setFileAmount(gson.fromJson(jsonElement, typeOfCount));
            logger.info("File Amount Deserialization completed!");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }





}
