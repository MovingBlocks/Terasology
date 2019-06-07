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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DependencyConnection {

    private String connectionName;
    private Type connectionType;
    private SimpleUri parentNode;
    private SimpleUri connectedNode;

    DependencyConnection(String name, Type type, SimpleUri parentNode) {
        this.connectionName = name;
        this.connectionType = type;
        this.parentNode = parentNode;
        connectedNode = null;
    }

    public enum Type { // Might differentiate by name only
        INPUT,
        OUTPUT
    }

    /**
     * Getter for a SimpleUri name of DependencyConnection.parentNode. This attribute should always contain a name
     * of a node object this node has been attributed to.
     * @return A name of the node this connection has been attributed to. This must not be null.
     */
    @Nonnull
    public SimpleUri getParentNode() {
        return this.parentNode;
    }

    /**
     * Getter for a SimpleUri name of DependencyConnection.connectedNode. The idea is that when connecting a toNode's input to another
     * fromNode's output, you query the OUTPUT connection for its connectedNode.
     * TODO Read TODO for setConnectedNode
     * @return A node name if this Connection has been connected to another node's connection. Null if the output
     * has not yet been connected.
     */
    @Nullable
    public SimpleUri getConnectedNode() {
        return this.connectedNode;
    }

    /**
     * Setter for a SimpleUri name of DependencyConnection.connectedNode. The idea is that when connecting a toNode's input to another
     * fromNode's output, you set the OUTPUT connection's connectedNode to the toNode's SimpleUri name.
     * TODO Whether to set this for input connections which do not origin in it's parent node remains a question.
     * @param connectedNode A SimpleUri name of connected node.
     */
    protected void setConnectedNode(SimpleUri connectedNode) {
        this.connectedNode = connectedNode;
    }

    public String getName() {
        return this.connectionName;
    }

    public Type getType() {
        return this.connectionType;
    }

    @Override
    public String toString() {
        return (this.connectionType == Type.OUTPUT) ?
                String.format("Output:%s([Parent]%s->%s)", this.connectionName, this.parentNode, this.connectedNode) :
                String.format("Input:%s([Parent]%s<-%s)", this.connectionName, this.parentNode, this.connectedNode);
    }

}
