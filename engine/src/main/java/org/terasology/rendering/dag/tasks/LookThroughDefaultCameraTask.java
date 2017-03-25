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

import org.terasology.rendering.dag.RenderPipelineTask;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * Instances of this class reset the ModelView and Projection matrices to identity matrices,
 * as per opengl default.
 *
 * WARNING: RenderPipelineTasks are not meant for direct instantiation and manipulation.
 * Modules or other parts of the engine should take advantage of them through classes
 * inheriting from StateChange.
 */
public class LookThroughDefaultCameraTask implements RenderPipelineTask {

    @Override
    public void execute() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), "default opengl camera");
    }
}
