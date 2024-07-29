// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.context.annotation.API;

/**
 * Behavior tree asset. Can be loaded and saved into json. Actors should never run the nodes behind a asset directly.
 * Instead, use a runner which operates on copies, so actors can share one tree asset.
 *
 */
@API
public class BehaviorTree extends Asset<BehaviorTreeData> {
    private BehaviorTreeData data;

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn,
     * and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    public BehaviorTree(ResourceUrn urn, AssetType<?, BehaviorTreeData> assetType, BehaviorTreeData data) {
        super(urn, assetType);
        reload(data);
    }

    public BehaviorNode getRoot() {
        return data.getRoot();
    }

    public BehaviorTreeData getData() {
        return data;
    }

    @Override
    public String toString() {
        return getUrn().toString();
    }

    @Override
    protected void doReload(BehaviorTreeData newData) {
        this.data = newData;
    }

    }
