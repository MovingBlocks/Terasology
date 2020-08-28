/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.location;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;

@BroadcastEvent
public class LocationResynchEvent implements Event {
    public Vector3f position;
    public Quaternionf rotation;

    public LocationResynchEvent() {
    }

    public LocationResynchEvent(Vector3f position, Quaternionf rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
