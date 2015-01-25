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
package org.terasology.logic.behavior.nui;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.FixedNodeExtentProvider;
import org.terasology.input.MouseInput;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.behavior.DefaultBehaviorTreeRunner;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeLoader;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.behavior.core.Visitor;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.ZoomableLayout;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class BehaviorEditor extends ZoomableLayout implements DefaultBehaviorTreeRunner.Callback {
    private Port activeConnectionStart;
    private RenderableNode selectedNode;
    private RenderableNode newNode;
    private BehaviorTree tree;
    private Vector2f mousePos = new Vector2f();
    private Binding<RenderableNode> selectionBinding;
    private Map<BehaviorNode, BehaviorState> stateMap = Maps.newHashMap();

    private final InteractionListener moveOver = new BaseInteractionListener() {
        @Override
        public void onMouseOver(Vector2i pos, boolean topMostElement) {
            mousePos = screenToWorld(pos);
        }

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (newNode != null) {
                newNode.setPosition(screenToWorld(pos));
                addNode(newNode);
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            newNode = null;
        }
    };

    public BehaviorEditor() {
        super();
    }

    public BehaviorEditor(String id) {
        super(id);
    }

    public void setTree(BehaviorTree tree) {
        this.tree = tree;
        selectedNode = null;
        if (selectionBinding != null) {
            selectionBinding.set(null);
        }
        removeAll();
        List<RenderableNode> renderables = createRenderables(tree);
        for (RenderableNode widget : renderables) {
            addWidget(widget);
        }
        if (renderables.size() > 0) {
            layout(renderables.get(0));
        }
    }

    public BehaviorTree getTree() {
        return tree;
    }

    public String save() {
        BehaviorTreeLoader loader = new BehaviorTreeLoader();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
        try {
            loader.save(baos, tree.getData());
            return baos.toString(Charsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.addInteractionRegion(moveOver);
        try (SubRegion subRegion = canvas.subRegion(canvas.getRegion(), false)) {
            canvas.setDrawOnTop(true);
            for (UIWidget widget : getWidgets()) {
                if (!widget.isVisible()) {
                    continue;
                }
                if (widget instanceof RenderableNode) {
                    RenderableNode renderableNode = (RenderableNode) widget;
                    for (Port port : renderableNode.getPorts()) {
                        Port targetPort = port.getTargetPort();
                        if (port.isInput() || targetPort == null || !targetPort.node.isVisible()) {
                            continue;
                        }
                        drawConnection(canvas, port, targetPort, port == activeConnectionStart ? Color.BLACK : Color.GREY);
                    }
                }
            }
            if (activeConnectionStart != null) {
                drawConnection(canvas, activeConnectionStart, mousePos, Color.WHITE);
            }
            if (selectedNode != null) {
                Vector2f size = selectedNode.getSize();
                Vector2f topLeft = selectedNode.getPosition();
                Vector2f topRight = new Vector2f(topLeft);
                topRight.add(new Vector2f(size.x + .1f, 0));
                Vector2f bottomLeft = new Vector2f(topLeft);
                bottomLeft.add(new Vector2f(0, size.y + .1f));
                Vector2f bottomRight = new Vector2f(topLeft);
                bottomRight.add(new Vector2f(size.x + 0.1f, size.y + 0.1f));
                drawConnection(canvas, topLeft, topRight, Color.GREEN);
                drawConnection(canvas, topRight, bottomRight, Color.GREEN);
                drawConnection(canvas, bottomRight, bottomLeft, Color.GREEN);
                drawConnection(canvas, bottomLeft, topLeft, Color.GREEN);
            }
            if (newNode != null) {
                Vector2i screenStart = worldToScreen(mousePos);
                Vector2f worldEnd = new Vector2f(mousePos);
                worldEnd.add(newNode.getSize());
                Vector2i screenEnd = worldToScreen(worldEnd);
                canvas.drawWidget(newNode, Rect2i.createFromMinAndMax(screenStart, screenEnd));
            }

            canvas.setDrawOnTop(false);
        }
    }

    public void portClicked(Port port) {
        if (activeConnectionStart == null) {
            activeConnectionStart = port;
        } else {
            if (activeConnectionStart.isInput() && !port.isInput()) {
                ((Port.OutputPort) port).setTarget((Port.InputPort) activeConnectionStart);
            } else if (!activeConnectionStart.isInput() && port.isInput()) {
                ((Port.OutputPort) activeConnectionStart).setTarget((Port.InputPort) port);
            }
            CoreRegistry.get(BehaviorSystem.class).treeModified(tree);
            activeConnectionStart = null;
        }
    }

    public void nodeClicked(RenderableNode node) {
        selectedNode = node;
        if (selectionBinding != null) {
            selectionBinding.set(node);
        }
    }

    private void drawConnection(Canvas canvas, Vector2f from, Vector2f to, Color color) {
        Vector2i s = worldToScreen(from);
        Vector2i e = worldToScreen(to);
        canvas.drawLine(s.x, s.y, e.x, e.y, color);

    }

    private void drawConnection(Canvas canvas, Port from, Vector2f to, Color color) {
        Vector2f start = new Vector2f(from.node.getPosition());
        start.add(from.mid());
        drawConnection(canvas, start, to, color);
    }

    private void drawConnection(Canvas canvas, Port from, Port to, Color color) {
        Vector2f start = new Vector2f(from.node.getPosition());
        start.add(from.mid());
        Vector2f end = new Vector2f(to.node.getPosition());
        end.add(to.mid());
        drawConnection(canvas, start, end, color);
    }

    public RenderableNode createNode(BehaviorNodeComponent data) {
        if (tree == null) {
            return null;
        }
        BehaviorNode node = CoreRegistry.get(BehaviorNodeFactory.class).createNode(data);
        newNode = createRenderableNode(node);

        CoreRegistry.get(BehaviorSystem.class).treeModified(tree);
        return newNode;
    }

    public void bindSelection(Binding<RenderableNode> binding) {
        selectionBinding = binding;
    }

    private void addNode(RenderableNode node) {
        addWidget(node);
        for (int i = 0; i < node.getChildrenCount(); i++) {
            addNode(node.getChild(i));
        }
    }

    @Override
    public void afterExecute(BehaviorNode node, BehaviorState state) {
        stateMap.put(node, state);
    }

    public BehaviorState getState(BehaviorNode node) {
        return stateMap.get(node);
    }

    /**
     * copy the given node. the new copy replaces the given one, so you should manipulate the original node, instead of the copy.
     * This is useful when in interaction listener, especially.
     */
    public void copyNode(RenderableNode node) {
//        BehaviorTreeData data = new BehaviorTreeData();
//        data.setRoot(node.getNode());
//        BehaviorTreeLoader loader = new BehaviorTreeLoader();
//        ByteArrayOutputStream os = new ByteArrayOutputStream(10000);
//
//        try {
//            loader.save(os, data);
//            BehaviorTreeData copy = loader.load(null, new ByteArrayInputStream(os.toByteArray()), null, Collections.<URL>emptyList());
//            Port.OutputPort parent = node.getInputPort().getTargetPort();
//            copy.createRenderable();
//            RenderableNode copyRenderable = copy.getRenderableNode(copy.getRoot());
//            addNode(copyRenderable);
//            RenderableNode nodeToLayout;
//            if (parent != null && copyRenderable.getInputPort() != null) {
//                parent.setTarget(copyRenderable.getInputPort());
//                nodeToLayout = parent.node;
//            } else {
//                nodeToLayout = copyRenderable;
//            }
//            Vector2f oldPos = nodeToLayout.getPosition();
//            tree.layout(nodeToLayout);
//            oldPos.sub(nodeToLayout.getPosition());
//            nodeToLayout.move(oldPos);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private RenderableNode createRenderableNode(BehaviorNode node) {
        BehaviorNodeComponent nodeComponent = CoreRegistry.get(BehaviorNodeFactory.class).getNodeComponent(node);
        RenderableNode self = new RenderableNode(nodeComponent);
        self.setNode(node);
        return self;
    }

    private List<RenderableNode> createRenderables(BehaviorTree aTree) {
        final List<RenderableNode> renderables = Lists.newArrayList();
        aTree.getRoot().visit(null, new Visitor<RenderableNode>() {
            @Override
            public RenderableNode visit(RenderableNode parent, BehaviorNode node) {
                RenderableNode self = createRenderableNode(node);
                renderables.add(self);
                if (parent != null) {
                    parent.withoutModel().insertChild(-1, self);
                }
                return self;
            }
        });
        return renderables;
    }

    public void layout(RenderableNode start) {
        LayoutTree layoutTree = new LayoutTree(start);
        TreeLayout<RenderableNode> layout = new TreeLayout<>(layoutTree, new FixedNodeExtentProvider<RenderableNode>(10, 5), new DefaultConfiguration<RenderableNode>(4, 2));
        Map<RenderableNode, Rectangle2D.Double> bounds = layout.getNodeBounds();
        for (Map.Entry<RenderableNode, Rectangle2D.Double> entry : bounds.entrySet()) {
            RenderableNode node = entry.getKey();
            Rectangle2D.Double rect = entry.getValue();
            node.setPosition((float) rect.getX(), (float) rect.getY());
        }
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
