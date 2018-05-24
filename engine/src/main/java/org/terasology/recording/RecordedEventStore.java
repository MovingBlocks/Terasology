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

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for saving the recorded events. Also contains the status of the RecordAndReplay.
 */
public final class RecordedEventStore {

    private static List<RecordedEvent> events = new ArrayList<>();

    private RecordedEventStore() {

    }


    public static boolean add(RecordedEvent event) {
        return events.add(event);
    }

    public static List<RecordedEvent> getEvents() {
        return events;
    }

    public static List<RecordedEvent> popEvents() {
        List<RecordedEvent> recordedEvents = events;
        events = new ArrayList<>();
        return recordedEvents;
    }

    public static void setEvents(List<RecordedEvent> deserializedEvents) {
        events = deserializedEvents;
    }
}
