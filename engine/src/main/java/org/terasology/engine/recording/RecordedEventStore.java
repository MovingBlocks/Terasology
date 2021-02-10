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

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for storing the recorded events.
 */
public class RecordedEventStore {

    private List<RecordedEvent> events;

    public RecordedEventStore() {
        events = new ArrayList<>();
    }


    public boolean add(RecordedEvent event) {
        return events.add(event);
    }

    public List<RecordedEvent> getEvents() {
        return events;
    }

    List<RecordedEvent> popEvents() {
        List<RecordedEvent> recordedEvents = events;
        events = new ArrayList<>();
        return recordedEvents;
    }

    public void setEvents(List<RecordedEvent> deserializedEvents) {
        events = deserializedEvents;
    }
}
