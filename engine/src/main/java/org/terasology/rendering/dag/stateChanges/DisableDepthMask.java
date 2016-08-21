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
 * TODO: Add javadocs
 */
public final class DisableDepthMask implements StateChange {
    private static StateChange defaultInstance = new DisableDepthMask(true);
    private static RenderPipelineTask enablingTask;
    private static RenderPipelineTask disablingTask;

    private final boolean enabled;

    public DisableDepthMask() {
        this(false);
    }

    private DisableDepthMask(boolean enabled) {
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
        return this == defaultInstance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DisableDepthMask) {
            return this.enabled == ((DisableDepthMask) obj).isEnabled();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return String.format("%21s", this.getClass().getSimpleName());
    }
}
