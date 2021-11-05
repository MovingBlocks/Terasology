// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Saves a recorded event, the id of the entity it was sent against, the component it was sent with, the timestamp in
 * which it was sent and its index. This is the class that's serialized and deserialized in the "event files".
 */
public class RecordedEvent {

    private long entityId;
    private Event event;
    private Component component;
    private long timestamp;
    private long index;

    /**
     *
     * @param entityId Id of the EntityRef which the event was sent against.
     * @param event The event to be recorded.
     * @param timestamp The timestamp in which the event was sent.
     * @param index The index of the RecordedEvent.
     */
    RecordedEvent(long entityId, Event event, long timestamp, long index) {
        this.entityId = entityId;
        this.event = event;
        this.timestamp = timestamp;
        this.index = index;
    }

    /**
     *
     * @param entityId Id of the EntityRef which the event was sent against.
     * @param event The event to be recorded.
     * @param component The component that was sent with the event
     * @param timestamp The timestamp in which the event was sent.
     * @param index The index of the RecordedEvent.
     */
    RecordedEvent(long entityId, Event event, Component component, long timestamp, long index) {
        this.entityId = entityId;
        this.event = event;
        this.component = component;
        this.timestamp = timestamp;
        this.index = index;
    }



    public long getTimestamp() {
        return timestamp;
    }

    public long getIndex() {
        return index;
    }

    long getEntityId() {
        return entityId;
    }

    public Event getEvent() {
        return event;
    }

    public Component getComponent() {
        return component;
    }
}
