// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

@ServerEvent
public class ChangeHeldItemRequest implements Event {

    private EntityRef item;

    protected ChangeHeldItemRequest() {
    }

    public ChangeHeldItemRequest(EntityRef item) {
        this.item = item;
    }

    public EntityRef getItem() {
        return item;
    }
}
