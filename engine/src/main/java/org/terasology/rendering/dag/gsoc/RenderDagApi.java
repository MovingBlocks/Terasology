/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.gsoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.api.RenderDagApiInterface;

/**
 * <p>
 * Singleton class which is used by modules trough {@link RenderDagApiInterface} obtained from {@link Context}.
 * </p>
 * <p>
 * Implements basic operations over DAG and dependency passing.
 * </p>
 */
public final class RenderDagApi implements RenderDagApiInterface {
    private static final Logger logger = LoggerFactory.getLogger(RenderDagApi.class);

    private static RenderDagApi singleInstance;

    private RenderGraph renderGraph;
    private Context context;
    private ShaderManager shaderManager;

    private RenderDagApi(RenderGraph renderGraph, ShaderManager shaderManager, Context context) {
        this.renderGraph = renderGraph;
        this.context = context;
        this.shaderManager = shaderManager;
    }

    public static RenderDagApi getRenderDagApi(RenderGraph renderGraph, ShaderManager shaderManager, Context context) {
        if (singleInstance == null) {
            singleInstance = new RenderDagApi(renderGraph, shaderManager, context);
        }
        return singleInstance;
    }

    public NewNode findNode(SimpleUri nodeUri) {
        return renderGraph.findNode(nodeUri);
    }

    public NewNode findNode(String nodeUri) {
        return renderGraph.findNode(nodeUri);
    }

    public void insertBefore(NewNode nodeBefore, String nodeAfter) {

    }

    /**
     * Deletes all desired state changes for the node and add them all again.
     * Must call after changing dependency connections.
     * This can be called directly from the node if the node is already retrieved.
     * @param node Said node
     */
    public void resetDesiredStateChanges(NewNode node) {
        node.resetDesiredStateChanges();
    }

    /**
     * Deletes all desired state changes for the node and add them all again.
     * Must call after changing dependency connections.
     * @param nodeUri Said node's uri as String.
     */
    public void resetDesiredStateChanges(String nodeUri) {
        resetDesiredStateChanges(renderGraph.findNode(new SimpleUri(nodeUri)));
    }

    public void addNode(NewNode node) {
        renderGraph.addNode(node);
        // create
        // connectDependencies
        // add to render graph
        // connectFbo in renderGraph (order, to be executed somewhere)
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
        if (!renderGraph.areConnected(toNode, fromNode)) {
            renderGraph.connect(toNode, fromNode);
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
        NewNode fromNode = renderGraph.findNode(new SimpleUri(fromNodeUri));
        connectFbo(toNode, inputId, fromNode, outputId);
    }

    public void disconnectOutputFbo(String nodeUri, int connectionId) {
        logger.info("Attempting disconnection of " + nodeUri + "'s output fbo number " + connectionId + "..");
        NewNode node = renderGraph.findNode(new SimpleUri(nodeUri));
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
        NewNode node = renderGraph.findNode(new SimpleUri(nodeUri));
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
        NewNode toNode = renderGraph.findNode(new SimpleUri(toNodeUri));
        NewNode fromNode = renderGraph.findNode(new SimpleUri(fromNodeUri));
        reconnectInputFboToOutput(toNode, inputId, fromNode, outputId);
    }

    public void reconnectInputFboToOutput(NewNode toNode, int inputId, String fromNodeUri, int outputId) {
        NewNode fromNode = renderGraph.findNode(new SimpleUri(fromNodeUri));
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

        if (!renderGraph.areConnected(toNode, fromNode)) {
            renderGraph.connect(fromNode, toNode);
        }
    }

    public void removeNode(String nodeUri) {
        // first check dependencies ? Won't have to if every dependency call has a check and automatically
        // connects/disconnects nodes

        // remove node from the graph - is not gonna be run - can only do this if node is disconnected
        // in the render graph from all other nodes.
        renderGraph.removeNode(new SimpleUri(nodeUri));
    }

    public void diconnectNode(NewNode fromNode, NewNode toNode) {
        // TODO ADD RENDER GRAPH DISCONNECTION if last dependency between the two
        // renderGraph.disconnect(fromNode, toNode);
        // TODO dependencies
    }

}
