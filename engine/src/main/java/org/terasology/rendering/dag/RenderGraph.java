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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.dag.gsoc.DependencyConnection;
import org.terasology.rendering.dag.gsoc.FboConnection;
import org.terasology.rendering.dag.gsoc.NewAbstractNode;
import org.terasology.rendering.dag.gsoc.NewNode;

/**
 * TODO: Add javadocs
 */
public class RenderGraph {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraph.class);

    private Map<SimpleUri, NewNode> nodeMap;
    private MutableGraph<NewNode> graph;
    private Context context;
    private ShaderManager shaderManager;

    public RenderGraph(Context context) {
        nodeMap = Maps.newHashMap();
        graph = GraphBuilder.directed().build();
        this.context = context;
        this.shaderManager = context.get(ShaderManager.class);
    }

    public void addNode(NewNode node) {
        Preconditions.checkNotNull(node, "node cannot be null!");

        SimpleUri nodeUri = node.getUri();
        if (nodeMap.containsKey(nodeUri)) {
            throw new RuntimeException("A node with uri " + nodeUri + " already exists!");
        }

        // TODO this must be moved to a later stage ideally with the improved connecting of nodes which would be based on
        // TODO on dep connections. So far when creating dep. connections, connections are made and destroyed, which is wrong.
        if (node instanceof NewAbstractNode) {
            node.setDependencies(context);
        }

        nodeMap.put(nodeUri, node);
        graph.addNode(node);
    }

    public NewNode removeNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        NewNode node = findNode(nodeUri);
        if (node == null) {
            throw new RuntimeException("NewNode removal failure: there is no '" + nodeUri + "' in the render graph!");
        }

        if (graph.adjacentNodes(node).size() != 0) {
            throw new RuntimeException("NewNode removal failure: node '" + nodeUri + "' is still connected to other nodes in the render graph!");
        }

        nodeMap.remove(nodeUri);
        return nodeMap.remove(nodeUri);
    }

    public NewNode findNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        return nodeMap.get(nodeUri);
    }

    public NewNode findNode(String simpleUri) {
        return findNode(new SimpleUri(simpleUri));
    }

    public void connect(NewNode... nodeList) {
        Preconditions.checkArgument(nodeList.length > 1, "Expected at least 2 nodes as arguments to connect() - found " + nodeList.length);

        NewNode fromNode = null;

        for (NewNode toNode : nodeList) {
            Preconditions.checkNotNull(toNode, "toNode cannot be null!");

            if (fromNode != null) {
                if (!graph.hasEdgeConnecting(fromNode, toNode)) {
                    graph.putEdge(fromNode, toNode);
                } else {
                    logger.warn("Trying to connect two already connected nodes, " + fromNode.getUri() + " and " + toNode.getUri());
                }
            }

            fromNode = toNode;
        }
    }

    public boolean areConnected(NewNode fromNode, NewNode toNode) {
        Preconditions.checkNotNull(fromNode, "fromNode cannot be null!");
        Preconditions.checkNotNull(toNode, "toNode cannot be null!");

        return graph.hasEdgeConnecting(fromNode, toNode);
    }

    public void disconnect(NewNode fromNode, NewNode toNode) {
        Preconditions.checkNotNull(fromNode, "fromNode cannot be null!");
        Preconditions.checkNotNull(toNode, "toNode cannot be null!");

        if (!graph.hasEdgeConnecting(fromNode, toNode)) {
            logger.warn("Trying to disconnect two nodes that aren't connected, " + fromNode.getUri() + " and " + toNode.getUri());
        }

        graph.removeEdge(fromNode, toNode);
    }

    // TODO: Add `boolean isFullyFunctional(NewNode node)`

    // TODO: Add handler methods which the graph uses to communicate changes to a node.

    public List<NewNode> getNodesInTopologicalOrder() {
        // This implementation of Kahn's Algorithm is adapted from the algorithm described at
        // https://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/

        List<NewNode> topologicalList = new ArrayList<>();

        // In-degree (or incoming-degree) is the number of incoming edges of a particular node.
        Map<NewNode, Integer> inDegreeMap = Maps.newHashMap();
        List<NewNode> nodesToExamine = Lists.newArrayList();
        int visitedNodes = 0;

        // Calculate the in-degree for each node, and mark all nodes with no incoming edges for examination.
        for (NewNode node : graph.nodes()) {
            int inDegree = graph.inDegree(node);
            inDegreeMap.put(node, inDegree);

            if (inDegree == 0) {
                nodesToExamine.add(node);
            }
        }

        while (!nodesToExamine.isEmpty()) {
            NewNode currentNode = nodesToExamine.remove(0);

            for (NewNode adjacentNode : graph.successors(currentNode)) {
                int updatedInDegree = inDegreeMap.get(adjacentNode) - 1;
                inDegreeMap.put(adjacentNode, updatedInDegree);

                if (updatedInDegree == 0) {
                    nodesToExamine.add(adjacentNode);
                }
            }

            topologicalList.add(currentNode);

            visitedNodes++;
        }

        if (visitedNodes != nodeMap.size()) {
            throw new RuntimeException("Cycle detected in the DAG: topological sorting not possible!");
        }

        return topologicalList;
    }

    public void dispose() {
        for (NewNode node : nodeMap.values()) {
            graph.removeNode(node);
            node.dispose();
        }
        nodeMap.clear();
    }

    public void resetDesiredStateChanges(NewNode node) {
        node.resetDesiredStateChanges();
    }

    public void resetDesiredStateChanges(String nodeUri) {
        resetDesiredStateChanges(findNode(new SimpleUri(nodeUri)));
    }

    public void addShader(String title, Name moduleName) {
        shaderManager.addShaderProgram(title, moduleName.toString());
    }

    public int getNodeMapSize() {
        return this.nodeMap.size();
    }

    /**
     *
     * @param inputFboId Input FBO id is a number of the input connection on this node.
     *                   Chosen arbitrarily, integers starting by 1 typically.
     * @param fromConnection FboConnection obtained form another node's output.
     */ // TODO simpleuri
    private void connectFbo(NewNode toNode, int inputFboId, DependencyConnection fromConnection) {
        // TODO this will have to be caught by a try-catch or redone if we were going use gui to tamper with dag
        // Is not yet connected?
        if (fromConnection.getConnectedConnection() != null) {
            logger.info("Warning, " + fromConnection + "connection is already read somewhere else.");
        }
        // If adding new input goes smoothly
        // TODO These checks might be redundant
        if (toNode.addInputConnection(inputFboId, fromConnection)) {
            DependencyConnection localConnection = toNode.getInputFboConnection(inputFboId);
            // Upon successful insertion - save connected connection. If node is already connected, throw an exception.
            localConnection.connectInputToOutput(fromConnection);

        } else { // if adding new input failed, it already existed - check for connections
            //TODO update
            logger.info(toNode.getUri() + ".connectFbo(" + inputFboId + ", " + fromConnection.getName() + "):" +
                    " Connection already existed. Testing for its connections..");
            DependencyConnection localConnection = toNode.getInputFboConnection(inputFboId);
            DependencyConnection localConnectionConnectedTo = localConnection.getConnectedConnection();
            // if our input is not connected
            if (localConnectionConnectedTo == null) {
                localConnection.connectInputToOutput(fromConnection);
            } else {
                throw new RuntimeException(" Could not connect node " + toNode + ", inputConnection with id " + inputFboId
                        + " already connected to " + localConnection.getConnectedConnection());
            }
        }
    }

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNode Output node
     * @param outputId Number/id of output
     */
    public void connectFbo(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        // TODO for buffer pairs enable new instance with swapped buffers
        connectFbo(toNode, inputId, fromNode.getOutputFboConnection(outputId));
        if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }
        logger.info("Connected " + fromNode.getOutputFboConnection(outputId) + " to " + toNode + ".");
    }

    /**
     *
     * @param inputConnectionId Input BufferPairConnection id is a number of the input connection on this node.
     *                   Chosen arbitrarily, integers starting by 1 typically.
     * @param fromConnection BufferPairConnection obtained form another node's output.
     */ // TODO merge with connectFbo()
    private void connectBufferPair(NewNode toNode, int inputConnectionId, DependencyConnection fromConnection) {
        // Is not yet connected?
        if (fromConnection.getConnectedConnection() != null) {
            logger.info("Warning, " + fromConnection + "connection is already read somewhere else.");
        }
        // If adding new input goes smoothly
        // TODO These checks might be redundant
        if (toNode.addInputConnection(inputConnectionId, fromConnection)) {
            DependencyConnection localConnection = toNode.getInputBufferPairConnection(inputConnectionId);
            // Upon successful insertion - save connected connection. If node is already connected, throw an exception.
            localConnection.connectInputToOutput(fromConnection);

        } else { // if adding new input failed, it already existed - check for connections
            //TODO update
            logger.info(toNode.getUri() + ".connectFbo(" + inputConnectionId + ", " + fromConnection.getName() + "):" +
                    " Connection already existed. Testing for its connections..");
            DependencyConnection localConnection = toNode.getInputBufferPairConnection(inputConnectionId);
            DependencyConnection localConnectionConnectedTo = localConnection.getConnectedConnection();
            // if our input is not connected
            if (localConnectionConnectedTo == null) {
                localConnection.connectInputToOutput(fromConnection);
            } else {
                throw new RuntimeException(" Could not connect node " + toNode + ", inputConnection with id " + inputConnectionId
                        + " already connected to " + localConnection.getConnectedConnection());
            }
        }
    }


    public void connectRunOrder(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        if (fromNode == null || toNode == null) {
            throw new RuntimeException("Node cannot be null.");
        }
        if(fromNode.addOutputRunOrderConnection(outputId)) {
            if (!toNode.addInputRunOrderConnection(fromNode.getOutputRunOrderConnection(outputId), inputId)) {
                throw new RuntimeException("Could not add input RunOrder" + inputId + " connection to " + toNode + ". Connection probably already exists.");
            }
        } else {
            throw new RuntimeException("Could not add output RunOrder" + outputId + " connection to " + fromNode + ". Connection probably already exists.");
        }

        if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }
    }

    /**
     * Connect BufferPair output of fromNode to toNode's BufferPair input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNode Output node
     * @param outputId Number/id of output
     */
    public void connectBufferPair(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        // TODO for buffer pairs enable new instance with swapped buffers
        connectBufferPair(toNode, inputId, fromNode.getOutputBufferPairConnection(outputId));
        if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }
        logger.info("Connected " + fromNode.getOutputBufferPairConnection(outputId) + " to " + toNode + ".");
    }

    /**
     * Reconnects dependencies only
     * @param inputId
     * @param fromNode
     * @param fromConnection
     */// TODO make it reconnectInputToOutput
    private void reconnectInputFboToOutput(NewNode toNode, int inputId, NewNode fromNode, DependencyConnection fromConnection) {
        logger.info("Attempting reconnection of " + toNode.getUri() + " to " + fromConnection.getParentNode());
        if (fromConnection.getConnectedConnection() != null) {
            throw new RuntimeException("Could not reconnect, destination connection (" + fromConnection + ") is already connected to ("
                    + fromConnection.getConnectedConnection() + "). Remove connection first.");
        } // TODO                                   make it getInputConnection
        DependencyConnection connectionToReconnect = toNode.getInputFboConnection(inputId);
        // If this connection exists
        if (connectionToReconnect != null) {
            // if this is connected to something
            if (connectionToReconnect.getConnectedConnection() != null) {
                // Save previous input connection source node to check whether we I'm still depending on it after reconnect
                NewNode previousFromNode = findNode(connectionToReconnect.getConnectedConnection().getParentNode());
                if (previousFromNode == null) {
                    throw new RuntimeException("Node uri " + previousFromNode + " not found in renderGraph.");
                }
                // Sets data and change toNode's connectedConnection to fromConnection. Sets previous fromConnection's connected node to null.
                connectionToReconnect.connectInputToOutput(fromConnection);
                // if not dependent on inputSourceConnection anymore, remove dag connection

                if (!toNode.isDependentOn(previousFromNode)) {
                    disconnect(previousFromNode, toNode);
                }
                // setDependencies(this.context); - needed here? probably not..either do this after everything is set up, or in renderGraph.addNode
                // and when calling these trough api, call resetDesiredStateChanges();
            } else {
                logger.info(toNode + "'s connection " + connectionToReconnect + " was not connected. Attempting new connection...");
                this.connectFbo(toNode, inputId, fromConnection);
            }
        } else { //                               TODO make it connectionToReconnect
            logger.info("No such input connection named " + FboConnection.getConnectionName(inputId, toNode.getUri()) + ". Attempting new connection...");
            this.connectFbo(toNode, inputId, fromConnection);
        }
        logger.info("Reconnecting finished."); // TODO return errors...connectFbo-true false
    }

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNodeUri Output node's Uri (already added to graph)
     * @param outputId Number/id of output
     */
    public void connectFbo(String fromNodeUri, int outputId, NewNode toNode, int inputId) {
        NewNode fromNode = findNode(new SimpleUri(fromNodeUri));
        connectFbo(toNode, inputId, fromNode, outputId);
    }

    public void disconnectOutputFbo(String nodeUri, int connectionId) {
        logger.info("Attempting disconnection of " + nodeUri + "'s output fbo number " + connectionId + "..");
        NewNode node = findNode(new SimpleUri(nodeUri));
        if (node != null) {
            DependencyConnection outputConnection = node.getOutputFboConnection(connectionId);
            if (outputConnection != null) {
                outputConnection.disconnect();
                logger.info("..disconnecting complete.");
            } else {
                logger.warn("Could not find output Fbo connection number " + connectionId + "within " + nodeUri + ".");
            }
        } else {
            throw new RuntimeException("Could not find node named " + nodeUri + " within renderGraph.");
        }
        //TODO disconnect from rendergraph if needed
    }

    public void disconnectInputFbo(String nodeUri, int connectionId) {
        logger.info("Attempting disconnection of " + nodeUri + "'s input fbo number " + connectionId);
        NewNode node = findNode(new SimpleUri(nodeUri));
        if (node != null) {
            DependencyConnection inputConnection = node.getInputFboConnection(connectionId);
            if (inputConnection != null) {
                inputConnection.disconnect();
                logger.info("..disconnecting complete.");
            } else {
                logger.warn("Could not find input Fbo connection number " + connectionId + "within " + nodeUri + ".");
            }
        } else {
            throw new RuntimeException("Could not find node named " + nodeUri + " within renderGraph.");
        }
        //TODO disconnect from rendergraph if needed
    }

    // TODO generic type all the way, reusability

    /**
     * API for reconnecting input FBO dependency.
     *
     * Expects 2 existing nodes and an existing output connection on fromNode. Output connection must be created explicitly
     * beforehand or simply exist already.
     *
     * If previous requirements are met, attempts to connectFbo or reconnect toNode's (toNodeUri) input (inputId)
     * to fromNode's (fromNodeUri) output (outputId).
     * @param toNodeUri toNode's SimpleUri name. Node must exist in the renderGraph.
     * @param inputId Id of toNode's input. Input does NOT have to exist beforehand.
     * @param fromNodeUri fromNode's SimpleUri name. Node must exist in the renderGraph.
     * @param outputId Id of fromNode's output. Output must exist.
     */
    public void reconnectInputFboToOutput(String toNodeUri, int inputId, String fromNodeUri, int outputId) {
        NewNode toNode = findNode(new SimpleUri(toNodeUri));
        NewNode fromNode = findNode(new SimpleUri(fromNodeUri));
        reconnectInputFboToOutput(toNode, inputId, fromNode, outputId);
    }

    public void reconnectInputFboToOutput(NewNode toNode, int inputId, String fromNodeUri, int outputId) {
        NewNode fromNode = findNode(new SimpleUri(fromNodeUri));
        reconnectInputFboToOutput(toNode, inputId, fromNode, outputId);
    }

    /**
     * API for reconnecting input FBO dependency.
     *
     * Expects 2 existing nodes and an existing output connection on fromNode. Output connection must be created explicitly
     * beforehand or simply exist already.
     *
     * If previous requirements are met, attempts to connectFbo or reconnect toNode's (toNodeUri) input (inputId)
     * to fromNode's (fromNodeUri) output (outputId).
     * @param toNode toNode's SimpleUri name. Node must exist in the renderGraph.
     * @param inputId Id of toNode's input. Input does NOT have to exist beforehand.
     * @param fromNode fromNode's SimpleUri name. Node must exist in the renderGraph.
     * @param outputId Id of fromNode's output. Output must exist.
     */
    public void reconnectInputFboToOutput(NewNode toNode, int inputId, NewNode fromNode, int outputId) {
        // Would use of Preconditions be clearer?
        if (toNode == null || fromNode == null) {
            throw new RuntimeException("Reconnecting FBO dependency failed. One of the nodes not found in the renderGraph."
                    + "\n toNode: " + toNode + ". fromNode: " + fromNode);
        }
        DependencyConnection fromConnection = fromNode.getOutputFboConnection(outputId);
        if (fromConnection == null) {
            throw new RuntimeException("Reconnecting FBO dependency failed. Could not find output connection.");
        }
        // TODO REMOVE RENDERGRAPH CONNECTION if needed

        reconnectInputFboToOutput(toNode, inputId, fromNode, fromConnection);

        if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }
    }

}
