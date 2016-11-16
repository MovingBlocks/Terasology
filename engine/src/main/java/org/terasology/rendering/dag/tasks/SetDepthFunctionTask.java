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
import org.terasology.rendering.dag.stateChanges.SetDepthFunction;

/**
 * Instances of this class change the depth function used for the depth test while rendering.
 *
 * See glDepthFunct for more information.
 *
 * WARNING: RenderPipelineTasks are not meant for direct instantiation and manipulation.
 * Modules or other parts of the engine should take advantage of them through classes
 * inheriting from StateChange.
 */
public class SetDepthFunctionTask implements RenderPipelineTask {
    private int depthFunction;

    /**
     * Constructs an instance of this class and initializes it with the given depth function.
     *
     * @param depthFunction An integer representing one of the depth functions known to OpenGL,
     *                      i.e. GL_LEQUAL (Terasology's default), GL_LESS (OpenGL default), etc.
     */
    public SetDepthFunctionTask(int depthFunction) {
        this.depthFunction = depthFunction;
    }

    @Override
    public void execute() {
        GL11.glDepthFunc(depthFunction);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), SetDepthFunction.OGL_TO_STRING.get(depthFunction));
    }
}
