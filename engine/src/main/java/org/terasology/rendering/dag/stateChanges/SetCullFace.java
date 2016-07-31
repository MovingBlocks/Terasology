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

import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetCullFaceTask;

/**
 * TODO: Add javadocs
 */
public class SetCullFace implements StateChange {

    /**
     * CullFace mode defined as an integer value in lwjgl, however in OpenGL documentation mode can be only three
     * symbolic constants, therefore Mode enumeration ensures just these values are given to this state change, apart
     * from having an integer with many different values.
     */
    public enum Mode {
        GL_BACK,
        GL_FRONT,
        GL_FRONT_AND_BACK
    }

    private static SetCullFace defaultInstance = new SetCullFace(Mode.GL_BACK); // also specified in OpenGL documentation
    private SetCullFaceTask task;
    private Mode mode;

    public SetCullFace(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetCullFaceTask(mode);
        }

        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof SetCullFace) {
            return mode == ((SetCullFace) stateChange).getMode();
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return defaultInstance == this;
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), mode.name());
    }
}
