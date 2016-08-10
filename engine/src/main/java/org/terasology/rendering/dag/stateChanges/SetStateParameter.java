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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

/**
 * TODO: Add javadocs
 * Indented for capabilities that are enabled/disabled via glEnable and glDisable.
 */
public abstract class SetStateParameter implements StateChange {
    private static Map<Integer, String> paramaterMap = ImmutableMap.of(GL11.GL_BLEND, "GL_BLEND",
            GL11.GL_DEPTH_TEST, "GL_DEPTH_TEST",
            GL11.GL_STENCIL_TEST, "GL_STENCIL_TEST",
            GL11.GL_CULL_FACE, "GL_CULL_FACE");
    private boolean enabled;

    SetStateParameter(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof SetStateParameter) {
            return this.enabled == ((SetStateParameter) stateChange).isEnabled();
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

    public static String getParameterName(int capability) {
        return paramaterMap.get(capability);
    }
}
