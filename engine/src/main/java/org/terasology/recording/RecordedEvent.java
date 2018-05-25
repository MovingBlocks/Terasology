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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.event.Event;

/**
 * Data from a Recorded Event.
 */
public class RecordedEvent {

    private long entityRefId;
    private Event event;
    private Component component;
    private double timestamp;
    private long position;

    /**
     *
     * @param entityRefId Id of the EntityRef which the event was sent against.
     * @param event The event to be recorded.
     * @param timestamp The timestamp in which the event was sent.
     * @param position The position of the RecordedEvent.
     */
    RecordedEvent(long entityRefId, Event event, double timestamp, long position) {
        this.entityRefId = entityRefId;
        this.event = event;
        this.timestamp = timestamp;
        this.position = position;
    }

    /**
     *
     * @param entityRefId Id of the EntityRef which the event was sent against.
     * @param event The event to be recorded.
     * @param component The component that was sent with the event
     * @param timestamp The timestamp in which the event was sent.
     * @param position The position of the RecordedEvent.
     */
    RecordedEvent(long entityRefId, Event event, Component component, double timestamp, long position) {
        this.entityRefId = entityRefId;
        this.event = event;
        this.component = component;
        this.timestamp = timestamp;
        this.position = position;
    }



    public double getTimestamp() {
        return timestamp;
    }

    public long getPosition() {
        return position;
    }

    long getEntityRefId() {
        return entityRefId;
    }

    public Event getEvent() {
        return event;
    }

    public Component getComponent() {
        return component;
    }
}
