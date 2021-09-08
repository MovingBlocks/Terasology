// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.time;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.NetFilterEvent;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class TimeClientSystem extends BaseComponentSystem {
    @In
    private Time time;

    @NetFilterEvent(netFilter = RegisterMode.REMOTE_CLIENT)
    @ReceiveEvent
    public void resynchTime(TimeResynchEvent event, EntityRef entityRef) {
        time.setGameTimeDilation(event.getGameTimeDilation());
    }
}
