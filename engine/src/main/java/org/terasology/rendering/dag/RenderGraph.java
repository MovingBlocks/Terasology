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

        if (node instanceof NewAbstractNode) {
            node.setDependencies(context);
        }

        node.setRenderGraph(this);

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

        node.setRenderGraph(null);

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

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNode Output node
     * @param outputId Number/id of output
     */
    public void connectFbo(NewNode toNode, int inputId, NewNode fromNode, int outputId) {
        toNode.connectFbo(inputId, fromNode.getOutputFboConnection(outputId));
        if (!areConnected(toNode, fromNode)) {
            connect(toNode, fromNode);
        }
    }

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNodeUri Output node's Uri (already added to graph)
     * @param outputId Number/id of output
     */
    public void connectFbo(NewNode toNode, int inputId, String fromNodeUri, int outputId) {
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

        toNode.reconnectInputFboToOutput(inputId, fromNode, fromConnection);

        if (!areConnected(toNode, fromNode)) {
            connect(fromNode, toNode);
        }
    }

}
