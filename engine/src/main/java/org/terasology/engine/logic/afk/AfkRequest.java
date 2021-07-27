// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.afk;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

@ServerEvent
public class AfkRequest extends NetworkEvent {

    private boolean afk;

    public AfkRequest() {
    }

    public AfkRequest(EntityRef instigator, boolean afk) {
        super(instigator);
        this.afk = afk;
    }

    public boolean isAfk() {
        return afk;
    }
}
