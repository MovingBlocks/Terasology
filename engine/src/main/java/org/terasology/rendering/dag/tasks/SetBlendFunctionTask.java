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
import org.terasology.rendering.dag.stateChanges.SetBlendFunction;

import static org.lwjgl.opengl.GL11.glBlendFunc;

/**
 * Instances of this class change the factors used for blending.
 *
 * See glBlendFunc for more information.
 *
 * WARNING: RenderPipelineTasks are not meant for direct instantiation and manipulation.
 * Modules or other parts of the engine should take advantage of them through classes
 * inheriting from StateChange.
 */
public class SetBlendFunctionTask implements RenderPipelineTask {
    private int sourceFactor;
    private int destinationFactor;

    /**
     * Constructs an instance of SetBlendFunction initialised with the given blend function factors.
     *
     * @param sourceFactor An integer representing one of the possible blend factors known to OpenGL,
     *                      i.e. GL_ONE, GL_SRC_COLOR, etc...
     * @param destinationFactor An integer representing one of the possible blend factors known to OpenGL,
     *                      i.e. GL_ZERO, GL_DST_COLOR, etc...
     */
    public SetBlendFunctionTask(int sourceFactor, int destinationFactor) {
        this.sourceFactor = sourceFactor;
        this.destinationFactor = destinationFactor;
    }

    @Override
    public void execute() {
        glBlendFunc(sourceFactor, destinationFactor);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s, %s", this.getClass().getSimpleName(), SetBlendFunction.OGL_TO_STRING.get(sourceFactor),
            SetBlendFunction.OGL_TO_STRING.get(destinationFactor));
    }
}
