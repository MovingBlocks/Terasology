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

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetWhatFacesToCullTask;

/**
 * TODO: Add javadocs
 */
public final class SetFacesToCull implements StateChange {

    private static SetFacesToCull defaultInstance = new SetFacesToCull(GL_BACK); // also specified in OpenGL documentation
    private SetWhatFacesToCullTask task;
    private int mode;

    public SetFacesToCull(int mode) {
        this.mode = mode;
        validate();
    }

    private void validate() {
        if (mode != GL_BACK
                && mode != GL_FRONT
                && mode != GL_FRONT_AND_BACK) {
            throw new IllegalArgumentException("Mode must be GL_BACK, GL_FRONT or GL_FRONT_AND_BACK.");
        }
    }

    public int getMode() {
        return mode;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetWhatFacesToCullTask(mode);
        }

        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof SetFacesToCull) {
            return mode == ((SetFacesToCull) stateChange).getMode();
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return defaultInstance == this;
    }

    public static String getModeName(int mode) {
        String modeName = "N/A";
        switch (mode) {
            case GL11.GL_BACK:
                modeName = "GL_BACK";
                break;
            case GL11.GL_FRONT:
                modeName = "GL_FRONT";
                break;
            case GL11.GL_FRONT_AND_BACK:
                modeName = "GL_FRONT_AND_BACK";
                break;
        }
        return modeName;
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), getModeName(mode));
    }
}
