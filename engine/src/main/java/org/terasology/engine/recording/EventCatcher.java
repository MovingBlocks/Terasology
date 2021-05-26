// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.event.PendingEvent;

import java.util.List;

/**
 * Gets the events sent in the EventSystemImpl and adds their copies to the RecordedEventStore if they are of a type
 * selected to be recorded.
 */
public class EventCatcher {

    private long startTime;
    private long eventCounter;
    private List<Class<?>> selectedClassesToRecord;
    private EventCopier eventCopier;
    private RecordedEventStore recordedEventStore;

    /**
     * EventCatcher constructor that receives a list of event classes it is supposed to record, and the RecordedEventStore that
     * will store said event classes.
     * @param selectedClassesToRecord A list of classes that should be recorded and sent to the RecordedEventStore.
     * @param recordedEventStore The Store that will save the events selected to be recorded.
     */
    public EventCatcher(List<Class<?>> selectedClassesToRecord, RecordedEventStore recordedEventStore) {
        this.selectedClassesToRecord = selectedClassesToRecord;
        this.eventCopier = new EventCopier();
        this.recordedEventStore = recordedEventStore;
    }

    /**
     * Starts the timer to generate the correct timestamp in which an event was sent and stored.
     */
    public void startTimer() {
        this.startTime = System.currentTimeMillis();
        this.eventCounter = 0;
    }

    /**
     * Receives a PendingEvent and add it as a RecordedEvent in the RecordedEventStore if it is an event type that should be
     * recorded.
     * @param pendingEvent PendingEvent to be checked and added
     * @return If the event was added to the RecordedEventStore
     */
    public boolean addEvent(PendingEvent pendingEvent) {
        if (shouldRecordEvent(pendingEvent)) {
            long timestamp = System.currentTimeMillis() - this.startTime;
            Event e = this.eventCopier.copyEvent(pendingEvent.getEvent());
            PendingEvent newPendingEvent = new PendingEvent(pendingEvent.getEntity(), e);
            RecordedEvent recordedEvent;
            if (pendingEvent.getComponent() == null) {
                recordedEvent = new RecordedEvent(newPendingEvent.getEntity().getId(), newPendingEvent.getEvent(), timestamp, this.eventCounter);
            } else {
                recordedEvent = new RecordedEvent(newPendingEvent.getEntity().getId(), newPendingEvent.getEvent(),
                        newPendingEvent.getComponent(), timestamp, eventCounter);
            }
            this.eventCounter++;
            return recordedEventStore.add(recordedEvent);
        } else {
            return false;
        }
    }

    private boolean shouldRecordEvent(PendingEvent pendingEvent) {
        boolean shouldRecord = false;
        for (Class<?> supportedEventClass : this.selectedClassesToRecord) {
            if (supportedEventClass.isInstance(pendingEvent.getEvent())) {
                shouldRecord = true;
                break;
            }
        }
        return shouldRecord;
    }
}
