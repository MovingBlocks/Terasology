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

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.Component;
import org.terasology.input.MouseInput;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.TreeAccessor;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.layouts.ZoomableLayout;

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * A widget to render and process inputs for a node of a behavior tree. *
 *
 * @author synopia
 */
public class RenderableNode extends CoreWidget implements ZoomableLayout.PositionalWidget<BehaviorEditor>, Component, TreeAccessor<RenderableNode> {
    private TextureRegion texture = Assets.getTextureRegion("engine:button");

    private final List<RenderableNode> children = Lists.newArrayList();
    private PortList portList;

    private Node node;
    private Vector2f position;
    private Vector2f size;
    private TreeAccessor<RenderableNode> withoutModel;
    private TreeAccessor<RenderableNode> withModel;
    private BehaviorNodeComponent data;
    private Vector2i last;
    private BehaviorEditor editor;
    private boolean dragged;
    private Status status;

    private InteractionListener moveListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(Vector2i pos, boolean topMostElement) {
        }

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            last = pos;
            dragged = false;
            return true;
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            if (!dragged) {
                editor.nodeClicked(RenderableNode.this);
            }
            dragged = false;
        }

        @Override
        public void onMouseDrag(Vector2i pos) {
            Vector2f diff = editor.screenToWorld(pos);
            diff.sub(editor.screenToWorld(last));
            if (diff.lengthSquared() != 0) {
                dragged = true;
            }
            move(diff);

            last = pos;
        }
    };

    public RenderableNode() {
        this(null);
    }

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
        String text = getData().name + " " + (status != null ? status : "");
        canvas.drawText(text);

        if (editor != null) {
            canvas.addInteractionRegion(moveListener);
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

    public void setNode(Node node) {
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

    public Node getNode() {
        return node;
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

    public void insertChild(int index, RenderableNode child) {
        if (index == -1) {
            children.add(child);
        } else {
            children.add(index, child);
        }
    }

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

    public RenderableNode removeChild(int index) {
        RenderableNode remove = children.remove(index);
        remove.getInputPort().setTarget(null);
        return remove;
    }

    public RenderableNode getChild(int index) {
        if (children.size() > index) {
            return children.get(index);
        }
        return null;
    }

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

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
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
            getNode().setChild(index, child.getNode());
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
