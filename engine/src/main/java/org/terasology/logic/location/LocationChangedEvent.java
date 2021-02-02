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

    public LocationChangedEvent(LocationComponent newLocation, Quaternionfc oRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quaternionfc oRotation, Quaternionfc nRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quaternionfc oRotation) {
        this(newLocation, oPosition, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quaternionfc oRotation,
                                Vector3f nPosition) {
        this(newLocation, oPosition, oRotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quaternionfc oRotation,
                                Quaternionfc nRotation) {
        this(newLocation, oPosition, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent nComponent, Vector3fc oPosition, Quaternionfc oRotation,
                                Vector3fc nPosition, Quaternionfc nRotation) {
        oldPosition = new Vector3f(oPosition);
        oldRotation = new Quaternionf(oRotation);
        newPosition = new Vector3f(nPosition);
        newRotation = new Quaternionf(nRotation);
        component = nComponent;
    }

    public Vector3f vectorMoved(Vector3f dest) {
        return oldPosition != null ? newPosition.sub(oldPosition, dest) : dest.set(0, 0, 0);
    }

    public float distanceMoved() {
        return oldPosition != null ? newPosition.distance(oldPosition) : 0.0F;
    }
}
