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

import org.terasology.engine.SimpleUri;
import org.terasology.registry.Share;
import org.terasology.rendering.dag.NewNode;
import org.terasology.rendering.dag.RenderGraph;

@Share(RenderGraphAPI.class)
public class RenderGraphAPI {

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

    }

    public void reconnectInputFboTo(NewNode fromNode, int inputFboId, FboConnection connectionToConnect) {
        FboConnection connectionToReconnect = fromNode.getInputFboConnection(inputFboId);
        // If this connection exists
        if(connectionToReconnect != null) {
            // if is connected to something
            if(connectionToReconnect.getConnectedNode() != null) {
                // reconnect to connectToConnection
                // remove connected node's connection
            } else {

            }
        } else {
            // addInputFboConnection(inputFboId, ); which
            // TODO log warning No such connection for node this.toString(); Add new connection first (read some node's output))
        }
    }

    public void removeNode(SimpleUri nodeUri) {
        // first check dependencies

        // remove node from the graph - is not gonna be run
        renderGraph.removeNode(nodeUri);
    }

    public void diconnectNode(NewNode fromNode, NewNode toNode) {
        renderGraph.disconnect(fromNode,toNode);
        // TODO dependencies
    }

}
