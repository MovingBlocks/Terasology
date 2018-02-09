/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.terasology.engine.SimpleUri;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RenderTaskListGeneratorTest {

    @Test
    public void testSimpleReducePersistingStateChanges() {

        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        Node alphaNode = new AlphaNode();
        alphaNode.setUri(new SimpleUri("engine:alphaNode"));
        orderedNodes.add(alphaNode);
        Node bravoNode = new BravoNode();
        bravoNode.setUri(new SimpleUri("engine:bravoNode"));
        orderedNodes.add(bravoNode);
        Node charlieNode = new CharlieNode();
        charlieNode.setUri(new SimpleUri("engine:charlieNode"));
        orderedNodes.add(charlieNode);

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("----- engine:alphaNode (AlphaNode)",        taskList.get(0).toString().trim()); // Strictly speaking we don't need
        assertEquals("SetName: foo",       taskList.get(1).toString().trim()); // trimming MarkerTask.toString(),
        assertEquals("engine:alphaNode (AlphaNode)",   taskList.get(2).toString().trim()); // resulting in "----- <NodeName>"
        assertEquals("----- engine:bravoNode (BravoNode)",        taskList.get(3).toString().trim()); // We just do it to avoid attracting
        assertEquals("engine:bravoNode (BravoNode)",   taskList.get(4).toString().trim()); // too much attention to it.
        assertEquals("----- engine:charlieNode (CharlieNode)",      taskList.get(5).toString().trim());
        assertEquals("engine:charlieNode (CharlieNode)", taskList.get(6).toString().trim());
        assertEquals("SetName: bar",       taskList.get(7).toString().trim());
    }

    @Test
    public void testReducePersistingStateChanges() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        Node alphaNode = new AlphaNode();
        alphaNode.setUri(new SimpleUri("engine:alphaNode"));
        orderedNodes.add(alphaNode);
        Node bravoNode = new BravoNode();
        bravoNode.setUri(new SimpleUri("engine:bravoNode"));
        orderedNodes.add(bravoNode);
        Node charlieNode = new CharlieNode();
        charlieNode.setUri(new SimpleUri("engine:charlieNode"));
        orderedNodes.add(charlieNode);
        Node deltaNode = new DeltaNode();
        deltaNode.setUri(new SimpleUri("engine:deltaNode"));
        orderedNodes.add(deltaNode);

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("----- engine:alphaNode (AlphaNode)",        taskList.get(0).toString().trim());
        assertEquals("SetName: foo",       taskList.get(1).toString().trim());
        assertEquals("engine:alphaNode (AlphaNode)",   taskList.get(2).toString().trim());
        assertEquals("----- engine:bravoNode (BravoNode)",        taskList.get(3).toString().trim());
        assertEquals("engine:bravoNode (BravoNode)",   taskList.get(4).toString().trim());
        assertEquals("----- engine:charlieNode (CharlieNode)",      taskList.get(5).toString().trim());
        assertEquals("engine:charlieNode (CharlieNode)", taskList.get(6).toString().trim());
        assertEquals("----- engine:deltaNode (DeltaNode)",        taskList.get(7).toString().trim());
        assertEquals("SetName: delta",     taskList.get(8).toString().trim());
        assertEquals("engine:deltaNode (DeltaNode)",   taskList.get(9).toString().trim());
        assertEquals("SetName: bar",       taskList.get(10).toString().trim());
    }

    @Test
    public void testReducePersistingStateChangesEcho() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        Node alphaNode = new AlphaNode();
        alphaNode.setUri(new SimpleUri("engine:alphaNode"));
        orderedNodes.add(alphaNode);
        Node bravoNode = new BravoNode();
        bravoNode.setUri(new SimpleUri("engine:bravoNode"));
        orderedNodes.add(bravoNode);
        Node echoNode = new EchoNode();
        echoNode.setUri(new SimpleUri("engine:echoNode"));
        orderedNodes.add(echoNode);
        Node charlieNode = new CharlieNode();
        charlieNode.setUri(new SimpleUri("engine:charlieNode"));
        orderedNodes.add(charlieNode);
        Node deltaNode = new DeltaNode();
        deltaNode.setUri(new SimpleUri("engine:deltaNode"));
        orderedNodes.add(deltaNode);

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("----- engine:alphaNode (AlphaNode)",        taskList.get(0).toString().trim());
        assertEquals("SetName: foo",       taskList.get(1).toString().trim());
        assertEquals("engine:alphaNode (AlphaNode)",   taskList.get(2).toString().trim());
        assertEquals("----- engine:bravoNode (BravoNode)",        taskList.get(3).toString().trim());
        assertEquals("engine:bravoNode (BravoNode)",   taskList.get(4).toString().trim());
        assertEquals("----- engine:echoNode (EchoNode)",         taskList.get(5).toString().trim());
        assertEquals("SetName: bar",       taskList.get(6).toString().trim());
        assertEquals("engine:echoNode (EchoNode)",    taskList.get(7).toString().trim());
        assertEquals("----- engine:charlieNode (CharlieNode)",      taskList.get(8).toString().trim());
        assertEquals("SetName: foo",       taskList.get(9).toString().trim());
        assertEquals("engine:charlieNode (CharlieNode)", taskList.get(10).toString().trim());
        assertEquals("----- engine:deltaNode (DeltaNode)",        taskList.get(11).toString().trim());
        assertEquals("SetName: delta",     taskList.get(12).toString().trim());
        assertEquals("engine:deltaNode (DeltaNode)",   taskList.get(13).toString().trim());
        assertEquals("SetName: bar",   taskList.get(14).toString().trim());
    }

    @SuppressWarnings("static-access") // actual node classes are not meant to be static
    private class AlphaNode extends AbstractNode {
        AlphaNode() {
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() { }
    }

    @SuppressWarnings("static-access") // actual node classes are not meant to be static
    private class BravoNode extends AbstractNode {
        BravoNode() {
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() { }
    }

    @SuppressWarnings("static-access") // actual node classes are not meant to be static
    private class CharlieNode extends AbstractNode {
        CharlieNode() {
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() { }
    }

    @SuppressWarnings("static-access") // actual node classes are not meant to be static
    private class DeltaNode extends AbstractNode {
        DeltaNode() {
            addDesiredStateChange(new SetName("delta"));
        }

        @Override
        public void process() { }
    }

    @SuppressWarnings("static-access") // actual node classes are not meant to be static
    private class EchoNode extends AbstractNode {
        EchoNode() { }

        @Override
        public void process() { }
    }

    // TODO: Add new tests with varying state changes
}
