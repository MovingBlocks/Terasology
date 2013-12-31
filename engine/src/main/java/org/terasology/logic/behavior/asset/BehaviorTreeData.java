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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.asset.AssetData;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.nui.RenderableNode;
import org.terasology.logic.behavior.tree.Node;

import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class BehaviorTreeData implements AssetData {
    private Map<Node, RenderableNode> renderableNodes = Maps.newHashMap();
    private Node root;
    private RenderableNode renderableRoot;

    public void setRoot(Node root) {
        this.root = root;
    }

    public void setRenderableRoot(RenderableNode renderableRoot) {
        this.renderableRoot = renderableRoot;
    }

    public void createRenderable(final BehaviorNodeFactory factory) {
        renderableRoot = root.visit(null, new Node.Visitor<RenderableNode>() {
            @Override
            public RenderableNode visit(RenderableNode parent, Node node) {
                BehaviorNodeComponent nodeComponent = factory.getNodeComponent(node);
                RenderableNode self = new RenderableNode(nodeComponent);
                self.setNode(node);
                if (parent != null) {
                    int total = parent.getNode().getChildrenCount();
                    int curr = parent.getChildrenCount();
                    self.setPosition(12 * curr - 6 * total, 7);
                    parent.withoutModel().insertChild(-1, self);
                }

                renderableNodes.put(self.getNode(), self);
                return self;
            }
        });
    }

    public boolean hasRenderable() {
        return renderableRoot != null;
    }

    public Node getRoot() {
        return root;
    }

    public RenderableNode getRenderableRoot() {
        return renderableRoot;
    }

    public List<RenderableNode> getRenderableNodes() {
        return Lists.newArrayList(renderableNodes.values());
    }
}
