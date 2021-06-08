// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * See {@link EngineEntityManager#subscribeForDestruction(EntityDestroySubscriber)}.
 */
@FunctionalInterface
public interface EntityDestroySubscriber {

    /**
     * At the point this method gets called the entity has still it's components, it should however not be modified
     * anymore.
     *
     * @param entity that is about to be destroyed.
     */
    void onEntityDestroyed(EntityRef entity);

}
