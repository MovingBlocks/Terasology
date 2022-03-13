// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.cameraTarget;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

public class CameraTargetChangedEvent implements Event {
    private EntityRef oldTarget;
    private EntityRef newTarget;

    public CameraTargetChangedEvent(EntityRef oldTarget, EntityRef newTarget) {
        this.oldTarget = oldTarget;
        this.newTarget = newTarget;
    }

    public EntityRef getOldTarget() {
        return oldTarget;
    }

    public EntityRef getNewTarget() {
        return newTarget;
    }
}
