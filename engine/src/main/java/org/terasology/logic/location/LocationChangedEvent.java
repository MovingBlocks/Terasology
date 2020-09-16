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

import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.ImmutableQuat4f;
import org.terasology.math.geom.ImmutableVector3f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

public class LocationChangedEvent implements Event {
    public final LocationComponent component;
    public final ImmutableVector3f oldPosition;
    public final ImmutableQuat4f oldRotation;
    public final ImmutableVector3f newPosition;
    public final ImmutableQuat4f newRotation;

    public LocationChangedEvent(LocationComponent newLocation) {
        this(newLocation, newLocation.position, newLocation.rotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition) {
        this(newLocation, oPosition, newLocation.rotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Vector3f nPosition) {
        this(newLocation, oPosition, newLocation.rotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quat4f oRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quat4f oRotation, Quat4f nRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quat4f oRotation) {
        this(newLocation, oPosition, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quat4f oRotation, Vector3f nPosition) {
        this(newLocation, oPosition, oRotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quat4f oRotation, Quat4f nRotation)
    {
        this(newLocation, oPosition, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent nComponent, Vector3f oPosition, Quat4f oRotation, Vector3f nPosition, Quat4f nRotation)
    {
        oldPosition = ImmutableVector3f.createOrUse(oPosition);
        oldRotation = new ImmutableQuat4f(oRotation.x, oRotation.y, oRotation.z, oRotation.w);
        newPosition = ImmutableVector3f.createOrUse(nPosition);
        newRotation = new ImmutableQuat4f(nRotation.x, nRotation.y, nRotation.z, nRotation.w);;
        component = nComponent;
    }

    public BaseVector3f vectorMoved()
    {
        return oldPosition != null ? newPosition.sub(oldPosition) : Vector3f.zero();
    }

    public float distanceMoved()
    {
        return oldPosition != null ? newPosition.distance(oldPosition) : 0.0F;
    }
}
