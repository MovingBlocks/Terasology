// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

/**
 */
@ServerEvent(lagCompensate = true)
public class AttackRequest extends NetworkEvent {

    private EntityRef item = EntityRef.NULL;

    protected AttackRequest() {
    }

    public AttackRequest(EntityRef withItem) {
        this.item = withItem;
    }

    public EntityRef getItem() {
        return item;
    }
}
