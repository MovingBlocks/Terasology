// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import java.util.List;

public interface ComponentContainer {

    /**
     * Check existence of component in container
     * @param component component class to check
     * @return If this has a component of the given type
     */
    boolean hasComponent(Class<? extends Component> component);

    /**
     * Check existence of any of provided components in container
     * @param filterComponents list of Component classes to check
     * @return If this has at least one component from the list of components
     */
    boolean hasAnyComponents(List<Class<? extends Component>> filterComponents);

    /**
     * Check existence of all provided components in container
     * @param filterComponents list of Component classes to check
     * @return If this has all components from the list of components
     */
    boolean hasAllComponents(List<Class<? extends Component>> filterComponents);

    /**
     * @param componentClass
     * @param <T>
     * @return The requested component, or null if the this doesn't have a component of this type
     */
    <T extends Component> T getComponent(Class<T> componentClass);

    /**
     * Iterates over all the components this entity has
     *
     * @return
     */
    Iterable<Component> iterateComponents();
}
