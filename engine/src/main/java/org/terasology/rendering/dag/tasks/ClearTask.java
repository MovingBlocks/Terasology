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

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import org.terasology.rendering.dag.RenderPipelineTask;

/**
 * TODO: Add javadocs
 */
public class ClearTask implements RenderPipelineTask {
    private int mask;
    private boolean colorBufferMask;
    private boolean depthBufferMask;
    private boolean stencilBufferMask;

    public ClearTask(boolean colorBufferMask, boolean depthBufferMask, boolean stencilBufferMask) {
        this.colorBufferMask = colorBufferMask;
        this.depthBufferMask = depthBufferMask;
        this.stencilBufferMask = stencilBufferMask;

        mask = 0;
        if (colorBufferMask) {
            mask = mask | GL_COLOR_BUFFER_BIT;
        }
        if (depthBufferMask) {
            mask = mask | GL_DEPTH_BUFFER_BIT;
        }
        if (stencilBufferMask) {
            mask = mask | GL_STENCIL_BUFFER_BIT;
        }
    }

    @Override
    public void execute() {
        glClear(mask);
    }

    @Override
    public String toString() {
        return String.format("%21s(%s, %s, %s)",
                this.getClass().getSimpleName(),
                colorBufferMask,
                depthBufferMask,
                stencilBufferMask);
    }
}
