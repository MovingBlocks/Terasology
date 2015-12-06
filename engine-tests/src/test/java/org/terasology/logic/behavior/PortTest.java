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
package org.terasology.logic.behavior;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.assets.management.AssetManager;
import org.terasology.logic.behavior.nui.Port;
import org.terasology.logic.behavior.nui.PortList;
import org.terasology.logic.behavior.nui.RenderableNode;
import org.terasology.logic.behavior.tree.CompositeNode;
import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.registry.CoreRegistry;

import static org.mockito.Mockito.mock;

/**
 */
public class PortTest extends TerasologyTestingEnvironment {

    @Test
    public void testConnectDecorator() {
        RenderableNode one = new RenderableNode();
        one.setNode(decorator(null));
        RenderableNode two = new RenderableNode();
        two.setNode(node());
        ((Port.OutputPort) one.getPortList().ports().get(0)).setTarget(two.getInputPort());

        Assert.assertEquals(one, two.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(one.getPortList().ports().get(0), two.getInputPort().getTargetPort());

        Assert.assertEquals(1, one.getChildrenCount());
        Assert.assertEquals(two, one.getChild(0));
    }

    @Test
    public void testConnectToConnectedDecorator() {
        RenderableNode one = new RenderableNode();
        one.setNode(decorator(null));
        RenderableNode two = new RenderableNode();
        two.setNode(node());
        ((Port.OutputPort) one.getPortList().ports().get(0)).setTarget(two.getInputPort());

        RenderableNode three = new RenderableNode();
        three.setNode(node());
        ((Port.OutputPort) one.getPortList().ports().get(0)).setTarget(three.getInputPort());

        Assert.assertEquals(one, three.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(one.getPortList().ports().get(0), three.getInputPort().getTargetPort());
        Assert.assertEquals(null, two.getInputPort().getTargetPort());

        Assert.assertEquals(1, one.getChildrenCount());
        Assert.assertEquals(three, one.getChild(0));
    }

    @Test
    public void testConnectToConnectedDecorator2() {
        RenderableNode one = new RenderableNode();
        one.setNode(decorator(null));
        RenderableNode two = new RenderableNode();
        two.setNode(decorator(null));
        ((Port.OutputPort) one.getPortList().ports().get(0)).setTarget(two.getInputPort());
        RenderableNode three = new RenderableNode();
        three.setNode(decorator(null));
        ((Port.OutputPort) three.getPortList().ports().get(0)).setTarget(two.getInputPort());

        Assert.assertEquals(three, two.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(three.getPortList().ports().get(0), two.getInputPort().getTargetPort());

        Assert.assertEquals(1, three.getChildrenCount());
        Assert.assertEquals(two, three.getChild(0));

        Assert.assertEquals(0, one.getChildrenCount());
    }

    @Test
    public void testDisconnectDecorator() {
        RenderableNode one = new RenderableNode();
        one.setNode(decorator(null));
        RenderableNode two = new RenderableNode();
        two.setNode(node());
        ((Port.OutputPort) one.getPortList().ports().get(0)).setTarget(two.getInputPort());

        Assert.assertEquals(1, one.getChildrenCount());
        Assert.assertEquals(two, one.getChild(0));
        ((Port.OutputPort) one.getPortList().ports().get(0)).setTarget(null);

        Assert.assertEquals(0, one.getChildrenCount());
        Assert.assertEquals(null, two.getInputPort().getTargetPort());
    }

    @Test
    public void testConnectComposite() {
        RenderableNode parent = new RenderableNode();
        parent.setNode(composite());
        RenderableNode one = new RenderableNode();
        one.setNode(node());
        RenderableNode two = new RenderableNode();
        two.setNode(node());

        ((Port.OutputPort) parent.getPortList().ports().get(0)).setTarget(one.getInputPort());
        ((Port.OutputPort) parent.getPortList().ports().get(2)).setTarget(two.getInputPort());

        Assert.assertEquals(parent, one.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(parent, two.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(parent.getPortList().ports().get(1), one.getInputPort().getTargetPort());
        Assert.assertEquals(parent.getPortList().ports().get(3), two.getInputPort().getTargetPort());

        Assert.assertEquals(2, parent.getChildrenCount());

        Assert.assertEquals(one, parent.getChild(0));
        Assert.assertEquals(two, parent.getChild(1));
    }

    @Test
    public void testConnectToConnectedComposite() {
        RenderableNode parent = new RenderableNode();
        parent.setNode(composite());
        RenderableNode one = new RenderableNode();
        one.setNode(node());
        RenderableNode two = new RenderableNode();
        two.setNode(node());

        ((Port.OutputPort) parent.getPortList().ports().get(0)).setTarget(one.getInputPort());
        ((Port.OutputPort) parent.getPortList().ports().get(2)).setTarget(two.getInputPort());

        RenderableNode three = new RenderableNode();
        three.setNode(node());
        ((Port.OutputPort) parent.getPortList().ports().get(3)).setTarget(three.getInputPort());

        Assert.assertEquals(parent, one.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(parent, three.getInputPort().getTargetPort().getSourceNode());
        Assert.assertEquals(parent.getPortList().ports().get(1), one.getInputPort().getTargetPort());
        Assert.assertEquals(parent.getPortList().ports().get(3), three.getInputPort().getTargetPort());

        Assert.assertEquals(2, parent.getChildrenCount());

        Assert.assertEquals(one, parent.getChild(0));
        Assert.assertEquals(three, parent.getChild(1));
        Assert.assertEquals(null, two.getInputPort().getTargetPort());
    }

    @Test
    public void testDisconnectComposite() {
        RenderableNode parent = new RenderableNode();
        parent.setNode(composite());
        RenderableNode one = new RenderableNode();
        one.setNode(node());
        RenderableNode two = new RenderableNode();
        two.setNode(node());

        ((Port.OutputPort) parent.getPortList().ports().get(0)).setTarget(one.getInputPort());
        ((Port.OutputPort) parent.getPortList().ports().get(2)).setTarget(two.getInputPort());

        ((Port.OutputPort) parent.getPortList().ports().get(1)).setTarget(null);
        Assert.assertEquals(1, parent.getChildrenCount());
        Assert.assertEquals(two, parent.getChild(0));
        Assert.assertEquals(null, one.getInputPort().getTargetPort());

        ((Port.OutputPort) parent.getPortList().ports().get(1)).setTarget(null);
        Assert.assertEquals(0, parent.getChildrenCount());
        Assert.assertEquals(null, two.getInputPort().getTargetPort());
    }

    @Test
    public void testLeaf() {
        RenderableNode node = new RenderableNode();
        node.setNode(node());
        PortList portList = node.getPortList();
        Assert.assertEquals(0, portList.ports().size());

    }

    @Test
    public void testDecorator() {
        RenderableNode node = new RenderableNode();
        node.setNode(decorator(node()));
        PortList portList = node.getPortList();
        Assert.assertEquals(1, portList.ports().size());
    }

    private Node node() {
        return new Node() {
            @Override
            public Task createTask() {
                return null;
            }
        };
    }

    private DecoratorNode decorator(final Node node) {
        return new DecoratorNode() {
            {
                this.child = node;
            }

            @Override
            public Task createTask() {
                return null;
            }
        };
    }

    private CompositeNode composite() {
        return new CompositeNode() {
            @Override
            public Task createTask() {
                return null;
            }
        };
    }

    @Test
    public void testComposite() {
        RenderableNode node = new RenderableNode();
        node.setNode(new CompositeNode() {
            @Override
            public Task createTask() {
                return null;
            }
        });
        PortList portList = node.getPortList();
        Assert.assertEquals(1, portList.ports().size());
    }
}
