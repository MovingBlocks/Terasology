// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;

/**
 * This event is sent to trigger the creation of drops (if any) for a destroyed block.
 * TODO: Remove this when blocks are more configurable in the future, and allow drops to be generated off of the DoDestroyEvent.
 */
public class CreateBlockDropsEvent extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef directCause;
    private Prefab damageType;

    public CreateBlockDropsEvent(EntityRef instigator, EntityRef directCause, Prefab damageType) {
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
