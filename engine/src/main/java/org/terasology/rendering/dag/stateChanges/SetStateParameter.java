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

/**
 * TODO: Add javadocs
 * Indented for capabilities that are enabled/disabled via glEnable and glDisable.
 */
abstract class SetStateParameter implements StateChange {
    private boolean enabled;

    SetStateParameter(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SetStateParameter) {
            return this.enabled == ((SetStateParameter) obj).isEnabled();
        }
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (enabled) {
            return getEnablingTask();
        } else {
            return getDisablingTask();
        }
    }

    protected abstract RenderPipelineTask getDisablingTask();

    protected abstract RenderPipelineTask getEnablingTask();

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        String status = "disabled";
        if (enabled) {
            status = "enabled";
        }

        return String.format(": capability %s", status);
    }
}
