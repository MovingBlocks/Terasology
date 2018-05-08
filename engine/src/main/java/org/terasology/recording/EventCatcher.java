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
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.PendingEvent;
import org.terasology.input.binds.movement.JumpButton;
import org.terasology.input.events.InputEvent;

public class EventCatcher {

    private EventStorage storage;
    private long startTime;
    //private long position;

    public EventCatcher() {
        storage = EventStorage.getInstance();
        //position = 0;
        startTime = System.currentTimeMillis(); // I have to check for how long I can record using this
    }

    public boolean addEvent(PendingEvent pe, long position) {
        if (filterEvents(pe)) {
            System.out.println("CATCHED EVENT: " + pe.getEvent().toString());
            printComponents(pe.getEntity());
            long timestamp = System.currentTimeMillis() - this.startTime;
            RecordedEvent re = new RecordedEvent(pe, timestamp, position);
            //this.position++;
            return storage.add(re);
        } else {
            return false;
        }
    }

    private boolean filterEvents(PendingEvent pe) {
        Event event = pe.getEvent();
        if ( event instanceof PlaySoundEvent ||
                event instanceof InputEvent) {
            return true;
        }

        return false;
        //return true;
    }
    private void printComponents(EntityRef e) {
        System.out.println("ENTITY: " + e.getId());
        for (Component c : e.iterateComponents()) {
            System.out.println("COMPONENTS: " + c.toString());
        }
    }
}
