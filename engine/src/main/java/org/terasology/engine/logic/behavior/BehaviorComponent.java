// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

/**
 * Entities with this component are handled by a behavior tree. Default tree to fetch may be set.
 *
 */
@API
public class BehaviorComponent implements Component<BehaviorComponent> {
    public BehaviorTree tree;
    public transient Interpreter interpreter;

    @Override
    public void copyFrom(BehaviorComponent other) {
        this.tree = other.tree;
        this.interpreter = other.interpreter;
    }
}
