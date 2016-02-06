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

/**
 */
public interface MutableComponentContainer extends ComponentContainer {

    /**
     * Adds a component. If this already has a component of the same class it is replaced.
     *
     * @param component
     */
    <T extends Component> T addComponent(T component);

    /**
     * @param componentClass
     */
    void removeComponent(Class<? extends Component> componentClass);

    /**
     * Saves changes made to a component
     *
     * @param component
     */
    void saveComponent(Component component);

    default void addOrSaveComponent(Component component) {
        if (hasComponent(component.getClass())) {
            saveComponent(component);
        } else {
            addComponent(component);
        }
    }
}
