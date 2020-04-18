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

import org.joml.Vector3f;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;

/**
 */
@BroadcastEvent
public class PhysicsResynchEvent implements Event {
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();

    protected PhysicsResynchEvent() {
    }

    public PhysicsResynchEvent(Vector3f velocity, Vector3f angularVelocity) {
        this.velocity.set(velocity);
        this.angularVelocity.set(angularVelocity);
    }

    /**
     * @return the linear velocity of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * @return The angular or rotational velocity of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
}
