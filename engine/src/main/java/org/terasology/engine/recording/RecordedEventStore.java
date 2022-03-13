// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

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
