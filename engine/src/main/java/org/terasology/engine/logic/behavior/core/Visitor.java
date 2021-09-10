// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * The visitor to iterate a tree of behavior nodes
 */
public interface Visitor<T> {
    T visit(T item, BehaviorNode node);
}
