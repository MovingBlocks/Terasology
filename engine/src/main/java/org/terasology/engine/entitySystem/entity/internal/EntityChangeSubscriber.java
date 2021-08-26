// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collection;

/**
 * Primarily for internal use, is informed of all component lifecycle events for all components.
 *
 */
public interface EntityChangeSubscriber {

    void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component);

    void onEntityComponentChange(EntityRef entity, Class<? extends Component> component);

    void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component);

    void onReactivation(EntityRef entity, Collection<Component> components);

    void onBeforeDeactivation(EntityRef entity, Collection<Component> components);


}
