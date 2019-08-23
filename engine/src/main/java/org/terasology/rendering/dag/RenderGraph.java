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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.dag.gsoc.BufferPairConnection;
import org.terasology.rendering.dag.gsoc.DependencyConnection;
import org.terasology.rendering.dag.gsoc.FboConnection;
import org.terasology.rendering.dag.gsoc.NewNode;
import org.terasology.rendering.dag.gsoc.NewAbstractNode;

/**
 * TODO: Add javadocs
 */
public class RenderGraph {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraph.class);

    private Map<SimpleUri, NewNode> nodeMap;
    private Map<Name, NewNode> akaNodeMap;
    private MutableGraph<NewNode> graph;
    private Context context;
    private ShaderManager shaderManager;

    public RenderGraph(Context context) {
        nodeMap = Maps.newHashMap();
        akaNodeMap = Maps.newHashMap();
        graph = GraphBuilder.directed().build();
        this.context = context;
        this.shaderManager = context.get(ShaderManager.class);
    }

    public void addNode(NewNode node) {
        Preconditions.checkNotNull(node, "node cannot be null!");

        SimpleUri nodeUri = node.getUri();
        Name nodeAka = node.getAka();
        // TODO how bout aka
        if (nodeMap.containsKey(nodeUri)) {
            throw new RuntimeException("A node with Uri " + nodeUri + " already exists!");
        }
        if (akaNodeMap.containsKey(nodeAka)) {
            NewNode aNode = akaNodeMap.get(nodeAka);
            logger.info("Node " + nodeUri + " also known as" + nodeAka + " already matches existing node with uri " + aNode.getUri() + " - attempting replacing...");
            replaceNode(aNode, node);
        } else {
            nodeMap.put(nodeUri, node);
            akaNodeMap.put(nodeAka, node);
            graph.addNode(node);
        }

        // TODO this must be moved to a later stage ideally with the improved connecting of nodes which would be based on
        // TODO on dep connections. So far when creating dep. connections, connections are made and destroyed, which is wrong.
        // if (node instanceof NewAbstractNode) {
        //    node.setDependencies(context);
        // }
    }

    public void replaceNode(NewNode aNode, NewNode byNode) {
        // Add connections from a node being replaced; This should in theory be sufficient and once we call setDep over
        // all new nodes again, everything should be set
         aNode.getInputConnections().forEach((k,v)->byNode.getInputConnections().replace(k,v));
         aNode.getOutputConnections().forEach((k,v)->byNode.getOutputConnections().replace(k,v));

        nodeMap.replace(aNode.getUri(), byNode);
        akaNodeMap.replace(aNode.getAka(), byNode);

        graph.removeNode(aNode);
        graph.addNode(byNode);
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
        akaNodeMap.remove(node.getAka());
        return nodeMap.remove(nodeUri);
    }

    public NewNode findNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        return nodeMap.get(nodeUri);
    }

    public NewNode findAka(Name nodeUriAka) {
        Preconditions.checkNotNull(nodeUriAka, "nodeUriAka cannot be null!");
        NewNode node = akaNodeMap.get(nodeUriAka);
        // TODO search all nodes for substrings in akas or both
        return node;
    }

    public NewNode findNode(String simpleUri) {
        return findNode(new SimpleUri(simpleUri));
    }

    public NewNode findAka(String simpleUriAka) {
        return findAka(new Name(simpleUriAka));
    }

    public void postConnectAll() {
        // for each node in the graph
        for(NewNode fromNode : nodeMap.values()) {
            // for each of node's output connection
            for (DependencyConnection outputConnection : fromNode.getOutputConnections().values()) {
                // get connections other ends (multiple, relationship 1 to N) and their parent nodes as toNode and call connect
                for (Object connectedConnection: outputConnection.getConnectedConnections().values()) {
                    NewNode toNode = findNode(((DependencyConnection)connectedConnection).getParentNode());
                    if (fromNode != toNode) {
                        connect(fromNode, toNode);
                    }
                }
            }
        }
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

        // Connect all nodes based on dependencies
        postConnectAll();

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

        topologicalList.forEach((key)->key.postInit(context));

        return topologicalList;
    }

    public void dispose() {
        for (NewNode node : nodeMap.values()) {
            graph.removeNode(node);
            node.dispose();
        }
        nodeMap.clear();
        akaNodeMap.clear();
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
        if (!fromConnection.getConnectedConnections().isEmpty()) {
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
            // DependencyConnection localConnectionConnectedTo = localConnection.getConnectedConnections();
            // if our input is connected
            if (!localConnection.getConnectedConnections().isEmpty()) {
                localConnection.connectInputToOutput(fromConnection);
            } else {
                throw new RuntimeException(" Could not connect node " + toNode + ", inputConnection with id " + inputFboId
                        + " already connected to " + localConnection.getConnectedConnections());
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
//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
        logger.info("Connected " + fromNode.getOutputFboConnection(outputId) + " to " + toNode + ".");
    }

    public void reconnectFbo(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        // for each output connection connected to input get it's connected inputs, find us and remove ourselves from its list
        DependencyConnection connectionToReconnect = toNode.getInputFboConnection(inputId);
        // for each output connected to toNode's input fbo connection(inputId) (should be just one)
        if (connectionToReconnect != null) {
            connectionToReconnect.getConnectedConnections().forEach((conUri, outputConnection) -> {
                ((DependencyConnection) outputConnection).getConnectedConnections().remove(connectionToReconnect.getName());
            });
            // connectionToReconnect gets removed and then created again
            toNode.removeFboConnection(inputId, DependencyConnection.Type.INPUT);
        }
        connectFbo(fromNode, outputId, toNode, inputId);
    }

    /**
     *
     * @param inputConnectionId Input BufferPairConnection id is a number of the input connection on this node.
     *                   Chosen arbitrarily, integers starting by 1 typically.
     * @param fromConnection BufferPairConnection obtained form another node's output.
     */ // TODO merge with connectFbo()
    private void connectBufferPair(NewNode toNode, int inputConnectionId, DependencyConnection fromConnection) {
        // Is not yet connected?
        if (!fromConnection.getConnectedConnections().isEmpty()) {
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
            // DependencyConnection localConnectionConnectedTo = localConnection.getConnectedConnections();
            // if our input is connected
            if (!localConnection.getConnectedConnections().isEmpty()) {
                if (!localConnection.getConnectedConnections().containsKey(fromConnection)) {
                    localConnection.connectInputToOutput(fromConnection);
                } else {
                    throw new RuntimeException(" Could not connect node " + toNode + ", inputConnection with id " + inputConnectionId
                            + " already connected to " + localConnection.getConnectedConnections());
                }
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

//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
    }

    public void reconnectRunOrder(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        DependencyConnection connectionToReconnect = toNode.getInputRunOrderConnection(inputId);
        // for each output connected to toNode's input fbo connection(inputId) (should be just one)
        if (connectionToReconnect != null) {
            connectionToReconnect.getConnectedConnections().forEach((conUri, outputConnection) -> {
                ((DependencyConnection) outputConnection).getConnectedConnections().remove(connectionToReconnect.getName());
            });
            // connectionToReconnect gets removed and then created again
            toNode.removeRunOrderConnection(inputId, DependencyConnection.Type.INPUT);
        }
        connectRunOrder(fromNode, outputId, toNode, inputId);
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
//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
        logger.info("Connected " + fromNode.getOutputBufferPairConnection(outputId) + " to " + toNode + ".");
    }

    /**
     * Remove previous input connection and connect a new input connection to previous output.
     * @param fromNode
     * @param outputId
     * @param toNode
     * @param inputId

    /*public void reconnectInputBufferPairToOutput(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        // for each output connection connected to input get it's connected inputs, find us and remove ourselves from its list
        DependencyConnection connectionToReconnect = toNode.getInputBufferPairConnection(inputId);
        // for each output connected to toNode's input fbo connection(inputId) (should be just one)
        if (connectionToReconnect != null) {
            connectionToReconnect.getConnectedConnections().forEach((conUri, outputConnection) -> {
                ((DependencyConnection) outputConnection).getConnectedConnections().remove(connectionToReconnect.getName());
            });
            // connectionToReconnect gets removed and then created again
            toNode.removeBufferPairConnection(inputId, DependencyConnection.Type.INPUT);
        }
        connectBufferPair(fromNode, outputId, toNode, inputId);
    }*/


    public enum ConnectionType {
        FBO,
        BUFFER_PAIR,
        RUN_ORDER,
        SHADER
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
    public void reconnectInputToOutput(String fromNodeUri, int outputId, String toNodeUri, int inputId, boolean disconnectPrevious) {
        NewNode toNode = findNode(toNodeUri);
        if (toNode == null) {
            toNode = findAka(toNodeUri);
            if (toNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + toNodeUri + "'"));
            }
        }

        NewNode fromNode = findNode(fromNodeUri);
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, ConnectionType.FBO, disconnectPrevious);
    }

    public void reconnectInputFboToOutput(String fromNodeUri, int outputId, NewNode toNode, int inputId, boolean disconnectPrevious) {
        NewNode fromNode = findNode(new SimpleUri(fromNodeUri));
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }

        reconnectInputToOutput(fromNodeUri, outputId, toNode, inputId, ConnectionType.FBO, disconnectPrevious);

        /*if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }*/
    }

    public void reconnectInputBufferPairToOutput(NewNode fromNode, int outputId, NewNode toNode, int inputId) {
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, ConnectionType.BUFFER_PAIR, true);
        /*if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }*/
    }

    public void reconnectInputBufferPairToOutput(String fromNodeUri, int outputId, NewNode toNode, int inputId, boolean disconnectPrevious) {
        NewNode fromNode = findNode(new SimpleUri(fromNodeUri));
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }

        reconnectInputToOutput(fromNodeUri, outputId, toNode, inputId, ConnectionType.BUFFER_PAIR, disconnectPrevious);
    }

    public void reconnectInputToOutput(String fromNodeUri, int outputId, NewNode toNode, int inputId, ConnectionType connectionType, boolean disconnectPrevious) {
        NewNode fromNode = findNode(new SimpleUri(fromNodeUri));
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, connectionType, disconnectPrevious);
    }

    /**
     * Reconnects dependencies only. BufferPairConnection or FboConnection types supported so far.
     * @param inputId
     * @param fromConnection
     */// TODO make it reconnectInputFboToOutput
    private void reconnectInputToOutput(NewNode toNode, int inputId, DependencyConnection fromConnection, ConnectionType connectionType, boolean disconnectPrevious) {
        logger.info("Attempting reconnection of " + toNode.getUri() + " to " + fromConnection.getParentNode() + "'s output.");
        NewNode fromNode;

        if (fromConnection != null) {
            fromNode = findNode(fromConnection.getParentNode());
            if (!fromConnection.getConnectedConnections().isEmpty()) {
                logger.warn("WARNING: destination connection (" + fromConnection + ") is already connected to ("
                        + fromConnection.getConnectedConnections());
                // TODO update the hashmap to string to be pretty
                // throw new RuntimeException("Could not reconnect, destination connection (" + fromConnection + ") is already connected to ("
                // + fromConnection.getConnectedConnections() + "). Remove connection first.");
            } // TODO                                   make it getInputConnection
        } else {
            throw new RuntimeException("Source connection null. Cannot reconnect node " + toNode + "'s input id " + inputId + ".\n");
        }
        DependencyConnection connectionToReconnect;

        switch (connectionType) {
            case FBO:
                connectionToReconnect = toNode.getInputFboConnection(inputId);
                break;
            case BUFFER_PAIR:
                connectionToReconnect = toNode.getInputBufferPairConnection(inputId);
                break;
            default:
                logger.error("Unknown type of connection: ");
                throw new RuntimeException("Unknown type of connection: ");
        }

        // If this connection exists
        if (connectionToReconnect != null) {

            // if this is connected to something
            if (!connectionToReconnect.getConnectedConnections().isEmpty()) {

                // Save previous input connection source node to check whether if it's still depending on it after reconnect
                // should work like this, an input connection should have only one connected connection
                DependencyConnection previousFromConnection = (DependencyConnection)connectionToReconnect.getConnectedConnections().values().iterator().next();
                NewNode previousFromNode = findNode((previousFromConnection).getParentNode());

                connectionToReconnect.getConnectedConnections().clear();

                if (previousFromNode == null) {
                    throw new RuntimeException("Node uri " + previousFromNode + " not found in renderGraph.");
                }

                // Sets data and change toNode's connectedConnection to fromConnection. Sets previous fromConnection's connected node to null.
                connectionToReconnect.connectInputToOutput(fromConnection);
                // if not dependent on inputSourceConnection anymore, remove dag connection

                if (!toNode.isDependentOn(previousFromNode) && disconnectPrevious) {
                    disconnect(previousFromNode, toNode);
                    //DISCONNECT in output connected connections
                    previousFromConnection.getConnectedConnections().remove(connectionToReconnect.getName());
                }
                // setDependencies(this.context); - needed here? probably not..either do this after everything is set up, or in renderGraph.addNode
                // and when calling these trough api, call resetDesiredStateChanges();
            } else {
                logger.info(toNode + "'s connection " + connectionToReconnect + " was not connected. Attempting new connection...");
                this.connectFbo(toNode, inputId, fromConnection);
            }
        } else { //                               TODO make it connectionToReconnect
            String connectionName;
            switch (connectionType) {
                case FBO:
                    connectionName = FboConnection.getConnectionName(inputId, toNode.getUri());
                    break;
                case BUFFER_PAIR:
                    connectionName = BufferPairConnection.getConnectionName(inputId, toNode.getUri());
                    break;
                default:
                    connectionName = "[unsupported connection type]";
            }

            logger.info("No such input connection named " + connectionName + ". Attempting new connection...");

            switch (connectionType) {
                case FBO:
                    this.connectFbo(toNode, inputId, fromConnection);
                    break;
                case BUFFER_PAIR:
                    this.connectBufferPair(toNode, inputId, fromConnection);
                    break;
                default:
                    logger.error("Unknown type of output connection: ");
                    throw new RuntimeException("Unknown type of output connection: ");
            }
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

    @Deprecated
    public void disconnectOutputFboConnection(NewNode node, int connectionId) {
        logger.info("Attempting disconnection of " + node + "'s output fbo number " + connectionId + "..");

        if (node != null) {
            DependencyConnection outputConnection = node.getOutputFboConnection(connectionId);
            if (outputConnection != null) {
                outputConnection.disconnect();
                logger.info("..disconnecting complete.");
            } else {
                logger.warn("Could not find output Fbo connection number " + connectionId + "within " + node + ".");
            }
        } else {
            throw new RuntimeException("Could not find node named " + node + " within renderGraph.");
        }
        //TODO disconnect from rendergraph if needed
    }

    @Deprecated
    public void disconnectOutputFboConnection(String nodeUri, int connectionId) {
        NewNode node = findNode(new SimpleUri(nodeUri));
        disconnectOutputFboConnection(node, connectionId);
    }

    public void disconnectInputFbo(String nodeUri, int connectionId) {
        logger.info("Attempting disconnection of " + nodeUri + "'s input fbo number " + connectionId);
        NewNode node = findNode(new SimpleUri(nodeUri));
        if (node != null) {
            ((NewAbstractNode) node).disconnectInputFbo(connectionId);
        } else {
            throw new RuntimeException("Could not find node named " + nodeUri + " within renderGraph.");
        }
        //TODO disconnect from rendergraph if needed
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
    public void reconnectInputToOutput(NewNode fromNode, int outputId, NewNode toNode, int inputId, ConnectionType connectionType, boolean disconnectPrevious) {
        // Would use of Preconditions be clearer?
        if (toNode == null || fromNode == null) {
            throw new RuntimeException("Reconnecting dependency failed. One of the nodes not found in the renderGraph."
                    + "\n toNode: " + toNode + ". fromNode: " + fromNode);
        }
        DependencyConnection fromConnection;
        switch (connectionType) {
            case FBO:
                fromConnection = fromNode.getOutputFboConnection(outputId);
                break;
            case BUFFER_PAIR:
                fromConnection = fromNode.getOutputBufferPairConnection(outputId);
                break;
            default:
                logger.error("Unknown type of output connection: ");
                throw new RuntimeException("Unknown type of output connection: ");
        }

        if (fromConnection == null) {
            throw new RuntimeException("Reconnecting dependency failed. Could not find output connection.");
        }
        // TODO REMOVE RENDERGRAPH CONNECTION if needed

        reconnectInputToOutput(toNode, inputId, fromConnection, connectionType, disconnectPrevious);

//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
    }

    /**
     * Insert node's connection after a specific node's connection and reconnect the other previously connected nodes's connections
     * to the new one. Simple approach, maintain structure 1 to N. For M to N output do manually.
     */
    public void reconnectAllConnectedInputsTo(DependencyConnection connectionToReplace, DependencyConnection newOutputConnection) {
        NewNode fromNode = findNode(connectionToReplace.getParentNode());
        NewNode newFromNode = findNode(newOutputConnection.getParentNode());
        ConnectionType connectionType;

        if (newOutputConnection instanceof FboConnection) {
            connectionType = ConnectionType.FBO;
        } else if (newOutputConnection instanceof BufferPairConnection) {
            connectionType = ConnectionType.BUFFER_PAIR;
        } else {
            logger.error("Unknown connection type: " + newOutputConnection + " .\n");
            throw new RuntimeException("Unknown connection type: " + newOutputConnection + " .\n");
        }

        if (!connectionToReplace.getConnectedConnections().isEmpty()) {
            // Hard to deal with concurency problems, iterate copy, edit original
            final HashMap<String, DependencyConnection> connectedConnections = connectionToReplace.getConnectedConnections();
            final HashMap<String, DependencyConnection> connectedConnectionsCopy = Maps.newHashMap(connectionToReplace.getConnectedConnections());
            for (DependencyConnection connectedConnectionCopy : connectedConnectionsCopy.values()) {
                DependencyConnection toConnection = connectedConnections.get(connectedConnectionCopy.getName());
                if (!toConnection.getParentNode().equals(fromNode.getUri())) {
                    // TODO potteintionally harmful ID guesswork
                    reconnectInputToOutput(findNode(toConnection.getParentNode()), DependencyConnection.getIdFromConnectionName(toConnection.getName()), newOutputConnection, connectionType, true);
                    NewNode toNode = findNode(toConnection.getParentNode());
//                    if (!areConnected(fromNode, toNode)) {
//                        connect(fromNode, toNode);
//                    }
                }
            }
        }
    }

   /* public void reconnectAllConnectedInputsTo(DependencyConnection connectionToReplace, DependencyConnection newOutputConnection) {
        NewNode fromNode = findNode(connectionToReplace.getParentNode());
        NewNode newFromNode = findNode(newOutputConnection.getParentNode());
        connectionToReplace.getConnectedConnections().forEach(
                (k, connectedConnection)-> {
                    DependencyConnection toConnection = (DependencyConnection) connectedConnection;
                    if (!(toConnection.getParentNode().equals(newOutputConnection.getParentNode()))) {
                        toConnection.disconnect();

                        toConnection.connectInputToOutput(newOutputConnection);

                        NewNode toNode = findNode(toConnection.getParentNode());
                        if (areConnected(fromNode, toNode)) {
                            disconnect(fromNode, toNode);
                        }
                        connect(newFromNode, toNode);
                    }
                }
        );
    }*/
}
