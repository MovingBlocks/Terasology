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

import org.terasology.asset.Assets;
import org.terasology.input.MouseInput;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;

import javax.vecmath.Vector2f;

/**
 * @author synopia
 */
public abstract class Port extends CoreWidget {
    private TextureRegion active = Assets.getTextureRegion("engine:checkboxChecked");
    private TextureRegion inactive = Assets.getTextureRegion("engine:checkbox");
    protected RenderableNode node;
    protected Rect2f rect;

    private InteractionListener connectListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            node.getEditor().portClicked(Port.this);
            return true;
        }
    };

    protected Port() {
    }

    protected Port(RenderableNode node) {
        this.node = node;
    }

    public int index() {
        return node.getPortList().indexOfPort(this);
    }

    public abstract void updateRect();

    public RenderableNode getSourceNode() {
        return node;
    }

    public RenderableNode getTargetNode() {
        return getTargetPort() != null ? getTargetPort().getSourceNode() : null;
    }

    public abstract Port getTargetPort();

    public boolean isInput() {
        return false;
    }

    public Rect2f getRect() {
        return rect;
    }

    public Vector2f mid() {
        Vector2f mid = new Vector2f(rect.size());
        mid.scale(0.5f);
        mid.add(rect.min());
        return mid;

    }

    @Override
    public String toString() {
        return getSourceNode() + "[" + index() + "]";
    }

    public boolean isVisible() {
        return index() < getSourceNode().getMaxChildren();
    }

    @Override
    public Vector2i calcContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(connectListener);
        if (getTargetPort() != null) {
            canvas.drawTexture(active);
        } else {
            canvas.drawTexture(inactive);
        }
    }

    public static class OutputPort extends Port {

        public OutputPort() {
            super();
        }

        public OutputPort(RenderableNode renderableNode) {
            super(renderableNode);
        }

        @Override
        public void updateRect() {
            this.rect = Rect2f.createFromMinAndSize(
                    index() + 0.4f,
                    4.05f,
                    0.6f, 0.9f);
        }

        public void setTarget(InputPort inputPort) {
            if (inputPort != null) {
                if (inputPort.getTargetPort() != null) {
                    inputPort.getTargetPort().setTarget(null);
                }
                node.withModel().setChild(index(), inputPort.getSourceNode());
            } else {
                node.withModel().removeChild(index());
            }
        }

        @Override
        public InputPort getTargetPort() {
            RenderableNode child = node.withModel().getChild(index());
            if (child != null) {
                return child.getInputPort();
            }
            return null;
        }
    }

    public static class InsertOutputPort extends OutputPort {
        public InsertOutputPort() {
            super();
        }

        public InsertOutputPort(RenderableNode renderableNode) {
            super(renderableNode);
        }

        @Override
        public void updateRect() {
            this.rect = Rect2f.createFromMinAndSize(
                    index(),
                    4.05f,
                    0.4f, 0.9f);
        }

        @Override
        public void setTarget(InputPort inputPort) {
            if (inputPort != null) {
                if (inputPort.getTargetPort() != null) {
                    inputPort.getTargetPort().setTarget(null);
                }
                node.withModel().insertChild(index(), inputPort.getSourceNode());
            } else {
                throw new IllegalStateException("Cannot remove target from an insert output port");
            }
        }

        @Override
        public boolean isVisible() {
            return getSourceNode().getChildrenCount() < getSourceNode().getMaxChildren();
        }

        @Override
        public InputPort getTargetPort() {
            return null;
        }
    }

    public static class InputPort extends Port {
        private OutputPort outputPort;

        public InputPort() {
            super();
        }

        public InputPort(RenderableNode node) {
            super(node);
        }

        @Override
        public void updateRect() {
            rect = Rect2f.createFromMinAndSize(4.5f, 0.05f, 1f, 1f);
        }

        public void setTarget(OutputPort port) {
            this.outputPort = port;
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public OutputPort getTargetPort() {
            return outputPort;
        }

        @Override
        public boolean isInput() {
            return true;
        }
    }
}
