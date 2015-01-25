/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.behavior.core.BehaviorNode;

/**
 * Behavior tree asset. Can be loaded and saved into json. Actors should never run the nodes behind a asset directly.
 * Instead, use a runner which operates on copies, so actors can share one tree asset.
 *
 * @author synopia
 */
public class BehaviorTree extends AbstractAsset<BehaviorTreeData> {
    private BehaviorTreeData data;

    public BehaviorTree(AssetUri uri, BehaviorTreeData data) {
        super(uri);
        this.data = data;
    }

    public BehaviorNode getRoot() {
        return data.getRoot();
    }

    public BehaviorTreeData getData() {
        return data;
    }

    @Override
    public String toString() {
        return getURI().getAssetName().toString();
    }

    @Override
    protected void onReload(BehaviorTreeData newData) {
        this.data = newData;
    }

    @Override
    protected void onDispose() {
        this.data = null;
    }
}
