// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.event.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Interface for a single event receiver
 */
@FunctionalInterface
public interface EventReceiver<T extends Event> {
    void onEvent(T event, EntityRef entity);
}
