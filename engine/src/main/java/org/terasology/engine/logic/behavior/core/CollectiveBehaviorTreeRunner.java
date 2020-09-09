// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.terasology.engine.logic.behavior.asset.BehaviorTree;

import java.util.Set;

/**
 * Interface for collective runnable trees
 */
public interface CollectiveBehaviorTreeRunner {

    BehaviorState step();

    Set<Actor> getActors();

    void setActors(Set<Actor> actors);

    BehaviorTree getTree();
}
