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
import com.google.common.collect.Sets;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.dag.gsoc.DependencyConnection;
import org.terasology.rendering.dag.gsoc.FboConnection;
import org.terasology.rendering.dag.gsoc.NewNode;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RenderTaskListGeneratorTest {

    @Test
    public void testSimpleReducePersistingStateChanges() {

        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<NewNode> orderedNodes = Lists.newArrayList();
        NewNode alphaNode = new AlphaNode("alphaNode");
        orderedNodes.add(alphaNode);
        NewNode bravoNode = new BravoNode("bravoNode");
        orderedNodes.add(bravoNode);
        NewNode charlieNode = new CharlieNode("charlieNode");
        orderedNodes.add(charlieNode);

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("SetName: foo",       taskList.get(0).toString().trim()); // trimming MarkerTask.toString(),
        assertEquals("test:alphaNode (AlphaNode)",   taskList.get(1).toString().trim()); // resulting in "----- <NodeName>"
        assertEquals("test:bravoNode (BravoNode)",   taskList.get(2).toString().trim()); // too much attention to it.
        assertEquals("test:charlieNode (CharlieNode)", taskList.get(3).toString().trim());
        assertEquals("SetName: bar",       taskList.get(4).toString().trim());
    }

    @Test
    public void testReducePersistingStateChanges() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<NewNode> orderedNodes = Lists.newArrayList();
        NewNode alphaNode = new AlphaNode("alphaNode");
        orderedNodes.add(alphaNode);
        NewNode bravoNode = new BravoNode("bravoNode");
        orderedNodes.add(bravoNode);
        NewNode charlieNode = new CharlieNode("charlieNode");
        orderedNodes.add(charlieNode);
        NewNode deltaNode = new DeltaNode("deltaNode");
        orderedNodes.add(deltaNode);

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("SetName: foo",       taskList.get(0).toString().trim());
        assertEquals("test:alphaNode (AlphaNode)",   taskList.get(1).toString().trim());
        assertEquals("test:bravoNode (BravoNode)",   taskList.get(2).toString().trim());
        assertEquals("test:charlieNode (CharlieNode)", taskList.get(3).toString().trim());
        assertEquals("SetName: bar",     taskList.get(4).toString().trim());
        assertEquals("test:deltaNode (DeltaNode)",   taskList.get(5).toString().trim());
        assertEquals("SetName: bar",       taskList.get(6).toString().trim());
    }

    @Test
    public void testReducePersistingStateChangesEcho() {
        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<NewNode> orderedNodes = Lists.newArrayList();
        NewNode alphaNode = new AlphaNode("alphaNode");
        orderedNodes.add(alphaNode);
        NewNode bravoNode = new BravoNode("bravoNode");
        orderedNodes.add(bravoNode);
        NewNode echoNode = new EchoNode("echoNode");
        orderedNodes.add(echoNode);
        NewNode charlieNode = new CharlieNode("charlieNode");
        orderedNodes.add(charlieNode);
        NewNode deltaNode = new DeltaNode("deltaNode");
        orderedNodes.add(deltaNode);

        List<RenderPipelineTask> taskList = renderTaskListGenerator.generateFrom(orderedNodes);

        assertEquals("SetName: foo",       taskList.get(0).toString().trim());
        assertEquals("test:alphaNode (AlphaNode)",   taskList.get(1).toString().trim());
        assertEquals("test:bravoNode (BravoNode)",   taskList.get(2).toString().trim());
        assertEquals("SetName: bar",       taskList.get(3).toString().trim());
        assertEquals("test:echoNode (EchoNode)",    taskList.get(4).toString().trim());
        assertEquals("SetName: foo",       taskList.get(5).toString().trim());
        assertEquals("test:charlieNode (CharlieNode)", taskList.get(6).toString().trim());
        assertEquals("SetName: bar",     taskList.get(7).toString().trim());
        assertEquals("test:deltaNode (DeltaNode)",   taskList.get(8).toString().trim());
        assertEquals("SetName: bar",   taskList.get(9).toString().trim());
    }

    private abstract class DummyNode implements NewNode {
        private SimpleUri nodeUri;
        private Set<StateChange> desiredStateChanges = Sets.newLinkedHashSet();
        private boolean enabled;

        DummyNode(String nodeUri) {
            this.nodeUri = new SimpleUri("test:" + nodeUri);
            enabled = true;
        }

        @Override
        public SimpleUri getUri() {
            return nodeUri;
        }

        void addDesiredStateChange(StateChange stateChange) {
            desiredStateChanges.add(stateChange);
        }

        @Override
        public Set<StateChange> getDesiredStateChanges() {
            return desiredStateChanges;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void handleCommand(String command, String... arguments) { }

        @Override
        public void dispose() { }

        @Override
        public String toString() {
            return String.format("%s (%s)", getUri(), this.getClass().getSimpleName());
        }

        public void connect(int inputFboId, DependencyConnection from) {
            // TODO: null checks everywhere
           // addInputFboConnection(inputFboId, from);
        }

        public FboConnection getOutputFboConnection(int outputFboId) {
            return null; // (FboConnection) getOutputConnection(FboConnection.getConnectionName(outputFboId));
        }

        public FboConnection getInputFboConnection(int inputFboId) {
            return null; // (FboConnection) getInputFboConnection(FboConnection.getConnectionName(inputFboId));
        }
    }

    private class AlphaNode extends DummyNode {
        AlphaNode(String nodeUri) {
            super(nodeUri);
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() { }

        @Override
        public void setDependencies(Context context) {

        }
    }

    private class BravoNode extends DummyNode {
        BravoNode(String nodeUri) {
            super(nodeUri);
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() { }

        @Override
        public void setDependencies(Context context) {

        }
    }

    private class CharlieNode extends DummyNode {
        CharlieNode(String nodeUri) {
            super(nodeUri);
            addDesiredStateChange(new SetName("foo"));
        }

        @Override
        public void process() { }

        @Override
        public void setDependencies(Context context) {

        }
    }

    private class DeltaNode extends DummyNode {
        DeltaNode(String nodeUri) {
            super(nodeUri);
            addDesiredStateChange(new SetName("bar"));
        }

        @Override
        public void process() { }

        @Override
        public void setDependencies(Context context) {

        }
    }

    private class EchoNode extends DummyNode {
        EchoNode(String nodeUri) {
            super(nodeUri);
        }

        @Override
        public void process() { }

        @Override
        public void setDependencies(Context context) {

        }
    }

    // TODO: Add new tests with varying state changes
}
