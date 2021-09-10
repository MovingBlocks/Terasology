// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection.copy.strategy;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.reflection.copy.CopyStrategy;

/**
 * Provides a deep copy of an entity, including any owned entities recursively.
 */
public final class EntityCopyStrategy implements CopyStrategy<EntityRef> {
    public static final EntityCopyStrategy INSTANCE = new EntityCopyStrategy();

    private EntityCopyStrategy() {
    }

    @Override
    public EntityRef copy(EntityRef value) {
        return value.copy();
    }
}
