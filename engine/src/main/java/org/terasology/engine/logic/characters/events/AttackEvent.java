// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * This event happens on the server after a AttackRequest has been sent by the client.
 * This event is sent on the entity being attacked.
 */
public class AttackEvent extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef directCause;

    /**
     * @param instigator  The instigator of the damage (which entity caused it)
     * @param directCause Tool used to cause the damage
     */
    public AttackEvent(EntityRef instigator, EntityRef directCause) {
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }
}
