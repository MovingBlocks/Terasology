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

import java.util.Objects;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;

/**
 * TODO: Add javadocs
 */
public final class SetWireframe implements StateChange {
    private static SetWireframe defaultInstance = new SetWireframe(false);
    private static SetWireframeTask enablingTask;
    private static SetWireframeTask disablingTask;

    private boolean enabled;

    public SetWireframe(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (enabled) {
            if (enablingTask == null) {
                enablingTask = new SetWireframeTask(true);
            }
            return enablingTask;
        } else {
            if (disablingTask == null) {
                disablingTask = new SetWireframeTask(false);
            }
            return disablingTask;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetWireframe) && this.enabled == ((SetWireframe) obj).isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        String status = "disabled";
        if (enabled) {
            status = "enabled";
        }

        return String.format("%30s: %s", this.getClass().getSimpleName(), status);
    }

    private final class SetWireframeTask implements RenderPipelineTask {
        private static final int ENABLED = GL_LINE;
        private static final int DISABLED = GL_FILL;
        private int mode;

        private SetWireframeTask(boolean enabled) {
            if (enabled) {
                this.mode = ENABLED;
            } else {
                this.mode = DISABLED;
            }
        }

        @Override
        public void execute() {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, mode);
        }

        @Override
        public String toString() {
            String status = "disabled";
            if (mode == ENABLED) {
                status = "enabled";
            }
            return String.format("%30s: %s", this.getClass().getSimpleName(), status);
        }
    }
}
