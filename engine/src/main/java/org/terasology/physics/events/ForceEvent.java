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

import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.BroadcastEvent;

/**
 */
@BroadcastEvent
public class ForceEvent implements Event {
    private Vector3f force;

    protected ForceEvent() {
    }

    public ForceEvent(Vector3f force) {
        this.force = force;
    }

    public Vector3f getForce() {
        return force;
    }
}
