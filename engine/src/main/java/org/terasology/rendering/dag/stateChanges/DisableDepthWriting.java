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
import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.glDepthMask;

/**
 * Instances of this class disable writing to the depth buffer.
 *
 * This can be useful when rendering semi-transparent objects, as the meaning of the depth value of a fragment
 * associated with a semi-transparent object is ambiguous and therefore has to be chosen arbitrarily:
 * should it be the object's distance from the near plane or should it be the first thing behind it?
 */
public final class DisableDepthWriting implements StateChange {
    private static StateChange defaultInstance = new DisableDepthWriting(true);

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
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DisableDepthWriting) && this.enabled == ((DisableDepthWriting) obj).enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    private String getStatus() {
        return enabled? "Enabled": "Disabled";
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), getStatus());
    }

    @Override
    public void process() {
        glDepthMask(enabled);
    }
}
