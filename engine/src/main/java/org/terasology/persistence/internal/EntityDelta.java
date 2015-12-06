/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.internal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;

import java.util.Map;
import java.util.Set;

/**
 */
public class EntityDelta {
    private Set<Class<? extends Component>> removedComponents = Sets.newHashSet();
    private Map<Class<? extends Component>, Component> changedComponents = Maps.newHashMap();
    /**
     *
     * @param component a snapshot of the original entity component at the time when the entity delta got created.
     */
    public void setChangedComponent(Component component) {
        if (component == null) {
            throw new RuntimeException("component null");
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
