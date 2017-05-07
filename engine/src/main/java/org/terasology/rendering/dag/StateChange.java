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
 * A StateChange denotes a change in the OpenGL state, that is required by a particular Node.
 *
 * Any of the concrete classes extending the StateChange interface can be used in a node's initialise() method in the form:
 * addDesiredStateChange(new StateChangeImplementation());
 *
 * This triggers the inclusion of an StateSet and a StateReset instance in the rendering task list.
 * These two task instances frame the execution of a node's process() method unless they are deemed redundant
 * by the RenderTaskListGenerator because the upstream or downstream node also requires the exact same StateChange.
 */
public interface StateChange extends RenderPipelineTask {
    StateChange getDefaultInstance();

    default boolean isTheDefaultInstance()  {
        return this.equals(getDefaultInstance());
    }
}
