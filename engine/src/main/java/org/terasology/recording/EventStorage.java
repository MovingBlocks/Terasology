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
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.network.serialization.NetEntityRefTypeHandler;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for saving the recorded events. Also contains the status of the RecordAndReplay.
 */
public final class EventStorage {

    private static final Logger logger = LoggerFactory.getLogger(EventStorage.class);
    private static List<RecordedEvent> events = new ArrayList<>();
    public static RecordAndReplayStatus recordAndReplayStatus = RecordAndReplayStatus.NOT_ACTIVATED;
    public static boolean beginReplay; //begins as false. This variable is true when the game is rendered and ready to replay events
    public static int recordCount; //begins as 0


    //temp
    public static long originalClientEntityId;
    public static long replayClientEntityId;
    public static EventLibrary eventLibrary;
    public static EntityManager entityManager;



    private EventStorage() {

    }


    public static boolean add(RecordedEvent event) {
        return events.add(event);
    }

    public static List<RecordedEvent> getEvents() {
        return events;
    }

    //This will probably not exist once serialization is done. Saves recorded events as String
    public static void saveEventsString() {
        StringBuffer sb = new StringBuffer();
        for (RecordedEvent re : events) {
            saveOneEventAsString(re, sb);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Recorded_Events"+recordCount+".txt")));
            recordCount++;
            writer.write(sb.toString());
            writer.flush();
            writer.close();

        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
        attemptToSerialize();


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

    }

    private static void attemptToSerialize() {
        RecordedEventSerializer serializer = new RecordedEventSerializer(entityManager);
        serializer.serializeRecordedEvents(events);
        System.out.println("Serialization completed!");
    }

    public static void attemptToDeserialize() {
        RecordedEventSerializer serializer = new RecordedEventSerializer(entityManager);
        events = serializer.deserializeRecordedEvents("events.json");
        System.out.println("Deserialization completed!");
    }

    /*
    private static void attemptToSerialize() {
        for (Class c : eventLibrary.getClassLookup().keySet()) {
            System.out.println("SUPPORTED CLASS: " + c.toString());
        }
    }
     */




}
