// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.dag;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.terasology.engine.context.Context;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.dag.Node;
import org.terasology.engine.rendering.dag.RenderGraph;
import org.terasology.engine.rendering.dag.RenderPipelineTask;
import org.terasology.engine.rendering.dag.RenderTaskListGenerator;
import org.terasology.engine.rendering.dag.StateChange;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.rendering.dag.dependencyConnections.BufferPairConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.BufferPair;
import org.terasology.engine.rendering.dag.dependencyConnections.DependencyConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.FboConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.RunOrderConnection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenderTaskListGeneratorTest {

    @Test
    public void testSimpleReducePersistingStateChanges() {

        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        List<Node> orderedNodes = Lists.newArrayList();
        Node alphaNode = new AlphaNode("alphaNode");
        orderedNodes.add(alphaNode);
        Node bravoNode = new BravoNode("bravoNode");
        orderedNodes.add(bravoNode);
        Node charlieNode = new CharlieNode("charlieNode");
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
        List<Node> orderedNodes = Lists.newArrayList();
        Node alphaNode = new AlphaNode("alphaNode");
        orderedNodes.add(alphaNode);
        Node bravoNode = new BravoNode("bravoNode");
        orderedNodes.add(bravoNode);
        Node charlieNode = new CharlieNode("charlieNode");
        orderedNodes.add(charlieNode);
        Node deltaNode = new DeltaNode("deltaNode");
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
        List<Node> orderedNodes = Lists.newArrayList();
        Node alphaNode = new AlphaNode("alphaNode");
        orderedNodes.add(alphaNode);
        Node bravoNode = new BravoNode("bravoNode");
        orderedNodes.add(bravoNode);
        Node echoNode = new EchoNode("echoNode");
        orderedNodes.add(echoNode);
        Node charlieNode = new CharlieNode("charlieNode");
        orderedNodes.add(charlieNode);
        Node deltaNode = new DeltaNode("deltaNode");
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

    private abstract class DummyNode implements Node {
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

        public void reconnectInputFboToOutput(int inputId, Node fromNode, DependencyConnection fromConnection) {
            // ???
        }

        public void resetDesiredStateChanges() {

        }

        public void clearDesiredStateChanges() {

        }

        public void setRenderGraph(RenderGraph renderGraph) {

        }

        public boolean isDependentOn(Node anotherNode) {
            return false;
        }

        @Override
        public boolean addInputConnection(int id, DependencyConnection connection) {
            return false;
        }

        public boolean addInputBufferPairConnection(int id, BufferPairConnection from) {
            return false;
        }

        public boolean addInputBufferPairConnection(int id, BufferPair fboPair) {
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
