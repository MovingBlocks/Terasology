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

import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.PendingEvent;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.input.events.InputEvent;
import org.terasology.logic.characters.CharacterMoveInputEvent;

/**
 * This class is responsible for catching the events during a Record and send the desired ones to the RecordedEventStore.
 */
public class EventCatcher {

    private long startTime;
    private long eventCounter;

    public EventCatcher() {
        this.startTime = System.currentTimeMillis();
        this.eventCounter = 0;
    }

    /**
     * Receives a PendingEvent and add it as a RecordedEvent in the RecordedEventStore if it is an event type that should be
     * recorded.
     * @param pe PendingEvent to be checked and added
     * @return If the event was added to the RecordedEventStore
     */
    public boolean addEvent(PendingEvent pe) {
        if (shouldRecordEvent(pe)) {
            long timestamp = System.currentTimeMillis() - this.startTime;
            Event e = EventCopier.copyEvent(pe.getEvent());
            PendingEvent newPendingEvent = new PendingEvent(pe.getEntity(), e);
            RecordedEvent re;
            if (pe.getComponent() == null) {
                re = new RecordedEvent(newPendingEvent.getEntity().getId(), newPendingEvent.getEvent(), timestamp, this.eventCounter);
            } else {
                re = new RecordedEvent(newPendingEvent.getEntity().getId(), newPendingEvent.getEvent(), newPendingEvent.getComponent(), timestamp, this.eventCounter);
            }
            this.eventCounter++;
            return RecordedEventStore.add(re);
        } else {
            return false;
        }
    }


    private boolean shouldRecordEvent(PendingEvent pe) {
        Event event = pe.getEvent();
        return (event instanceof PlaySoundEvent
                || event instanceof InputEvent
                || event instanceof CameraTargetChangedEvent
                || event instanceof CharacterMoveInputEvent);
    }
}
