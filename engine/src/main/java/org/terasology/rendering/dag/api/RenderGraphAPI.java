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
package org.terasology.rendering.dag.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.registry.Share;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.gsoc.DependencyConnection;
import org.terasology.rendering.dag.gsoc.NewNode;

@Share(RenderGraphAPI.class)
public class RenderGraphAPI {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraphAPI.class);

    private static RenderGraphAPI singleInstance = null;

    private RenderGraph renderGraph;

    private RenderGraphAPI(RenderGraph renderGraph) {
        this.renderGraph = renderGraph;
    }

    public static RenderGraphAPI getRenderGraphAPI(RenderGraph renderGraph) {
        if (singleInstance == null) {
            singleInstance = new RenderGraphAPI(renderGraph);
        }
        return singleInstance;
    }

    public void addNode() {
        // create
        // connectDependencies
        // add to render graph
        // connect in renderGraph (order, to be executed somewhere)
    }

    public void disconnectOutputFbo(String nodeUri, int connectionId) {
        logger.info("Attempting disconnection of " + nodeUri + "'s output fbo number " + connectionId);
        NewNode node = renderGraph.findNode(new SimpleUri(nodeUri));
        if (node != null) {
            DependencyConnection outputConnection = node.getOutputFboConnection(connectionId);
            if (outputConnection != null) {
                outputConnection.disconnect();
            } else {
                logger.warn("Could not find output Fbo connection number " + connectionId + "within " + nodeUri + ".");
            }
        } else {
            throw new RuntimeException("Could not find node named " + nodeUri + " within renderGraph.");
        }
    }

    public void disconnectInputFbo(String nodeUri, int connectionId) {
        logger.info("Attempting disconnection of " + nodeUri + "'s input fbo number " + connectionId);
        NewNode node = renderGraph.findNode(new SimpleUri(nodeUri));
        if (node != null) {
            DependencyConnection inputConnection = node.getInputFboConnection(connectionId);
            if (inputConnection != null) {
                inputConnection.disconnect();
            } else {
                logger.warn("Could not find input Fbo connection number " + connectionId + "within " + nodeUri + ".");
            }
        } else {
            throw new RuntimeException("Could not find node named " + nodeUri + " within renderGraph.");
        }
    }

    // TODO generic type all the way, reusability

    /**
     * API for reconnecting input FBO dependency.
     *
     * Expects 2 existing nodes and an existing output connection on fromNode. Output connection must be created explicitly
     * beforehand or simply exist already.
     *
     * If previous requirements are met, attempts to connect or reconnect toNode's (toNodeUri) input (inputId)
     * to fromNode's (fromNodeUri) output (outputId).
     * @param toNodeUri toNode's SimpleUri name. Node must exist in the renderGraph.
     * @param inputId Id of toNode's input. Input does NOT have to exist beforehand.
     * @param fromNodeUri fromNode's SimpleUri name. Node must exist in the renderGraph.
     * @param outputId Id of fromNode's output. Output must exist.
     */
    public void reconnectInputFboToOutput(String toNodeUri, int inputId, String fromNodeUri, int outputId) {
        NewNode toNode = renderGraph.findNode(new SimpleUri(toNodeUri));
        NewNode fromNode = renderGraph.findNode(new SimpleUri(fromNodeUri));
        // Would use of Preconditions be clearer?
        if (toNode == null || fromNode == null) {
            throw new RuntimeException("Reconnecting FBO dependency failed. One of the nodes not found in the renderGraph."
                                        + "\n toNode: " + toNode + ". fromNode: " + fromNode);
        }
        DependencyConnection fromConnection = fromNode.getOutputFboConnection(outputId);
        if (fromConnection == null) {
            throw new RuntimeException("Reconnecting FBO dependency failed. Could not find output connection.");
        }

        toNode.reconnectInputFboToOutput(inputId, fromNode, fromConnection);
        // TODO ADD RENDERGRAPH CONNECTION if needed
    }

    private void reconnectNodeRunOrder(){}

    public void removeNode(SimpleUri nodeUri) {
        // first check dependencies

        // remove node from the graph - is not gonna be run
        renderGraph.removeNode(nodeUri);
    }

    public void diconnectNode(NewNode fromNode, NewNode toNode) {
        // TODO ADD RENDER GRAPH DISCONNECTION if last dependency between the two
        // renderGraph.disconnect(fromNode, toNode);
        // TODO dependencies
    }

}
