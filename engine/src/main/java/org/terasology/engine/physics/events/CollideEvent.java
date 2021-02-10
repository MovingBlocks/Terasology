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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * TODO Make CollideEvent as a server event.
 * TODO Have a CollideRequest before the CollideEvent?
 */
public class CollideEvent extends AbstractConsumableEvent {
    private EntityRef otherEntity;
    private Vector3f entityContactPoint;
    private Vector3f otherEntityContactPoint;
    private float penetration;
    private Vector3f normal;

    public CollideEvent(EntityRef other, Vector3f entityContactPoint, Vector3f otherEntityContactPoint, float penetration, Vector3f normal) {
        this.otherEntity = other;
        this.normal = normal;
        this.entityContactPoint = entityContactPoint;
        this.otherEntityContactPoint = otherEntityContactPoint;
        this.penetration = penetration;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Vector3f getEntityContactPoint() {
        return entityContactPoint;
    }

    public Vector3f getOtherEntityContactPoint() {
        return otherEntityContactPoint;
    }

    public float getPenetration() {
        return penetration;
    }

    public EntityRef getOtherEntity() {
        return otherEntity;
    }
}
