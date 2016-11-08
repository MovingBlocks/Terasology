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

import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.BroadcastEvent;

/**
 */
@BroadcastEvent
public class ImpactEvent implements Event {
    private Vector3f impactPoint;
    private Vector3f impactNormal;
    private Vector3f impactSpeed;
    private float travelDistance;

    protected ImpactEvent() {
    }

    public ImpactEvent(Vector3f impactPoint, Vector3f impactNormal, Vector3f impactSpeed, float travelDistance) {
        this.impactPoint = impactPoint;
        this.impactNormal = impactNormal;
        this.impactSpeed = impactSpeed;
        this.travelDistance = travelDistance;
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

    public float getTravelDistance(){
        return travelDistance;
    }
}
