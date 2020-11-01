// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.afk;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

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
