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
        orderedNodes.add(initialiseNode(new AlphaNode()));
        orderedNodes.add(initialiseNode(new BravoNode()));
        orderedNodes.add(initialiseNode(new CharlieNode()));

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals(taskList.get(0).toString(), "SetNameTask: foo");
        assertEquals(taskList.get(1).toString(), "NodeTask: AlphaNode");
        assertEquals(taskList.get(2).toString(), "NodeTask: BravoNode");
        assertEquals(taskList.get(3).toString(), "NodeTask: CharlieNode");
    }

    @Test
    public void testReducePersistingStateChanges() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        orderedNodes.add(initialiseNode(new AlphaNode()));
        orderedNodes.add(initialiseNode(new BravoNode()));
        orderedNodes.add(initialiseNode(new CharlieNode()));
        orderedNodes.add(initialiseNode(new DeltaNode()));

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals(taskList.get(0).toString(), "SetNameTask: foo");
        assertEquals(taskList.get(1).toString(), "NodeTask: AlphaNode");
        assertEquals(taskList.get(2).toString(), "NodeTask: BravoNode");
        assertEquals(taskList.get(3).toString(), "NodeTask: CharlieNode");
        assertEquals(taskList.get(4).toString(), "SetNameTask: delta");
        assertEquals(taskList.get(5).toString(), "NodeTask: DeltaNode");
    }


    @Test
    public void testReducePersistingStateChangesEcho() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        orderedNodes.add(initialiseNode(new AlphaNode()));
        orderedNodes.add(initialiseNode(new BravoNode()));
        orderedNodes.add(initialiseNode(new EchoNode()));
        orderedNodes.add(initialiseNode(new CharlieNode()));
        orderedNodes.add(initialiseNode(new DeltaNode()));

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals(taskList.get(0).toString(), "SetNameTask: foo");
        assertEquals(taskList.get(1).toString(), "NodeTask: AlphaNode");
        assertEquals(taskList.get(2).toString(), "NodeTask: BravoNode");
        assertEquals(taskList.get(3).toString(), "SetNameTask: bar");
        assertEquals(taskList.get(4).toString(), "NodeTask: EchoNode");
        assertEquals(taskList.get(5).toString(), "SetNameTask: foo");
        assertEquals(taskList.get(6).toString(), "NodeTask: CharlieNode");
        assertEquals(taskList.get(7).toString(), "SetNameTask: delta");
        assertEquals(taskList.get(8).toString(), "NodeTask: DeltaNode");

    }

    private Node initialiseNode(Node node) {
        node.initialise();
        return node;
    }



    private class AlphaNode extends AbstractNode {
        @Override
        public void initialise() {
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() {
        }
    }

    private class BravoNode extends AbstractNode {
        @Override
        public void initialise() {
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() {
        }
    }

    private class CharlieNode extends AbstractNode {
        @Override
        public void initialise() {
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() {
        }
    }

    private class DeltaNode extends AbstractNode {
        @Override
        public void initialise() {
            addDesiredStateChange(new SetName("delta"));
        }

        @Override
        public void process() {
        }
    }

    private class EchoNode extends AbstractNode {
        @Override
        public void initialise() {

        }

        @Override
        public void process() {
        }
    }

    // TODO: Add new tests with varying state changes
}
