// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.terasology.engine.logic.behavior.asset.BehaviorTree;

/**
 * Interface for runnable trees.
 */
public interface BehaviorTreeRunner {
    BehaviorState step();

    void setActor(Actor actor);

    Actor getActor();

    BehaviorTree getTree();
}
