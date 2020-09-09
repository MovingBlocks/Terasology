// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.BaseVector3f;

public class MovedEvent implements Event {
    private final BaseVector3f delta;
    private final BaseVector3f finalPosition;

    public MovedEvent(BaseVector3f delta, BaseVector3f finalPosition) {
        this.delta = delta;
        this.finalPosition = finalPosition;
    }

    public BaseVector3f getDelta() {
        return delta;
    }

    public BaseVector3f getPosition() {
        return finalPosition;
    }
}
