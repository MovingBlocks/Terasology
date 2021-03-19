// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

import com.google.common.collect.Lists;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.events.NUIMouseDragEvent;
import org.terasology.nui.events.NUIMouseOverEvent;
import org.terasology.nui.events.NUIMouseReleaseEvent;
import org.terasology.nui.layouts.ZoomableLayout;
import org.terasology.nui.properties.PropertyProvider;
import org.terasology.engine.utilities.Assets;

import java.util.List;

/**
 * A widget to render a node of a behavior tree. Appearance is defined by a BehaviorNodeComponent which is queried using
 * the BehaviorNodeFactory for a given behavior node.
 *
 * Can be wired to other nodes.
 *
 */
public class RenderableNode extends CoreWidget implements ZoomableLayout.PositionalWidget<BehaviorEditor>, TreeAccessor<RenderableNode> {
    private UITextureRegion texture = Assets.getTextureRegion("engine:button").get();

    private final List<RenderableNode> children = Lists.newArrayList();
    private PortList portList;

    private BehaviorNode node;
    private Vector2f position;
    private Vector2f size;
    private TreeAccessor<RenderableNode> withoutModel;
    private TreeAccessor<RenderableNode> withModel;
    private BehaviorNodeComponent data;
    private Vector2i last;
    private BehaviorEditor editor;
    private boolean dragged;
    private boolean collapsed;
    private boolean copyMode;

    private InteractionListener moveListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            last = event.getRelativeMousePosition();
            MouseInput button = event.getMouseButton();
            KeyboardDevice keyboard = event.getKeyboard();
            dragged = false;
            copyMode = button == MouseInput.MOUSE_LEFT && (keyboard.isKeyDown(Keyboard.KeyId.LEFT_SHIFT) || keyboard.isKeyDown(Keyboard.KeyId.RIGHT_SHIFT));
            if (copyMode) {
                editor.copyNode(RenderableNode.this);
            }
            return true;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (!dragged) {
                if (event.getMouseButton() == MouseInput.MOUSE_RIGHT) {
                    collapsed = !collapsed;
                    for (RenderableNode child : children) {
                        child.setVisible(!collapsed);
                    }
                } else {
                    editor.nodeClicked(RenderableNode.this);
                }
            }
            dragged = false;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2f diff = editor.screenToWorld(event.getRelativeMousePosition());
            diff.sub(editor.screenToWorld(last));
            if (diff.lengthSquared() != 0) {
                dragged = true;
            }
            move(diff);

            last = event.getRelativeMousePosition();
        }
    };

    public RenderableNode(BehaviorNodeComponent data) {
        this.data = data;
        position = new Vector2f();
        size = new Vector2f(10, 5);
        portList = new PortList(this);
        withoutModel = new ChainedTreeAccessor<>(this, portList);
        withModel = new ChainedTreeAccessor<>(this, portList, new NodeTreeAccessor());
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawTexture(texture);
        BehaviorState status = getState();
        String text = getData().displayName + " " + (status != null ? status : "");
        if (collapsed) {
            text += "[+]";
        }
        canvas.drawText(text);

        if (editor != null) {
            canvas.addInteractionRegion(moveListener, getData().description);
        }
        portList.onDraw(canvas);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    public void update() {
        List<RenderableNode> all = Lists.newArrayList(children);
        children.clear();
        for (RenderableNode renderableNode : all) {
            withoutModel.insertChild(-1, renderableNode);
        }
    }

    @Override
    public void onAdded(BehaviorEditor layout) {
        this.editor = layout;
    }

    @Override
    public void onRemoved(BehaviorEditor layout) {
        this.editor = null;
    }

    public TreeAccessor<RenderableNode> withoutModel() {
        return withoutModel;
    }

    public TreeAccessor<RenderableNode> withModel() {
        return withModel;
    }

    public PortList getPortList() {
        return portList;
    }

    public void setNode(BehaviorNode node) {
        this.node = node;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        position = new Vector2f(x, y);
    }

    public void move(Vector2f diff) {
        position = new Vector2f(position);
        position.add(diff);
        for (RenderableNode child : children) {
            child.move(diff);
        }
    }

    public BehaviorEditor getEditor() {
        return editor;
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public BehaviorNode getNode() {
        return node;
    }

    public PropertyProvider getProperties() {
        return node.getProperties();
    }

    public BehaviorNodeComponent getData() {
        return data;
    }

    public Port.InputPort getInputPort() {
        return getPortList().getInputPort();
    }

    public Iterable<Port> getPorts() {
        return getPortList().ports();
    }

    @Override
    public void insertChild(int index, RenderableNode child) {
        if (index == -1) {
            children.add(child);
        } else {
            children.add(index, child);
        }
    }

    @Override
    public void setChild(int index, RenderableNode child) {
        if (children.size() == index) {
            children.add(null);
        }
        if (children.get(index) != null) {
            Port.InputPort inputPort = children.get(index).getInputPort();
            inputPort.setTarget(null);
        }
        children.set(index, child);
    }

    @Override
    public RenderableNode removeChild(int index) {
        RenderableNode remove = children.remove(index);
        remove.getInputPort().setTarget(null);
        return remove;
    }

    @Override
    public RenderableNode getChild(int index) {
        if (children.size() > index) {
            return children.get(index);
        }
        return null;
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    public List<RenderableNode> children() {
        return children;
    }

    @Override
    public int getMaxChildren() {
        return getNode().getMaxChildren();
    }

    @Override
    public Vector2f getPosition() {
        return position;
    }

    @Override
    public Vector2f getSize() {
        return size;
    }

    @Override
    public String toString() {
        return getNode() != null ? getNode().toString() : "";
    }

    public void visit(Visitor visitor) {
        visitor.visit(this);
        for (RenderableNode child : children) {
            child.visit(visitor);
        }
    }

    public BehaviorState getState() {
        return editor != null ? editor.getState(getNode()) : null;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        for (RenderableNode child : children) {
            child.setVisible(visible);
        }
    }

    public interface Visitor {
        void visit(RenderableNode node);
    }

    public class NodeTreeAccessor implements TreeAccessor<RenderableNode> {
        @Override
        public void insertChild(int index, RenderableNode child) {
            getNode().insertChild(index, child.getNode());
        }

        @Override
        public void setChild(int index, RenderableNode child) {
            getNode().replaceChild(index, child.getNode());
        }

        @Override
        public RenderableNode removeChild(int index) {
            getNode().removeChild(index);
            return null;
        }

        @Override
        public RenderableNode getChild(int index) {
            return null;
        }

        @Override
        public int getChildrenCount() {
            return getNode().getChildrenCount();
        }

        @Override
        public int getMaxChildren() {
            return getNode().getMaxChildren();
        }
    }
}
