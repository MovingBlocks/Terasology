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
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.event.AbstractEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class VerticalCollisionEvent extends AbstractEvent {
    private Vector3f velocity;
    private Vector3f location;

    public VerticalCollisionEvent(Vector3f velocity, Vector3f location) {
        this.velocity = new Vector3f(velocity);
        this.location = new Vector3f(location);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getLocation() {
        return location;
    }
}
