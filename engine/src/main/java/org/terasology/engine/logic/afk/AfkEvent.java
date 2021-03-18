// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.afk;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.network.NetworkEvent;

@BroadcastEvent
public class AfkEvent extends NetworkEvent {

    private EntityRef target;
    private boolean afk;

    public AfkEvent() {
    }

    public AfkEvent(EntityRef target, boolean afk) {
        this.target = target;
        this.afk = afk;
    }

    public EntityRef getTarget() {
        return target;
    }

    public boolean isAfk() {
        return afk;
    }
}
