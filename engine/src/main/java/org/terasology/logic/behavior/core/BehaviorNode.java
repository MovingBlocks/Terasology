/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior.core;

import org.terasology.nui.properties.PropertyProvider;

/**
 * Nodes, that are part of a behavior tree. Can have a ordered list of children.
 */
public interface BehaviorNode {
    String getName();

    void insertChild(int index, BehaviorNode child);

    void replaceChild(int index, BehaviorNode child);

    BehaviorNode removeChild(int index);

    BehaviorNode getChild(int index);

    int getChildrenCount();

    int getMaxChildren();

    void construct(Actor actor);

    BehaviorState execute(Actor actor);

    void destruct(Actor actor);

    BehaviorNode deepCopy();

    <T> T visit(T item, Visitor<T> visitor);

    PropertyProvider getProperties();
}
