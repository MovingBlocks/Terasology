// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.health;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Sent to request the destruction of an entity.
 *
 */
public class DestroyEvent implements Event {
    private EntityRef instigator;
    private EntityRef directCause;
    private Prefab damageType;

    public DestroyEvent(EntityRef instigator, EntityRef directCause, Prefab damageType) {
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
