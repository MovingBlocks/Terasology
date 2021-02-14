/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.metrics;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.MouseInput;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UITreeView;
import org.terasology.nui.widgets.treeView.GenericTree;
import org.terasology.registry.In;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Displays the content of the WorldRenderer renderGraph.
 */
public class RenderGraphOverlay extends CoreScreenLayer {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraphOverlay.class);

    @In
    private WorldRenderer worldRenderer;

    private UITreeView<RenderNodeInfo> renderGraphTreeView;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", widget -> getManager().closeScreen(RenderGraphOverlay.this));
        renderGraphTreeView = find("renderGraphTreeView", UITreeView.class);
        renderGraphTreeView.subscribeNodeClick((event, treeNode) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                RenderNodeInfo renderNodeInfo = (RenderNodeInfo) treeNode.getValue();
                Node renderNode = renderNodeInfo.getNode();
                String uriString = renderNode.getUri().toString();
                logger.info(uriString);
            }
        });

        renderGraphTreeView.setItemRenderer(new StringTextRenderer<RenderNodeInfo>() {
            @Override
            public String getString(RenderNodeInfo renderNodeInfo) {
                Node renderNode = renderNodeInfo.getNode();
                if (null == renderNode) {
                    return "ROOT";
                }
                String uriString = renderNode.getUri().toString();
                if (!renderNode.isEnabled()) {
                    return uriString + " -- DISABLED";
                }


                StringBuilder sb = new StringBuilder();
                sb.append(uriString);
                sb.append('\n');
                sb.append('\n');

                // printing out individual desired state changes
                for (StateChange desiredStateChange : renderNode.getDesiredStateChanges()) {
                    sb.append(desiredStateChange.toString());
                    sb.append('\n');
                }
                sb.append('\n');

                sb.append(renderNode);
                sb.append(": process()");
                sb.append('\n');
                return sb.toString();
            }
        });

        RenderGraph renderGraph = worldRenderer.getRenderGraph();
        RenderNodeInfoTree renderGraphTreeModel = new RenderNodeInfoTree(renderGraph);
        renderGraphTreeView.setModel(renderGraphTreeModel);
    }

    private static class RenderNodeInfo {
        private Node node;

        public RenderNodeInfo() {
            super();
        }

        public RenderNodeInfo(Node node) {
            super();
            this.node = node;
        }

        public Node getNode() {
            return node;
        }

        @Override
        public String toString() {
            if (null == node) {
                return "ROOT";
            }
            return node.getUri().toString();
        }
    }

    private static class RenderNodeInfoTree extends GenericTree<RenderNodeInfo> {
        public RenderNodeInfoTree(RenderGraph renderGraph) {
            super(new RenderNodeInfo());

            List<Node> nodes = renderGraph.getStartingNodes();
            for (Node node : nodes) {
                RenderNodeInfoTree child = new RenderNodeInfoTree(renderGraph, new RenderNodeInfo(node));
                addChild(child);
            }
        }

        private RenderNodeInfoTree(RenderGraph renderGraph, RenderNodeInfo renderNodeInfo) {
            super(renderNodeInfo);
            Node node = renderNodeInfo.getNode();
            if (null == node) {
                throw new NullPointerException("RenderNodeInfo.getNode()");
            }
            Set<Node> childNodeList = renderGraph.getOutgoingNodesForNode(node);
            for (Node childNode : childNodeList) {
                RenderNodeInfoTree child = new RenderNodeInfoTree(renderGraph, new RenderNodeInfo(childNode));
                addChild(child);
            }
        }
    }

    @Override
    public void update(float delta) {
        renderGraphTreeView.update(delta);
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return true;
    }
}
