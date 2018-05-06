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

//TODO: consider removing the word "Node" from the name of all Node implementations now that they are in the dag.nodes package.

import java.util.Set;
import org.terasology.engine.SimpleUri;

/**
 * A node is the processing unit within the Renderer.
 *
 * Individual nodes are added to the RenderGraph and processed by the TaskList generator to provide rendering features,
 * from the rendering of the 3d landscape to 2d post-processing effects such as Depth-of-Field.
 *
 * Classes implementing this interface are meant to be fairly atomic in what they do and, when possible, reusable.
 */
public interface Node extends RenderPipelineTask {
    // TODO: invoked when Node is removed from RenderGraph

    /**
     * Called to dispose the Node and any support object it instantiated.
     */
    void dispose();

    /**
     * Used to obtain the set of StateChange objects representing the state changes desired by the node.
     *
     * To function correctly a node may require an OpenGL state that is different from the one Terasology
     * provides by default. To modify the OpenGL state and restore it after use, a node stores the Set
     * of StateChange objects that is returned by this method.
     *
     * They are "desired" state changes as some might end up redundant and unused: if consecutive nodes
     * A and B desire an identical state change the state change provided by node A is used and automatically
     * persists during the processing of node B - node B's state change is unused.
     *
     * @return a Set of StateChange objects.
     */
    Set<StateChange> getDesiredStateChanges();

    /**
     * Used to inspect whether a node is enabled or not.
     *
     * The RenderGraph contains both enabled and disabled nodes. When it comes the time to generate the
     * list of tasks executed by the renderer each frame, only enabled nodes are considered. Disabled
     * nodes are ignored and their processing method nor their StateChange objects make it into the task list.
     *
     * @return a boolean: True if the node is enabled, False otherwise.
     */
    boolean isEnabled();

    /**
     * Enables or disables the node.
     *
     * Disabled nodes in the RenderGraph are ignored when the Rendering TaskList is generated.
     * See isEnabled() for more details.
     *
     * @param enable a boolean: True to enable the node, false to disable it.
     */
    void setEnabled(boolean enable);

    /**
     * Handles Console commands sent to a node.
     *
     * In the Console, commands have the following syntax:
     *
     *     dagCommandNode <nodeUri> <command> <arguments...>
     *
     *  A node doesn't need to register the commands it can handle: commands must be known to the user by means
     *  of a node's documentation.
     *
     *  Commands are passed to a node whether the node can handle them or not. Implementations of this method
     *  must therefore be capable of handling unknown commands gracefully.
     *
     * @param command a String storing the command name.
     * @param arguments a list of Strings to pass as arguments
     */
    void handleCommand(String command, String... arguments);

    /**
     * Nodes have a unique identifier in the form "namespace:nodeId", i.e. "engine:hazeNode".
     *
     * Node URIs are set during node construction and are immutable.
     *
     * @return a SimpleUri providing the namespace and name of the node.
     */
    SimpleUri getUri();
}
