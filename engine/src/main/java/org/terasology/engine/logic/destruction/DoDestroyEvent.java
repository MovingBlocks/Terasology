// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.destruction;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;

/**
 * Sent when the entity is destroyed. Occurs only after {@link BeforeDestroyEvent} and {@link DestroyEvent} have been
 * sent.
 */
public class DoDestroyEvent implements Event {
    private final EntityRef instigator;
    private final EntityRef directCause;
    private final Prefab damageType;

    public DoDestroyEvent(EntityRef instigator, EntityRef directCause, Prefab damageType) {
        this.instigator = instigator;
        this.directCause = directCause;
        this.damageType = damageType;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }

    public Prefab getDamageType() {
        return damageType;
    }
}
