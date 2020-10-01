// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.reflection.copy.strategy;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.reflection.copy.CopyStrategy;

/**
 * Provides a deep copy of an entity, including any owned entities recursively.
 */
public class EntityCopyStrategy implements CopyStrategy<EntityRef> {
    public static final EntityCopyStrategy INSTANCE = new EntityCopyStrategy();

    private EntityCopyStrategy() {
    }

    @Override
    public EntityRef copy(EntityRef value) {
        return value.copy();
    }
}
