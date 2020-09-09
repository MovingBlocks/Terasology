// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.gestalt.assets.AssetData;

/**
 *
 */
public class BehaviorTreeData implements AssetData {
    private BehaviorNode root;

    public BehaviorNode getRoot() {
        return root;
    }

    public void setRoot(BehaviorNode root) {
        this.root = root;
    }
}
