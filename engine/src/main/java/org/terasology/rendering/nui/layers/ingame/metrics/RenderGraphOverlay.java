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

import org.terasology.registry.In;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.layers.ingame.PauseMenu;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UITreeView;
import org.terasology.rendering.nui.widgets.treeView.GenericTree;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Displays the content of the WorldRenderer renderGraph.
 */
public class RenderGraphOverlay extends CoreScreenLayer {

    @In
    private WorldRenderer worldRenderer;
    
    private UITreeView<RenderNodeInfo> renderGraphTreeView;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", widget -> getManager().closeScreen(RenderGraphOverlay.this));
        renderGraphTreeView = find("renderGraphTreeView", UITreeView.class);
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
