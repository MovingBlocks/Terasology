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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.reflection.TypeRegistry;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for serializing and saving every Recording data.
 */
public final class RecordAndReplaySerializer {

    private static final Logger logger = LoggerFactory.getLogger(RecordAndReplaySerializer.class);
    private static final String EVENT_DIR = "/events";
    private static final String JSON = ".json";
    private static final String FILE_AMOUNT = "/file_amount" + JSON;
    private static final String STATE_EVENT_POSITION = "/state_event_position" + JSON;
    private static final String DIRECTION_ORIGIN_LIST = "/direction_origin_list" + JSON;

    private RecordedEventStore recordedEventStore;
    private RecordAndReplayUtils recordAndReplayUtils;
    private CharacterStateEventPositionMap characterStateEventPositionMap;
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    private RecordedEventSerializer recordedEventSerializer;

    public RecordAndReplaySerializer(EntityManager manager, RecordedEventStore store,
                                     RecordAndReplayUtils recordAndReplayUtils,
                                     CharacterStateEventPositionMap characterStateEventPositionMap,
                                     DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList,
                                     ModuleManager moduleManager, TypeRegistry typeRegistry) {
        this.recordedEventStore = store;
        this.recordAndReplayUtils = recordAndReplayUtils;
        this.characterStateEventPositionMap = characterStateEventPositionMap;
        this.directionAndOriginPosRecorderList = directionAndOriginPosRecorderList;
        this.recordedEventSerializer = new RecordedEventSerializer(manager, moduleManager, typeRegistry);
    }

    /**
     * Serialize the recorded data.
     */
    public void serializeRecordAndReplayData() {
        String recordingPath = PathManager.getInstance().getRecordingPath(recordAndReplayUtils.getGameTitle()).toString();
        serializeRecordedEvents(recordingPath);
        Gson gson = new GsonBuilder().create();
        serializeFileAmount(gson, recordingPath);
        serializeCharacterStateEventPositionMap(gson, recordingPath);
        serializeAttackEventExtraRecorder(gson, recordingPath);
    }

    /**
     * Serialize RecordedEvents.
     *
     * @param recordingPath path where the data should be saved.
     */
    public void serializeRecordedEvents(String recordingPath) {
        String filepath = recordingPath + EVENT_DIR + recordAndReplayUtils.getFileCount() + JSON;
        recordAndReplayUtils.setFileAmount(recordAndReplayUtils.getFileAmount() + 1);
        recordAndReplayUtils.setFileCount(recordAndReplayUtils.getFileCount() + 1);
        recordedEventSerializer.serializeRecordedEvents(recordedEventStore.popEvents(), filepath);
        logger.info("RecordedEvents Serialization completed!");
    }

    /**
     * Deserialize recorded data.
     */
    public void deserializeRecordAndReplayData() {
        String recordingPath = PathManager.getInstance().getRecordingPath(recordAndReplayUtils.getGameTitle()).toString();
        deserializeRecordedEvents(recordingPath);
        Gson gson = new GsonBuilder().create();
        deserializeFileAmount(gson, recordingPath);
        deserializeCharacterStateEventPositionMap(gson, recordingPath);
        deserializeAttackEventExtraRecorder(gson, recordingPath);
    }

    /**
     * Deserialize RecordedEvents.
     *
     * @param recordingPath path where the data was saved.
     */
    void deserializeRecordedEvents(String recordingPath) {
        String filepath = recordingPath + EVENT_DIR + recordAndReplayUtils.getFileCount() + JSON;
        recordAndReplayUtils.setFileCount(recordAndReplayUtils.getFileCount() + 1);
        recordedEventStore.setEvents(recordedEventSerializer.deserializeRecordedEvents(filepath));
        logger.info("RecordedEvents Deserialization completed!");
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
        try (FileReader fileReader = new FileReader(recordingPath + FILE_AMOUNT)) {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(fileReader);
            Type typeOfCount = new TypeToken<Integer>() {
            }.getType();
            recordAndReplayUtils.setFileAmount(gson.fromJson(jsonElement, typeOfCount));
            logger.info("File Amount Deserialization completed!");
        } catch (Exception e) {
            logger.error("Error while deserializing file amount:", e);
        }
    }

    private void serializeCharacterStateEventPositionMap(Gson gson, String recordingPath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + STATE_EVENT_POSITION));
            gson.toJson(characterStateEventPositionMap.getIdToData(), HashMap.class, writer);
            writer.close();
            characterStateEventPositionMap.reset();
            logger.info("CharacterStateEvent positions Serialization completed!");
        } catch (Exception e) {
            logger.error("Error while serializing CharacterStateEvent positions:", e);
        }
    }

    private void deserializeCharacterStateEventPositionMap(Gson gson, String recordingPath) {
        try (FileReader fileReader = new FileReader(recordingPath + STATE_EVENT_POSITION)) {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(fileReader);
            Type typeOfHashMap = new TypeToken<HashMap<Integer, Vector3f[]>>() {
            }.getType();
            Map<Integer, Vector3f[]> previousMap = gson.fromJson(jsonElement, typeOfHashMap);
            characterStateEventPositionMap.setIdToData(previousMap);
            logger.info("CharacterStateEvent positions Deserialization completed!");
        } catch (Exception e) {
            logger.error("Error while deserializing CharacterStateEvent positions:", e);
        }
    }

    private void serializeAttackEventExtraRecorder(Gson gson, String recordingPath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + DIRECTION_ORIGIN_LIST));
            gson.toJson(directionAndOriginPosRecorderList.getList(), ArrayList.class, writer);
            writer.close();
            directionAndOriginPosRecorderList.reset();
            logger.info("AttackEvent extras serialization completed!");
        } catch (Exception e) {
            logger.error("Error while serializing AttackEvent extras:", e);
        }
    }

    private void deserializeAttackEventExtraRecorder(Gson gson, String recordingPath) {
        try (FileReader fileReader = new FileReader(recordingPath + DIRECTION_ORIGIN_LIST)) {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(fileReader);
            Type type = new TypeToken<ArrayList<DirectionAndOriginPosRecorder>>() {
            }.getType();
            ArrayList<DirectionAndOriginPosRecorder> list = gson.fromJson(jsonElement, type);
            directionAndOriginPosRecorderList.setList(list);
            logger.info("AttackEvent extras deserialization completed!");
        } catch (Exception e) {
            logger.error("Error while deserializing AttackEvent extras:", e);
        }
    }
}
