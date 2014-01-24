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
package org.terasology.logic.behavior.nui;

import org.terasology.input.MouseInput;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeLoader;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layouts.ZoomableLayout;

import javax.vecmath.Vector2f;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author synopia
 */
public class BehaviorEditor extends ZoomableLayout {
    private Port activeConnectionStart;
    private RenderableNode selectedNode;
    private RenderableNode newNode;
    private BehaviorTree tree;
    private Vector2f mousePos;
    private Binding<RenderableNode> selectionBinding;

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
        removeAll();
        for (RenderableNode widget : tree.getRenderableNodes()) {
            addWidget(widget);
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
            return baos.toString();
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
                if (widget instanceof RenderableNode) {
                    RenderableNode renderableNode = (RenderableNode) widget;
                    for (Port port : renderableNode.getPorts()) {
                        Port targetPort = port.getTargetPort();
                        if (port.isInput() || targetPort == null) {
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
        Node node = CoreRegistry.get(BehaviorNodeFactory.class).getNode(data);
        newNode = tree.createNode(node);
        return newNode;
    }

    public void bindSelection(Binding<RenderableNode> binding) {
        selectionBinding = binding;
    }

    private void addNode(RenderableNode node) {
        addWidget(node);
        for (int i = 0; i < node.getChildrenCount(); i++) {
            addWidget(node.getChild(i));
        }
    }
}
