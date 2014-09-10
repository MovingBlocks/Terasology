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
package org.terasology.entitySystem.entity;

import org.terasology.entitySystem.Component;

/**
 * @author Immortius
 */
public interface LowLevelEntityManager extends EntityManager {

    boolean isExistingEntity(int id);

    boolean isActiveEntity(int id);

    <T extends Component> T getComponent(int id, Class<T> componentClass);

    boolean hasComponent(int id, Class<? extends Component> componentClass);

    <T extends Component> T addComponent(int id, T component);

    <T extends Component> T removeComponent(int id, Class<T> componentClass);

    void saveComponent(int id, Component component);

    Iterable<Component> iterateComponents(int id);

    void destroy(int id);

}
