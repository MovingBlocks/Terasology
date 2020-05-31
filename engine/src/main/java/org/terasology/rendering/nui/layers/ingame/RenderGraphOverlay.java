/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.input.MouseInput;
import org.terasology.registry.In;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Displays the content of the WorldRenderer renderGraph.
 */
public class RenderGraphOverlay extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:renderGraphOverlay");

    private static final Logger logger = LoggerFactory.getLogger(RenderGraphOverlay.class);

    @In
    private WorldRenderer worldRenderer;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", widget -> getManager().closeScreen(RenderGraphOverlay.this));
        find("graph", GraphRenderer.class).setRenderGraph(worldRenderer.getRenderGraph());
//        renderGraphTreeView = find("renderGraphTreeView", UITreeView.class);
//        renderGraphTreeView.subscribeNodeClick((event, treeNode) -> {
//            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
//                RenderNodeInfo renderNodeInfo = (RenderNodeInfo) treeNode.getValue();
//                Node renderNode = renderNodeInfo.getNode();
//                String uriString = renderNode.getUri().toString();
//                logger.info(uriString);
//            }
//        });
//
//        renderGraphTreeView.setItemRenderer(new StringTextRenderer<RenderNodeInfo>() {
//            @Override
//            public String getString(RenderNodeInfo renderNodeInfo) {
//                Node renderNode = renderNodeInfo.getNode();
//                if (null == renderNode) {
//                    return "ROOT";
//                }
//                String uriString = renderNode.getUri().toString();
//                if (!renderNode.isEnabled()) {
//                    return uriString + " -- DISABLED";
//                }
//
//
//                StringBuilder sb = new StringBuilder();
//                sb.append(uriString);
//                sb.append('\n');
//                sb.append('\n');
//
//                // printing out individual desired state changes
//                for (StateChange desiredStateChange : renderNode.getDesiredStateChanges()) {
//                    sb.append(desiredStateChange.toString());
//                    sb.append('\n');
//                }
//                sb.append('\n');
//
//                sb.append(renderNode);
//                sb.append(": process()");
//                sb.append('\n');
//                return sb.toString();
//            }
//        });
//
//        RenderGraph renderGraph = worldRenderer.getRenderGraph();
//        RenderNodeInfoTree renderGraphTreeModel = new RenderNodeInfoTree(renderGraph);
//        renderGraphTreeView.setModel(renderGraphTreeModel);
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

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return true;
    }
}
