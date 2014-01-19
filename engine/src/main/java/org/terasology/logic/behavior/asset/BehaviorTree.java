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

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.behavior.nui.RenderableNode;
import org.terasology.logic.behavior.tree.Node;

import java.util.List;

/**
 * Behavior tree asset. Can be loaded and saved into json.
 * <p/>
 * This asset keeps track of the tree of Nodes and the associated RenderableNodes. If there are no RenderableNodes,
 * the helper class will generate and layout some.
 *
 * @author synopia
 */
public class BehaviorTree extends AbstractAsset<BehaviorTreeData> {
    private BehaviorTreeData data;

    public BehaviorTree(AssetUri uri, BehaviorTreeData data) {
        super(uri);
        this.data = data;
    }

    public Node getRoot() {
        return data.getRoot();
    }

    public BehaviorTreeData getData() {
        return data;
    }

    public RenderableNode getRenderableNode(Node node) {
        return data.getRenderableNode(node);
    }

    public List<RenderableNode> getRenderableNodes() {
        if (!data.hasRenderable()) {
            data.createRenderable();
            layout(null);
        }
        return data.getRenderableNodes();
    }

    public void layout(RenderableNode start) {
        data.layout(start);
    }

    @Override
    public String toString() {
        return getURI().getAssetName();
    }

    @Override
    public void reload(BehaviorTreeData newData) {
        this.data = newData;
    }

    @Override
    public void dispose() {
        this.data = null;
    }

    @Override
    public boolean isDisposed() {
        return data == null;
    }

    public RenderableNode createNode(Node node) {
        RenderableNode renderable = data.createRenderable(node);
        data.layout(renderable);
        return renderable;
    }
}
