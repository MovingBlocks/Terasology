// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

/**
 * A StateChange instance encapsulate a change in the OpenGL state that is required by one or more consecutive nodes.
 *
 * Any of the concrete classes extending the StateChange interface can be used in a node's initialise() method in the form:
 * addDesiredStateChange(new StateChangeImplementation());
 *
 * Each StateChange instance is included for processing in the task list, alongside a second StateChange called
 * the "default instance" resetting the state back to its OpenGL or Terasology default.
 * These two state change instances effectively become tasks and frame the execution of a node's process() method
 * unless they are deemed redundant by the RenderTaskListGenerator.
 * State changes are deemed redundant when the upstream and/or downstream nodes require the exact same StateChange:
 * no point in sending the same OpenGL directive to the GPU multiple times if the OpenGL state doesn't change as a result.
 */
public interface StateChange extends RenderPipelineTask {
    StateChange getDefaultInstance();

    default boolean isTheDefaultInstance()  {
        return this.equals(getDefaultInstance());
    }
}
