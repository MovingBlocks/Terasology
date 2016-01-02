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
import org.terasology.logic.behavior.tree.TreeAccessor;
import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class PortList implements TreeAccessor<RenderableNode> {
    private List<Port> ports = Lists.newLinkedList();
    private Port.InputPort inputPort;
    private RenderableNode node;

    public PortList(RenderableNode node) {
        inputPort = new Port.InputPort(node);
        Port.InsertOutputPort addLastPortIns = new Port.InsertOutputPort(node);
        ports.add(addLastPortIns);
        this.node = node;
    }

    public void onDraw(Canvas canvas) {
        Rect2i region = canvas.getRegion();
        inputPort.updateRect();
        canvas.drawWidget(inputPort, Rect2i.createFromMinAndMax(
                (int) (inputPort.rect.minX() / 10.f * region.width()),
                (int) (inputPort.rect.minY() / 5.f * region.height()),
                (int) (inputPort.rect.maxX() / 10.f * region.width()),
                (int) (inputPort.rect.maxY() / 5.f * region.height())));
        for (Port port : ports) {
            port.updateRect();
            canvas.drawWidget(port, Rect2i.createFromMinAndMax(
                    (int) (port.rect.minX() / 10.f * region.width()),
                    (int) (port.rect.minY() / 5.f * region.height()),
                    (int) (port.rect.maxX() / 10.f * region.width()),
                    (int) (port.rect.maxY() / 5.f * region.height())));
        }
    }

    public Port.InputPort getInputPort() {
        return inputPort;
    }

    public List<Port> ports() {
        return ports.stream().filter(Port::isVisible).collect(Collectors.toCollection(ArrayList::new));
    }

    int indexOfPort(Port port) {
        return ports.indexOf(port) / 2;
    }

    @Override
    public void insertChild(int index, RenderableNode child) {
        Port.OutputPort outputPort = new Port.OutputPort(node);
        Port.InsertOutputPort insertOutputPort = new Port.InsertOutputPort(node);
        child.getInputPort().setTarget(outputPort);
        if (index == -1) {
            ports.add(ports.size() - 1, insertOutputPort);
            ports.add(ports.size() - 1, outputPort);
        } else {
            ports.add(index * 2, insertOutputPort);
            ports.add(index * 2 + 1, outputPort);
        }
    }

    @Override
    public void setChild(int index, RenderableNode child) {
        if (ports.size() == index * 2 + 1) {
            Port.OutputPort outputPort = new Port.OutputPort(node);
            Port.InsertOutputPort insertOutputPort = new Port.InsertOutputPort(node);
            ports.add(ports.size() - 1, insertOutputPort);
            ports.add(ports.size() - 1, outputPort);
        }
        child.getInputPort().setTarget((Port.OutputPort) ports.get(index * 2 + 1));
    }

    @Override
    public RenderableNode removeChild(int index) {
        ports.remove(index * 2);
        Port output = ports.remove(index * 2);
        output.getSourceNode().getInputPort().setTarget(null);
        return output.getSourceNode();
    }

    @Override
    public RenderableNode getChild(int index) {
        return ports.get(index * 2 + 1).getTargetNode();
    }

    @Override
    public int getChildrenCount() {
        return ports.size() / 2;
    }

    @Override
    public int getMaxChildren() {
        return Integer.MAX_VALUE;
    }
}
