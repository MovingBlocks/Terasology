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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;
import org.terasology.rendering.dag.gsoc.BufferPairConnection;
import org.terasology.rendering.dag.gsoc.BufferPair;
import org.terasology.rendering.dag.gsoc.DependencyConnection;
import org.terasology.rendering.dag.gsoc.FboConnection;
import org.terasology.rendering.dag.gsoc.NewNode;
import org.terasology.rendering.dag.gsoc.RunOrderConnection;
import org.terasology.rendering.opengl.FBO;

import java.util.List;
import java.util.Map;
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
        private Map<String, DependencyConnection> inputConnections = Maps.newHashMap();
        private Map<String, DependencyConnection> outputConnections = Maps.newHashMap();
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

        public void connectFbo(int inputFboId, DependencyConnection from) {
            // TODO: null checks everywhere
           // addInputFboConnection(inputFboId, from);
        }

        public FboConnection getOutputFboConnection(int outputFboId) {
            return null; // (FboConnection) getOutputConnection(FboConnection.getConnectionName(outputFboId));
        }

        public FboConnection getInputFboConnection(int inputFboId) {
            return null; // (FboConnection) getInputFboConnection(FboConnection.getConnectionName(inputFboId));
        }

        public void reconnectInputFboToOutput(int inputId, NewNode fromNode, DependencyConnection fromConnection) {
            // ???
        }

        public void resetDesiredStateChanges() {

        }

        public void clearDesiredStateChanges() {

        }

        public void setRenderGraph(RenderGraph renderGraph) {

        }

        public boolean isDependentOn(NewNode anotherNode) {
            return false;
        }

        @Override
        public boolean addInputConnection(int id, DependencyConnection connection) {
            return false;
        }

        public boolean addInputBufferPairConnection(int id, BufferPairConnection from) {
            return false;
        }

        public boolean addInputBufferPairConnection(int id, Pair<FBO,FBO> fboPair) {
            return false;
        }

        public BufferPairConnection getOutputBufferPairConnection(int outputBufferPairId) {
            return null;
        }

        public BufferPairConnection getInputBufferPairConnection(int inputBufferPairId) {
            return null;
        }

        public boolean addInputRunOrderConnection(RunOrderConnection from, int inputId) {
            return false;
        }

        public boolean addOutputRunOrderConnection(int outputId) {
            return false;
        }

        public RunOrderConnection getOutputRunOrderConnection(int outputId) {
            return null;
        }

        public RunOrderConnection getInputRunOrderConnection(int inputId) {
            return null;
        }

        public Name getAka() {
            return new Name("test");
        }

        public Map<String, DependencyConnection> getInputConnections() {
            return Maps.newHashMap();
        }

        public Map<String, DependencyConnection> getOutputConnections() {
            return Maps.newHashMap();
        }

        public void setInputConnections(Map<String, DependencyConnection> inputConnections) {

        }

        public void setOutputConnections(Map<String, DependencyConnection> outputConnections) {

        }

        public void postInit(Context context) {
            setDependencies(context);
        }

        public boolean addOutputFboConnection(int id) {
            return false;
        }

        public boolean addOutputBufferPairConnection(int id) {
            return false;
        }

        public boolean addOutputBufferPairConnection(int id, BufferPairConnection bufferPairConnection) {
            return false;
        }

        public boolean addOutputBufferPairConnection(int id, BufferPair bufferPair) {
            return false;
        }

        public void removeFboConnection(int id, DependencyConnection.Type type) {

        }

        public void removeBufferPairConnection(int id, DependencyConnection.Type type) {

        }

        public void removeRunOrderConnection(int id, DependencyConnection.Type type) {

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
