// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * Fired whenever the targeted entity changes (must be within activation range).
 */
public class PlayerTargetChangedEvent implements Event {
    private final EntityRef oldTarget;
    private final EntityRef newTarget;

    public PlayerTargetChangedEvent(EntityRef oldTarget, EntityRef newTarget) {
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
