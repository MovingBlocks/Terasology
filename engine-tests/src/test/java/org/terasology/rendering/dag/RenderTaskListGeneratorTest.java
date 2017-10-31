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

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RenderTaskListGeneratorTest {

    @Test
    public void testSimpleReducePersistingStateChanges() {

        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        orderedNodes.add(new AlphaNode());
        orderedNodes.add(new BravoNode());
        orderedNodes.add(new CharlieNode());

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("----- AlphaNode",        taskList.get(0).toString().trim()); // Strictly speaking we don't need
        assertEquals("SetName: foo",       taskList.get(1).toString().trim()); // trimming MarkerTask.toString(),
        assertEquals("AlphaNode",   taskList.get(2).toString().trim()); // resulting in "----- <NodeName>"
        assertEquals("----- BravoNode",        taskList.get(3).toString().trim()); // We just do it to avoid attracting
        assertEquals("BravoNode",   taskList.get(4).toString().trim()); // too much attention to it.
        assertEquals("----- CharlieNode",      taskList.get(5).toString().trim());
        assertEquals("CharlieNode", taskList.get(6).toString().trim());
        assertEquals("SetName: bar",       taskList.get(7).toString().trim());
    }

    @Test
    public void testReducePersistingStateChanges() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        orderedNodes.add(new AlphaNode());
        orderedNodes.add(new BravoNode());
        orderedNodes.add(new CharlieNode());
        orderedNodes.add(new DeltaNode());

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("----- AlphaNode",        taskList.get(0).toString().trim());
        assertEquals("SetName: foo",       taskList.get(1).toString().trim());
        assertEquals("AlphaNode",   taskList.get(2).toString().trim());
        assertEquals("----- BravoNode",        taskList.get(3).toString().trim());
        assertEquals("BravoNode",   taskList.get(4).toString().trim());
        assertEquals("----- CharlieNode",      taskList.get(5).toString().trim());
        assertEquals("CharlieNode", taskList.get(6).toString().trim());
        assertEquals("----- DeltaNode",        taskList.get(7).toString().trim());
        assertEquals("SetName: delta",     taskList.get(8).toString().trim());
        assertEquals("DeltaNode",   taskList.get(9).toString().trim());
        assertEquals("SetName: bar",       taskList.get(10).toString().trim());
    }

    @Test
    public void testReducePersistingStateChangesEcho() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        orderedNodes.add(new AlphaNode());
        orderedNodes.add(new BravoNode());
        orderedNodes.add(new EchoNode());
        orderedNodes.add(new CharlieNode());
        orderedNodes.add(new DeltaNode());

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("----- AlphaNode",        taskList.get(0).toString().trim());
        assertEquals("SetName: foo",       taskList.get(1).toString().trim());
        assertEquals("AlphaNode",   taskList.get(2).toString().trim());
        assertEquals("----- BravoNode",        taskList.get(3).toString().trim());
        assertEquals("BravoNode",   taskList.get(4).toString().trim());
        assertEquals("----- EchoNode",         taskList.get(5).toString().trim());
        assertEquals("SetName: bar",       taskList.get(6).toString().trim());
        assertEquals("EchoNode",    taskList.get(7).toString().trim());
        assertEquals("----- CharlieNode",      taskList.get(8).toString().trim());
        assertEquals("SetName: foo",       taskList.get(9).toString().trim());
        assertEquals("CharlieNode", taskList.get(10).toString().trim());
        assertEquals("----- DeltaNode",        taskList.get(11).toString().trim());
        assertEquals("SetName: delta",     taskList.get(12).toString().trim());
        assertEquals("DeltaNode",   taskList.get(13).toString().trim());
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
