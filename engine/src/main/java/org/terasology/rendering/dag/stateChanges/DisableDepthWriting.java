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
package org.terasology.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetDepthMaskTask;

/**
 * Instances of this class disable writing to the depth buffer.
 *
 * This can be useful when rendering semi-transparent objects, as the meaning of the depth value of a fragment
 * associated with a semi-transparent object is ambiguous and therefore has to be chosen arbitrarily:
 * should it be the object's distance from the near plane or should it be the first thing behind it?
 */
public final class DisableDepthWriting implements StateChange {
    private static StateChange defaultInstance = new DisableDepthWriting(true);
    private static RenderPipelineTask enablingTask;
    private static RenderPipelineTask disablingTask;

    private final boolean enabled;

    /** Constructs an instance of this StateChange. This can then be used in a node's initialise() method in the form:
     *
     * addDesiredStateChange(new DisableDepthWriting());
     *
     * This triggers the inclusion of a SetDepthMaskTask(false) instance and a SetDepthMaskTask(true) instance
     * in the rendering task list, each instance disabling/enabling writing to the depth buffer respectively. The
     * two task instances frame the execution of a node's process() method unless they are deemed redundant,
     * i.e. because the upstream or downstream node also disables depth buffer writing.
     */
    public DisableDepthWriting() {
        this(false);
    }

    private DisableDepthWriting(boolean enabled) {
        this.enabled = enabled;
        enablingTask = new SetDepthMaskTask(true);
        disablingTask = new SetDepthMaskTask(false);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (enabled) {
            return enablingTask;
        } else {
            return disablingTask;
        }
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this.equals(defaultInstance);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DisableDepthWriting) && this.enabled == ((DisableDepthWriting) obj).isEnabled();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStatus() {
        String status = "disabled";
        if (enabled) {
            status = "enabled";
        }

        return status;
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), getStatus());
    }

}
