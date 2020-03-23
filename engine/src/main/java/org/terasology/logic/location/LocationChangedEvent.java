/*
 * Copyright 2019 MovingBlocks
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
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.entitySystem.event.Event;

public class LocationChangedEvent implements Event {
    public final LocationComponent component;
    public final Vector3fc oldPosition;
    public final Quaternionfc oldRotation;
    public final Vector3fc newPosition;
    public final Quaternionfc newRotation;

    public LocationChangedEvent(LocationComponent newLocation) {
        this(newLocation, newLocation.position, newLocation.rotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition) {
        this(newLocation, oPosition, newLocation.rotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Vector3f nPosition) {
        this(newLocation, oPosition, newLocation.rotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quaternionf oRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quaternionf oRotation, Quaternionf nRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quaternionf oRotation) {
        this(newLocation, oPosition, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quaternionf oRotation, Vector3f nPosition) {
        this(newLocation, oPosition, oRotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quaternionf oRotation, Quaternionf nRotation)
    {
        this(newLocation, oPosition, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent nComponent, Vector3f oPosition, Quaternionf oRotation, Vector3f nPosition, Quaternionf nRotation)
    {
        oldPosition = new org.joml.Vector3f(oPosition);
        oldRotation = new Quaternionf(oRotation.x, oRotation.y, oRotation.z, oRotation.w);
        newPosition = new org.joml.Vector3f(nPosition);
        newRotation = new Quaternionf(nRotation.x, nRotation.y, nRotation.z, nRotation.w);;
        component = nComponent;
    }

    public Vector3fc vectorMoved()
    {
        return oldPosition != null ? new Vector3f(newPosition).sub(oldPosition) : new Vector3f();
    }

    public float distanceMoved()
    {
        return oldPosition != null ? newPosition.distance(oldPosition) : 0.0F;
    }
}
