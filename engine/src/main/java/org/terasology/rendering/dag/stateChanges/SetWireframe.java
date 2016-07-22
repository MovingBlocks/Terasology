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


import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_LINE;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetWireframeTask;

/**
 * TODO: Add javadocs
 */
public final class SetWireframe implements StateChange {
    public static final int ENABLED = GL_LINE;
    public static final int DISABLED = GL_FILL;

    private static SetWireframe defaultInstance = new SetWireframe(false);
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
            return new SetWireframeTask(ENABLED);
        } else {
            return new SetWireframeTask(DISABLED);
        }
    }

    @Override
    public boolean compare(StateChange stateChange) {
        if (stateChange instanceof SetWireframe) {
            return this.enabled == ((SetWireframe) stateChange).enabled;
        }

        return false;
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        String status = "disabled";
        if (enabled) {
            status = "enabled";
        }

        return String.format("%s: wireframe %s", this.getClass().getSimpleName(), status);
    }
}
