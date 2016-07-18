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
    private static SetWireframe defaultInstance = new SetWireframe(false);
    private boolean enabled;

    private SetWireframe(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (enabled) {
            return new SetWireframeTask(GL_FILL);
        } else {
            return new SetWireframeTask(GL_LINE);
        }
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return this.getClass().getSimpleName() + ": " + enabled;
    }
}
