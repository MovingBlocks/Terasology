// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.recording;

import org.terasology.engine.bootstrap.eventSystem.AbstractEventSystemDecorator;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.PendingEvent;
import org.terasology.entitySystem.event.internal.EventSystem;

/**
 * Decorator for recording Events to Record&Replay subsystem.
 */
public class RecordingEventSystemDecorator extends AbstractEventSystemDecorator {
    private final EventCatcher eventCatcher;
    private final RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    public RecordingEventSystemDecorator(EventSystem eventSystem, EventCatcher eventCatcher,
                                         RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) {
        super(eventSystem);
        this.eventCatcher = eventCatcher;
        this.recordAndReplayCurrentStatus = recordAndReplayCurrentStatus;
    }

    @Override
    public void send(EntityRef entity, Event event) {
        if (currentThreadIsMain()) {
            if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
                eventCatcher.addEvent(new PendingEvent(entity, event));
            }
        }
        super.send(entity, event);
    }

    @Override
    public void send(EntityRef entity, Event event, Component component) {
        if (currentThreadIsMain()) {
            if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
                eventCatcher.addEvent(new PendingEvent(entity, event, component));
            }
        }
        super.send(entity, event, component);
    }
}
