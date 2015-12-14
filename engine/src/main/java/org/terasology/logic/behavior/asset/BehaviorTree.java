/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.nui.RenderableNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.module.sandbox.API;

import java.util.List;

/**
 * Behavior tree asset. Can be loaded and saved into json.
 * <br><br>
 * This asset keeps track of the tree of Nodes and the associated RenderableNodes. If there are no RenderableNodes,
 * the helper class will generate and layout some.
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

//    public BehaviorTree(AssetUri uri, BehaviorTreeData data) {
//        super(uri);
//        this.data = data;
//    }

    public Node getRoot() {
        return data.getRoot();
    }

    public BehaviorTreeData getData() {
        return data;
    }

    public RenderableNode getRenderableNode(Node node) {
        return data.getRenderableNode(node);
    }

    public List<RenderableNode> getRenderableNodes(BehaviorNodeFactory factory) {
        if (!data.hasRenderable()) {
            data.createRenderable(factory);
            layout(null);
        }
        return data.getRenderableNodes();
    }

    public void layout(RenderableNode start) {
        data.layout(start);
    }

    @Override
    public String toString() {
        return getUrn().toString();
    }

    @Override
    protected void doReload(BehaviorTreeData newData) {
        this.data = newData;
    }

    public RenderableNode createNode(Node node, BehaviorNodeFactory factory) {
        RenderableNode renderable = data.createRenderable(node, factory);
        data.layout(renderable);
        return renderable;
    }
}
