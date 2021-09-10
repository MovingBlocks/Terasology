// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;
import java.util.Set;

public class EntityDelta {
    private Set<Class<? extends Component>> removedComponents = Sets.newHashSet();
    private Map<Class<? extends Component>, Component> changedComponents = Maps.newHashMap();

    /**
     * @param component a snapshot of the original entity component at the time when the entity delta got created.
     */
    public void setChangedComponent(Component component) {
        if (component == null) {
            throw new RuntimeException("Cannot set a changed component with a null component variable");
        }
        Class<? extends Component> clazz = component.getClass();
        changedComponents.put(clazz, component);
    }

    public void removeComponent(Class<? extends Component> clazz) {
        changedComponents.remove(clazz);
        removedComponents.add(clazz);
    }

    public Map<Class<? extends Component>, Component> getChangedComponents() {
        return changedComponents;
    }

    public Set<Class<? extends Component>> getRemovedComponents() {
        return removedComponents;
    }
}
