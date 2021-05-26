// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * Optional parent for events that are replicated over the network. Inherit this to make use of advanced features
 *
 */
public abstract class NetworkEvent implements Event {

    private EntityRef instigator = EntityRef.NULL;

    protected NetworkEvent() {
    }

    /**
     * @param instigator The instigator of this event. Can be used with BroadcastEvent's skipInstigator option
     *                   to avoid sending the event to the client that caused the event (who will have simulated it
     *                   client-side).
     */
    protected NetworkEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
