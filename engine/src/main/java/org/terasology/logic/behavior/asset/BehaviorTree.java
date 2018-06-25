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
package org.terasology.logic.behavior.asset;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.module.sandbox.API;

/**
 * Behavior tree asset. Can be loaded and saved into json. Actors should never run the nodes behind a asset directly.
 * Instead, use a runner which operates on copies, so actors can share one tree asset.
 *
 */
@API
public class BehaviorTree extends Asset<BehaviorTreeData> {
    private BehaviorTreeData data;

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
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
