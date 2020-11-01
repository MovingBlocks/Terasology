// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.afk;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.NetworkEvent;

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
