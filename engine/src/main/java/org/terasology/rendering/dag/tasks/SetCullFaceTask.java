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
package org.terasology.rendering.dag.tasks;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.stateChanges.SetCullFace;

/**
 * TODO: Add javadocs
 */
public class SetCullFaceTask implements RenderPipelineTask {
    private int modeValue;
    private SetCullFace.Mode mode;

    public SetCullFaceTask(SetCullFace.Mode mode) {
        this.mode = mode;
        switch (mode) {
            case GL_BACK:
                this.modeValue = GL11.GL_BACK;
                break;
            case GL_FRONT:
                this.modeValue = GL11.GL_FRONT;
                break;
            case GL_FRONT_AND_BACK:
                this.modeValue = GL11.GL_FRONT_AND_BACK;
                break;
        }
    }

    @Override
    public void execute() {
        GL11.glCullFace(modeValue);
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), mode.name());
    }
}
