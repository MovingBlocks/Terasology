// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.nui.reflection.copy.CopyStrategy;

/**
 * This copy strategy return {@link DelayedEntityRef}s for persistent entities that exists.
 * For non persistent entities or entities that do no longer exist it returns {@link EntityRef#NULL}.
 *
 */
class DelayedEntityRefCopyStrategy implements CopyStrategy<EntityRef> {

    private final DelayedEntityRefFactory delayedEntityRefFactory;

    DelayedEntityRefCopyStrategy(DelayedEntityRefFactory delayedEntityRefFactory) {
        this.delayedEntityRefFactory = delayedEntityRefFactory;
    }

    @Override
    public EntityRef copy(EntityRef value) {
        if (value != null) {
            if (value.exists() && value.isPersistent()) {
                return delayedEntityRefFactory.createDelayedEntityRef(value.getId());
            } else {
                return EntityRef.NULL;
            }
        } else {
            return null;
        }
    }
}
