// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorState;

public interface Callback {
    void afterExecute(BehaviorNode node, BehaviorState state);
}
