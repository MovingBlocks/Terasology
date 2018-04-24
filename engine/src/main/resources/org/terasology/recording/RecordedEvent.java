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

import org.terasology.entitySystem.event.internal.PendingEvent;

public class RecordedEvent {

    private PendingEvent pendingEvent;
    private double timestamp;
    private long position;

    public RecordedEvent(PendingEvent pe, double timestamp, long position) {
        this.pendingEvent = pe;
        this.timestamp = timestamp;
        this.position = position;
    }


    public PendingEvent getPendingEvent() {
        return pendingEvent;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public long getPosition() {
        return position;
    }
}
