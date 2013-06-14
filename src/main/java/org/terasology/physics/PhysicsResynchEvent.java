/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.physics;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@BroadcastEvent
public class PhysicsResynchEvent implements Event {
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f();
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();

    protected PhysicsResynchEvent() {
    }

    public PhysicsResynchEvent(Vector3f position, Quat4f rotation, Vector3f velocity, Vector3f angularVelocity) {
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.angularVelocity.set(angularVelocity);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quat4f getRotation() {
        return rotation;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
}
