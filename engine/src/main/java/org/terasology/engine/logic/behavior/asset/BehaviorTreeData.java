// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.gestalt.assets.AssetData;
import org.terasology.engine.logic.behavior.core.BehaviorNode;

/**
 */
public class BehaviorTreeData implements AssetData {
    private BehaviorNode root;

    public void setRoot(BehaviorNode root) {
        this.root = root;
    }

    public BehaviorNode getRoot() {
        return root;
    }
}
