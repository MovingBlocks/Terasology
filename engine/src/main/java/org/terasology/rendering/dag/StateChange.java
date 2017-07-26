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
package org.terasology.rendering.dag;

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
