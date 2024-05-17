// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.dag.dependencyConnections.BufferPairConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.DependencyConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.FboConnection;
import org.terasology.gestalt.naming.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add javadocs
 */
public class RenderGraph {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraph.class);

    private Map<SimpleUri, Node> nodeMap;
    private Map<Name, Node> akaNodeMap;
    private MutableGraph<Node> graph;
    private Context context;
    private ShaderManager shaderManager;

    public RenderGraph(Context context) {
        nodeMap = Maps.newHashMap();
        akaNodeMap = Maps.newHashMap();
        graph = GraphBuilder.directed().build();
        this.context = context;
        this.shaderManager = context.get(ShaderManager.class);
    }

    public void addNode(Node node) {
        Preconditions.checkNotNull(node, "node cannot be null!");

        SimpleUri nodeUri = node.getUri();
        Name nodeAka = node.getAka();
        // TODO how bout aka
        if (nodeMap.containsKey(nodeUri)) {
            throw new RuntimeException("A node with Uri " + nodeUri + " already exists!");
        }
        if (akaNodeMap.containsKey(nodeAka)) {
            Node aNode = akaNodeMap.get(nodeAka);
            logger.atInfo().log("Node {} also known as {} already matches existing node with uri {} - attempting replacing...",
                    nodeUri, nodeAka, aNode.getUri());
            replaceNode(aNode, node);
        } else {
            nodeMap.put(nodeUri, node);
            akaNodeMap.put(nodeAka, node);
            graph.addNode(node);
        }

        // TODO this must be moved to a later stage ideally with the improved connecting of nodes which would be based on
        // TODO on dep connections. So far when creating dep. connections, connections are made and destroyed, which is wrong.
        // if (node instanceof AbstractNode) {
        //    node.setDependencies(context);
        // }
    }

    public void replaceNode(Node aNode, Node byNode) {
        // Add connections from a node being replaced; This should in theory be sufficient and once we call setDep over
        // all new nodes again, everything should be set
         aNode.getInputConnections().forEach((k, v)-> byNode.getInputConnections().replace(k, v));
         aNode.getOutputConnections().forEach((k, v)-> byNode.getOutputConnections().replace(k, v));

        nodeMap.replace(aNode.getUri(), byNode);
        akaNodeMap.replace(aNode.getAka(), byNode);

        graph.removeNode(aNode);
        graph.addNode(byNode);
    }

    public Node removeNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        Node node = findNode(nodeUri);
        if (node == null) {
            throw new RuntimeException("Node removal failure: there is no '" + nodeUri + "' in the render graph!");
        }

        if (graph.adjacentNodes(node).size() != 0) {
            throw new RuntimeException("Node removal failure: node '" + nodeUri
                    + "' is still connected to other nodes in the render graph!");
        }

        nodeMap.remove(nodeUri);
        akaNodeMap.remove(node.getAka());
        return nodeMap.remove(nodeUri);
    }

    public Node findNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        return nodeMap.get(nodeUri);
    }

    public Node findAka(Name nodeUriAka) {
        Preconditions.checkNotNull(nodeUriAka, "nodeUriAka cannot be null!");
        Node node = akaNodeMap.get(nodeUriAka);
        // TODO search all nodes for substrings in akas or both
        return node;
    }

    public Node findNode(String simpleUri) {
        return findNode(new SimpleUri(simpleUri));
    }

    public Node findAka(String simpleUriAka) {
        return findAka(new Name(simpleUriAka));
    }

    private void postConnectAll() {
        // for each node in the graph
        for (Node fromNode : nodeMap.values()) {
            // for each of node's output connection
            for (DependencyConnection outputConnection : fromNode.getOutputConnections().values()) {
                // get connections other ends (multiple, relationship 1 to N) and their parent nodes as toNode and call connect
                for (Object connectedConnection: outputConnection.getConnectedConnections().values()) {
                    Node toNode = findNode(((DependencyConnection) connectedConnection).getParentNode());
                    if (fromNode != toNode) {
                        connect(fromNode, toNode);
                    }
                }
            }
        }
    }

    private void connect(Node... nodeList) {
        Preconditions.checkArgument(nodeList.length > 1,
                "Expected at least 2 nodes as arguments to connect() - found " + nodeList.length);

        Node fromNode = null;

        for (Node toNode : nodeList) {
            Preconditions.checkNotNull(toNode, "toNode cannot be null!");

            if (fromNode != null) {
                if (!graph.hasEdgeConnecting(fromNode, toNode)) {
                    graph.putEdge(fromNode, toNode);
                } else {
                    logger.warn("Trying to connect two already connected nodes, {} and {}", fromNode.getUri(), toNode.getUri()); //NOPMD
                }
            }

            fromNode = toNode;
        }
    }

    public boolean areConnected(Node fromNode, Node toNode) {
        Preconditions.checkNotNull(fromNode, "fromNode cannot be null!");
        Preconditions.checkNotNull(toNode, "toNode cannot be null!");

        return graph.hasEdgeConnecting(fromNode, toNode);
    }

    public void disconnect(Node fromNode, Node toNode) {
        Preconditions.checkNotNull(fromNode, "fromNode cannot be null!");
        Preconditions.checkNotNull(toNode, "toNode cannot be null!");

        if (!graph.hasEdgeConnecting(fromNode, toNode)) {
            logger.warn("Trying to disconnect two nodes that aren't connected, {} and {}", fromNode.getUri(), toNode.getUri()); //NOPMD
        }

        graph.removeEdge(fromNode, toNode);
    }

    // TODO: Add `boolean isFullyFunctional(Node node)`

    // TODO: Add handler methods which the graph uses to communicate changes to a node.

    public List<Node> getNodesInTopologicalOrder() {
        // This implementation of Kahn's Algorithm is adapted from the algorithm described at
        // https://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/

        List<Node> topologicalList = new ArrayList<>();

        // Connect all nodes based on dependencies
        postConnectAll();

        // In-degree (or incoming-degree) is the number of incoming edges of a particular node.
        Map<Node, Integer> inDegreeMap = Maps.newHashMap();
        List<Node> nodesToExamine = Lists.newArrayList();
        int visitedNodes = 0;

        // Calculate the in-degree for each node, and mark all nodes with no incoming edges for examination.
        for (Node node : graph.nodes()) {
            int inDegree = graph.inDegree(node);
            inDegreeMap.put(node, inDegree);

            if (inDegree == 0) {
                nodesToExamine.add(node);
            }
        }

        while (!nodesToExamine.isEmpty()) {
            Node currentNode = nodesToExamine.remove(0);

            for (Node adjacentNode : graph.successors(currentNode)) {
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

        topologicalList.forEach((key)-> key.postInit(context));

        return topologicalList;
    }

    public void dispose() {
        for (Node node : nodeMap.values()) {
            graph.removeNode(node);
            node.dispose();
        }
        nodeMap.clear();
        akaNodeMap.clear();
    }

    public void resetDesiredStateChanges(Node node) {
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
    private void connectFbo(Node toNode, int inputFboId, DependencyConnection fromConnection) {
        // TODO this will have to be caught by a try-catch or redone if we were going use gui to tamper with dag
        // Is not yet connected?
        if (!fromConnection.getConnectedConnections().isEmpty()) {
            logger.warn("Warning, {} connection is already read somewhere else.", fromConnection);
        }
        // If adding new input goes smoothly
        // TODO These checks might be redundant
        if (toNode.addInputConnection(inputFboId, fromConnection)) {
            DependencyConnection localConnection = toNode.getInputFboConnection(inputFboId);
            // Upon successful insertion - save connected connection. If node is already connected, throw an exception.
            localConnection.connectInputToOutput(fromConnection);

        } else { // if adding new input failed, it already existed - check for connections
            //TODO update
            logger.atInfo().log("{}.connectFbo({}, {}): Connection already existed. Testing for its connections..",
                    toNode.getUri(), inputFboId, fromConnection.getName());
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
    public void connectFbo(Node fromNode, int outputId, Node toNode, int inputId) {
        // TODO for buffer pairs enable new instance with swapped buffers
        connectFbo(toNode, inputId, fromNode.getOutputFboConnection(outputId));
//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
        logger.atDebug().log("Connected {} to {}.", fromNode.getOutputFboConnection(outputId), toNode);
    }

    public void reconnectFbo(Node fromNode, int outputId, Node toNode, int inputId) {
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
    private void connectBufferPair(Node toNode, int inputConnectionId, DependencyConnection fromConnection) {
        // Is not yet connected?
        if (!fromConnection.getConnectedConnections().isEmpty()) {
            logger.info("Warning, {} connection is already read somewhere else.", fromConnection);
        }
        // If adding new input goes smoothly
        // TODO These checks might be redundant
        if (toNode.addInputConnection(inputConnectionId, fromConnection)) {
            DependencyConnection localConnection = toNode.getInputBufferPairConnection(inputConnectionId);
            // Upon successful insertion - save connected connection. If node is already connected, throw an exception.
            localConnection.connectInputToOutput(fromConnection);

        } else { // if adding new input failed, it already existed - check for connections
            //TODO update
            logger.atInfo().log("{}.connectFbo({}, {}): Connection already existed. Testing for its connections..",
                    toNode.getUri(), inputConnectionId, fromConnection.getName());
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


    public void connectRunOrder(Node fromNode, int outputId, Node toNode, int inputId) {
        if (fromNode == null || toNode == null) {
            throw new RuntimeException("Node cannot be null.");
        }
        if (fromNode.addOutputRunOrderConnection(outputId)) {
            if (!toNode.addInputRunOrderConnection(fromNode.getOutputRunOrderConnection(outputId), inputId)) {
                throw new RuntimeException("Could not add input RunOrder" + inputId + " connection to " + toNode
                        + ". Connection probably already exists.");
            }
        } else {
            throw new RuntimeException("Could not add output RunOrder" + outputId + " connection to " + fromNode
                    + ". Connection probably already exists.");
        }

//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
    }

    public void reconnectRunOrder(Node fromNode, int outputId, Node toNode, int inputId) {
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
    public void connectBufferPair(Node fromNode, int outputId, Node toNode, int inputId) {
        // TODO for buffer pairs enable new instance with swapped buffers
        connectBufferPair(toNode, inputId, fromNode.getOutputBufferPairConnection(outputId));
//        if (!areConnected(fromNode, toNode)) {
//            connect(fromNode, toNode);
//        }
        logger.atDebug().log("Connected {} to {}.", fromNode.getOutputBufferPairConnection(outputId), toNode);
    }

    /**
     * Remove previous input connection and connect a new input connection to previous output.
     */
    /*public void reconnectInputBufferPairToOutput(Node fromNode, int outputId, Node toNode, int inputId) {
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
        Node toNode = findNode(toNodeUri);
        if (toNode == null) {
            toNode = findAka(toNodeUri);
            if (toNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + toNodeUri + "'"));
            }
        }

        Node fromNode = findNode(fromNodeUri);
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, ConnectionType.FBO, disconnectPrevious);
    }

    public void reconnectInputFboToOutput(String fromNodeUri, int outputId, Node toNode, int inputId,
                                          boolean disconnectPrevious) {
        Node fromNode = findNode(new SimpleUri(fromNodeUri));
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

    public void reconnectInputFboToOutput(Node fromNode, int outputId, Node toNode, int inputId) {
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, ConnectionType.FBO, true);
    }

    public void reconnectInputBufferPairToOutput(Node fromNode, int outputId, Node toNode, int inputId) {
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, ConnectionType.BUFFER_PAIR, true);
        /*if (!areConnected(fromNode, toNode)) {
            connect(fromNode, toNode);
        }*/
    }

    public void reconnectInputBufferPairToOutput(String fromNodeUri, int outputId, Node toNode, int inputId,
                                                 boolean disconnectPrevious) {
        Node fromNode = findNode(new SimpleUri(fromNodeUri));
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }

        reconnectInputToOutput(fromNodeUri, outputId, toNode, inputId, ConnectionType.BUFFER_PAIR, disconnectPrevious);
    }

    public void reconnectInputToOutput(String fromNodeUri, int outputId, Node toNode, int inputId,
                                       ConnectionType connectionType, boolean disconnectPrevious) {
        Node fromNode = findNode(new SimpleUri(fromNodeUri));
        if (fromNode == null) {
            fromNode = findAka(fromNodeUri);
            if (fromNode == null) {
                throw new RuntimeException(("No node is associated with URI '" + fromNodeUri + "'"));
            }
        }
        reconnectInputToOutput(fromNode, outputId, toNode, inputId, connectionType, disconnectPrevious);
    }

    /**
     * API for reconnecting input dependency to another output.
     *
     * Attempts to connectFbo or reconnect toNode's input (inputId)
     * to fromNode's (fromNodeUri) output (outputId).
     * @param toNode toNode's SimpleUri name. Node must exist in the renderGraph.
     * @param inputId Id of toNode's input. Input does NOT have to exist beforehand.
     * @param fromConnection Id of fromNode's output connection. Connection must exist.
     * @param connectionType {@link ConnectionType} saying whether the dependency is Fbo, BufferPair or something else
     * @param disconnectPrevious  TO DEPRECATE; Whether to disconnect previous connection. This should now be always true.
     */
    private void reconnectInputToOutput(Node toNode, int inputId, DependencyConnection fromConnection,
                                        ConnectionType connectionType, boolean disconnectPrevious) {
        logger.debug("Attempting reconnection of {} to {}'s output.", toNode.getUri(), fromConnection.getParentNode()); //NOPMD
        Node fromNode;

        fromNode = findNode(fromConnection.getParentNode());
        if (!fromConnection.getConnectedConnections().isEmpty()) {
            logger.atWarn().log("WARNING: destination connection ({}) is already connected to ({})",
                    fromConnection, fromConnection.getConnectedConnections());
            // TODO update the hashmap to string to be pretty
            // throw new RuntimeException("Could not reconnect, destination connection (" + fromConnection + ") is already connected to ("
            // + fromConnection.getConnectedConnections() + "). Remove connection first.");
        } // TODO                                   make it getInputConnection

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
                DependencyConnection previousFromConnection =
                        (DependencyConnection) connectionToReconnect.getConnectedConnections().values().iterator().next();
                Node previousFromNode = findNode((previousFromConnection).getParentNode());

                connectionToReconnect.getConnectedConnections().clear();

                if (previousFromNode == null) {
                    throw new RuntimeException("Node uri " + previousFromNode + " not found in renderGraph.");
                }

                // Sets data and change toNode's connectedConnection to fromConnection.
                // Sets previous fromConnection's connected node to null.
                connectionToReconnect.connectInputToOutput(fromConnection);
                // if not dependent on inputSourceConnection anymore, remove dag connection

                if (!toNode.isDependentOn(previousFromNode) && disconnectPrevious) {
                    disconnect(previousFromNode, toNode);
                    //DISCONNECT in output connected connections
                    previousFromConnection.getConnectedConnections().remove(connectionToReconnect.getName());
                }
                // setDependencies(this.context); - needed here? probably not..
                // either do this after everything is set up, or in renderGraph.addNode
                // and when calling these trough api, call resetDesiredStateChanges();
            } else {
                logger.info("{}'s connection {} was not connected. Attempting new connection...", toNode, connectionToReconnect);
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

            logger.info("No such input connection named {}. Attempting new connection...", connectionName);

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
        logger.debug("Reconnecting finished."); // TODO return errors...connectFbo-true false
    }

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNodeUri Output node's Uri (already added to graph)
     * @param outputId Number/id of output
     */
    public void connectFbo(String fromNodeUri, int outputId, Node toNode, int inputId) {
        Node fromNode = findNode(new SimpleUri(fromNodeUri));
        connectFbo(toNode, inputId, fromNode, outputId);
    }

    @Deprecated
    public void disconnectOutputFboConnection(Node node, int connectionId) {
        logger.debug("Attempting disconnection of {}'s output fbo number {}..", node, connectionId);

        if (node != null) {
            DependencyConnection outputConnection = node.getOutputFboConnection(connectionId);
            if (outputConnection != null) {
                outputConnection.disconnect();
                logger.debug("..disconnecting complete.");
            } else {
                logger.warn("Could not find output Fbo connection number {} within {}.", connectionId, node);
            }
        } else {
            throw new RuntimeException("Could not find node named " + node + " within renderGraph.");
        }
        //TODO disconnect from rendergraph if needed
    }

    @Deprecated
    public void disconnectOutputFboConnection(String nodeUri, int connectionId) {
        Node node = findNode(new SimpleUri(nodeUri));
        disconnectOutputFboConnection(node, connectionId);
    }

    public void disconnectInputFbo(String nodeUri, int connectionId) {
        logger.debug("Attempting disconnection of {}'s input fbo number {}.", nodeUri, connectionId);
        Node node = findNode(new SimpleUri(nodeUri));
        if (node != null) {
            ((AbstractNode) node).disconnectInputFbo(connectionId);
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
    public void reconnectInputToOutput(Node fromNode, int outputId, Node toNode, int inputId,
                                       ConnectionType connectionType, boolean disconnectPrevious) {
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
        Node fromNode = findNode(connectionToReplace.getParentNode());
        ConnectionType connectionType;

        if (newOutputConnection instanceof FboConnection) {
            connectionType = ConnectionType.FBO;
        } else if (newOutputConnection instanceof BufferPairConnection) {
            connectionType = ConnectionType.BUFFER_PAIR;
        } else {
            logger.error("Unknown connection type: {} .\n", newOutputConnection);
            throw new RuntimeException("Unknown connection type: " + newOutputConnection + " .\n");
        }

        if (!connectionToReplace.getConnectedConnections().isEmpty()) {
            // Hard to deal with concurency problems, iterate copy, edit original
            final Map<String, DependencyConnection> connectedConnections = connectionToReplace.getConnectedConnections();
            final Map<String, DependencyConnection> connectedConnectionsCopy = Maps.newHashMap(connectionToReplace.getConnectedConnections());
            for (DependencyConnection connectedConnectionCopy : connectedConnectionsCopy.values()) {
                DependencyConnection toConnection = connectedConnections.get(connectedConnectionCopy.getName());
                if (!toConnection.getParentNode().equals(fromNode.getUri())) {
                    // TODO potteintionally harmful ID guesswork
                    reconnectInputToOutput(findNode(toConnection.getParentNode()),
                            DependencyConnection.getIdFromConnectionName(toConnection.getName()),
                            newOutputConnection, connectionType, true);
//                    Node toNode = findNode(toConnection.getParentNode());
//                    if (!areConnected(fromNode, toNode)) {
//                        connect(fromNode, toNode);
//                    }
                }
            }
        }
    }

   /* public void reconnectAllConnectedInputsTo(DependencyConnection connectionToReplace, DependencyConnection newOutputConnection) {
        Node fromNode = findNode(connectionToReplace.getParentNode());
        Node newFromNode = findNode(newOutputConnection.getParentNode());
        connectionToReplace.getConnectedConnections().forEach(
                (k, connectedConnection)-> {
                    DependencyConnection toConnection = (DependencyConnection) connectedConnection;
                    if (!(toConnection.getParentNode().equals(newOutputConnection.getParentNode()))) {
                        toConnection.disconnect();

                        toConnection.connectInputToOutput(newOutputConnection);

                        Node toNode = findNode(toConnection.getParentNode());
                        if (areConnected(fromNode, toNode)) {
                            disconnect(fromNode, toNode);
                        }
                        connect(newFromNode, toNode);
                    }
                }
        );
    }*/
}
