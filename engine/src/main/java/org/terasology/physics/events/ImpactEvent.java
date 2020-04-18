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

package org.terasology.physics.events;

import org.joml.Vector3f;
import org.terasology.math.JomlUtil;
import org.terasology.network.BroadcastEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.Side;

/**
 * Impact event is called whenever an item has enough speed to detect
 * penetration of a block or entity in the next frame. It computes the
 * reflection angle, speed and the next position the item should be in.
 */
@BroadcastEvent
public class ImpactEvent extends AbstractConsumableEvent {
    private Vector3f impactPoint;
    private Vector3f impactNormal;
    private Vector3f impactSpeed;
    private float travelDistance;
    private EntityRef impactEntity;

    protected ImpactEvent() {
    }

    public ImpactEvent(Vector3f impactPoint, Vector3f impactNormal, Vector3f impactSpeed, float travelDistance, EntityRef impactEntity) {
        this.impactPoint = impactPoint;
        this.impactNormal = impactNormal;
        this.impactSpeed = impactSpeed;
        this.travelDistance = travelDistance;
        this.impactEntity = impactEntity;
    }

    public Vector3f getImpactPoint() {
        return impactPoint;
    }

    public Vector3f getImpactNormal() {
        return impactNormal;
    }

    public Vector3f getImpactSpeed() {
        return impactSpeed;
    }

    public float getTravelDistance() {
        return travelDistance;
    }

    public EntityRef getImpactEntity() {
        return impactEntity;
    }

    public Side getSide() {
        return Side.inDirection(JomlUtil.from(impactNormal));
    }
}
