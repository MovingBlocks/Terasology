/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

/**
 * An entity prefab describes the recipe for creating an entity.
 * Like an entity it groups a collection of components.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface Prefab {

    /**
     * @return The identifier for this prefab
     */
    public String getName();

    /**
     * @param componentClass
     * @param <T>
     * @return The requested component, or null if the entity doesn't have a component of this class
     */
    public <T extends Component> T getComponent(Class<T> componentClass);

    /**
     * Adds a component to this entity. If the entity already has a component of the same class it is replaced.
     *
     * @param component
     */
    public <T extends Component> T setComponent(T component);

    /**
     * @param componentClass
     */
    public void removeComponent(Class<? extends Component> componentClass);

    /**
     * Iterates over all the components
     *
     * @return
     */
    public Iterable<Component> iterateComponents();

    /**
     * Iterate only over OWN components, excluding inheritance.
     * Required for proper serializing
     *
     * @return
     */
    public Iterable<Component> iterateOwnedComponents();

    /**
     * Return parents prefabs
     *
     * @return
     */
    public Iterable<Prefab> getParents();

    public void addParent(Prefab parent);

    public void removeParent(Prefab parent);

    public boolean isPersisted();

    public void setPersisted(boolean persisted);

}
