/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.physics.events;

import org.joml.Vector3fc;
import org.terasology.entitySystem.event.Event;

public class MovedEvent implements Event {
    private Vector3fc delta;
    private Vector3fc finalPosition;

    public MovedEvent(Vector3fc delta, Vector3fc finalPosition) {
        this.delta = delta;
        this.finalPosition = finalPosition;
    }

    public Vector3fc getDelta() {
        return delta;
    }

    public Vector3fc getPosition() {
        return finalPosition;
    }
}
