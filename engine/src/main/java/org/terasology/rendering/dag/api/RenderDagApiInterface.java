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

import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;
import org.terasology.rendering.dag.gsoc.NewNode;

public interface RenderDagApiInterface {

    public NewNode findNode(SimpleUri nodeUri);

    public NewNode findNode(String simpleUri);

    /**
     * Deletes all desired state changes for the node and add them all again.
     * Must call after changing dependency connections.
     * This can be called directly from the node if the node is already retrieved.
     * @param node Said node
     */
    public void resetDesiredStateChanges(NewNode node);

    /**
     * Deletes all desired state changes for the node and add them all again.
     * Must call after changing dependency connections.
     * @param nodeUri Said node's uri as String.
     */
    public void resetDesiredStateChanges(String nodeUri);


    public void insertBefore(NewNode nodeBefore, String nodeAfter);

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
    void reconnectInputFboToOutput(String toNodeUri, int inputId, String fromNodeUri, int outputId);

    /**
     * API for reconnecting input FBO dependency.
     *
     * Expects 2 existing nodes and an existing output connection on fromNode. Output connection must be created explicitly
     * beforehand or simply exist already.
     *
     * If previous requirements are met, attempts to connectFbo or reconnect toNode's (toNodeUri) input (inputId)
     * to fromNode's (fromNodeUri) output (outputId).
     * @param toNode Node whose input is being reconnected.
     * @param inputId Id of toNode's input. Input does NOT have to exist beforehand.
     * @param fromNodeUri fromNode's SimpleUri name. Node must exist in the renderGraph.
     * @param outputId Id of fromNode's output. Output must exist.
     */
    void reconnectInputFboToOutput(NewNode toNode, int inputId, String fromNodeUri, int outputId);

    /**
     * API for reconnecting input FBO dependency.
     *
     * Expects 2 existing nodes and an existing output connection on fromNode. Output connection must be created explicitly
     * beforehand or simply exist already.
     *
     * If previous requirements are met, attempts to connectFbo or reconnect toNode's (toNodeUri) input (inputId)
     * to fromNode's (fromNodeUri) output (outputId).
     * @param toNode Node whose input is being reconnected.
     * @param inputId Id of toNode's input. Input does NOT have to exist beforehand.
     * @param fromNode Node whose out is being read.
     * @param outputId Id of fromNode's output. Output must exist.
     */
    void reconnectInputFboToOutput(NewNode toNode, int inputId, NewNode fromNode, int outputId);

    void disconnectOutputFbo(String nodeUri, int connectionId);
    void disconnectInputFbo(String nodeUri, int connectionId);
    void removeNode(String nodeUri);

    void addShader(String title, Name moduleName);

    void addNode(NewNode node);

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNode Output node
     * @param outputId Number/id of output
     */
    void connectFbo(NewNode toNode, int inputId, NewNode fromNode, int outputId);

    /**
     * Connect Fbo output of fromNode to toNode's Fbo input.
     * @param toNode Input node
     * @param inputId Number/id of input
     * @param fromNodeUri Output node's Uri (already added to graph)
     * @param outputId Number/id of output
     */
    void connectFbo(NewNode toNode, int inputId, String fromNodeUri, int outputId);
}
