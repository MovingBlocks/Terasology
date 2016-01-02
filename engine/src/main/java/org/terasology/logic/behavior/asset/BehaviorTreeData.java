/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.FixedNodeExtentProvider;
import org.terasology.assets.AssetData;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.nui.RenderableNode;
import org.terasology.logic.behavior.tree.Node;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
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

    public RenderableNode createNode(Node node, BehaviorNodeFactory factory) {
        BehaviorNodeComponent nodeComponent = factory.getNodeComponent(node);
        RenderableNode self = new RenderableNode(nodeComponent);
        self.setNode(node);
        renderableNodes.put(node, self);
        return self;
    }

    public void createRenderable(BehaviorNodeFactory factory) {
        renderableRoot = createRenderable(root, factory);
    }

    public RenderableNode createRenderable(Node node, BehaviorNodeFactory factory) {
        return node.visit(null, new Node.Visitor<RenderableNode>() {
            @Override
            public RenderableNode visit(RenderableNode parent, Node n) {
                RenderableNode self = createNode(n, factory);
                if (parent != null) {
                    parent.withoutModel().insertChild(-1, self);
                }
                return self;
            }
        });
    }

    public void layout(RenderableNode start) {
        LayoutTree layoutTree;
        if (start == null) {
            layoutTree = new LayoutTree(renderableRoot);
        } else {
            layoutTree = new LayoutTree(start);
        }
        TreeLayout<RenderableNode> layout = new TreeLayout<>(layoutTree, new FixedNodeExtentProvider<>(10, 5), new DefaultConfiguration<>(4, 2));
        Map<RenderableNode, Rectangle2D.Double> bounds = layout.getNodeBounds();
        for (Map.Entry<RenderableNode, Rectangle2D.Double> entry : bounds.entrySet()) {
            RenderableNode node = entry.getKey();
            Rectangle2D.Double rect = entry.getValue();
            node.setPosition((float) rect.getX(), (float) rect.getY());
        }
    }

    public boolean hasRenderable() {
        return renderableRoot != null;
    }

    public Node getRoot() {
        return root;
    }

    public List<RenderableNode> getRenderableNodes() {
        final List<RenderableNode> result = Lists.newArrayList();
        root.visit(null, (Node.Visitor<RenderableNode>) (item, node) -> {
            result.add(renderableNodes.get(node));
            return null;
        });
        return result;
    }

    public RenderableNode getRenderableNode(Node node) {
        return renderableNodes.get(node);
    }

    private static final class LayoutTree implements TreeForTreeLayout<RenderableNode> {
        private RenderableNode root;

        private LayoutTree(RenderableNode root) {
            this.root = root;
        }

        @Override
        public RenderableNode getRoot() {
            return root;
        }

        @Override
        public boolean isLeaf(RenderableNode uiWidgets) {
            return uiWidgets.getChildrenCount() == 0;
        }

        @Override
        public boolean isChildOfParent(RenderableNode node, RenderableNode parentNode) {
            return parentNode.children().contains(node);
        }

        @Override
        public Iterable<RenderableNode> getChildren(RenderableNode parentNode) {
            return parentNode.children();
        }

        @Override
        public Iterable<RenderableNode> getChildrenReverse(RenderableNode parentNode) {
            List<RenderableNode> list = Lists.newArrayList(parentNode.children());
            Collections.reverse(list);
            return list;
        }

        @Override
        public RenderableNode getFirstChild(RenderableNode parentNode) {
            return parentNode.getChild(0);
        }

        @Override
        public RenderableNode getLastChild(RenderableNode parentNode) {
            return parentNode.getChild(Math.max(0, parentNode.getChildrenCount() - 1));
        }
    }
}
