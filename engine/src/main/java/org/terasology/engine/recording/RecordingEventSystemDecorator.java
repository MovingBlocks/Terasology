// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.recording;

import org.terasology.engine.core.bootstrap.eventSystem.AbstractEventSystemDecorator;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.PendingEvent;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Decorator for recording Events to Record&amp;Replay subsystem.
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
        if (currentThreadIsMain() && recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
            eventCatcher.addEvent(new PendingEvent(entity, event));
        }
        super.send(entity, event);
    }

    @Override
    public void send(EntityRef entity, Event event, Component component) {
        if (currentThreadIsMain() && recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
            eventCatcher.addEvent(new PendingEvent(entity, event, component));
        }
        super.send(entity, event, component);
    }
}
