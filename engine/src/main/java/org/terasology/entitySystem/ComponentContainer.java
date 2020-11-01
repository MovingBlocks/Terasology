/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.entitySystem;

import java.util.List;

/**
 */
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
