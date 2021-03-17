// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

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
